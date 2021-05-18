package io.github.ocelot.beyond.common.space.satellite;

import com.mojang.authlib.GameProfile;
import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.space.simulation.CelestialBodySimulation;
import io.github.ocelot.beyond.common.space.simulation.PlayerRocketBody;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * <p>A representation of a player inside a rocket.</p>
 *
 * @author Ocelot
 */
public class PlayerRocket implements Satellite
{
    private final ResourceLocation id;
    private final GameProfile profile;
    private final Component displayName;
    private ResourceLocation orbitingBody;

    private PlayerRocket(GameProfile profile, Component displayName, ResourceLocation orbitingBody)
    {
        this.id = new ResourceLocation(Beyond.MOD_ID, DigestUtils.md5Hex(profile.getName()));
        this.profile = profile;
        this.displayName = displayName;
        this.orbitingBody = orbitingBody;
    }

    public PlayerRocket(Player player, ResourceLocation orbitingBody)
    {
        this(player.getGameProfile(), player.getDisplayName(), orbitingBody);
    }

    public PlayerRocket(FriendlyByteBuf buf)
    {
        this(new GameProfile(buf.readUUID(), buf.readUtf(16)), buf.readComponent(), buf.readResourceLocation());
    }


    @Override
    public void write(FriendlyByteBuf buf)
    {
        buf.writeUUID(this.profile.getId());
        buf.writeUtf(this.profile.getName());
        buf.writeComponent(this.displayName);
        buf.writeResourceLocation(this.orbitingBody);
    }

    @Override
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
     * @return The id of the player rocket in the simulation
     */
    public ResourceLocation getId()
    {
        return id;
    }

    /**
     * @return The profile for the player
     */
    public GameProfile getProfile()
    {
        return profile;
    }

    /**
     * @return The display name of the player
     */
    public Component getDisplayName()
    {
        return displayName;
    }

    /**
     * @return The body to orbit
     */
    public Optional<ResourceLocation> getOrbitingBody()
    {
        return Optional.ofNullable(this.orbitingBody);
    }

    /**
     * Sets the body for this satellite to orbit
     *
     * @param orbitingBody The body to orbit
     */
    public void setOrbitingBody(@Nullable ResourceLocation orbitingBody)
    {
        this.orbitingBody = orbitingBody;
    }
}
