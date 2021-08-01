package io.github.ocelot.beyond.common.init;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.world.generator.PlanetChunkGenerator;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * @author Ocelot
 */
public class BeyondDimensions
{
    public static final ResourceKey<Level> MOON = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(Beyond.MOD_ID, "moon"));

    public static void init()
    {
        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(Beyond.MOD_ID, "planet"), PlanetChunkGenerator.CODEC);
    }
}
