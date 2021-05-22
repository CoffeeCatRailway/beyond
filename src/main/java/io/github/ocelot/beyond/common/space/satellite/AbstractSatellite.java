package io.github.ocelot.beyond.common.space.satellite;

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
    private int id;
    private Component displayName;
    private ResourceLocation orbitingBody;

    protected AbstractSatellite(Component displayName, @Nullable ResourceLocation orbitingBody)
    {
        this.id = SATELLITE_COUNTER.incrementAndGet();
        this.displayName = displayName;
        this.orbitingBody = orbitingBody;
    }

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

    @Override
    public void setId(int id)
    {
        this.id = id;
    }

    /**
     * Sets the displaying name of this satellite.
     *
     * @param displayName The new name to show
     */
    public void setDisplayName(Component displayName)
    {
        this.displayName = displayName;
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
