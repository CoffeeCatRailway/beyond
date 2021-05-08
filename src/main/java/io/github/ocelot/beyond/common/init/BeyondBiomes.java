package io.github.ocelot.beyond.common.init;

import io.github.ocelot.beyond.Beyond;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author Ocelot
 */
public class BeyondBiomes
{
    public static final RegistryKey<Biome> MOON_LOCATION = RegistryKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Beyond.MOD_ID, "moon"));
}
