package me.paulf.fairylights.server.fastener.connection.type;

import com.google.common.base.MoreObjects;
import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.fastener.Fastener;
import me.paulf.fairylights.server.fastener.FastenerType;
import me.paulf.fairylights.server.fastener.accessor.FastenerAccessor;
import me.paulf.fairylights.server.fastener.connection.Catenary;
import me.paulf.fairylights.server.fastener.connection.ConnectionType;
import me.paulf.fairylights.server.fastener.connection.FeatureType;
import me.paulf.fairylights.server.fastener.connection.PlayerAction;
import me.paulf.fairylights.server.fastener.connection.Segment;
import me.paulf.fairylights.server.fastener.connection.collision.Collidable;
import me.paulf.fairylights.server.fastener.connection.collision.ConnectionCollision;
import me.paulf.fairylights.server.fastener.connection.collision.FeatureCollisionTree;
import me.paulf.fairylights.server.fastener.connection.collision.Intersection;
import me.paulf.fairylights.server.item.ConnectionItem;
import me.paulf.fairylights.server.net.serverbound.InteractionConnectionMessage;
import me.paulf.fairylights.server.sound.FLSounds;
import me.paulf.fairylights.util.CubicBezier;
import me.paulf.fairylights.util.NBTSerializable;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Connection implements NBTSerializable {
    public static final int MAX_LENGTH = 32;

    public static final double PULL_RANGE = 5;

    public static final FeatureType CORD_FEATURE = FeatureType.create("cord");

    private static final CubicBezier SLACK_CURVE = new CubicBezier(0.495F, 0.505F, 0.495F, 0.505F);

    private static final float MAX_SLACK = 3;

    protected final Fastener<?> fastener;

    private final UUID uuid;

    private FastenerAccessor destination;

    protected World world;

    private boolean isOrigin;

    @Nullable
    private Catenary catenary;

    @Nullable
    private Catenary prevCatenary;

    protected float slack = 1;

    private final ConnectionCollision collision = new ConnectionCollision();

    private boolean updateCatenary;

    private boolean catenaryUpdateState;

    protected boolean dataUpdateState;

    public boolean forceRemove;

    private int prevStretchStage;

    private boolean removed;

    @Nullable
    private List<Runnable> removeListeners;

    public Connection(final World world, final Fastener<?> fastener, final UUID uuid, final Fastener<?> destination, final boolean isOrigin, final CompoundNBT compound) {
        this(world, fastener, uuid);
        this.destination = destination.createAccessor();
        this.isOrigin = isOrigin;
        this.deserializeLogic(compound);
    }

    public Connection(final World world, final Fastener<?> fastener, final UUID uuid) {
        this.world = world;
        this.fastener = fastener;
        this.uuid = uuid;
        this.computeCatenary();
    }

    @Nullable
    public final Catenary getCatenary() {
        return this.catenary;
    }

    @Nullable
    public final Catenary getPrevCatenary() {
        return this.prevCatenary == null ? this.catenary : this.prevCatenary;
    }

    public void setWorld(final World world) {
        this.world = world;
    }

    public final World getWorld() {
        return this.world;
    }

    public final boolean isOrigin() {
        return this.isOrigin;
    }

    public final ConnectionCollision getCollision() {
        return this.collision;
    }

    public final Fastener<?> getFastener() {
        return this.fastener;
    }

    public final UUID getUUID() {
        return this.uuid;
    }

    public final void setDestination(final Fastener<?> destination) {
        this.destination = destination.createAccessor();
        this.computeCatenary();
    }

    public final FastenerAccessor getDestination() {
        return this.destination;
    }

    public boolean isDestination(final FastenerAccessor location) {
        return this.destination.equals(location);
    }

    public boolean shouldDrop() {
        return this.fastener.shouldDropConnection() && this.destination.isLoaded(this.world) && this.destination.get(this.world).shouldDropConnection();
    }

    public boolean shouldDisconnect() {
        return !this.destination.exists(this.world) || this.forceRemove;
    }

    public ItemStack getItemStack() {
        final ItemStack stack = new ItemStack(this.getType().getItem());
        final CompoundNBT tagCompound = this.serializeLogic();
        if (!tagCompound.isEmpty()) {
            stack.setTag(tagCompound);
        }
        return stack;
    }

    public float getRadius() {
        return 0.0625F;
    }

    public final boolean isDynamic() {
        if (this.destination.isLoaded(this.world)) {
            return this.fastener.isMoving() || this.destination.get(this.world).isMoving();
        }
        return false;
    }

    public final boolean isModifiable(final PlayerEntity player) {
        return this.world.isBlockModifiable(player, this.fastener.getPos());
    }

    public final void addRemoveListener(final Runnable listener) {
        if (this.removeListeners == null) {
            this.removeListeners = new ArrayList<>();
        }
        this.removeListeners.add(listener);
    }

    public final void remove() {
        if (!this.removed) {
            this.removed = true;
            this.onRemove();
            if (this.removeListeners != null) {
                this.removeListeners.forEach(Runnable::run);
            }
        }
    }

    public void computeCatenary() {
        this.updateCatenary = this.dataUpdateState = true;
    }

    public void processClientAction(final PlayerEntity player, final PlayerAction action, final Intersection intersection) {
        FairyLights.network.sendToServer(new InteractionConnectionMessage(this, action, intersection));
    }

    public void disconnect(final PlayerEntity player, final Vec3d hit) {
        if (!this.destination.isLoaded(this.world)) {
            return;
        }
        this.fastener.removeConnection(this);
        this.destination.get(this.world).removeConnection(this.uuid);
        if (this.shouldDrop()) {
            final ItemStack stack = this.getItemStack();
            final ItemEntity item = new ItemEntity(this.world, hit.x, hit.y, hit.z, stack);
            final float scale = 0.05F;
            item.setMotion(
                this.world.rand.nextGaussian() * scale,
                this.world.rand.nextGaussian() * scale + 0.2F,
                this.world.rand.nextGaussian() * scale
            );
            this.world.addEntity(item);
        }
        this.world.playSound(null, hit.x, hit.y, hit.z, FLSounds.CORD_DISCONNECT.orElseThrow(IllegalStateException::new), SoundCategory.BLOCKS, 1, 1);
    }

    public boolean interact(final PlayerEntity player, final Vec3d hit, final FeatureType featureType, final int feature, final ItemStack heldStack, final Hand hand) {
        final Item item = heldStack.getItem();
        if (item instanceof ConnectionItem && !this.matches(heldStack)) {
            if (this.destination.isLoaded(this.world)) {
                this.replace(player, hit, heldStack);
                return true;
            }
        } else if (heldStack.getItem().isIn(Tags.Items.STRING)) {
            return this.slacken(hit, heldStack, 0.2F);
        } else if (heldStack.getItem() == Items.STICK) {
            return this.slacken(hit, heldStack, -0.2F);
        }
        return false;
    }

    public boolean matches(final ItemStack stack) {
        if (!(stack.getItem() instanceof ConnectionItem)) {
            return false;
        }
        if (!((ConnectionItem) stack.getItem()).getConnectionType().isInstance(this)) {
            return false;
        }
        return !stack.hasTag() || NBTUtil.areNBTEquals(this.serializeLogic(), stack.getTag(), true);
    }

    private void replace(final PlayerEntity player, final Vec3d hit, final ItemStack heldStack) {
        final Fastener<?> dest = this.destination.get(this.world);
        this.fastener.removeConnectionImmediately(this);
        dest.removeConnectionImmediately(this.uuid);
        if (this.shouldDrop()) {
            player.inventory.addItemStackToInventory(this.getItemStack());
        }
        final CompoundNBT data = MoreObjects.firstNonNull(heldStack.getTag(), new CompoundNBT());
        final ConnectionType type = ((ConnectionItem) heldStack.getItem()).getConnectionType();
        this.fastener.connectWith(this.world, dest, type, data).onConnect(player.world, player, heldStack);
        heldStack.shrink(1);
        this.world.playSound(null, hit.x, hit.y, hit.z, FLSounds.CORD_CONNECT.orElseThrow(IllegalStateException::new), SoundCategory.BLOCKS, 1, 1);
    }

    private boolean slacken(final Vec3d hit, final ItemStack heldStack, final float amount) {
        if (this.slack <= 0 && amount < 0 || this.slack >= MAX_SLACK && amount > 0) {
            return false;
        }
        this.slack = MathHelper.clamp(this.slack + amount, 0, MAX_SLACK);
        if (this.slack < 1e-2F) {
            this.slack = 0;
        }
        this.dataUpdateState = true;
        this.world.playSound(null, hit.x, hit.y, hit.z, FLSounds.CORD_STRETCH.orElseThrow(IllegalStateException::new), SoundCategory.BLOCKS, 1, 0.8F + (MAX_SLACK - this.slack) * 0.4F);
        return true;
    }

    public void onConnect(final World world, final PlayerEntity user, final ItemStack heldStack) {}

    protected void onRemove() {}

    protected void updatePrev() {}

    protected void onUpdateEarly() {}

    protected void onUpdateLate() {}

    protected void onCalculateCatenary() {}

    public abstract ConnectionType getType();

    public final void update(final Vec3d from) {
        this.prevCatenary = this.catenary;
        this.updatePrev();
        this.destination.update(this.world, this.fastener.getPos());
        if (this.destination.isLoaded(this.world)) {
            this.onUpdateEarly();
            final Fastener dest = this.destination.get(this.world);
            final Vec3d point = dest.getConnectionPoint();
            this.updateCatenary(from, dest, point);
            final double dist = point.distanceTo(from);
            final double pull = dist - MAX_LENGTH + PULL_RANGE;
            if (pull > 0) {
                final int stage = (int) (pull + 0.1F);
                if (stage > this.prevStretchStage) {
                    this.world.playSound(null, point.x, point.y, point.z, FLSounds.CORD_STRETCH.orElseThrow(IllegalStateException::new), SoundCategory.BLOCKS, 0.25F, 0.5F + stage / 8F);
                }
                this.prevStretchStage = stage;
            }
            if (dist > MAX_LENGTH + PULL_RANGE) {
                this.world.playSound(null, point.x, point.y, point.z, FLSounds.CORD_SNAP.orElseThrow(IllegalStateException::new), SoundCategory.BLOCKS, 0.75F, 0.8F + this.world.rand.nextFloat() * 0.3F);
                this.forceRemove = true;
            } else if (dest.isMoving()) {
                dest.resistSnap(from);
            }
            this.onUpdateLate();
        }
    }

    public void updateCatenary(final Vec3d from) {
        if (this.world.isBlockLoaded(this.fastener.getPos())) {
            this.destination.update(this.world, this.fastener.getPos());
            if (this.destination.isLoaded(this.world)) {
                final Fastener dest = this.destination.get(this.world);
                final Vec3d point = dest.getConnectionPoint();
                this.updateCatenary(from, dest, point);
                this.updateCatenary = false;
            }
        }
    }

    private void updateCatenary(final Vec3d from, final Fastener<?> dest, final Vec3d point) {
        if (this.updateCatenary || this.isDynamic()) {
            final Vec3d vec = point.subtract(from);
            if (vec.length() > 1e-6) {
                this.catenary = Catenary.from(vec, SLACK_CURVE, this.slack);
                this.onCalculateCatenary();
                this.collision.update(this, from);
            }
            this.catenaryUpdateState = true;
            this.updateCatenary = false;
        }
    }

    public final boolean pollCateneryUpdate() {
        final boolean state = this.catenaryUpdateState;
        this.catenaryUpdateState = false;
        return state;
    }

    public final boolean pollDataUpdate() {
        final boolean state = this.dataUpdateState;
        this.dataUpdateState = false;
        return state;
    }

    public void addCollision(final List<Collidable> collision, final Vec3d origin) {
        final Segment[] segments = this.catenary.getSegments();
        if (segments.length < 2) {
            return;
        }
        final float radius = this.getRadius();
        collision.add(FeatureCollisionTree.build(CORD_FEATURE, segments, s -> {
            final Vec3d start = s.getStart();
            final Vec3d end = s.getEnd();
            return new AxisAlignedBB(
                origin.x + start.x / 16, origin.y + start.y / 16, origin.z + start.z / 16,
                origin.x + end.x / 16, origin.y + end.y / 16, origin.z + end.z / 16
            ).grow(radius);
        }, 1, segments.length - 2));
    }

    @Override
    public CompoundNBT serialize() {
        final CompoundNBT compound = new CompoundNBT();
        compound.putBoolean("isOrigin", this.isOrigin);
        compound.put("destination", FastenerType.serialize(this.destination));
        compound.put("logic", this.serializeLogic());
        compound.putFloat("slack", this.slack);
        return compound;
    }

    @Override
    public void deserialize(final CompoundNBT compound) {
        this.isOrigin = compound.getBoolean("isOrigin");
        this.destination = FastenerType.deserialize(compound.getCompound("destination"));
        this.deserializeLogic(compound.getCompound("logic"));
        this.slack = compound.contains("slack", NBT.TAG_ANY_NUMERIC) ? compound.getFloat("slack") : 1;
        this.updateCatenary = true;
    }

    public CompoundNBT serializeLogic() {
        return new CompoundNBT();
    }

    public void deserializeLogic(final CompoundNBT compound) {}
}
