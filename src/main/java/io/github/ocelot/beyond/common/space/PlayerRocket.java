package io.github.ocelot.beyond.common.space;

import com.mojang.authlib.GameProfile;
import io.github.ocelot.beyond.Beyond;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * <p>A representation of a player inside a rocket.</p>
 *
 * @author Ocelot
 */
public class PlayerRocket
{
    private final ResourceLocation id;
    private final GameProfile profile;
    private final ITextComponent displayName;
    private ResourceLocation orbitingBody;

    private PlayerRocket(GameProfile profile, ITextComponent displayName, ResourceLocation orbitingBody)
    {
        this.id = new ResourceLocation(Beyond.MOD_ID, DigestUtils.md5Hex(profile.getName()));
        this.profile = profile;
        this.displayName = displayName;
        this.orbitingBody = orbitingBody;
    }

    public PlayerRocket(PlayerEntity player, ResourceLocation orbitingBody)
    {
        this(player.getGameProfile(), player.getDisplayName(), orbitingBody);
    }

    public PlayerRocket(PacketBuffer buf)
    {
        this(new GameProfile(buf.readUUID(), buf.readUtf(16)), buf.readComponent(), buf.readResourceLocation());
    }

    /**
     * Writes this rocket into the specified buffer.
     *
     * @param buf The buffer to write data into
     */
    public void write(PacketBuffer buf)
    {
        buf.writeUUID(this.profile.getId());
        buf.writeUtf(this.profile.getName());
        buf.writeComponent(this.displayName);
        buf.writeResourceLocation(this.orbitingBody);
    }

    /**
     * @return The profile for the player
     */
    public GameProfile getProfile()
    {
        return profile;
    }

    /**
     * @return The id of the player rocket in the simulation
     */
    public ResourceLocation getId()
    {
        return id;
    }

    /**
     * @return The display name of the player
     */
    public ITextComponent getDisplayName()
    {
        return displayName;
    }

    public ResourceLocation getOrbitingBody()
    {
        return orbitingBody;
    }

    public void setOrbitingBody(ResourceLocation orbitingBody)
    {
        this.orbitingBody = orbitingBody;
    }
}
