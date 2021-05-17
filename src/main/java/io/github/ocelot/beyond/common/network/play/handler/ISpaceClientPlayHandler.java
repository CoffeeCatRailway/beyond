package io.github.ocelot.beyond.common.network.play.handler;

import io.github.ocelot.beyond.common.network.common.handler.ISpaceClientCommonHandler;
import io.github.ocelot.beyond.common.network.play.message.SOpenSpaceTravelScreenMessage;
import io.github.ocelot.beyond.common.network.play.message.SPlanetTravelResponseMessage;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author Ocelot
 */
public interface ISpaceClientPlayHandler extends ISpaceClientCommonHandler
{
    /**
     * Called when the server tells the client to open the space travel screen.
     *
     * @param msg The message received
     * @param ctx The message context
     */
    void handleOpenSpaceTravelScreenMessage(SOpenSpaceTravelScreenMessage msg, NetworkEvent.Context ctx);

    /**
     * Called when the server notifies the client they were transported to another planet.
     *
     * @param msg The message received
     * @param ctx The message context
     */
    void handlePlanetTravelResponseMessage(SPlanetTravelResponseMessage msg, NetworkEvent.Context ctx);
}
