package io.github.ocelot.space.common.network.play.handler;

import io.github.ocelot.space.SpacePrototype;
import io.github.ocelot.space.common.init.SpaceMessages;
import io.github.ocelot.space.common.network.play.message.CPlanetTravelMessage;
import io.github.ocelot.space.common.network.play.message.SPlanetTravelResponseMessage;
import net.minecraft.block.PortalInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
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
            if (msg.getBodyId() == null)
            {
                LOGGER.debug(player + " has exited GUI");
                return; // TODO expect player to be in level again
            }

            ServerWorld level = player.server.getLevel(RegistryKey.create(Registry.DIMENSION_REGISTRY, msg.getBodyId()));
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
                SpaceMessages.PLAY.reply(new SPlanetTravelResponseMessage(SPlanetTravelResponseMessage.Status.SUCCESS), ctx);
                return;
            }
            // TODO return player to correct body
            SpaceMessages.PLAY.reply(new SPlanetTravelResponseMessage(new ResourceLocation(SpacePrototype.MOD_ID, "earth")), ctx);
        });
    }
}
