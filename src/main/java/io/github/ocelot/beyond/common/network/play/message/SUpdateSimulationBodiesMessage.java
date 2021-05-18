package io.github.ocelot.beyond.common.network.play.message;

import io.github.ocelot.beyond.common.network.play.handler.ISpaceClientPlayHandler;
import io.github.ocelot.beyond.common.space.PlayerRocket;
import io.github.ocelot.sonar.common.network.message.SonarMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author Ocelot
 */
public class SUpdateSimulationBodiesMessage implements SonarMessage<ISpaceClientPlayHandler>
{
    private PlayerRocket[] addedPlayers;
    private ResourceLocation[] removed;

    public SUpdateSimulationBodiesMessage()
    {
    }

    public SUpdateSimulationBodiesMessage(PlayerRocket[] addedPlayers, ResourceLocation[] removed)
    {
        this.addedPlayers = addedPlayers;
        this.removed = removed;
    }

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        this.addedPlayers = new PlayerRocket[buf.readVarInt()];
        for (int i = 0; i < this.addedPlayers.length; i++)
            this.addedPlayers[i] = new PlayerRocket(buf);
        this.removed = new ResourceLocation[buf.readVarInt()];
        for (int i = 0; i < this.removed.length; i++)
            this.removed[i] = buf.readResourceLocation();
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        buf.writeVarInt(this.addedPlayers.length);
        for (PlayerRocket rocket : this.addedPlayers)
            rocket.write(buf);
        buf.writeVarInt(this.removed.length);
        for (ResourceLocation remove : this.removed)
            buf.writeResourceLocation(remove);
    }

    @Override
    public void processPacket(ISpaceClientPlayHandler handler, NetworkEvent.Context ctx)
    {
        handler.handleUpdateSimulationMessage(this, ctx);
    }

    /**
     * @return The players currently in the simulation. One is assumed to never the local player and that removal will be ignored.
     */
    @OnlyIn(Dist.CLIENT)
    public PlayerRocket[] getAddedPlayers()
    {
        return addedPlayers;
    }

    /**
     * @return The list of bodies to remove from the simulation
     */
    @OnlyIn(Dist.CLIENT)
    public ResourceLocation[] getRemoved()
    {
        return removed;
    }
}
