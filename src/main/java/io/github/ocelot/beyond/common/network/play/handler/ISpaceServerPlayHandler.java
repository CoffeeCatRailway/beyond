package io.github.ocelot.beyond.common.network.play.handler;

import io.github.ocelot.beyond.common.network.play.message.CPlanetTravelMessage;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author Ocelot
 */
public interface ISpaceServerPlayHandler
{
    /**
     * Called when the client requests to be sent to a planet.
     *
     * @param msg The message received
     * @param ctx The message context
     */
    void handlePlanetTravelMessage(CPlanetTravelMessage msg, NetworkEvent.Context ctx);
}
