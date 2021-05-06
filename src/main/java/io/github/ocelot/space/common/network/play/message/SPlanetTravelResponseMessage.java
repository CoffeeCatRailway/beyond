package io.github.ocelot.space.common.network.play.message;

import io.github.ocelot.sonar.common.network.message.SonarMessage;
import io.github.ocelot.space.common.network.play.handler.ISpaceClientPlayHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
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
    public void readPacketData(PacketBuffer buf)
    {
        this.status = buf.readEnum(Status.class);
        this.status.read(this, buf);
    }

    @Override
    public void writePacketData(PacketBuffer buf)
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

        private final BiConsumer<SPlanetTravelResponseMessage, PacketBuffer> writer;
        private final BiConsumer<SPlanetTravelResponseMessage, PacketBuffer> reader;

        Status(BiConsumer<SPlanetTravelResponseMessage, PacketBuffer> writer, BiConsumer<SPlanetTravelResponseMessage, PacketBuffer> reader)
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
        public void write(SPlanetTravelResponseMessage msg, PacketBuffer buf)
        {
            this.writer.accept(msg, buf);
        }

        /**
         * Reads this type from the specified buffer.
         *
         * @param msg The message
         * @param buf The buffer to read from
         */
        public void read(SPlanetTravelResponseMessage msg, PacketBuffer buf)
        {
            this.reader.accept(msg, buf);
        }
    }
}
