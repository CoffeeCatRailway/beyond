package io.github.ocelot.beyond.common.network.play.message;

import io.github.ocelot.sonar.common.network.message.SonarMessage;
import io.github.ocelot.beyond.common.network.play.handler.ISpaceClientPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

/**
 * @author Ocelot
 */
public class SPlanetTravelResponseMessage implements SonarMessage<ISpaceClientPlayHandler>
{
    private Status status;
    private ResourceLocation body;

    public SPlanetTravelResponseMessage()
    {
    }

    public SPlanetTravelResponseMessage(Status status)
    {
        this.status = status;
    }

    public SPlanetTravelResponseMessage(ResourceLocation body)
    {
        this.status = Status.FAILURE;
        this.body = body;
    }

    @Override
    public void readPacketData(FriendlyByteBuf buf)
    {
        this.status = buf.readEnum(Status.class);
        this.status.read(this, buf);
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf)
    {
        buf.writeEnum(this.status);
        this.status.write(this, buf);
    }

    @Override
    public void processPacket(ISpaceClientPlayHandler handler, NetworkEvent.Context ctx)
    {
        handler.handlePlanetTravelResponseMessage(this, ctx);
    }

    /**
     * @return The status response from the server
     */
    public Status getStatus()
    {
        return status;
    }

    /**
     * @return The body the client is supposed to be on or <code>null</code> if {@link #getStatus()} is not {@link Status#FAILURE}
     */
    @Nullable
    public ResourceLocation getBody()
    {
        return body;
    }

    /**
     * <p>Types of responses from the server.</p>
     *
     * @author Ocelot
     */
    public enum Status
    {
        SUCCESS((msg, buf) ->
        {
        }, (msg, buf) ->
        {
        }),
        FAILURE((msg, buf) ->
        {
            buf.writeResourceLocation(msg.body);
        }, (msg, buf) ->
        {
            msg.body = buf.readResourceLocation();
        });

        private final BiConsumer<SPlanetTravelResponseMessage, FriendlyByteBuf> writer;
        private final BiConsumer<SPlanetTravelResponseMessage, FriendlyByteBuf> reader;

        Status(BiConsumer<SPlanetTravelResponseMessage, FriendlyByteBuf> writer, BiConsumer<SPlanetTravelResponseMessage, FriendlyByteBuf> reader)
        {
            this.writer = writer;
            this.reader = reader;
        }

        /**
         * Writes this type to the specified buffer.
         *
         * @param msg The message to write
         * @param buf The buffer to write into
         */
        public void write(SPlanetTravelResponseMessage msg, FriendlyByteBuf buf)
        {
            this.writer.accept(msg, buf);
        }

        /**
         * Reads this type from the specified buffer.
         *
         * @param msg The message
         * @param buf The buffer to read from
         */
        public void read(SPlanetTravelResponseMessage msg, FriendlyByteBuf buf)
        {
            this.reader.accept(msg, buf);
        }
    }
}
