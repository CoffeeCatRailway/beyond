package io.github.ocelot.space.common.planet;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CelestialBodySimulation
{
    private final Map<ResourceLocation, SimulatedBody> bodies;

    public CelestialBodySimulation(Map<ResourceLocation, CelestialBody> bodies)
    {
        this.bodies = new HashMap<>();

        for (Map.Entry<ResourceLocation, CelestialBody> entry : bodies.entrySet())
        {
            CelestialBody body = entry.getValue();
            float offset = 0;
            if (body.getParent().isPresent())
            {
                ResourceLocation parent = body.getParent().get();
                if (!bodies.containsKey(parent))
                    continue;
                offset = bodies.get(parent).getScale() * 2F;
            }
            SimulatedBody simulatedBody = new SimulatedBody(body);
            simulatedBody.position.setX(offset); // FIXME
            this.bodies.put(entry.getKey(), simulatedBody);
        }

        // TODO set all bodies to initial positions
    }

    public Stream<SimulatedBody> getBodies()
    {
        return this.bodies.values().stream();
    }

    public void tick()
    {
        this.bodies.values().forEach(SimulatedBody::move);
        this.bodies.values().forEach(SimulatedBody::tick);
    }

    public static class SimulatedBody
    {
        private final CelestialBody body;
        private final Vector3f lastPosition;
        private final Vector3f position;
        private float lastRotation;
        private float rotation;

        public SimulatedBody(CelestialBody body)
        {
            this.body = body;
            this.lastPosition = new Vector3f();
            this.position = new Vector3f();
            this.rotation = 0;
        }

        private void tick()
        {
            this.lastRotation = this.rotation;
            this.lastPosition.set(this.position.x(), this.position.y(), this.position.z());
            this.rotation++;
        }

        private void move()
        {
        }

        public CelestialBody getBody()
        {
            return body;
        }

        public Vector3f getLastPosition()
        {
            return lastPosition;
        }

        public Vector3f getPosition()
        {
            return position;
        }

        public float getLastRotation()
        {
            return lastRotation;
        }

        public float getRotation()
        {
            return rotation;
        }
    }
}
