package io.github.ocelot.beyond.common.world.space;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.ocelot.beyond.common.init.BeyondMessages;
import io.github.ocelot.beyond.common.network.common.message.SSyncDimensionSettingsMessage;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Server side implementation of {@link DimensionSpaceSettingsManager}. Loads and manages direct copies of settings from datapacks.</p>
 *
 * @author Ocelot
 */
public class DimensionSpaceSettingsLoader extends JsonReloadListener implements DimensionSpaceSettingsManager
{
    public static final DimensionSpaceSettingsLoader INSTANCE = new DimensionSpaceSettingsLoader();
    private static final Logger LOGGER = LogManager.getLogger();

    private final Object2ObjectArrayMap<ResourceLocation, DimensionSpaceSettings> settings;

    public DimensionSpaceSettingsLoader()
    {
        super(new Gson(), "dimension_space_settings");
        this.settings = new Object2ObjectArrayMap<>();
        this.settings.defaultReturnValue(DimensionSpaceSettings.DEFAULT);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> data, IResourceManager resourceManager, IProfiler profiler)
    {
        Map<ResourceLocation, DimensionSpaceSettings> settings = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : data.entrySet())
        {
            try
            {
                settings.put(entry.getKey(), DimensionSpaceSettings.CODEC.parse(JsonOps.INSTANCE, entry.getValue()).getOrThrow(false, LOGGER::error));
            }
            catch (Exception e)
            {
                LOGGER.error("Failed to load space settings for dimension: " + entry.getKey(), e);
            }
        }
        this.settings.clear();
        this.settings.putAll(settings);
        LOGGER.info("Loaded " + this.settings.size() + " space dimension settings");

        if (ServerLifecycleHooks.getCurrentServer() != null)
        {
            BeyondMessages.PLAY.send(PacketDistributor.ALL.noArg(), this.createSyncPacket());
        }
    }

    @Override
    public DimensionSpaceSettings getSettings(ResourceLocation dimensionLocation)
    {
        return this.settings.get(dimensionLocation);
    }

    /**
     * @return Creates a new packet for syncing the settings with a client
     */
    public SSyncDimensionSettingsMessage createSyncPacket()
    {
        return new SSyncDimensionSettingsMessage(this.settings);
    }
}
