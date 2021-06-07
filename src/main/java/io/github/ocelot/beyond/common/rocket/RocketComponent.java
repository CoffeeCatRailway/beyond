package io.github.ocelot.beyond.common.rocket;

import io.github.ocelot.beyond.common.blockentity.RocketControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * <p>A component of a rocket that performs actions based on {@link RocketControllerBlockEntity}.</p>
 *
 * @author Ocelot
 */
public interface RocketComponent
{
    /**
     * Ticks launch client and server side.
     *
     * @param level The level the launch is happening in
     * @param state the state of the component
     * @param x The x position of the block
     * @param y The y position of the block
     * @param z The z position of the block
     */
    default void addParticles(Level level, BlockState state, double x, double y, double z, double motionX, double motionY, double motionZ)
    {
    }
}
