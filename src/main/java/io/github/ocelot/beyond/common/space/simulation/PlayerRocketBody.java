package io.github.ocelot.beyond.common.space.simulation;

import com.mojang.math.Vector3f;
import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.MagicMath;
import io.github.ocelot.beyond.common.space.satellite.PlayerRocket;
import io.github.ocelot.beyond.common.util.Listenable;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * <p>The player inside the simulation.</p>
 *
 * @author Ocelot
 */
public class PlayerRocketBody extends AbstractSimulatedBody implements SatelliteBody<PlayerRocket>, Listenable<PlayerRocketBody.PlayerTravelListener>
{
    private static final float TRANSITION_SPEED = 0.0125F;

    private final Set<PlayerTravelListener> listeners;
    private final PlayerRocket rocket;
    private final Component displayName;
    private final float size;
    private ResourceLocation newParent;
    private final Vector3f transitionStart;
    private float newDistanceFromParent;
    private float lastTransition;
    private float transition;

    public PlayerRocketBody(CelestialBodySimulation simulation, PlayerRocket rocket)
    {
        super(simulation, new ResourceLocation(Beyond.MOD_ID, "satellite_" + rocket.getId()));
        this.setDistanceFromParent(Math.max(rocket.getOrbitingBody().map(simulation::getBody).map(SimulatedBody::getSize).orElse(1.0F) * 1.25F, 1.0F));
        this.listeners = new HashSet<>();
        this.rocket = rocket;
        this.displayName = rocket.getDisplayName();
        Vec3i structureSize = rocket.getRocket().getSize();
        this.size = 1.0F / Math.min(Math.min(structureSize.getX(), structureSize.getZ()), structureSize.getY());
        this.transitionStart = new Vector3f();
    }

    private float getTransition(float partialTicks)
    {
        return MagicMath.ease(Mth.lerp(partialTicks, this.lastTransition, this.transition));
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
                this.rocket.setOrbitingBody(this.newParent);
                this.newParent = null;
                this.setDistanceFromParent(this.newDistanceFromParent);
                this.listeners.forEach(listener -> listener.onArrive(this, this.rocket.getOrbitingBody().orElseThrow(() -> new IllegalStateException("Rocket should have a new parent body"))));
            }
        }
    }

    private float getNewHorizontalDistance(float partialTicks)
    {
        return this.newDistanceFromParent * Mth.cos(Mth.lerp(partialTicks, this.lastYaw, this.yaw));
    }

    private float getNewVerticalDistance(float partialTicks)
    {
        return this.newDistanceFromParent * Mth.sin(Mth.lerp(partialTicks, this.lastYaw, this.yaw));
    }

    @Override
    public float getX(float partialTicks)
    {
        if (this.newParent == null)
            return super.getX(partialTicks);
        float newX = this.getNewParent().map(this.simulation::getBody).map(simulatedBody -> simulatedBody.getX(partialTicks) + this.getNewHorizontalDistance(partialTicks)).orElse(0F);
        return Mth.lerp(this.getTransition(partialTicks), this.transitionStart.x(), newX);
    }

    @Override
    public float getY(float partialTicks)
    {
        if (this.newParent == null)
            return super.getY(partialTicks);
        float newY = 0;
        return Mth.lerp(this.getTransition(partialTicks), this.transitionStart.y(), newY);
    }

    @Override
    public float getZ(float partialTicks)
    {
        if (this.newParent == null)
            return super.getZ(partialTicks);
        float newZ = this.getNewParent().map(this.simulation::getBody).map(simulatedBody -> simulatedBody.getZ(partialTicks) + this.getNewVerticalDistance(partialTicks)).orElse(0F);
        return Mth.lerp(this.getTransition(partialTicks), this.transitionStart.z(), newZ);
    }

    @Override
    public Optional<ResourceLocation> getParent()
    {
        return this.rocket.getOrbitingBody();
    }

    public Optional<ResourceLocation> getNewParent()
    {
        return Optional.ofNullable(this.newParent);
    }

    @Override
    public Component getDisplayName()
    {
        return displayName;
    }

    @Override
    public Optional<Component> getDescription()
    {
        return Optional.empty();
    }

    @Override
    public float getSize()
    {
        return this.size * 0.5F;
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

    @Override
    public PlayerRocket getSatellite()
    {
        return rocket;
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
        this.rocket.setOrbitingBody(parent);
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
