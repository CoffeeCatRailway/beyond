package io.github.ocelot.beyond.common.init;

import io.github.ocelot.beyond.Beyond;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.*;

/**
 * @author Ocelot
 */
public class BeyondTags
{
    public static final ITag.INamedTag<Block> INFINIBURN_MOON = makeBlockWrapperTag("infiniburn_moon");
    public static final ITag.INamedTag<Block> BASE_STONE_MOON = makeBlockWrapperTag("base_stone_moon");

    /* Registry Methods */

    /**
     * Creates a new tag wrapper using the specified name.
     *
     * @param name The name of the tag
     * @return The tag wrapper
     */
    public static ITag.INamedTag<Item> makeItemWrapperTag(String name)
    {
        return ItemTags.bind(Beyond.MOD_ID + ":" + name);
    }

    /**
     * Creates a new tag wrapper using the specified name.
     *
     * @param name The name of the tag
     * @return The tag wrapper
     */
    public static ITag.INamedTag<Block> makeBlockWrapperTag(String name)
    {
        return BlockTags.bind(Beyond.MOD_ID + ":" + name);
    }

    /**
     * Creates a new tag wrapper using the specified name.
     *
     * @param name The name of the tag
     * @return The tag wrapper
     */
    public static ITag.INamedTag<EntityType<?>> makeEntityWrapperTag(String name)
    {
        return EntityTypeTags.bind(Beyond.MOD_ID + ":" + name);
    }

    /**
     * Creates a new tag wrapper using the specified name.
     *
     * @param name The name of the tag
     * @return The tag wrapper
     */
    public static ITag.INamedTag<Fluid> makeFluidWrapperTag(String name)
    {
        return FluidTags.bind(Beyond.MOD_ID + ":" + name);
    }
}
