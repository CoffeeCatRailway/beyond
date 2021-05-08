package io.github.ocelot.beyond.common.init;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.world.feature.CraterFeature;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ocelot
 */
@Mod.EventBusSubscriber(modid = Beyond.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BeyondFeatures
{
    private static final Set<Feature<?>> FEATURES = new HashSet<>();

    public static final Feature<BlockStateFeatureConfig> CRATER = register("crater", new CraterFeature(BlockStateFeatureConfig.CODEC));

    private static <C extends IFeatureConfig, F extends Feature<C>> F register(String name, F feature)
    {
        feature.setRegistryName(new ResourceLocation(Beyond.MOD_ID, name));
        FEATURES.add(feature);
        return feature;
    }

    @SubscribeEvent
    public static void onEvent(RegistryEvent.Register<Feature<?>> event)
    {
        FEATURES.forEach(event.getRegistry()::register);
        FEATURES.clear();
    }
}
