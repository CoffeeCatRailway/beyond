package io.github.ocelot.beyond.common.network.play.handler;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.init.BeyondMessages;
import io.github.ocelot.beyond.common.network.play.message.CPlanetTravelMessage;
import io.github.ocelot.beyond.common.network.play.message.CTemporaryOpenSpaceTravelMessage;
import io.github.ocelot.beyond.common.network.play.message.SPlanetTravelResponseMessage;
import io.github.ocelot.beyond.common.network.play.message.SPlayerTravelMessage;
import io.github.ocelot.beyond.common.space.SpaceManager;
import io.github.ocelot.beyond.common.space.planet.Planet;
import io.github.ocelot.beyond.common.space.simulation.PlayerRocketBody;
import io.github.ocelot.beyond.common.space.simulation.SimulatedBody;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

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
            SpaceManager spaceManager = SpaceManager.get(player.server);
            if (spaceManager == null)
            {
                LOGGER.error("No Overworld for the space manager exists.");
                player.connection.disconnect(new TranslatableComponent("multiplayer." + Beyond.MOD_ID + ".disconnect.invalid_space_travel"));
                return;
            }

            if (msg.getBodyId() == null || msg.isArrive())
                spaceManager.removePlayer(player.getUUID());

            if (msg.getBodyId() == null)
            {
                LOGGER.debug(player + " has exited GUI");
                return; // TODO expect player to be in level again
            }

            SimulatedBody destinationBody = spaceManager.getSimulation().getBody(msg.getBodyId());
            if (destinationBody != null && destinationBody.canTeleportTo() && destinationBody.getDimension().isPresent())
            {
                if (msg.isArrive())
                {
                    ServerLevel level = player.server.getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, destinationBody.getDimension().get()));
                    if (level != null)
                    {
                        player.changeDimension(level, new ITeleporter()
                        {
                            @Override
                            public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld)
                            {
                                return false;
                            }

                            @Override
                            public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo)
                            {
                                // TODO drop at probe location
                                return new PortalInfo(new Vec3(entity.position().x(), destWorld.getMaxBuildHeight(), entity.position().z()), Vec3.ZERO, entity.yRot, entity.xRot);
                            }

                            @Override
                            public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity)
                            {
                                return repositionEntity.apply(false);
                            }
                        });
                        BeyondMessages.PLAY.reply(new SPlanetTravelResponseMessage(SPlanetTravelResponseMessage.Status.SUCCESS), ctx);
                        return;
                    }
                }
                else
                {
                    spaceManager.relay(player, new SPlayerTravelMessage(player.getUUID(), msg.getBodyId()));
                    return;
                }
            }
            PlayerRocketBody rocket = spaceManager.getSimulation().getPlayer(player.getUUID());
            ResourceLocation body = rocket != null ? rocket.getParent().orElse(Planet.EARTH) : Planet.EARTH;
            BeyondMessages.PLAY.reply(new SPlanetTravelResponseMessage(body), ctx);
        });
    }

    @Override
    public void handleTemporaryOpenSpaceTravel(CTemporaryOpenSpaceTravelMessage msg, NetworkEvent.Context ctx)
    {
        ServerPlayer player = ctx.getSender();
        if (player == null)
            return;

        SpaceManager spaceManager = SpaceManager.get(player.server);
        if (spaceManager == null)
        {
            LOGGER.error("No Overworld for the space manager exists.");
            return;
        }

        ctx.enqueueWork(() -> BeyondMessages.PLAY.reply(spaceManager.insertPlayer(player), ctx));
    }
}
