package io.github.ocelot.beyond.common.util;

import io.github.ocelot.beyond.common.space.simulation.SimulatedBody;
import net.minecraft.world.phys.Vec3;

/**
 * <p>Holds the result of a ray trace in a simulation.</p>
 *
 * @author Ocelot
 */
public class CelestialBodyRayTraceResult
{
    private final SimulatedBody body;
    private final Vec3 pos;

    public CelestialBodyRayTraceResult(SimulatedBody body, Vec3 pos)
    {
        this.body = body;
        this.pos = pos;
    }

    /**
     * @return The body hit
     */
    public SimulatedBody getBody()
    {
        return body;
    }

    /**
     * @return The specific position the hit was at
     */
    public Vec3 getPos()
    {
        return pos;
    }
}
