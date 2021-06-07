package io.github.ocelot.beyond.common.world.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import io.github.ocelot.beyond.common.init.BeyondBlocks;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

import java.util.Random;

public class MoonCaveCarver extends CaveWorldCarver
{
    public MoonCaveCarver(Codec<ProbabilityFeatureConfiguration> codec)
    {
        super(codec, 256);
        this.replaceableBlocks = ImmutableSet.of(BeyondBlocks.MOON_ROCK.get());
        this.liquids = ImmutableSet.of();
    }

    @Override
    protected int getCaveBound()
    {
        return 32;
    }

    @Override
    protected double getYScale()
    {
        return 0.8;
    }

    @Override
    protected float getThickness(Random random)
    {
        return (random.nextFloat() * 2.0F + random.nextFloat()) * 4.0F;
    }
}
