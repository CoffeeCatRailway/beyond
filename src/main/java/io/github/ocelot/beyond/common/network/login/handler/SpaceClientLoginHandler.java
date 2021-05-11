package io.github.ocelot.beyond.common.network.login.handler;

import io.github.ocelot.beyond.common.init.BeyondMessages;
import io.github.ocelot.beyond.common.network.common.message.SSyncDimensionSettingsMessage;
import io.github.ocelot.beyond.common.network.login.message.CAcknowledgeServerMessage;
import io.github.ocelot.beyond.common.world.space.ClientDimensionSpaceSettings;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author Ocelot
 */
public class SpaceClientLoginHandler implements ISpaceClientLoginHandler
{
    @Override
    public void handleSyncDimensionSettingsMessage(SSyncDimensionSettingsMessage msg, NetworkEvent.Context ctx)
    {
        ClientDimensionSpaceSettings.INSTANCE.receiveSyncDimensionSettings(msg);
        BeyondMessages.LOGIN.reply(new CAcknowledgeServerMessage(), ctx);
    }
}
