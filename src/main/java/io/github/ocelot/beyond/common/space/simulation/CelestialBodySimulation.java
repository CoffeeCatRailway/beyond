package io.github.ocelot.beyond.common.space.simulation;

import io.github.ocelot.beyond.common.space.planet.Planet;
import io.github.ocelot.beyond.common.util.CelestialBodyRayTraceResult;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * <p>Simulates the solar system based on rotations around bodies.</p>
 *
 * @author Ocelot
 */
public class CelestialBodySimulation
{
    private final Map<ResourceLocation, SimulatedBody> bodies;
    private final Set<ResourceLocation> removedBodies;
    private final Random random;

    public CelestialBodySimulation(Map<ResourceLocation, Planet> bodies)
    {
        this.bodies = new HashMap<>();
        this.removedBodies = ConcurrentHashMap.newKeySet();
        this.random = new Random();

        for (Map.Entry<ResourceLocation, Planet> entry : bodies.entrySet())
        {
            Planet body = entry.getValue();
            if (body.getParent().isPresent())
            {
                ResourceLocation parent = body.getParent().get();
                if (!bodies.containsKey(parent))
                    continue;
            }
            this.bodies.put(entry.getKey(), new NaturalSimulatedBody(this, entry.getKey(), body));
        }

        List<SimulatedBody> unvisitedBodies = new ArrayList<>(this.bodies.values());

        while (!unvisitedBodies.isEmpty())
        {
            SimulatedBody body = unvisitedBodies.remove(0);
            Optional<SimulatedBody> optionalParent = body.getParent().map(this.bodies::get);
            if (!optionalParent.isPresent() || !unvisitedBodies.contains(optionalParent.get()))
            {
                ((NaturalSimulatedBody) body).initializePosition();
            }
            else
            {
                unvisitedBodies.add(body);
            }
        }
    }

    /**
     * Steps through the simulation.
     */
    public void tick()
    {
        this.removedBodies.forEach(this.bodies::remove);
        this.removedBodies.clear();
        this.bodies.values().forEach(SimulatedBody::tick);
    }

    /**
     * Adds the specified body to the simulation.
     *
     * @param body The body to add
     */
    public boolean add(SimulatedBody body)
    {
        if (this.bodies.containsKey(body.getId()))
            return false;
        Minecraft.getInstance().execute(() -> this.bodies.put(body.getId(), body));
        return true;
    }

    /**
     * Removes the specified body from the simulation.
     *
     * @param id The id of the body to remove
     */
    public void remove(ResourceLocation id)
    {
        this.removedBodies.add(id);
    }

    /**
     * Retrieves a body by the specified id.
     *
     * @param id The id of the body to get
     * @return The body with that id or <code>null</code> for no body with that id
     */
    public SimulatedBody getBody(ResourceLocation id)
    {
        return this.bodies.get(id);
    }

    /**
     * Retrieves a player rocket by the specified player id.
     *
     * @param id The id of the body to get
     * @return The body with that id or <code>null</code> for no body with that id
     */
    public PlayerRocket getPlayer(UUID id)
    {
        return (PlayerRocket) this.bodies.values().stream().filter(body -> body instanceof PlayerRocket && ((PlayerRocket) body).getPlayer().getId().equals(id)).findFirst().orElse(null);
    }

    /**
     * @return All bodies in the simulation
     */
    public Stream<SimulatedBody> getBodies()
    {
        return this.bodies.values().stream();
    }

    public Random getRandom()
    {
        return random;
    }

    /**
     * Casts a ray through the simulation to check for an intersection with a celestial body.
     *
     * @param start        The starting position of the ray
     * @param end          The ending position of the ray
     * @param partialTicks The percentage from last update and this update
     * @return The optional result of the ray trace
     */
    public Optional<CelestialBodyRayTraceResult> clip(Vector3d start, Vector3d end, float partialTicks)
    {
        SimulatedBody resultBody = null;
        Vector3d result = null;
        double resultDistanceSq = Double.MAX_VALUE;
        for (SimulatedBody body : this.bodies.values())
        {
            Optional<Vector3d> bodyResult = body.clip(start, end, partialTicks);
            if (!bodyResult.isPresent())
                continue;

            double bodyResultDistanceSq = start.distanceToSqr(bodyResult.get());
            if (bodyResultDistanceSq < resultDistanceSq)
            {
                resultBody = body;
                result = bodyResult.get();
                resultDistanceSq = bodyResultDistanceSq;
            }
        }
        return resultBody != null ? Optional.of(new CelestialBodyRayTraceResult(resultBody, result)) : Optional.empty();
    }
}
