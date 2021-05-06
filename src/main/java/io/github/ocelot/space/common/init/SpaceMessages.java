package io.github.ocelot.space.common.init;

import io.github.ocelot.sonar.common.network.SonarNetworkManager;
import io.github.ocelot.space.SpacePrototype;
import io.github.ocelot.space.common.network.play.handler.SpaceClientPlayHandler;
import io.github.ocelot.space.common.network.play.handler.SpaceServerPlayHandler;
import io.github.ocelot.space.common.network.play.message.CPlanetTravelMessage;
import io.github.ocelot.space.common.network.play.message.SPlanetTravelResponseMessage;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

/**
 * @author Ocelot
 */
public class SpaceMessages
{
    public static final String PROTOCOL_VERSION = "1";
    //    public static final SimpleChannel LOGIN = NetworkRegistry.newSimpleChannel(new ResourceLocation(SpacePrototype.MOD_ID, "login"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    public static final SimpleChannel PLAY = NetworkRegistry.newSimpleChannel(new ResourceLocation(SpacePrototype.MOD_ID, "play"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    //
//    public static final SonarNetworkManager LOGIN_MANAGER = new SonarNetworkManager(LOGIN, () -> Object::new, () -> Object::new);
    public static final SonarNetworkManager PLAY_MANAGER = new SonarNetworkManager(PLAY, () -> SpaceClientPlayHandler::new, () -> SpaceServerPlayHandler::new);

    public static void init()
    {
        PLAY_MANAGER.register(CPlanetTravelMessage.class, CPlanetTravelMessage::new, NetworkDirection.PLAY_TO_SERVER);
        PLAY_MANAGER.register(SPlanetTravelResponseMessage.class, SPlanetTravelResponseMessage::new, NetworkDirection.PLAY_TO_CLIENT);
    }
}
