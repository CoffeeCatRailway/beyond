package io.github.ocelot.beyond.common.space.satellite;

import io.github.ocelot.beyond.common.space.simulation.ArtificialSatelliteBody;
import io.github.ocelot.beyond.common.space.simulation.CelestialBodySimulation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * <p>A representation of a science satellite in space.</p>
 *
 * @author Ocelot
 */
public class ArtificialSatellite extends AbstractSatellite
{
    private final ResourceLocation model;

    public ArtificialSatellite(Component displayName, ResourceLocation orbitingBody, ResourceLocation model)
    {
        super(displayName, orbitingBody);
        this.model = model;
    }

    public ArtificialSatellite(FriendlyByteBuf buf)
    {
        super(buf);
        this.model = buf.readResourceLocation();
    }

    @Override
    protected void writeAdditional(FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(this.model);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
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
     * @return The model this satellite uses
     */
    public ResourceLocation getModel()
    {
        return model;
    }
}
