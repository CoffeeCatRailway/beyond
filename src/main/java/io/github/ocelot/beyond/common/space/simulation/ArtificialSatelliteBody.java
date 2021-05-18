package io.github.ocelot.beyond.common.space.simulation;

import io.github.ocelot.beyond.common.space.satellite.ArtificialSatellite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * <p>A basic artificial satellite.</p>
 *
 * @author Ocelot
 */
public class ArtificialSatelliteBody extends AbstractSimulatedBody implements SatelliteBody<ArtificialSatellite>, ModelSimulatedBody
{
    private final ArtificialSatellite satellite;

    public ArtificialSatelliteBody(CelestialBodySimulation simulation, ArtificialSatellite satellite)
    {
        super(simulation, satellite.getId());
        this.satellite = satellite;
        this.setDistanceFromParent(5.0F);
    }

    @Override
    public ArtificialSatellite getSatellite()
    {
        return satellite;
    }

    @Override
    public BakedModel getModel()
    {
        ModelManager modelManager = Minecraft.getInstance().getModelManager();
        if (this.satellite.getModel() == null)
            return modelManager.getMissingModel();
        return modelManager.getModel(this.satellite.getModel());
    }

    @Override
    public Optional<ResourceLocation> getParent()
    {
        return Optional.ofNullable(this.satellite.getOrbitingBody());
    }

    @Override
    public Component getDisplayName()
    {
        return this.satellite.getDisplayName();
    }

    @Override
    public Optional<Component> getDescription()
    {
        return Optional.empty();
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
        return RenderType.MODEL;
    }
}
