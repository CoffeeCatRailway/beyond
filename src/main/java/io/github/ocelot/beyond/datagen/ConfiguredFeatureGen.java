package io.github.ocelot.beyond.datagen;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;
import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.init.BeyondBlocks;
import io.github.ocelot.beyond.common.init.BeyondFeatures;
import io.github.ocelot.beyond.common.world.feature.CaveCrystalConfiguration;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.CarvingMaskDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ConfiguredFeatureGen implements DataProvider
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final DataGenerator generator;
    private final String domain;

    public ConfiguredFeatureGen(DataGenerator generator)
    {
        this.generator = generator;
        this.domain = Beyond.MOD_ID;
    }

    @Override
    public void run(HashCache arg)
    {
        Path folder = this.generator.getOutputFolder().resolve("data/" + this.domain + "/worldgen/configured_feature");
        Map<String, ConfiguredFeature<?, ?>> features = new HashMap<>();
        this.addFeatures((name, feature) ->
        {
            if (features.put(name, feature) != null)
                throw new IllegalArgumentException("Duplicate feature with id: " + this.domain + ":" + name);
        });

        for (Map.Entry<String, ConfiguredFeature<?, ?>> entry : features.entrySet())
        {
            Path file = folder.resolve(entry.getKey() + ".json");

            try
            {
                DataProvider.save(GSON, arg, ConfiguredFeature.CODEC.encodeStart(JsonOps.INSTANCE, entry::getValue).getOrThrow(false, LOGGER::error), file);
            }
            catch (IOException e)
            {
                LOGGER.error("Couldn't save configured feature {}", file, e);
            }
        }
    }

    private void addFeatures(BiConsumer<String, ConfiguredFeature<?, ?>> featureConsumer)
    {
        featureConsumer.accept("test_crystal", BeyondFeatures.CAVE_CRYSTAL.get().configured(new CaveCrystalConfiguration(Blocks.WHITE_STAINED_GLASS.defaultBlockState(), ImmutableList.of(BeyondBlocks.MOON_ROCK.get().defaultBlockState()), ImmutableList.of(Blocks.AIR.defaultBlockState(), Blocks.CAVE_AIR.defaultBlockState()), 80, 4, 12, 2, 3)).decorated(FeatureDecorator.CARVING_MASK.configured(new CarvingMaskDecoratorConfiguration(GenerationStep.Carving.AIR, 0.001F))));
    }

    @Override
    public String getName()
    {
        return "Configured Features";
    }
}
