package io.github.ocelot.beyond.common.world.space;

import io.github.ocelot.beyond.common.network.common.message.SSyncDimensionSettingsMessage;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>Client implementation of {@link DimensionSpaceSettingsManager}. Reflects what the server has loaded.</p>
 *
 * @author Ocelot
 */
public class ClientDimensionSpaceSettings implements DimensionSpaceSettingsManager
{
    public static final ClientDimensionSpaceSettings INSTANCE = new ClientDimensionSpaceSettings();
    private static final Logger LOGGER = LogManager.getLogger();

    private final Object2ObjectArrayMap<ResourceLocation, DimensionSpaceSettings> settings;

    public ClientDimensionSpaceSettings()
    {
        this.settings = new Object2ObjectArrayMap<>();
        this.settings.defaultReturnValue(DimensionSpaceSettings.DEFAULT);
        MinecraftForge.EVENT_BUS.addListener(this::onLoggedOut);
    }

    private void onLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event)
    {
        this.settings.clear();
    }

    public void receiveSyncDimensionSettings(SSyncDimensionSettingsMessage msg)
    {
        LOGGER.debug("Received dimension settings from server. " + msg.getSettings());
        this.settings.clear();
        this.settings.putAll(msg.getSettings());
    }

    @Override
    public DimensionSpaceSettings getSettings(ResourceLocation dimensionLocation)
    {
        return this.settings.get(dimensionLocation);
    }
}
