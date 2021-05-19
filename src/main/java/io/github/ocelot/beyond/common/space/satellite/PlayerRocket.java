package io.github.ocelot.beyond.common.space.satellite;

import com.mojang.authlib.GameProfile;
import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.space.simulation.CelestialBodySimulation;
import io.github.ocelot.beyond.common.space.simulation.PlayerRocketBody;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * <p>A representation of a player inside a rocket.</p>
 *
 * @author Ocelot
 */
public class PlayerRocket extends AbstractSatellite
{
    private final GameProfile profile;

    public PlayerRocket(Player player, ResourceLocation orbitingBody)
    {
        super(player.getDisplayName(), orbitingBody);
        this.profile = player.getGameProfile();
    }

    public PlayerRocket(FriendlyByteBuf buf)
    {
        super(buf);
        this.profile = new GameProfile(buf.readUUID(), buf.readUtf(16));
    }

    @Override
    protected void writeAdditional(FriendlyByteBuf buf)
    {
        buf.writeUUID(this.profile.getId());
        buf.writeUtf(this.profile.getName());
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

    /**
     * @return The profile for the player
     */
    public GameProfile getProfile()
    {
        return profile;
    }
}
