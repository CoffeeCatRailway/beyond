package io.github.ocelot.space.common.planet;

import net.minecraft.util.ResourceLocation;

import java.util.*;

public class CelestialBodySimulation
{
    private final Map<ResourceLocation, SimulatedBody> bodies;
    private final ResourceLocation[] roots;
    private int time;

    public CelestialBodySimulation(Map<ResourceLocation, CelestialBody> bodies)
    {
        this.bodies = new HashMap<>();

        List<CelestialBody> unprocessed = new ArrayList<>();
        for (Map.Entry<ResourceLocation, CelestialBody> entry : bodies.entrySet())
        {
            CelestialBody body = entry.getValue();
            if (body.getParent().isPresent())
            {
                ResourceLocation parent = body.getParent().get();
                if (!bodies.containsKey(parent))
                    continue;
            }
            this.bodies.put(entry.getKey(), new SimulatedBody(body, bodies.entrySet().stream().filter(e -> e.getValue().getParent().map(parent -> parent.equals(entry.getKey())).orElse(false)).map(Map.Entry::getKey).toArray(ResourceLocation[]::new)));
        }

        this.roots = bodies.entrySet().stream().filter(entry -> !entry.getValue().getParent().isPresent()).map(Map.Entry::getKey).toArray(ResourceLocation[]::new);
        if (this.roots.length == 0)
            throw new IllegalStateException("There must be at least a single root body");
    }

    public Optional<SimulatedBody> getBody(ResourceLocation id)
    {
        return Optional.ofNullable(this.bodies.get(id));
    }

    public ResourceLocation[] getRootBodies()
    {
        return this.roots;
    }

    public void tick()
    {
        this.time++;
    }

    public static class SimulatedBody
    {
        private final CelestialBody body;
        private final ResourceLocation[] children;
        private float distance;
        private float angle;

        public SimulatedBody(CelestialBody body, ResourceLocation[] children)
        {
            this.body = body;
            this.children = children;
            this.distance = body.getScale() * 4;
            this.angle = 0;
        }

        public CelestialBody getBody()
        {
            return body;
        }

        public ResourceLocation[] getChildren()
        {
            return children;
        }

        public float getDistance()
        {
            return distance;
        }

        public float getAngle()
        {
            return angle;
        }
    }
}
