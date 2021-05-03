package io.github.ocelot.space.client;

import io.github.ocelot.space.SpacePrototype;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.util.Optional;

/**
 * <p>The class that registers all client only client features.</p>
 *
 * @author Ocelot
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = SpacePrototype.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SpacePrototypeClientRegistry
{
    private static final String MOD_VERSION;

    static
    {
        Optional<? extends ModContainer> modContainer = ModList.get().getModContainerById(SpacePrototype.MOD_ID);
        if (modContainer.isPresent())
        {
            ArtifactVersion version = modContainer.get().getModInfo().getVersion();
            MOD_VERSION = String.format("%s.%s.%s", version.getMajorVersion(), version.getMinorVersion(), version.getIncrementalVersion());
        }
        else
        {
            MOD_VERSION = "???";
        }
    }

    @SubscribeEvent
    public static void onRegisterParticleFactories(ParticleFactoryRegisterEvent event)
    {
//        ParticleManager particleManager = Minecraft.getInstance().particleEngine;
//        particleManager.register(BattlefieldsParticles.BRICK.get(), new BrickParticle.BrickParticleFactory());
//        particleManager.register(BattlefieldsParticles.NUMBER.get(), new NumericalParticle.DamageParticleFactory());
    }

    // Minecraft can be null if using data runs
//    @SuppressWarnings("ConstantConditions")
    public static void init(IEventBus bus)
    {
        SpaceGuiSpriteManager.init(bus);
        SpacePlanetSpriteManager.init(bus);
    }

    public static void setup(FMLClientSetupEvent event)
    {
//        RenderingRegistry.registerEntityRenderingHandler(BattlefieldsEntities.THROWABLE_BRICK.get(), manager -> new ThrowableBrickEntityRenderer(manager, Minecraft.getInstance().getItemRenderer()));
    }

    @SubscribeEvent
    public static void onEvent(ColorHandlerEvent.Item event)
    {
    }

    @SubscribeEvent
    public static void onEvent(ColorHandlerEvent.Block event)
    {
    }
}
