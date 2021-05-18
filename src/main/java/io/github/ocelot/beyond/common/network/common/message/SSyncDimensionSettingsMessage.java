package io.github.ocelot.beyond.common.network.common.message;

import io.github.ocelot.beyond.common.network.common.handler.ISpaceClientCommonHandler;
import io.github.ocelot.beyond.common.world.space.DimensionSpaceSettings;
import io.github.ocelot.sonar.common.network.message.SimpleSonarLoginMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ocelot
 */
public class SSyncDimensionSettingsMessage extends SimpleSonarLoginMessage<ISpaceClientCommonHandler>
{
    private final Map<ResourceLocation, DimensionSpaceSettings> settings;

    public SSyncDimensionSettingsMessage()
    {
        this.settings = new HashMap<>();
    }

    public SSyncDimensionSettingsMessage(Map<ResourceLocation, DimensionSpaceSettings> settings)
    {
        this.settings = new HashMap<>(settings);
    }

    @Override
    public void readPacketData(FriendlyByteBuf buf)
    {
        try
        {
            this.settings.clear();
            int count = buf.readVarInt();
            for (int i = 0; i < count; i++)
                this.settings.put(buf.readResourceLocation(), buf.readWithCodec(DimensionSpaceSettings.CODEC));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf)
    {
        buf.writeVarInt(this.settings.size());
        this.settings.forEach((dimensionId, settings) ->
        {
            try
            {
                buf.writeResourceLocation(dimensionId);
                buf.writeWithCodec(DimensionSpaceSettings.CODEC, settings);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void processPacket(ISpaceClientCommonHandler handler, NetworkEvent.Context ctx)
    {
        handler.handleSyncDimensionSettingsMessage(this, ctx);
    }

    /**
     * @return The settings the server sent to the client
     */
    @OnlyIn(Dist.CLIENT)
    public Map<ResourceLocation, DimensionSpaceSettings> getSettings()
    {
        return settings;
    }
}
