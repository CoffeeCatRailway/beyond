package io.github.ocelot.beyond.common.init;

import io.github.ocelot.beyond.Beyond;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

/**
 * @author Ocelot
 */
public class BeyondBiomes
{
    public static final ResourceKey<Biome> MOON = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Beyond.MOD_ID, "moon"));
}
