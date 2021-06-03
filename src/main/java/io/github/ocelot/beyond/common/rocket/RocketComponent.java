package io.github.ocelot.beyond.common.rocket;

import io.github.ocelot.beyond.common.blockentity.RocketControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

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
     * @param pos   The position of the component
     */
    default void tickLaunch(Level level, BlockPos pos)
    {
    }
}
