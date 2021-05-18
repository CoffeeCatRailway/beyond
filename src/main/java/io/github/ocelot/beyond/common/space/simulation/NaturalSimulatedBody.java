package io.github.ocelot.beyond.common.space.simulation;

import io.github.ocelot.beyond.common.space.planet.Planet;
import io.github.ocelot.beyond.common.space.planet.PlanetAtmosphere;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * <p>A naturally spawning planet, moon, or asteroid.</p>
 *
 * @author Ocelot
 */
public class NaturalSimulatedBody extends AbstractSimulatedBody
{
    private final Planet body;

    public NaturalSimulatedBody(CelestialBodySimulation simulation, ResourceLocation id, Planet body)
    {
        super(simulation, id);
        this.body = body;
    }

    /**
     * Randomizes the distance from the parent.
     */
    public void initializePosition()
    {
        Optional<SimulatedBody> optional = this.body.getParent().map(this.simulation::getBody);
        if (!optional.isPresent())
            return;
        float scale = optional.get().getSize();
        this.setDistanceFromParent(scale * 5F * this.body.getDistanceFactor());
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

    /**
     * @return The atmosphere to render on the body
     */
    public Optional<PlanetAtmosphere> getAtmosphere()
    {
        return this.body.getAtmosphere();
    }

    @Override
    public Optional<ResourceLocation> getParent()
    {
        return this.body.getParent();
    }

    @Override
    public Component getDisplayName()
    {
        return this.body.getDisplayName();
    }

    @Override
    public float getSize()
    {
        return this.body.getSize();
    }

    @Override
    public boolean canTeleportTo()
    {
        return true;
    }

    @Override
    public Optional<ResourceLocation> getDimension()
    {
        return this.body.getDimension();
    }

    @Override
    public RenderType getRenderType()
    {
        return RenderType.CUBE;
    }
}
