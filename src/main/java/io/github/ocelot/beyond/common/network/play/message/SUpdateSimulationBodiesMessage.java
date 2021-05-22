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
public class SUpdateSimulationBodiesMessage implements SonarMessage<ISpaceClientPlayHandler>
{
    private Satellite[] added;
    private int[] removed;

    public SUpdateSimulationBodiesMessage()
    {
    }

    public SUpdateSimulationBodiesMessage(Satellite[] added, int[] removed)
    {
        this.added = added;
        this.removed = removed;
    }

    @Override
    public void readPacketData(FriendlyByteBuf buf)
    {
        this.added = new Satellite[buf.readVarInt()];
        for (int i = 0; i < this.added.length; i++)
            this.added[i] = Satellite.read(buf);
        this.removed = new int[buf.readVarInt()];
        for (int i = 0; i < this.removed.length; i++)
            this.removed[i] = buf.readVarInt();
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf)
    {
        buf.writeVarInt(this.added.length);
        for (Satellite satellite : this.added)
            Satellite.write(satellite, buf);
        buf.writeVarInt(this.removed.length);
        for (int remove : this.removed)
            buf.writeVarInt(remove);
    }

    @Override
    public void processPacket(ISpaceClientPlayHandler handler, NetworkEvent.Context ctx)
    {
        handler.handleUpdateSimulationMessage(this, ctx);
    }

    /**
     * @return The satellites added to the simulation
     */
    @OnlyIn(Dist.CLIENT)
    public Satellite[] getAddedSatellites()
    {
        return added;
    }

    /**
     * @return The list of bodies to remove from the simulation
     */
    @OnlyIn(Dist.CLIENT)
    public int[] getRemoved()
    {
        return removed;
    }
}
