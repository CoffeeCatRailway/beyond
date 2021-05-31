package io.github.ocelot.beyond.common.space.satellite;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.ocelot.beyond.common.space.simulation.CelestialBodySimulation;
import io.github.ocelot.beyond.common.space.simulation.PlayerRocketBody;
import io.netty.handler.codec.DecoderException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * <p>A representation of a player inside a rocket.</p>
 *
 * @author Ocelot
 */
public class PlayerRocket extends AbstractSatellite
{
    public static final Codec<PlayerRocket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("displayName").<Component>xmap(Component.Serializer::fromJson, Component.Serializer::toJson).forGetter(AbstractSatellite::getDisplayName),
            ResourceLocation.CODEC.optionalFieldOf("orbitingBody").forGetter(AbstractSatellite::getOrbitingBody),
            Codec.LONG.fieldOf("profileIdMost").forGetter(rocket -> rocket.getProfile().getId().getMostSignificantBits()),
            Codec.LONG.fieldOf("profileIdLeast").forGetter(rocket -> rocket.getProfile().getId().getLeastSignificantBits()),
            Codec.STRING.fieldOf("profileName").forGetter(rocket -> rocket.getProfile().getName()),
            Codec.BYTE_BUFFER.fieldOf("rocket").xmap(buf ->
            {
                try
                {
                    CompoundTag nbt = NbtIo.readCompressed(new ByteArrayInputStream(buf.array()));
                    StructureTemplate template = new StructureTemplate();
                    template.load(nbt);
                    return template;
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }, template ->
            {
                try
                {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    NbtIo.writeCompressed(template.save(new CompoundTag()), os);
                    return ByteBuffer.wrap(os.toByteArray());
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }).forGetter(PlayerRocket::getRocket)
    ).apply(instance, (displayName, orbitingBody, profileIdMost, profileIdLeast, profileName, rocket) -> new PlayerRocket(displayName, orbitingBody.orElse(null), new UUID(profileIdMost, profileIdLeast), profileName, rocket)));

    private final GameProfile profile;
    private final StructureTemplate rocket;

    private PlayerRocket(Component displayName, @Nullable ResourceLocation orbitingBody, UUID profileId, String profileName, StructureTemplate rocket)
    {
        super(displayName, orbitingBody);
        if (profileName.length() > 16)
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + profileName.length() + " > 16)");
        this.profile = new GameProfile(profileId, profileName);
        this.rocket = rocket;
    }

    public PlayerRocket(Player player, @Nullable ResourceLocation orbitingBody, StructureTemplate rocket)
    {
        super(player.getDisplayName(), orbitingBody);
        this.profile = player.getGameProfile();
        this.rocket = rocket;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public PlayerRocketBody createBody(CelestialBodySimulation simulation)
    {
        return new PlayerRocketBody(simulation, this);
    }

    @Override
    public Type getType()
    {
        return Type.PLAYER;
    }

    @Override
    public Codec<PlayerRocket> getCodec()
    {
        return CODEC;
    }

    /**
     * @return The profile for the player
     */
    public GameProfile getProfile()
    {
        return profile;
    }

    /**
     * @return The structure that represents the player rocket
     */
    public StructureTemplate getRocket()
    {
        return rocket;
    }
}
