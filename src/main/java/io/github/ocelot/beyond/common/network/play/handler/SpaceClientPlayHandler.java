package io.github.ocelot.beyond.common.network.play.handler;

import io.github.ocelot.beyond.client.screen.SpaceTravelScreen;
import io.github.ocelot.beyond.common.network.common.message.SSyncDimensionSettingsMessage;
import io.github.ocelot.beyond.common.network.play.message.SOpenSpaceTravelScreenMessage;
import io.github.ocelot.beyond.common.network.play.message.SPlanetTravelResponseMessage;
import io.github.ocelot.beyond.common.network.play.message.SPlayerTravelMessage;
import io.github.ocelot.beyond.common.network.play.message.SUpdateSimulationBodiesMessage;
import io.github.ocelot.beyond.common.world.space.ClientDimensionSpaceSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Ocelot
 */
public class SpaceClientPlayHandler implements ISpaceClientPlayHandler
{
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void handleOpenSpaceTravelScreenMessage(SOpenSpaceTravelScreenMessage msg, NetworkEvent.Context ctx)
    {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null || player.level == null)
            return;

        // Don't open the screen if the player isn't in it
        if (Arrays.stream(msg.getPlayers()).noneMatch(rocket -> rocket.getProfile().getId().equals(player.getUUID())))
        {
            LOGGER.warn("Player was not found in the simulation they attempted to join!");
            return;
        }

        ctx.enqueueWork(() -> Minecraft.getInstance().setScreen(new SpaceTravelScreen(msg)));
    }

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

    @Override
    public void handleUpdatePlayerTravelMessage(SPlayerTravelMessage msg, NetworkEvent.Context ctx)
    {
        this.notifySpaceTravelScreen(ctx, screen -> screen.receivePlayerTravel(msg));
    }

    @Override
    public void handleUpdateSimulationMessage(SUpdateSimulationBodiesMessage msg, NetworkEvent.Context ctx)
    {
        this.notifySpaceTravelScreen(ctx, screen -> screen.receiveSimulationUpdate(msg));
    }

    @Override
    public void handleSyncDimensionSettingsMessage(SSyncDimensionSettingsMessage msg, NetworkEvent.Context ctx)
    {
        ctx.enqueueWork(() -> ClientDimensionSpaceSettings.INSTANCE.receiveSyncDimensionSettings(msg));
    }

    private void notifySpaceTravelScreen(NetworkEvent.Context ctx, Consumer<SpaceTravelScreen> consumer)
    {
        ctx.enqueueWork(() ->
        {
            if (!(Minecraft.getInstance().screen instanceof SpaceTravelScreen))
            {
                LOGGER.warn("Server sent updates for simulation when it wasn't opened.");
                return;
            }

            consumer.accept((SpaceTravelScreen) Minecraft.getInstance().screen);
        });
    }
}
