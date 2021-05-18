package io.github.ocelot.beyond.common.space.satellite;

import io.github.ocelot.beyond.common.space.simulation.CelestialBodySimulation;
import io.github.ocelot.beyond.common.space.simulation.SimulatedBody;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Function;

/**
 * <p>A satellite provided by the server when the user opens the travel screen.</p>
 *
 * @author Ocelot
 */
public interface Satellite
{
    /**
     * Writes this rocket into the specified buffer.
     *
     * @param buf The buffer to write data into
     */
    void write(FriendlyByteBuf buf);

    /**
     * Creates a body that can be added to the simulation.
     *
     * @param simulation The simulation instance
     * @return A new body for that simulation
     */
    SimulatedBody createBody(CelestialBodySimulation simulation);

    /**
     * @return The type of satellite this is
     */
    Type getType();

    /**
     * Writes the specified satellite into the buffer.
     *
     * @param satellite The satellite to write
     * @param buf       The buffer to write data into
     */
    static void write(Satellite satellite, FriendlyByteBuf buf)
    {
        buf.writeEnum(satellite.getType());
        satellite.write(buf);
    }

    /**
     * Reads a new satellite from the buffer.
     *
     * @param buf The buffer of data
     * @return A new satellite from the buffer
     */
    static Satellite read(FriendlyByteBuf buf)
    {
        Type type = buf.readEnum(Type.class);
        return type.factory.apply(buf);
    }

    /**
     * <p>Types of satellites in the simulation.</p>
     *
     * @author Ocelot
     */
    enum Type
    {
        PLAYER(PlayerRocket::new),
        ARTIFICIAL(ArtificialSatellite::new);

        private final Function<FriendlyByteBuf, Satellite> factory;

        Type(Function<FriendlyByteBuf, Satellite> factory)
        {
            this.factory = factory;
        }
    }
}
