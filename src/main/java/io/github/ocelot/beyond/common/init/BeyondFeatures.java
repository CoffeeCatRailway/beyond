package io.github.ocelot.beyond.common.init;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.world.feature.CaveCrystalConfiguration;
import io.github.ocelot.beyond.common.world.feature.CaveCrystalFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author Ocelot
 */
public class BeyondFeatures
{
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Beyond.MOD_ID);

    public static final RegistryObject<Feature<CaveCrystalConfiguration>> CAVE_CRYSTAL = FEATURES.register("cave_crystal", () -> new CaveCrystalFeature(CaveCrystalConfiguration.CODEC));
}
