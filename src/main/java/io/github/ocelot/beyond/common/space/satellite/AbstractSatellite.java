package io.github.ocelot.beyond.common.space.satellite;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * <p>A {@link Satellite} that fulfills the display name, id, and orbiting body.</p>
 *
 * @author Ocelot
 */
public abstract class AbstractSatellite implements Satellite
{
    private final int id;
    private final Component displayName;
    private ResourceLocation orbitingBody;

    private AbstractSatellite(int id, Component displayName, ResourceLocation orbitingBody)
    {
        this.id = id;
        this.displayName = displayName;
        this.orbitingBody = orbitingBody;
    }

    protected AbstractSatellite(Component displayName, ResourceLocation orbitingBody)
    {
        this(SATELLITE_COUNTER.incrementAndGet(), displayName, orbitingBody);
    }

    protected AbstractSatellite(FriendlyByteBuf buf)
    {
        this(buf.readVarInt(), buf.readComponent(), buf.readResourceLocation());
    }

    @Override
    public final void write(FriendlyByteBuf buf)
    {
        buf.writeVarInt(this.id);
        buf.writeComponent(this.displayName);
        buf.writeResourceLocation(this.orbitingBody);
        this.writeAdditional(buf);
    }

    /**
     * Writes additional data into the specified buffer.
     *
     * @param buf The buffer to write into
     */
    protected abstract void writeAdditional(FriendlyByteBuf buf);

    @Override
    public int getId()
    {
        return id;
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
