package io.github.ocelot.beyond.common.world.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import io.github.ocelot.beyond.common.init.BeyondBlocks;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class MoonCaveCarver extends CaveWorldCarver
{
    public MoonCaveCarver(Codec<ProbabilityFeatureConfiguration> codec)
    {
        super(codec, 256);
        this.replaceableBlocks = ImmutableSet.of(BeyondBlocks.MOON_ROCK.get());
        this.liquids = ImmutableSet.of();
    }
}
