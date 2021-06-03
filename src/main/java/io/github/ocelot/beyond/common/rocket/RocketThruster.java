package io.github.ocelot.beyond.common.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * <p>A {@link RocketComponent} that provides thrust to a rocket.</p>
 *
 * @author Ocelot
 */
public interface RocketThruster extends RocketComponent
{
    /**
     * Calculates the amount of thrust this thruster can give a rocket.
     *
     * @param level The level the thruster is in
     * @param pos   The position of the thruster
     * @return The amount of thrust to give to the rocket
     */
    float getThrust(Level level, BlockPos pos);
}
