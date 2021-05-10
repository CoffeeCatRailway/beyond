package io.github.ocelot.beyond.client;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.client.world.MoonRenderInfo;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.util.Objects;
import java.util.Optional;

/**
 * <p>The class that registers all client only client features.</p>
 *
 * @author Ocelot
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Beyond.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SpacePrototypeClientRegistry
{
    private static final Object2ObjectMap<ResourceLocation, DimensionRenderInfo> EFFECTS = Objects.requireNonNull(ObfuscationReflectionHelper.getPrivateValue(DimensionRenderInfo.class, null, "field_239208_a_"));
    private static final String MOD_VERSION;

    static
    {
        Optional<? extends ModContainer> modContainer = ModList.get().getModContainerById(Beyond.MOD_ID);
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
        event.enqueueWork(() ->
        {
            EFFECTS.put(new ResourceLocation(Beyond.MOD_ID, "moon"), new MoonRenderInfo());
        });
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

    @SubscribeEvent
    public static void onEvent(ModelRegistryEvent event)
    {
        for (ResourceLocation location : Minecraft.getInstance().getResourceManager().listResources("models/body", s -> s.endsWith(".json")))
            ModelLoader.addSpecialModel(new ResourceLocation(location.getNamespace(), location.getPath().substring(7, location.getPath().length() - 5)));
    }
}
