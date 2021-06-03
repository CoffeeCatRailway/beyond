package io.github.ocelot.beyond.common.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public interface RocketComponent
{
    default void tickLaunch(Level level, BlockPos pos)
    {
    }

    ResourceLocation getRegistryName();
}
