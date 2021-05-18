package io.github.ocelot.beyond.common.network.play.message;

import io.github.ocelot.beyond.common.network.play.handler.ISpaceServerPlayHandler;
import io.github.ocelot.sonar.common.network.message.SonarMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author Ocelot
 */
public class CTemporaryOpenSpaceTravelMessage implements SonarMessage<ISpaceServerPlayHandler>
{
    public CTemporaryOpenSpaceTravelMessage()
    {
    }

    @Override
    public void readPacketData(FriendlyByteBuf buf)
    {
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf)
    {
    }

    @Override
    public void processPacket(ISpaceServerPlayHandler handler, NetworkEvent.Context ctx)
    {
        handler.handleTemporaryOpenSpaceTravel(this, ctx);
    }
}
