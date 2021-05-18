package io.github.ocelot.beyond.common.space.simulation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

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
    private Component displayName;
    private float size;

    public ArtificialSatellite(CelestialBodySimulation simulation, ResourceLocation id)
    {
        super(simulation, id);
        this.model = null;
        this.parent = null;
        this.displayName = new TextComponent(id.toString());
        this.size = 1.0F;
    }

    @Override
    public BakedModel getModel()
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
    public Component getDisplayName()
    {
        return displayName;
    }

    @Override
    public float getSize()
    {
        return size;
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

    /**
     * Sets the model to use for this satellite
     *
     * @param model The new model to use
     */
    public void setModel(ResourceLocation model)
    {
        this.model = model;
    }

    /**
     * Sets the parent body for this satellite
     *
     * @param parent The new parent of this body
     */
    public void setParent(@Nullable ResourceLocation parent)
    {
        this.parent = parent;
    }

    /**
     * Sets the display name for the satellite.
     *
     * @param displayName The new display name
     */
    public void setDisplayName(Component displayName)
    {
        this.displayName = displayName;
    }

    /**
     * Sets the size of the satellite.
     *
     * @param size The new size
     */
    public void setSize(float size)
    {
        this.size = size;
    }
}
