package io.github.ocelot.space.common.network.play.message;

import io.github.ocelot.sonar.common.network.message.SonarMessage;
import io.github.ocelot.space.common.network.play.handler.ISpaceServerPlayHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author Ocelot
 */
public class CPlanetTravelMessage implements SonarMessage<ISpaceServerPlayHandler>
{
    private ResourceLocation bodyId;

    public CPlanetTravelMessage()
    {
    }

    public CPlanetTravelMessage(ResourceLocation bodyId)
    {
        this.bodyId = bodyId;
    }

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        this.bodyId = buf.readResourceLocation();
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        buf.writeResourceLocation(this.bodyId);
    }

    @Override
    public void processPacket(ISpaceServerPlayHandler handler, NetworkEvent.Context ctx)
    {
        handler.handlePlanetTravelMessage(this, ctx);
    }

    /**
     * @return The id of the body the client wants to travel to
     */
    public ResourceLocation getBodyId()
    {
        return bodyId;
    }
}
