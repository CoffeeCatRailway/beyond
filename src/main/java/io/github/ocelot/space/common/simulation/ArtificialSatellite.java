package io.github.ocelot.space.common.simulation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * <p>A basic artificial satellite.</p>
 *
 * @author Ocelot
 */
public class ArtificialSatellite extends AbstractSimulatedBody implements ModelSimulatedBody
{
    private ResourceLocation model;
    private ResourceLocation parent;
    private ITextComponent displayName;

    public ArtificialSatellite(CelestialBodySimulation simulation, ResourceLocation id)
    {
        super(simulation, id);
        this.model = null;
        this.parent = null;
        this.displayName = new StringTextComponent(id.toString());
    }

    @Override
    public IBakedModel getModel()
    {
        ModelManager modelManager = Minecraft.getInstance().getModelManager();
        if (this.model == null)
            return modelManager.getMissingModel();
        return modelManager.getModel(this.model);
    }

    @Override
    public Optional<ResourceLocation> getParent()
    {
        return Optional.ofNullable(this.parent);
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
    public RenderType getRenderType()
    {
        return RenderType.MODEL;
    }

    public void setModel(ResourceLocation model)
    {
        this.model = model;
    }

    public void setParent(@Nullable ResourceLocation parent)
    {
        this.parent = parent;
    }

    public void setDisplayName(ITextComponent displayName)
    {
        this.displayName = displayName;
    }
}
