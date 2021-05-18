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
import net.minecraft.block.PortalInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
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
        ServerPlayerEntity player = ctx.getSender();
        if (player == null)
            return;

        // TODO check if player can go to body
        ctx.enqueueWork(() ->
        {
            SpaceManager spaceManager = SpaceManager.get(player.server);
            if (spaceManager == null)
            {
                LOGGER.error("No Overworld for the space manager exists.");
                player.connection.disconnect(new TranslationTextComponent("multiplayer." + Beyond.MOD_ID + ".disconnect.invalid_space_travel"));
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
                    ServerWorld level = player.server.getLevel(RegistryKey.create(Registry.DIMENSION_REGISTRY, destinationBody.getDimension().get()));
                    if (level != null)
                    {
                        player.changeDimension(level, new ITeleporter()
                        {
                            @Override
                            public boolean playTeleportSound(ServerPlayerEntity player, ServerWorld sourceWorld, ServerWorld destWorld)
                            {
                                return false;
                            }

                            @Override
                            public PortalInfo getPortalInfo(Entity entity, ServerWorld destWorld, Function<ServerWorld, PortalInfo> defaultPortalInfo)
                            {
                                // TODO drop at probe location
                                return new PortalInfo(new Vector3d(entity.position().x(), destWorld.getMaxBuildHeight(), entity.position().z()), Vector3d.ZERO, entity.yRot, entity.xRot);
                            }

                            @Override
                            public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity)
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
        ServerPlayerEntity player = ctx.getSender();
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
