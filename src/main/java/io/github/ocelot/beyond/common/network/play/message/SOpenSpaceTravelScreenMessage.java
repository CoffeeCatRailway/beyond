package io.github.ocelot.beyond.common.network.play.message;

import io.github.ocelot.beyond.common.network.play.handler.ISpaceClientPlayHandler;
import io.github.ocelot.beyond.common.space.PlayerRocket;
import io.github.ocelot.sonar.common.network.message.SonarMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author Ocelot
 */
public class SOpenSpaceTravelScreenMessage implements SonarMessage<ISpaceClientPlayHandler>
{
    private PlayerRocket[] players;

    public SOpenSpaceTravelScreenMessage()
    {
    }

    public SOpenSpaceTravelScreenMessage(PlayerRocket... players)
    {
        this.players = players;
    }

    @Override
    public void readPacketData(PacketBuffer buf)
    {
        this.players = new PlayerRocket[buf.readVarInt()];
        for (int i = 0; i < this.players.length; i++)
            this.players[i] = new PlayerRocket(buf);
    }

    @Override
    public void writePacketData(PacketBuffer buf)
    {
        buf.writeVarInt(this.players.length);
        for (PlayerRocket rocket : this.players)
            rocket.write(buf);
    }

    @Override
    public void processPacket(ISpaceClientPlayHandler handler, NetworkEvent.Context ctx)
    {
        handler.handleOpenSpaceTravelScreenMessage(this, ctx);
    }

    /**
     * @return The players currently in the simulation. One is assumed to be the local player or the player doesn't open the screen.
     */
    @OnlyIn(Dist.CLIENT)
    public PlayerRocket[] getPlayers()
    {
        return players;
    }
}
