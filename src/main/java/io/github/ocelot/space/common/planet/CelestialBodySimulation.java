package io.github.ocelot.space.common.planet;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

import java.util.*;
import java.util.stream.Stream;

public class CelestialBodySimulation
{
    private final Map<ResourceLocation, SimulatedBody> bodies;
    private final Random random;

    public CelestialBodySimulation(Map<ResourceLocation, CelestialBody> bodies)
    {
        this.bodies = new HashMap<>();
        this.random = new Random();

        for (Map.Entry<ResourceLocation, CelestialBody> entry : bodies.entrySet())
        {
            CelestialBody body = entry.getValue();
            if (body.getParent().isPresent())
            {
                ResourceLocation parent = body.getParent().get();
                if (!bodies.containsKey(parent))
                    continue;
            }
            this.bodies.put(entry.getKey(), new SimulatedBody(this, body));
        }

        Set<SimulatedBody> initializedBodies = new HashSet<>();
        List<SimulatedBody> unvisitedBodies = new ArrayList<>(this.bodies.values());

        while (!unvisitedBodies.isEmpty())
        {
            SimulatedBody body = unvisitedBodies.remove(0);
            Optional<SimulatedBody> optionalParent = body.getBody().getParent().map(this.bodies::get);
            if (!optionalParent.isPresent() || !unvisitedBodies.contains(optionalParent.get()) || initializedBodies.contains(optionalParent.get()))
            {
                body.move();
            }
            else
            {
                unvisitedBodies.add(body);
            }
            initializedBodies.add(body);
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
        private final CelestialBodySimulation simulation;
        private final CelestialBody body;
        private final Vector3f lastPosition;
        private final Vector3f position;
        private float lastYaw;
        private float yaw;
        private float lastRotation;
        private float rotation;
        private float distanceFromParent;

        public SimulatedBody(CelestialBodySimulation simulation, CelestialBody body)
        {
            this.simulation = simulation;
            this.body = body;
            this.lastPosition = new Vector3f();
            this.position = new Vector3f();
            this.lastYaw = (float) (simulation.random.nextFloat() * Math.PI * 2);
            this.yaw = this.lastYaw;
            this.lastRotation = (float) (simulation.random.nextFloat() * Math.PI * 2);
            this.rotation = this.lastRotation;
            this.distanceFromParent = 20;
        }

        private void tick()
        {
            this.lastPosition.set(this.position.x(), this.position.y(), this.position.z());
            this.lastYaw = this.yaw;
            this.lastRotation = this.rotation;
            this.rotation += 1F / 180F * Math.PI;
            this.yaw += 0.01F / this.body.getScale();
        }

        private void move()
        {
            Optional<SimulatedBody> optional = this.body.getParent().map(this.simulation.bodies::get);
            if (!optional.isPresent())
                return;

            SimulatedBody parent = optional.get();
            this.position.setX(parent.lastPosition.x() + this.distanceFromParent * MathHelper.cos(this.yaw));
            this.position.setZ(parent.lastPosition.z() + this.distanceFromParent * MathHelper.sin(this.yaw));
            this.distanceFromParent = parent.getBody().getScale() * 5F;
        }

        private float getHorizontalDistance(float partialTicks)
        {
            return this.distanceFromParent * MathHelper.cos(MathHelper.lerp(partialTicks, this.lastYaw, this.yaw));
        }

        private float getVerticalDistance(float partialTicks)
        {
            return this.distanceFromParent * MathHelper.sin(MathHelper.lerp(partialTicks, this.lastYaw, this.yaw));
        }

        public CelestialBody getBody()
        {
            return body;
        }

        public float getX(float partialTicks)
        {
            Optional<SimulatedBody> optional = this.body.getParent().map(this.simulation.bodies::get);
            return optional.map(simulatedBody -> simulatedBody.getX(partialTicks) + this.getHorizontalDistance(partialTicks)).orElse(0F);
        }

        public float getY(float partialTicks)
        {
            return this.position.y();
        }

        public float getZ(float partialTicks)
        {
            Optional<SimulatedBody> optional = this.body.getParent().map(this.simulation.bodies::get);
            return optional.map(simulatedBody -> simulatedBody.getZ(partialTicks) + this.getVerticalDistance(partialTicks)).orElse(0F);
        }

        public float getRotation(float partialTicks)
        {
            return MathHelper.lerp(partialTicks, this.lastRotation, this.rotation);
        }
    }
}
