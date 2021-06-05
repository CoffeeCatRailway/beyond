package io.github.ocelot.beyond.common.network.play.handler;

import io.github.ocelot.beyond.common.init.BeyondMessages;
import io.github.ocelot.beyond.common.network.play.message.CPlanetTravelMessage;
import io.github.ocelot.beyond.common.network.play.message.SPlanetTravelResponseMessage;
import io.github.ocelot.beyond.common.space.SpaceManager;
import io.github.ocelot.beyond.common.space.planet.Planet;
import io.github.ocelot.beyond.common.space.satellite.PlayerRocket;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * @author Ocelot
 */
public class SpaceServerPlayHandler implements ISpaceServerPlayHandler
{
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void handlePlanetTravelMessage(CPlanetTravelMessage msg, NetworkEvent.Context ctx)
    {
        ServerPlayer player = ctx.getSender();
        if (player == null)
            return;

        // TODO check if player can go to body
        ctx.enqueueWork(() ->
        {
            SpaceManager spaceManager = SpaceManager.get();

            if (!spaceManager.hasTransaction(player.getUUID()))
            {
                ResourceLocation body = spaceManager.getPlayer(player.getUUID()).map(PlayerRocket::getOrbitingBody).map(planet -> planet.orElse(Planet.EARTH)).orElse(Planet.EARTH);
                BeyondMessages.PLAY.reply(new SPlanetTravelResponseMessage(body), ctx);
                LOGGER.warn(player + " was not commander and attempted to select a destination!");
                return;
            }

            if (msg.getBodyId() == null)
            {
                spaceManager.cancelTransaction(player);
                return;
            }

            Optional<ResourceLocation> destinationDimension = spaceManager.getDimension(msg.getBodyId());
            if (destinationDimension.isPresent())
            {
                if (msg.isArrive())
                {
                    ServerLevel level = player.server.getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, destinationDimension.get()));
                    if (level != null)
                    {
                        spaceManager.arrive(player, level);
                        return;
                    }
                }
                else
                {
                    spaceManager.depart(player, msg.getBodyId());
                    return;
                }
            }

            ResourceLocation body = spaceManager.getPlayer(player.getUUID()).map(PlayerRocket::getOrbitingBody).map(planet -> planet.orElse(Planet.EARTH)).orElse(Planet.EARTH);
            BeyondMessages.PLAY.reply(new SPlanetTravelResponseMessage(body), ctx);
        });
    }
}
