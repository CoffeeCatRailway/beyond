package io.github.ocelot.beyond.common.network.play.message;

import io.github.ocelot.beyond.common.network.play.handler.ISpaceServerPlayHandler;
import io.github.ocelot.sonar.common.network.message.SonarMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;

/**
 * @author Ocelot
 */
public class CPlanetTravelMessage implements SonarMessage<ISpaceServerPlayHandler>
{
    private ResourceLocation bodyId;
    private boolean arrive;

    public CPlanetTravelMessage()
    {
    }

    public CPlanetTravelMessage(@Nullable ResourceLocation bodyId, boolean arrive)
    {
        this.bodyId = bodyId;
        this.arrive = arrive;
    }

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        if (buf.readBoolean())
        {
            this.bodyId = buf.readResourceLocation();
            this.arrive = buf.readBoolean();
        }
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        buf.writeBoolean(this.bodyId != null);
        if (this.bodyId != null)
        {
            buf.writeResourceLocation(this.bodyId);
            buf.writeBoolean(this.arrive);
        }
    }

    @Override
    public void processPacket(ISpaceServerPlayHandler handler, NetworkEvent.Context ctx)
    {
        handler.handlePlanetTravelMessage(this, ctx);
    }

    /**
     * @return The id of the body the client wants to travel to or <code>null</code> to cancel travelling anywhere
     */
    @Nullable
    public ResourceLocation getBodyId()
    {
        return bodyId;
    }

    /**
     * @return Whether or not the player is arriving at the body
     */
    public boolean isArrive()
    {
        return arrive;
    }
}
