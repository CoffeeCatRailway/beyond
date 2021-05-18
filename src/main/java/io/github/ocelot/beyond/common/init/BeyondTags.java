package io.github.ocelot.beyond.common.init;

import io.github.ocelot.beyond.Beyond;
import net.minecraft.tags.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

/**
 * @author Ocelot
 */
public class BeyondTags
{
    public static final Tag.Named<Block> INFINIBURN_MOON = makeBlockWrapperTag("infiniburn_moon");
    public static final Tag.Named<Block> BASE_STONE_MOON = makeBlockWrapperTag("base_stone_moon");

    /* Registry Methods */

    /**
     * Creates a new tag wrapper using the specified name.
     *
     * @param name The name of the tag
     * @return The tag wrapper
     */
    public static Tag.Named<Item> makeItemWrapperTag(String name)
    {
        return ItemTags.bind(Beyond.MOD_ID + ":" + name);
    }

    /**
     * Creates a new tag wrapper using the specified name.
     *
     * @param name The name of the tag
     * @return The tag wrapper
     */
    public static Tag.Named<Block> makeBlockWrapperTag(String name)
    {
        return BlockTags.bind(Beyond.MOD_ID + ":" + name);
    }

    /**
     * Creates a new tag wrapper using the specified name.
     *
     * @param name The name of the tag
     * @return The tag wrapper
     */
    public static Tag.Named<EntityType<?>> makeEntityWrapperTag(String name)
    {
        return EntityTypeTags.bind(Beyond.MOD_ID + ":" + name);
    }

    /**
     * Creates a new tag wrapper using the specified name.
     *
     * @param name The name of the tag
     * @return The tag wrapper
     */
    public static Tag.Named<Fluid> makeFluidWrapperTag(String name)
    {
        return FluidTags.bind(Beyond.MOD_ID + ":" + name);
    }
}
