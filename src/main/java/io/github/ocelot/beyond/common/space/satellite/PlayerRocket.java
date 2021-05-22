package io.github.ocelot.beyond.common.space.satellite;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.ocelot.beyond.common.space.simulation.CelestialBodySimulation;
import io.github.ocelot.beyond.common.space.simulation.PlayerRocketBody;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
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
            Codec.STRING.fieldOf("profileName").forGetter(rocket -> rocket.getProfile().getName())
    ).apply(instance, (displayName, orbitingBody, profileIdMost, profileIdLeast, profileName) -> new PlayerRocket(displayName, orbitingBody.orElse(null), new UUID(profileIdMost, profileIdLeast), profileName)));

    private final GameProfile profile;

    private PlayerRocket(Component displayName, @Nullable ResourceLocation orbitingBody, UUID profileId, String profileName)
    {
        super(displayName, orbitingBody);
        if (profileName.length() > 16)
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + profileName.length() + " > 16)");
        this.profile = new GameProfile(profileId, profileName);
    }

    public PlayerRocket(Player player, @Nullable ResourceLocation orbitingBody)
    {
        super(player.getDisplayName(), orbitingBody);
        this.profile = player.getGameProfile();
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
}
