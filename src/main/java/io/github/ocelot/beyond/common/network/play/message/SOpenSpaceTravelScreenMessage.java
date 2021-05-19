package io.github.ocelot.beyond.common.network.play.message;

import io.github.ocelot.beyond.common.network.play.handler.ISpaceClientPlayHandler;
import io.github.ocelot.beyond.common.space.satellite.Satellite;
import io.github.ocelot.sonar.common.network.message.SonarMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author Ocelot
 */
public class SOpenSpaceTravelScreenMessage implements SonarMessage<ISpaceClientPlayHandler>
{
    private Satellite[] satellites;

    public SOpenSpaceTravelScreenMessage()
    {
    }

    public SOpenSpaceTravelScreenMessage(Satellite... satellites)
    {
        this.satellites = satellites;
    }

    @Override
    public void readPacketData(FriendlyByteBuf buf)
    {
        this.satellites = new Satellite[buf.readVarInt()];
        for (int i = 0; i < this.satellites.length; i++)
            this.satellites[i] = Satellite.read(buf);
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf)
    {
        buf.writeVarInt(this.satellites.length);
        for (Satellite satellite : this.satellites)
            Satellite.write(satellite, buf);
    }

    @Override
    public void processPacket(ISpaceClientPlayHandler handler, NetworkEvent.Context ctx)
    {
        handler.handleOpenSpaceTravelScreenMessage(this, ctx);
    }

    /**
     * @return The satellites currently in the simulation. One is assumed to be the local player or the player doesn't open the screen.
     */
    @OnlyIn(Dist.CLIENT)
    public Satellite[] getSatellites()
    {
        return satellites;
    }
}
