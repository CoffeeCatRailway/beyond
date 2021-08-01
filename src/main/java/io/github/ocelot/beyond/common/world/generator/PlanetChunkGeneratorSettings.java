package io.github.ocelot.beyond.common.world.generator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.ocelot.beyond.common.init.BeyondBiomes;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Supplier;

public class PlanetChunkGeneratorSettings
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Codec<PlanetChunkGeneratorSettings> CODEC = RecordCodecBuilder.<PlanetChunkGeneratorSettings>create(instance -> instance.group(
            RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(settings -> settings.biomes),
            StructureSettings.CODEC.fieldOf("structures").forGetter(PlanetChunkGeneratorSettings::structureSettings),
            FlatLayerInfo.CODEC.listOf().fieldOf("layers").forGetter(PlanetChunkGeneratorSettings::getLayersInfo)
    ).apply(instance, PlanetChunkGeneratorSettings::new)).stable();

    private final Registry<Biome> biomes;
    private final StructureSettings structureSettings;
    private final List<FlatLayerInfo> layersInfo = Lists.newArrayList();
    private Supplier<Biome> biome;
    private final BlockState[] layers = new BlockState[256];
    private boolean voidGen;

    public PlanetChunkGeneratorSettings(Registry<Biome> biomeRegistry, StructureSettings structureSettings, List<FlatLayerInfo> layers)
    {
        this(structureSettings, biomeRegistry);
        this.layersInfo.addAll(layers);
        this.updateLayers();
        this.biome = () -> biomeRegistry.getOrThrow(BeyondBiomes.MOON);
    }

    public PlanetChunkGeneratorSettings(StructureSettings structureSettings, Registry<Biome> biomeRegistry)
    {
        this.biomes = biomeRegistry;
        this.structureSettings = structureSettings;
        this.biome = () -> biomeRegistry.getOrThrow(Biomes.PLAINS);
    }

    public Biome getBiomeFromSettings()
    {
        Biome biome = this.getBiome();
        BiomeGenerationSettings biomegenerationsettings = biome.getGenerationSettings();
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(biomegenerationsettings.getSurfaceBuilder());
//        if (this.addLakes)
//        {
//            biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_WATER);
//            biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_LAVA);
//        }

        Map<StructureFeature<?>, ConfiguredStructureFeature<?, ?>> map = new HashMap<>();
        BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE.stream().filter(f -> !map.containsKey(f.feature)).forEach(f -> map.put(f.feature, f));

        for (Map.Entry<StructureFeature<?>, StructureFeatureConfiguration> entry : this.structureSettings.structureConfig().entrySet())
        {
            if (!map.containsKey(entry.getKey()))
            {
                LOGGER.error("FORGE: There's no known StructureFeature for {} when preparing the {} space biome. The structure will be skipped and may not spawn. Please register your StructureFeatures in the WorldGenRegistries!", entry.getKey().getFeatureName(), biome.getRegistryName());
            }
            else
            {
                biomegenerationsettings$builder.addStructureStart(biomegenerationsettings.withBiomeConfig(map.get(entry.getKey())));
            }
        }

        boolean flag = !this.voidGen || this.biomes.getResourceKey(biome).equals(Optional.of(Biomes.THE_VOID));
        if (flag)
        {
            List<List<Supplier<ConfiguredFeature<?, ?>>>> list = biomegenerationsettings.features();

            for (int i = 0; i < list.size(); ++i)
            {
                if (i != GenerationStep.Decoration.UNDERGROUND_STRUCTURES.ordinal() && i != GenerationStep.Decoration.SURFACE_STRUCTURES.ordinal())
                {
                    for (Supplier<ConfiguredFeature<?, ?>> supplier : list.get(i))
                    {
                        biomegenerationsettings$builder.addFeature(i, supplier);
                    }
                }
            }
        }

        BlockState[] ablockstate = this.getLayers();

        for (int j = 0; j < ablockstate.length; ++j)
        {
            BlockState blockstate = ablockstate[j];
            if (blockstate != null && !Heightmap.Types.MOTION_BLOCKING.isOpaque().test(blockstate))
            {
                this.layers[j] = null;
                biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Feature.FILL_LAYER.configured(new LayerConfiguration(j, blockstate)));
            }
        }

        return (new Biome.BiomeBuilder()).precipitation(biome.getPrecipitation()).biomeCategory(biome.getBiomeCategory()).depth(biome.getDepth()).scale(biome.getScale()).temperature(biome.getBaseTemperature()).downfall(biome.getDownfall()).specialEffects(biome.getSpecialEffects()).generationSettings(biomegenerationsettings$builder.build()).mobSpawnSettings(biome.getMobSettings()).build().setRegistryName(biome.getRegistryName());
    }

    public StructureSettings structureSettings()
    {
        return this.structureSettings;
    }

    public Biome getBiome()
    {
        return (Biome) this.biome.get();
    }

    @OnlyIn(Dist.CLIENT)
    public void setBiome(Supplier<Biome> p_242870_1_)
    {
        this.biome = p_242870_1_;
    }

    public List<FlatLayerInfo> getLayersInfo()
    {
        return this.layersInfo;
    }

    public BlockState[] getLayers()
    {
        return this.layers;
    }

    public void updateLayers()
    {
        Arrays.fill(this.layers, 0, this.layers.length, (Object) null);
        int i = 0;

        for (FlatLayerInfo flatlayerinfo : this.layersInfo)
        {
            flatlayerinfo.setStart(i);
            i += flatlayerinfo.getHeight();
        }

        this.voidGen = true;

        for (FlatLayerInfo flatlayerinfo1 : this.layersInfo)
        {
            for (int j = flatlayerinfo1.getStart(); j < flatlayerinfo1.getStart() + flatlayerinfo1.getHeight(); ++j)
            {
                BlockState blockstate = flatlayerinfo1.getBlockState();
                if (!blockstate.is(Blocks.AIR))
                {
                    this.voidGen = false;
                    this.layers[j] = blockstate;
                }
            }
        }

    }

    public static PlanetChunkGeneratorSettings getDefault(Registry<Biome> p_242869_0_)
    {
        StructureSettings dimensionstructuressettings = new StructureSettings(Optional.of(StructureSettings.DEFAULT_STRONGHOLD), Maps.newHashMap(ImmutableMap.of(StructureFeature.VILLAGE, StructureSettings.DEFAULTS.get(StructureFeature.VILLAGE))));
        PlanetChunkGeneratorSettings flatgenerationsettings = new PlanetChunkGeneratorSettings(dimensionstructuressettings, p_242869_0_);
        flatgenerationsettings.biome = () -> p_242869_0_.getOrThrow(Biomes.PLAINS);
        flatgenerationsettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
        flatgenerationsettings.getLayersInfo().add(new FlatLayerInfo(2, Blocks.DIRT));
        flatgenerationsettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.GRASS_BLOCK));
        flatgenerationsettings.updateLayers();
        return flatgenerationsettings;
    }
}
