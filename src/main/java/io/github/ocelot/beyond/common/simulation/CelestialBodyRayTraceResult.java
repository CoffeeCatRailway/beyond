package io.github.ocelot.beyond.common.simulation;

import io.github.ocelot.beyond.common.simulation.body.SimulatedBody;
import net.minecraft.util.math.vector.Vector3d;

/**
 * <p>Holds the result of a ray trace in a simulation.</p>
 *
 * @author Ocelot
 */
public class CelestialBodyRayTraceResult
{
    private final SimulatedBody body;
    private final Vector3d pos;

    public CelestialBodyRayTraceResult(SimulatedBody body, Vector3d pos)
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
    public Vector3d getPos()
    {
        return pos;
    }
}
