package io.github.ocelot.space.common.network.play.handler;

import io.github.ocelot.space.client.screen.SpaceTravelScreen;
import io.github.ocelot.space.common.network.play.message.SPlanetTravelResponseMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;

/**
 * @author Ocelot
 */
public class SpaceClientPlayHandler implements ISpaceClientPlayHandler
{
    @Override
    public void handlePlanetTravelResponseMessage(SPlanetTravelResponseMessage msg, NetworkEvent.Context ctx)
    {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null)
            return;

        if (msg.getStatus() == SPlanetTravelResponseMessage.Status.FAILURE)
        {
            ctx.enqueueWork(() ->
            {
                if (Minecraft.getInstance().screen instanceof SpaceTravelScreen)
                    ((SpaceTravelScreen) Minecraft.getInstance().screen).notifyFailure(Objects.requireNonNull(msg.getBody()));
            });
        }
    }
}
