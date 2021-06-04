package io.github.ocelot.beyond.common.init;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.network.common.message.SSyncDimensionSettingsMessage;
import io.github.ocelot.beyond.common.network.login.handler.SpaceClientLoginHandler;
import io.github.ocelot.beyond.common.network.login.handler.SpaceServerLoginHandler;
import io.github.ocelot.beyond.common.network.login.message.CAcknowledgeServerMessage;
import io.github.ocelot.beyond.common.network.play.handler.SpaceClientPlayHandler;
import io.github.ocelot.beyond.common.network.play.handler.SpaceServerPlayHandler;
import io.github.ocelot.beyond.common.network.play.message.*;
import io.github.ocelot.beyond.common.world.space.DimensionSpaceSettingsLoader;
import io.github.ocelot.sonar.common.network.SonarNetworkManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;

/**
 * @author Ocelot
 */
public class BeyondMessages
{
    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel LOGIN = NetworkRegistry.newSimpleChannel(new ResourceLocation(Beyond.MOD_ID, "login"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    public static final SimpleChannel PLAY = NetworkRegistry.newSimpleChannel(new ResourceLocation(Beyond.MOD_ID, "play"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static final SonarNetworkManager LOGIN_MANAGER = new SonarNetworkManager(LOGIN, () -> SpaceClientLoginHandler::new, () -> SpaceServerLoginHandler::new);
    public static final SonarNetworkManager PLAY_MANAGER = new SonarNetworkManager(PLAY, () -> SpaceClientPlayHandler::new, () -> SpaceServerPlayHandler::new);

    public static void init()
    {
        // Common
        PLAY_MANAGER.register(SSyncDimensionSettingsMessage.class, SSyncDimensionSettingsMessage::new, NetworkDirection.PLAY_TO_CLIENT);
        LOGIN_MANAGER.registerLogin(SSyncDimensionSettingsMessage.class, SSyncDimensionSettingsMessage::new, isLocal -> Collections.singletonList(Pair.of(SSyncDimensionSettingsMessage.class.getName(), DimensionSpaceSettingsLoader.INSTANCE.createSyncPacket())), NetworkDirection.LOGIN_TO_CLIENT);

        // Login
        LOGIN_MANAGER.registerLoginReply(CAcknowledgeServerMessage.class, CAcknowledgeServerMessage::new, NetworkDirection.LOGIN_TO_SERVER);

        // Play
        PLAY_MANAGER.register(CPlanetTravelMessage.class, CPlanetTravelMessage::new, NetworkDirection.PLAY_TO_SERVER);
        PLAY_MANAGER.register(SOpenSpaceTravelScreenMessage.class, SOpenSpaceTravelScreenMessage::new, NetworkDirection.PLAY_TO_CLIENT);
        PLAY_MANAGER.register(SPlanetTravelResponseMessage.class, SPlanetTravelResponseMessage::new, NetworkDirection.PLAY_TO_CLIENT);
        PLAY_MANAGER.register(SPlayerTravelMessage.class, SPlayerTravelMessage::new, NetworkDirection.PLAY_TO_CLIENT);
        PLAY_MANAGER.register(SUpdateSimulationBodiesMessage.class, SUpdateSimulationBodiesMessage::new, NetworkDirection.PLAY_TO_CLIENT);
    }
}
