package io.github.ocelot.space.common.planet;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import java.util.*;
import java.util.stream.Stream;

/**
 * <p>Simulates the solar system based on rotations around bodies.</p>
 *
 * @author Ocelot
 */
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
            this.bodies.put(entry.getKey(), new SimulatedBody(this, entry.getKey(), body));
        }

        Set<SimulatedBody> initializedBodies = new HashSet<>();
        List<SimulatedBody> unvisitedBodies = new ArrayList<>(this.bodies.values());

        while (!unvisitedBodies.isEmpty())
        {
            SimulatedBody body = unvisitedBodies.remove(0);
            Optional<SimulatedBody> optionalParent = body.getBody().getParent().map(this.bodies::get);
            if (!optionalParent.isPresent() || !unvisitedBodies.contains(optionalParent.get()))
            {
                if (optionalParent.isPresent())
                    body.randomizeDistance();
                body.move();
            }
            else if (initializedBodies.contains(optionalParent.get()))
            {
                body.randomizeDistance();
                body.move();
                body.root = true;
            }
            else
            {
                unvisitedBodies.add(body);
            }
            initializedBodies.add(body);
        }
    }

    /**
     * Steps through the simulation.
     */
    public void tick()
    {
        this.bodies.values().forEach(SimulatedBody::move);
        this.bodies.values().forEach(SimulatedBody::tick);
    }

    /**
     * @return All bodies in the simulation
     */
    public Stream<SimulatedBody> getBodies()
    {
        return this.bodies.values().stream();
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

    /**
     * <p>A body in the simulation.</p>
     *
     * @author Ocelot
     */
    public static class SimulatedBody
    {
        private final CelestialBodySimulation simulation;
        private final ResourceLocation id;
        private final CelestialBody body;
        private final Vector3f lastPosition;
        private final Vector3f position;
        private float lastYaw;
        private float yaw;
        private float lastRotation;
        private float rotation;
        private float distanceFromParent;
        private boolean root;

        public SimulatedBody(CelestialBodySimulation simulation, ResourceLocation id, CelestialBody body)
        {
            this.simulation = simulation;
            this.id = id;
            this.body = body;
            this.lastPosition = new Vector3f();
            this.position = new Vector3f();
            this.lastYaw = (float) (simulation.random.nextFloat() * Math.PI * 2);
            this.yaw = this.lastYaw;
            this.lastRotation = (float) (simulation.random.nextFloat() * Math.PI * 2);
            this.rotation = this.lastRotation;
            this.distanceFromParent = 0;
            this.root = false;
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
        }

        private void randomizeDistance()
        {
            Optional<SimulatedBody> optional = this.body.getParent().map(this.simulation.bodies::get);
            if (!optional.isPresent())
                return;
            float scale = optional.get().getBody().getScale();
            this.distanceFromParent = (float) (this.simulation.random.nextGaussian() * scale * 0.25F + scale * 5F);
        }

        private float getHorizontalDistance(float partialTicks)
        {
            return this.distanceFromParent * MathHelper.cos(MathHelper.lerp(partialTicks, this.lastYaw, this.yaw));
        }

        private float getVerticalDistance(float partialTicks)
        {
            return this.distanceFromParent * MathHelper.sin(MathHelper.lerp(partialTicks, this.lastYaw, this.yaw));
        }

        /**
         * @return The id of this body in the simulation
         */
        public ResourceLocation getId()
        {
            return id;
        }

        /**
         * @return The attributes of this body in the simulation
         */
        public CelestialBody getBody()
        {
            return body;
        }

        /**
         * Casts a ray through this body to check for an intersection.
         *
         * @param start        The starting position of the ray
         * @param end          The ending position of the ray
         * @param partialTicks The percentage from last update and this update
         * @return The optional result of the ray trace
         */
        public Optional<Vector3d> clip(Vector3d start, Vector3d end, float partialTicks)
        {
            float size = this.body.getScale();
            float x = this.getX(partialTicks);
            float y = this.getY(partialTicks);
            float z = this.getZ(partialTicks);
            float rotation = this.getRotation(partialTicks);
            AxisAlignedBB box = new AxisAlignedBB(x - size, y - size, z - size, x + size, y + size, z + size);
            return box.clip(rotate(start, rotation, x, z), rotate(end, rotation, x, z)).map(p -> rotate(p, -rotation, x, z));
        }

        /**
         * Calculates the x position of this body.
         *
         * @param partialTicks The percentage from last tick and this tick
         * @return The x position of this body
         */
        public float getX(float partialTicks)
        {
            Optional<SimulatedBody> optional = this.body.getParent().map(this.simulation.bodies::get);
            return optional.map(simulatedBody -> (this.root ? 0 : simulatedBody.getX(partialTicks)) + this.getHorizontalDistance(partialTicks)).orElse(0F);
        }

        /**
         * Calculates the y position of this body.
         *
         * @param partialTicks The percentage from last tick and this tick
         * @return The y position of this body
         */
        public float getY(float partialTicks)
        {
            return this.position.y();
        }

        /**
         * Calculates the z position of this body.
         *
         * @param partialTicks The percentage from last tick and this tick
         * @return The z position of this body
         */
        public float getZ(float partialTicks)
        {
            Optional<SimulatedBody> optional = this.body.getParent().map(this.simulation.bodies::get);
            return optional.map(simulatedBody -> (this.root ? 0 : simulatedBody.getZ(partialTicks)) + this.getVerticalDistance(partialTicks)).orElse(0F);
        }

        /**
         * Calculates the rotation of this body.
         *
         * @param partialTicks The percentage from last tick and this tick
         * @return The y rotation of this body
         */
        public float getRotation(float partialTicks)
        {
            return MathHelper.lerp(partialTicks, this.lastRotation, this.rotation);
        }
    }

    /**
     * <p>Holds the result of a ray trace in a simulation.</p>
     *
     * @author Ocelot
     */
    public static class CelestialBodyRayTraceResult
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

    private static Vector3d rotate(Vector3d pos, double angle, double rotX, double rotZ)
    {
        float cos = MathHelper.cos((float) angle);
        float sin = MathHelper.sin((float) angle);
        double x = rotX + (pos.x - rotX) * (double) cos - (pos.z - rotZ) * (double) sin;
        double z = rotZ + (pos.x - rotX) * (double) sin + (pos.z - rotZ) * (double) cos;
        return new Vector3d(x, pos.y, z);
    }
}
