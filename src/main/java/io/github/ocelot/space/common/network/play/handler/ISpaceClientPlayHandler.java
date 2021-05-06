package io.github.ocelot.space.common.network.play.handler;

import io.github.ocelot.space.common.network.play.message.SPlanetTravelResponseMessage;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author Ocelot
 */
public interface ISpaceClientPlayHandler
{
    /**
     * Called when the server notifies the client they were transported to another planet.
     *
     * @param msg The message received
     * @param ctx The message context
     */
    void handlePlanetTravelResponseMessage(SPlanetTravelResponseMessage msg, NetworkEvent.Context ctx);
}
