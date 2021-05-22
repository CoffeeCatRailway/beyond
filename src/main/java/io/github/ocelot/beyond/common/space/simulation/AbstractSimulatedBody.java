package io.github.ocelot.beyond.common.space.simulation;

import com.mojang.math.Vector3f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * <p>An abstract implementation of {@link SimulatedBody} that handles orbits and parenting.</p>
 *
 * @author Ocelot
 */
public abstract class AbstractSimulatedBody implements SimulatedBody
{
    protected final CelestialBodySimulation simulation;
    private final ResourceLocation id;
    private final Vector3f lastRotation;
    private final Vector3f rotation;
    private float distanceFromParent;
    protected float distanceFromParentSqrt;
    protected float lastYaw;
    protected float yaw;

    protected AbstractSimulatedBody(CelestialBodySimulation simulation, ResourceLocation id)
    {
        this.simulation = simulation;
        this.id = id;
        this.lastRotation = new Vector3f(0.0F, (float) (simulation.getRandom().nextFloat() * Math.PI * 2), 0.0F);
        this.rotation = new Vector3f(this.lastRotation.x(), this.lastRotation.y(), this.lastRotation.z());
        this.distanceFromParent = 0;
        this.distanceFromParentSqrt = 0;
        this.lastYaw = (float) (simulation.getRandom().nextFloat() * Math.PI * 2);
        this.yaw = this.lastYaw;
    }

    @Override
    public void tick()
    {
        this.lastRotation.set(this.rotation.x(), this.rotation.y(), this.rotation.z());
        this.lastYaw = this.yaw;
        this.rotation.setY((float) (this.rotation.y() + (0.2F + this.distanceFromParentSqrt / 10F) / 180F * Math.PI));
        this.yaw += 0.01F / this.getSize() / this.distanceFromParentSqrt;
    }

    /**
     * Calculates the distance from the rotation point of this body based on the current rotation
     *
     * @param partialTicks The percentage from last tick and this tick
     * @return The distance in the x
     */
    protected float getHorizontalDistance(float partialTicks)
    {
        if (this.distanceFromParent == 0.0F)
            return 0.0F;
        float angle = Mth.lerp(partialTicks, this.lastYaw, this.yaw);
        if (this.distanceFromParent < 64)
            return this.distanceFromParent * Mth.cos(angle);
        return (float) (this.distanceFromParent * Math.cos(angle));
    }

    /**
     * Calculates the distance from the rotation point of this body based on the current rotation
     *
     * @param partialTicks The percentage from last tick and this tick
     * @return The distance in the z
     */
    protected float getVerticalDistance(float partialTicks)
    {
        if (this.distanceFromParent == 0.0F)
            return 0.0F;
        float angle = Mth.lerp(partialTicks, this.lastYaw, this.yaw);
        if (this.distanceFromParent < 64)
            return this.distanceFromParent * Mth.sin(angle);
        return (float) (this.distanceFromParent * Math.sin(angle));
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public float getDistanceFromParent()
    {
        return distanceFromParent;
    }

    @Override
    public float getX(float partialTicks)
    {
        return this.getParent().map(this.simulation::getBody).map(simulatedBody -> simulatedBody.getX(partialTicks) + this.getHorizontalDistance(partialTicks)).orElse(0F);
    }

    @Override
    public float getY(float partialTicks)
    {
        return 0;
    }

    @Override
    public float getZ(float partialTicks)
    {
        return this.getParent().map(this.simulation::getBody).map(simulatedBody -> simulatedBody.getZ(partialTicks) + this.getVerticalDistance(partialTicks)).orElse(0F);
    }

    @Override
    public float getRotationX(float partialTicks)
    {
        return Mth.lerp(partialTicks, this.lastRotation.x(), this.rotation.x());
    }

    @Override
    public float getRotationY(float partialTicks)
    {
        return Mth.lerp(partialTicks, this.lastRotation.y(), this.rotation.y());
    }

    @Override
    public float getRotationZ(float partialTicks)
    {
        return Mth.lerp(partialTicks, this.lastRotation.z(), this.rotation.z());
    }

    /**
     * Sets the distance from this body and the parent body.
     *
     * @param distanceFromParent The new distance
     */
    public void setDistanceFromParent(float distanceFromParent)
    {
        this.distanceFromParent = distanceFromParent;
        this.distanceFromParentSqrt = Mth.sqrt(distanceFromParent);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractSimulatedBody that = (AbstractSimulatedBody) o;
        return this.id.equals(that.id);
    }

    @Override
    public int hashCode()
    {
        return this.id.hashCode();
    }
}
