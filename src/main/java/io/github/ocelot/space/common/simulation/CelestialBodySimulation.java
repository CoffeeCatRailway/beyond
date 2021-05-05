package io.github.ocelot.space.common.simulation;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;

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
            this.bodies.put(entry.getKey(), new SimulatedBodyOld(this, entry.getKey(), body));
        }

        Set<SimulatedBody> initializedBodies = new HashSet<>();
        List<SimulatedBody> unvisitedBodies = new ArrayList<>(this.bodies.values());

        while (!unvisitedBodies.isEmpty())
        {
            SimulatedBody body = unvisitedBodies.remove(0);
            Optional<SimulatedBody> optionalParent = body.getParent().map(this.bodies::get);
            if (!optionalParent.isPresent() || !unvisitedBodies.contains(optionalParent.get()))
            {
                if (optionalParent.isPresent())
                    ((SimulatedBodyOld) body).randomizeDistance();
            }
            else if (initializedBodies.contains(optionalParent.get()))
            {
                ((SimulatedBodyOld) body).randomizeDistance();
                ((SimulatedBodyOld) body).root = true;
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
    public static class SimulatedBodyOld implements SimulatedBody
    {
        private final CelestialBodySimulation simulation;
        private final ResourceLocation id;
        private final CelestialBody body;
        private float lastYaw;
        private float yaw;
        private float lastRotation;
        private float rotation;
        private float distanceFromParent;
        private boolean root;

        public SimulatedBodyOld(CelestialBodySimulation simulation, ResourceLocation id, CelestialBody body)
        {
            this.simulation = simulation;
            this.id = id;
            this.body = body;
            this.lastYaw = (float) (simulation.random.nextFloat() * Math.PI * 2);
            this.yaw = this.lastYaw;
            this.lastRotation = (float) (simulation.random.nextFloat() * Math.PI * 2);
            this.rotation = this.lastRotation;
            this.distanceFromParent = 0;
            this.root = false;
        }

        @Override
        public void tick()
        {
            this.lastYaw = this.yaw;
            this.lastRotation = this.rotation;
            this.rotation += 1F / 180F * Math.PI;
            this.yaw += 0.01F / this.body.getSize();
        }

        private void randomizeDistance()
        {
            Optional<SimulatedBody> optional = this.body.getParent().map(this.simulation.bodies::get);
            if (!optional.isPresent())
                return;
            float scale = optional.get().getSize();
            this.distanceFromParent = scale * 5F;
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
         * @return The texture of this body
         */
        public ResourceLocation getTexture()
        {
            return this.body.getTexture();
        }

        /**
         * @return Whether or not shading should be applied to this body
         */
        public boolean isShade()
        {
            return this.body.isShade();
        }

        @Override
        public ResourceLocation getId()
        {
            return id;
        }

        @Override
        public Optional<ResourceLocation> getParent()
        {
            return this.body.getParent();
        }

        @Override
        public ITextComponent getDisplayName()
        {
            return this.body.getDisplayName();
        }

        @Override
        public float getSize()
        {
            return this.body.getSize();
        }

        @Override
        public float getX(float partialTicks)
        {
            Optional<SimulatedBody> optional = this.body.getParent().map(this.simulation.bodies::get);
            return optional.map(simulatedBody -> (this.root ? 0 : simulatedBody.getX(partialTicks)) + this.getHorizontalDistance(partialTicks)).orElse(0F);
        }

        @Override
        public float getY(float partialTicks)
        {
            return 0;
        }

        @Override
        public float getZ(float partialTicks)
        {
            Optional<SimulatedBody> optional = this.body.getParent().map(this.simulation.bodies::get);
            return optional.map(simulatedBody -> (this.root ? 0 : simulatedBody.getZ(partialTicks)) + this.getVerticalDistance(partialTicks)).orElse(0F);
        }

        @Override
        public float getRotationX(float partialTicks)
        {
            return this.getRotationY(partialTicks);
        }

        @Override
        public float getRotationY(float partialTicks)
        {
            return MathHelper.lerp(partialTicks, this.lastRotation, this.rotation);
        }

        @Override
        public float getRotationZ(float partialTicks)
        {
            return this.getRotationY(partialTicks);
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
}
