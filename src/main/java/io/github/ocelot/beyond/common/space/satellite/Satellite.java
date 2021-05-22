package io.github.ocelot.beyond.common.space.satellite;

import com.mojang.serialization.Codec;
import io.github.ocelot.beyond.common.space.simulation.CelestialBodySimulation;
import io.github.ocelot.beyond.common.space.simulation.SimulatedBody;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>A satellite provided by the server when the user opens the travel screen.</p>
 *
 * @author Ocelot
 */
public interface Satellite
{
    AtomicInteger SATELLITE_COUNTER = new AtomicInteger();

    /**
     * Creates a body that can be added to the simulation.
     *
     * @param simulation The simulation instance
     * @return A new body for that simulation
     */
    @OnlyIn(Dist.CLIENT)
    SimulatedBody createBody(CelestialBodySimulation simulation);

    /**
     * @return The integer id of the satellite
     */
    int getId();

    /**
     * @return The type of satellite this is
     */
    Type getType();

    /**
     * @return The serializer for this satellite
     */
    Codec<? extends Satellite> getCodec();

    /**
     * Sets the id of this satellite.
     *
     * @param id The new id to use
     */
    void setId(int id);

    /**
     * Writes the specified satellite into the buffer.
     *
     * @param satellite The satellite to write
     * @param buf       The buffer to write data into
     */
    @SuppressWarnings("unchecked")
    static void write(Satellite satellite, FriendlyByteBuf buf)
    {
        try
        {
            buf.writeEnum(satellite.getType());
            buf.writeVarInt(satellite.getId());
            buf.writeWithCodec((Codec<? super Satellite>) satellite.getCodec(), satellite);
        }
        catch (IOException e)
        {
            throw new DecoderException(e);
        }
    }

    /**
     * Reads a new satellite from the buffer.
     *
     * @param buf The buffer of data
     * @return A new satellite from the buffer
     */
    static Satellite read(FriendlyByteBuf buf)
    {
        try
        {
            Satellite.Type type = buf.readEnum(Satellite.Type.class);
            int id = buf.readVarInt();
            Satellite satellite = buf.readWithCodec(type.getCodec());
            satellite.setId(id);
            return satellite;
        }
        catch (IOException e)
        {
            throw new DecoderException(e);
        }
    }

    /**
     * <p>Types of satellites in the simulation.</p>
     *
     * @author Ocelot
     */
    enum Type
    {
        PLAYER(false, PlayerRocket.CODEC),
        ARTIFICIAL(true, ArtificialSatellite.CODEC);

        private final boolean save;
        private final Codec<? extends Satellite> codec;

        Type(boolean save, Codec<? extends Satellite> codec)
        {
            this.save = save;
            this.codec = codec;
        }

        /**
         * @return Whether or not this satellite should be read to/from disk
         */
        public boolean shouldSave()
        {
            return save;
        }

        /**
         * @return The codec for this satellite type
         */
        public Codec<? extends Satellite> getCodec()
        {
            return codec;
        }
    }
}
