package io.github.ocelot.beyond.common.world.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import io.github.ocelot.beyond.common.init.BeyondBlocks;
import net.minecraft.world.gen.carver.CaveWorldCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;

public class MoonCaveCarver extends CaveWorldCarver
{
    public MoonCaveCarver(Codec<ProbabilityConfig> codec)
    {
        super(codec, 256);
        this.replaceableBlocks = ImmutableSet.of(BeyondBlocks.MOON_ROCK.get());
        this.liquids = ImmutableSet.of();
    }
}
