package io.github.ocelot.beyond.common.space.satellite;

import io.github.ocelot.beyond.common.space.simulation.ArtificialSatelliteBody;
import io.github.ocelot.beyond.common.space.simulation.CelestialBodySimulation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * <p>A representation of a player inside a rocket.</p>
 *
 * @author Ocelot
 */
public class ArtificialSatellite implements Satellite
{
    private final ResourceLocation id;
    private final ResourceLocation model;
    private final Component displayName;
    private ResourceLocation orbitingBody;

    public ArtificialSatellite(ResourceLocation id, ResourceLocation model, Component displayName, ResourceLocation orbitingBody)
    {
        this.id = id;
        this.model = model;
        this.displayName = displayName;
        this.orbitingBody = orbitingBody;
    }

    public ArtificialSatellite(FriendlyByteBuf buf)
    {
        this(buf.readResourceLocation(), buf.readResourceLocation(), buf.readComponent(), buf.readResourceLocation());
    }

    @Override
    public void write(FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(this.id);
        buf.writeResourceLocation(this.model);
        buf.writeComponent(this.displayName);
        buf.writeResourceLocation(this.orbitingBody);
    }

    @Override
    public ArtificialSatelliteBody createBody(CelestialBodySimulation simulation)
    {
        return new ArtificialSatelliteBody(simulation, this);
    }

    @Override
    public Type getType()
    {
        return Type.ARTIFICIAL;
    }

    /**
     * @return The id of the player rocket in the simulation
     */
    public ResourceLocation getId()
    {
        return id;
    }

    /**
     * @return The model this satellite uses
     */
    public ResourceLocation getModel()
    {
        return model;
    }

    /**
     * @return The display name of the player
     */
    public Component getDisplayName()
    {
        return displayName;
    }

    /**
     * @return The body to orbit
     */
    public ResourceLocation getOrbitingBody()
    {
        return orbitingBody;
    }

    /**
     * Sets the body for this satellite to orbit
     *
     * @param orbitingBody The body to orbit
     */
    public void setOrbitingBody(ResourceLocation orbitingBody)
    {
        this.orbitingBody = orbitingBody;
    }
}
