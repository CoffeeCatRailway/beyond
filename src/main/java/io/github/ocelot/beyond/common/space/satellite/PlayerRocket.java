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
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <p>A representation of a player inside a rocket.</p>
 *
 * @author Ocelot
 */
public class PlayerRocket extends AbstractSatellite
{
    public static final Codec<StructureTemplate> TEMPLATE_CODEC = Codec.BYTE_BUFFER.fieldOf("data").xmap(buf ->
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
    }).codec();

    public static final Codec<PlayerRocket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("displayName").<Component>xmap(Component.Serializer::fromJson, Component.Serializer::toJson).forGetter(AbstractSatellite::getDisplayName),
            ResourceLocation.CODEC.optionalFieldOf("orbitingBody").forGetter(AbstractSatellite::getOrbitingBody),
            Codec.LONG.listOf().fieldOf("profileIdMost").forGetter(rocket -> Arrays.stream(rocket.getProfiles()).map(profile -> profile.getId().getMostSignificantBits()).collect(Collectors.toList())),
            Codec.LONG.listOf().fieldOf("profileIdLeast").forGetter(rocket -> Arrays.stream(rocket.getProfiles()).map(profile -> profile.getId().getLeastSignificantBits()).collect(Collectors.toList())),
            Codec.STRING.listOf().fieldOf("profileNames").forGetter(rocket -> Arrays.stream(rocket.getProfiles()).map(GameProfile::getName).collect(Collectors.toList())),
            TEMPLATE_CODEC.fieldOf("rocket").forGetter(PlayerRocket::getRocket)
    ).apply(instance, (displayName, orbitingBody, profileIdMost, profileIdLeast, profileNames, rocket) -> new PlayerRocket(displayName, orbitingBody.orElse(null), profileIdMost.stream().mapToLong(Long::longValue).toArray(), profileIdLeast.stream().mapToLong(Long::longValue).toArray(), profileNames.toArray(new String[0]), rocket)));

    private final GameProfile[] profiles;
    private final StructureTemplate rocket;

    private PlayerRocket(Component displayName, @Nullable ResourceLocation orbitingBody, long[] profileIdMost, long[] profileIdLeast, String[] profileNames, StructureTemplate rocket)
    {
        super(displayName, orbitingBody);
        if (profileIdMost.length != profileIdLeast.length)
            throw new DecoderException("The received player profile ids were not complete.");
        if (profileIdMost.length != profileNames.length)
            throw new DecoderException("The received player profiles were incomplete. " + profileIdMost.length + " ids, but " + profileNames.length + " names");

        UUID[] profileIds = IntStream.range(0, profileIdMost.length).mapToObj(i -> new UUID(profileIdMost[i], profileIdLeast[i])).toArray(UUID[]::new);
        this.profiles = new GameProfile[profileIds.length];
        for (int i = 0; i < this.profiles.length; i++)
        {
            String profileName = profileNames[i];
            if (profileName.length() > 16)
                throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + profileName.length() + " > 16)");
            this.profiles[i] = new GameProfile(profileIds[i], profileName);
        }
        this.rocket = rocket;
    }

    public PlayerRocket(Player[] players, @Nullable ResourceLocation orbitingBody, StructureTemplate rocket)
    {
        super(players[0].getDisplayName(), orbitingBody);
        this.profiles = Arrays.stream(players).map(Player::getGameProfile).toArray(GameProfile[]::new);
        this.rocket = rocket;
    }

    public boolean contains(UUID id)
    {
        return Arrays.stream(this.profiles).anyMatch(profile -> profile.getId().equals(id));
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
     * @return The profile for the commanding player
     */
    public GameProfile getCommandingProfile()
    {
        return this.profiles[0];
    }

    /**
     * @return The profile for all players in the rocket
     */
    public GameProfile[] getProfiles()
    {
        return profiles;
    }

    /**
     * @return The structure that represents the player rocket
     */
    public StructureTemplate getRocket()
    {
        return rocket;
    }
}
