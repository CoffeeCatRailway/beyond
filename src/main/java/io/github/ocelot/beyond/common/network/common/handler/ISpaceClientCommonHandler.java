package io.github.ocelot.beyond.common.network.common.handler;

import io.github.ocelot.beyond.common.network.common.message.SSyncDimensionSettingsMessage;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author Ocelot
 */
public interface ISpaceClientCommonHandler
{
    /**
     * Called when the server notifies the client of the space settings for all dimensions.
     *
     * @param msg The message received
     * @param ctx The message context
     */
    void handleSyncDimensionSettingsMessage(SSyncDimensionSettingsMessage msg, NetworkEvent.Context ctx);
}
