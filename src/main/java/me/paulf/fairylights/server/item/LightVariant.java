package me.paulf.fairylights.server.item;

import me.paulf.fairylights.server.item.crafting.FLCraftingRecipes;
import me.paulf.fairylights.util.Utils;
import me.paulf.fairylights.util.crafting.GenericRecipe;
import me.paulf.fairylights.util.crafting.GenericRecipeBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.RegistryObject;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public enum LightVariant {
    FAIRY("fairy_light", () -> FLItems.FAIRY_LIGHT, true, 5, 5, b -> b
        .withShape(" I ", "IDI", " G ")
        .withIngredient('I', Tags.Items.INGOTS_IRON)
        .withIngredient('D', FLCraftingRecipes.LIGHT_DYE)
        .withIngredient('G', Tags.Items.GLASS_PANES_COLORLESS),
        Placement.ONWARD
    ),
    PAPER("paper_lantern", () -> FLItems.PAPER_LANTERN, false, 9, 16.5F, b -> b
        .withShape(" I ", "PDP", "PPP")
        .withIngredient('I', Tags.Items.INGOTS_IRON)
        .withIngredient('D', FLCraftingRecipes.LIGHT_DYE)
        .withIngredient('P', Items.PAPER),
        Placement.UPRIGHT
    ),
    ORB("orb_lantern", () -> FLItems.ORB_LANTERN, false, 10, 11.5F, b -> b
        .withShape(" I ", "SDS", " W ")
        .withIngredient('I', Tags.Items.INGOTS_IRON)
        .withIngredient('D', FLCraftingRecipes.LIGHT_DYE)
        .withIngredient('S', Items.STRING)
        .withIngredient('W', Blocks.WHITE_WOOL),
        Placement.UPRIGHT
    ),
    FLOWER("flower_light", () -> FLItems.FLOWER_LIGHT, true, 10, 6, b -> b
        .withShape(" I ", "RDB", " Y ")
        .withIngredient('I', Tags.Items.INGOTS_IRON)
        .withIngredient('D', FLCraftingRecipes.LIGHT_DYE)
        .withIngredient('R', Blocks.POPPY)
        .withIngredient('Y', Blocks.DANDELION)
        .withIngredient('B', Blocks.BLUE_ORCHID),
        Placement.OUTWARD
    ),
    ORNATE("ornate_lantern", () -> FLItems.ORNATE_LANTERN, false, 24, 8, 12, b -> b
        .withShape(" I ", "GDG", "IGI")
        .withIngredient('I', Tags.Items.INGOTS_IRON)
        .withIngredient('D', FLCraftingRecipes.LIGHT_DYE)
        .withIngredient('G', Tags.Items.NUGGETS_GOLD),
        Placement.UPRIGHT
    ),
    OIL("oil_lantern", () -> FLItems.OIL_LANTERN, false, 32, 8, 13.5F, b -> b
        .withShape(" I ", "SDS", "IGI")
        .withIngredient('I', Tags.Items.INGOTS_IRON)
        .withIngredient('D', FLCraftingRecipes.LIGHT_DYE)
        .withIngredient('S', Items.STICK)
        .withIngredient('G', Tags.Items.GLASS_PANES_COLORLESS),
        Placement.UPRIGHT
    ),
    JACK_O_LANTERN("jack_o_lantern", () -> FLItems.JACK_O_LANTERN, true, 7, 9, b -> b
        .withShape(" I ", "SDS", "GPG")
        .withIngredient('I', Tags.Items.INGOTS_IRON)
        .withIngredient('D', FLCraftingRecipes.LIGHT_DYE)
        .withIngredient('S', ItemTags.WOODEN_SLABS)
        .withIngredient('G', Blocks.TORCH)
        .withIngredient('P', Blocks.PUMPKIN),
        Placement.UPRIGHT
    ),
    SKULL("skull_light", () -> FLItems.SKULL_LIGHT, true, 6, 9, b -> b
        .withShape(" I ", "IDI", " B ")
        .withIngredient('I', Tags.Items.INGOTS_IRON)
        .withIngredient('D', FLCraftingRecipes.LIGHT_DYE)
        .withAnyIngredient('B', Items.BONE, new ItemStack(Items.SKELETON_SKULL)),
        Placement.UPRIGHT
    ),
    GHOST("ghost_light", () -> FLItems.GHOST_LIGHT, true, 6, 8, b -> b
        .withShape(" I ", "PDP", "IGI")
        .withIngredient('I', Tags.Items.INGOTS_IRON)
        .withIngredient('D', FLCraftingRecipes.LIGHT_DYE)
        .withIngredient('P', Items.PAPER)
        .withIngredient('G', Tags.Items.GLASS_PANES_WHITE),
        Placement.UPRIGHT
    ),
    SPIDER("spider_light", () -> FLItems.SPIDER_LIGHT, true, 12, 14, b -> b
        .withShape(" I ", "WDW", "SES")
        .withIngredient('I', Tags.Items.INGOTS_IRON)
        .withIngredient('D', FLCraftingRecipes.LIGHT_DYE)
        .withIngredient('W', Blocks.COBWEB)
        .withIngredient('S', Items.STRING)
        .withIngredient('E', Items.SPIDER_EYE),
        Placement.UPRIGHT
    ),
    WITCH("witch_light", () -> FLItems.WITCH_LIGHT, true, 8, 10, b -> b
        .withShape(" I ", "BDW", " S ")
        .withIngredient('I', Tags.Items.INGOTS_IRON)
        .withIngredient('D', FLCraftingRecipes.LIGHT_DYE)
        .withIngredient('B', Items.GLASS_BOTTLE)
        .withIngredient('W', Items.WHEAT)
        .withIngredient('S', Items.STICK),
        Placement.UPRIGHT
    ),
    SNOWFLAKE("snowflake_light", () -> FLItems.SNOWFLAKE_LIGHT, true, 8, 12.5F, b -> b
        .withShape(" I ", "SDS", " G ")
        .withIngredient('I', Tags.Items.INGOTS_IRON)
        .withIngredient('D', FLCraftingRecipes.LIGHT_DYE)
        .withIngredient('S', Items.SNOWBALL)
        .withIngredient('G', Tags.Items.GLASS_PANES_WHITE),
        Placement.UPRIGHT
    ),
    ICICLE("icicle_lights", () -> FLItems.ICICLE_LIGHTS, false, 10, 7, 20, b -> b
        .withShape(" I ", "GDG", " B ")
        .withIngredient('I', Tags.Items.INGOTS_IRON)
        .withIngredient('D', FLCraftingRecipes.LIGHT_DYE)
        .withIngredient('G', Tags.Items.GLASS_PANES_COLORLESS)
        .withAnyIngredient('B', Items.WATER_BUCKET, Blocks.ICE, Blocks.PACKED_ICE),
        Placement.UPRIGHT
    ),
    METEOR("meteor_light", () -> FLItems.METEOR_LIGHT, false, 24, 3, 28.5F, b -> b
        .withShape(" I ", "GDG", "IPI")
        .withIngredient('I', Tags.Items.INGOTS_IRON)
        .withIngredient('D', FLCraftingRecipes.LIGHT_DYE)
        .withIngredient('G', Items.GLOWSTONE_DUST)
        .withIngredient('P', Items.PAPER),
        0.02F, 100,
        Placement.UPRIGHT
    );

    private final String name;

    private final Supplier<RegistryObject<? extends Item>> item;

    private final boolean parallelsCord;

    private final float spacing;

    private final float width;

    private final float height;

    private final UnaryOperator<GenericRecipeBuilder> recipe;

    private final float twinkleChance;

    private final int tickCycle;

    private final boolean alwaysTwinkle;

    private final Placement placement;

    LightVariant(final String name, final Supplier<RegistryObject<? extends Item>> item, final boolean parallelsCord, final float width, final float height, final UnaryOperator<GenericRecipeBuilder> recipe, final Placement orientable) {
        this(name, item, parallelsCord, 16, width, height, recipe, orientable);
    }

    LightVariant(final String name, final Supplier<RegistryObject<? extends Item>> item, final boolean parallelsCord, final float spacing, final float width, final float height, final UnaryOperator<GenericRecipeBuilder> recipe, final Placement orientable) {
        this(name, item, parallelsCord, spacing, width, height, recipe, 0.05F, 40, false, orientable);
    }

    LightVariant(final String name, final Supplier<RegistryObject<? extends Item>> item, final boolean parallelsCord, final float spacing, final float width, final float height, final UnaryOperator<GenericRecipeBuilder> recipe, final float twinkleChance, final int tickCycle, final Placement orientable) {
        this(name, item, parallelsCord, spacing, width, height, recipe, twinkleChance, tickCycle, true, orientable);
    }

    LightVariant(final String name, final Supplier<RegistryObject<? extends Item>> item, final boolean parallelsCord, final float spacing, final float width, final float height, final UnaryOperator<GenericRecipeBuilder> recipe, final float twinkleChance, final int tickCycle, final boolean alwaysTwinkle, final Placement orientable) {
        this.name = name;
        this.item = item;
        this.parallelsCord = parallelsCord;
        this.spacing = spacing;
        this.width = width / 16;
        this.height = height / 16;
        this.recipe = recipe;
        this.twinkleChance = twinkleChance;
        this.tickCycle = tickCycle;
        this.alwaysTwinkle = alwaysTwinkle;
        this.placement = orientable;
    }

    public String getName() {
        return this.name;
    }

    public Item getItem() {
        return this.item.get().orElseThrow(IllegalStateException::new);
    }

    public boolean parallelsCord() {
        return this.parallelsCord;
    }

    public float getSpacing() {
        return this.spacing;
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }

    public GenericRecipe getRecipe(final ResourceLocation name, final IRecipeSerializer<GenericRecipe> serializer) {
        return this.recipe.apply(new GenericRecipeBuilder(name, serializer))
            .withOutput(this.getItem(), 4)
            .build();
    }

    public float getTwinkleChance() {
        return this.twinkleChance;
    }

    public int getTickCycle() {
        return this.tickCycle;
    }

    public boolean alwaysDoTwinkleLogic() {
        return this.alwaysTwinkle;
    }

    public Placement getPlacement() {
        return this.placement;
    }

    public static LightVariant getLightVariant(final int index) {
        return Utils.getEnumValue(LightVariant.class, index);
    }

    public enum Placement {
        UPRIGHT,
        OUTWARD,
        ONWARD
    }
}
