package io.github.ocelot.space.common.network.play.handler;

import io.github.ocelot.space.common.network.play.message.CPlanetTravelMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author Ocelot
 */
public class SpaceServerPlayHandler implements ISpaceServerPlayHandler
{
    @Override
    public void handlePlanetTravelMessage(CPlanetTravelMessage msg, NetworkEvent.Context ctx)
    {
        ServerPlayerEntity player = ctx.getSender();
        if (player == null)
            return;

        // TODO check if player can go to body
        ctx.enqueueWork(() ->
        {
            ServerWorld level = player.server.getLevel(RegistryKey.create(Registry.DIMENSION_REGISTRY, msg.getBodyId()));
            if (level != null)
                player.changeDimension(level);
        });
    }
}
