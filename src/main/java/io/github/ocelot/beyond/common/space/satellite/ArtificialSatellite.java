package io.github.ocelot.beyond.common.space.satellite;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.ocelot.beyond.common.space.simulation.ArtificialSatelliteBody;
import io.github.ocelot.beyond.common.space.simulation.CelestialBodySimulation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * <p>A representation of a science satellite in space.</p>
 *
 * @author Ocelot
 */
public class ArtificialSatellite extends AbstractSatellite
{
    public static final Codec<ArtificialSatellite> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("displayName").<Component>xmap(Component.Serializer::fromJson, Component.Serializer::toJson).forGetter(AbstractSatellite::getDisplayName),
            ResourceLocation.CODEC.optionalFieldOf("orbitingBody").forGetter(AbstractSatellite::getOrbitingBody),
            ResourceLocation.CODEC.fieldOf("model").forGetter(ArtificialSatellite::getModel)
    ).apply(instance, (displayName, orbitingBody, model) -> new ArtificialSatellite(displayName, orbitingBody.orElse(null), model)));

    private final ResourceLocation model;

    public ArtificialSatellite(Component displayName, @Nullable ResourceLocation orbitingBody, ResourceLocation model)
    {
        super(displayName, orbitingBody);
        this.model = model;
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

    @Override
    public Codec<ArtificialSatellite> getCodec()
    {
        return CODEC;
    }

    /**
     * @return The model this satellite uses
     */
    public ResourceLocation getModel()
    {
        return model;
    }
}
