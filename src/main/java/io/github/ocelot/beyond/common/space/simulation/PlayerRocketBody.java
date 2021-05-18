package io.github.ocelot.beyond.common.space.simulation;

import io.github.ocelot.beyond.common.MagicMath;
import io.github.ocelot.beyond.common.space.PlayerRocket;
import io.github.ocelot.beyond.common.util.Listenable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * <p>The player inside the simulation.</p>
 *
 * @author Ocelot
 */
public class PlayerRocketBody extends AbstractSimulatedBody implements Listenable<PlayerRocketBody.PlayerTravelListener>
{
    private static final float TRANSITION_SPEED = 0.0125F;

    private final Set<PlayerTravelListener> listeners;
    private final PlayerRocket rocket;
    private final ITextComponent displayName;
    private ResourceLocation parent;
    private ResourceLocation newParent;
    private final Vector3f transitionStart;
    private float newDistanceFromParent;
    private float lastTransition;
    private float transition;

    public PlayerRocketBody(CelestialBodySimulation simulation, PlayerRocket rocket)
    {
        super(simulation, rocket.getId());
        this.setDistanceFromParent(Math.max(Optional.ofNullable(simulation.getBody(rocket.getOrbitingBody())).map(SimulatedBody::getSize).orElse(1.0F) * 1.25F, 1.0F));
        this.listeners = new HashSet<>();
        this.parent = rocket.getOrbitingBody();
        this.rocket = rocket;
        this.displayName = rocket.getDisplayName();
        this.transitionStart = new Vector3f();
    }

    private float getTransition(float partialTicks)
    {
        return MagicMath.ease(MathHelper.lerp(partialTicks, this.lastTransition, this.transition));
    }

    @Override
    public void tick()
    {
        super.tick();
        this.lastTransition = this.transition;
        if (this.newParent != null && this.transition < 1.0F)
        {
            this.transition += TRANSITION_SPEED;
            if (this.transition >= 1.0F)
            {
                this.transition = 1.0F;
                this.parent = this.newParent;
                this.newParent = null;
                this.setDistanceFromParent(this.newDistanceFromParent);
                this.listeners.forEach(listener -> listener.onArrive(this, this.parent));
            }
        }
    }

    private float getNewHorizontalDistance(float partialTicks)
    {
        return this.newDistanceFromParent * MathHelper.cos(MathHelper.lerp(partialTicks, this.lastYaw, this.yaw));
    }

    private float getNewVerticalDistance(float partialTicks)
    {
        return this.newDistanceFromParent * MathHelper.sin(MathHelper.lerp(partialTicks, this.lastYaw, this.yaw));
    }

    @Override
    public float getX(float partialTicks)
    {
        if (this.newParent == null)
            return super.getX(partialTicks);
        float newX = this.getNewParent().map(this.simulation::getBody).map(simulatedBody -> simulatedBody.getX(partialTicks) + this.getNewHorizontalDistance(partialTicks)).orElse(0F);
        return MathHelper.lerp(this.getTransition(partialTicks), this.transitionStart.x(), newX);
    }

    @Override
    public float getY(float partialTicks)
    {
        if (this.newParent == null)
            return super.getY(partialTicks);
        float newY = 0;
        return MathHelper.lerp(this.getTransition(partialTicks), this.transitionStart.y(), newY);
    }

    @Override
    public float getZ(float partialTicks)
    {
        if (this.newParent == null)
            return super.getZ(partialTicks);
        float newZ = this.getNewParent().map(this.simulation::getBody).map(simulatedBody -> simulatedBody.getZ(partialTicks) + this.getNewVerticalDistance(partialTicks)).orElse(0F);
        return MathHelper.lerp(this.getTransition(partialTicks), this.transitionStart.z(), newZ);
    }

    @Override
    public Optional<ResourceLocation> getParent()
    {
        return Optional.of(this.parent);
    }

    public Optional<ResourceLocation> getNewParent()
    {
        return Optional.ofNullable(this.newParent);
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return displayName;
    }

    @Override
    public float getSize()
    {
        return 1.0F;
    }

    @Override
    public boolean canTeleportTo()
    {
        return false;
    }

    @Override
    public Optional<ResourceLocation> getDimension()
    {
        return Optional.empty();
    }

    @Override
    public RenderType getRenderType()
    {
        return RenderType.PLAYER;
    }

    @Override
    public Set<PlayerTravelListener> getListeners()
    {
        return listeners;
    }

    /**
     * @return The rocket this body is simulating
     */
    public PlayerRocket getRocket()
    {
        return this.rocket;
    }

    /**
     * Sets the body the player rocket should travel to.
     *
     * @param body The body to transition towards
     */
    public void travelTo(ResourceLocation body)
    {
        SimulatedBody simulatedBody = Objects.requireNonNull(this.simulation.getBody(body));
        this.transitionStart.set(this.getX(1.0F), this.getY(1.0F), this.getZ(1.0F));
        this.lastTransition = 0.0F;
        this.transition = 0.0F;
        this.newParent = body;
        this.newDistanceFromParent = Math.max(simulatedBody.getSize() * 1.25F, 1.0F);
        this.listeners.forEach(listener -> listener.onDepart(this, this.newParent));
    }

    /**
     * Sets the body this rocket should orbit.
     *
     * @param parent The new parent
     */
    public void setParent(ResourceLocation parent)
    {
        SimulatedBody simulatedBody = Objects.requireNonNull(this.simulation.getBody(parent));
        this.parent = parent;
        this.setDistanceFromParent(Math.max(simulatedBody.getSize() * 1.25F, 1.0F));
    }

    /**
     * <p>Listens for when a player in a rocket arrives at a body.</p>
     *
     * @author Ocelot
     */
    public interface PlayerTravelListener
    {
        /**
         * Called when the specified player rocket travels to the specified body.
         *
         * @param rocket The rocket travelling
         * @param body   The body the rocket is travelling to
         */
        void onDepart(PlayerRocketBody rocket, ResourceLocation body);

        /**
         * Called when the specified player rocket arrives at the specified body.
         *
         * @param rocket The rocket travelling
         * @param body   The body the rocket arrived at
         */
        void onArrive(PlayerRocketBody rocket, ResourceLocation body);
    }
}
