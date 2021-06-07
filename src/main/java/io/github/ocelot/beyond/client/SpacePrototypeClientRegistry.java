package io.github.ocelot.beyond.client;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.client.render.entity.layer.SpaceHelmetLayer;
import io.github.ocelot.beyond.client.render.entity.RocketEntityRenderer;
import io.github.ocelot.beyond.client.world.MoonRenderInfo;
import io.github.ocelot.beyond.common.init.BeyondEntities;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
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
    private static final Object2ObjectMap<ResourceLocation, DimensionSpecialEffects> EFFECTS = Objects.requireNonNull(ObfuscationReflectionHelper.getPrivateValue(DimensionSpecialEffects.class, null, "field_239208_a_"));
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
        RenderingRegistry.registerEntityRenderingHandler(BeyondEntities.ROCKET.get(), RocketEntityRenderer::new);

        event.enqueueWork(() ->
        {
            EntityRenderDispatcher rendererManager = Minecraft.getInstance().getEntityRenderDispatcher();
            addSpaceHelmet((VillagerRenderer) rendererManager.renderers.get(EntityType.VILLAGER));
        });
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

    private static <T extends LivingEntity, M extends EntityModel<T>> void addSpaceHelmet(LivingEntityRenderer<T, M> renderer)
    {
        renderer.addLayer(new SpaceHelmetLayer<>(renderer));
    }
}
