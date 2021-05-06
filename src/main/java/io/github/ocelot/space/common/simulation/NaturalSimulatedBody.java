package io.github.ocelot.space.common.simulation;

import io.github.ocelot.space.common.simulation.body.CelestialBody;
import io.github.ocelot.space.common.simulation.body.CelestialBodyAtmosphere;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.Optional;

/**
 * <p>A naturally spawning planet, moon, or asteroid.</p>
 *
 * @author Ocelot
 */
public class NaturalSimulatedBody extends AbstractSimulatedBody
{
    private final CelestialBody body;

    public NaturalSimulatedBody(CelestialBodySimulation simulation, ResourceLocation id, CelestialBody body)
    {
        super(simulation, id);
        this.body = body;
    }

    /**
     * Randomizes the distance from the parent.
     */
    protected void initializePosition()
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
    public Optional<CelestialBodyAtmosphere> getAtmosphere()
    {
        return this.body.getAtmosphere();
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
