package io.github.ocelot.beyond.common.network.play.message;

import io.github.ocelot.beyond.common.network.play.handler.ISpaceClientPlayHandler;
import io.github.ocelot.sonar.common.network.message.SonarMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;

/**
 * @author Ocelot
 */
public class SPlayerTravelMessage implements SonarMessage<ISpaceClientPlayHandler>
{
    private UUID player;
    private ResourceLocation body;

    public SPlayerTravelMessage()
    {
    }

    public SPlayerTravelMessage(UUID player, ResourceLocation body)
    {
        this.player = player;
        this.body = body;
    }

    @Override
    public void readPacketData(FriendlyByteBuf buf)
    {
        this.player = buf.readUUID();
        this.body = buf.readResourceLocation();
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf)
    {
        buf.writeUUID(this.player);
        buf.writeResourceLocation(this.body);
    }

    @Override
    public void processPacket(ISpaceClientPlayHandler handler, NetworkEvent.Context ctx)
    {
        handler.handleUpdatePlayerTravelMessage(this, ctx);
    }

    /**
     * @return The player travelling
     */
    @OnlyIn(Dist.CLIENT)
    public UUID getPlayer()
    {
        return player;
    }

    /**
     * @return The body the player is travelling to
     */
    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getBody()
    {
        return body;
    }
}
