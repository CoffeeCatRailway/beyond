package io.github.ocelot.beyond.common.world.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.ocelot.beyond.common.util.WeightedRange;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

/**
 * <p>Config for {@link CraterCarver}.</p>
 *
 * @author Ocelot
 */
public class CraterConfig extends ProbabilityFeatureConfiguration
{
    public static final Codec<CraterConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("surfaceBlock").forGetter(CraterConfig::getSurfaceBlock),
            WeightedRange.CODEC.fieldOf("radius").forGetter(CraterConfig::getRadius),
            WeightedRange.CODEC.fieldOf("rimWidth").forGetter(CraterConfig::getRimWidth),
            WeightedRange.CODEC.fieldOf("rimSteepness").forGetter(CraterConfig::getRimSteepness),
            WeightedRange.CODEC.fieldOf("floorHeight").forGetter(CraterConfig::getFloorHeight),
            Codec.FLOAT.fieldOf("smoothness").forGetter(CraterConfig::getSmoothness),
            Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(config -> config.probability)
    ).apply(instance, CraterConfig::new));

//    float radius = MagicMath.bias(random.nextFloat(), 0.8F) * 6.0F;
//        if (radius <= 3)
//        return false;
//
//    float rimWidth = 1.2F;
//    float rimSteepness = 0.4F;
//    float floorHeight = -random.nextFloat() * 0.5F - 0.5F;
//    float smoothness = 0.34F;
//    int min = -MathHelper.floor(2 * radius);
//    int max = MathHelper.ceil(2 * radius);

    private final BlockState surfaceBlock;
    private final WeightedRange radius;
    private final WeightedRange rimWidth;
    private final WeightedRange rimSteepness;
    private final WeightedRange floorHeight;
    private final float smoothness;

    public CraterConfig(BlockState surfaceBlock, WeightedRange radius, WeightedRange rimWidth, WeightedRange rimSteepness, WeightedRange floorHeight, float smoothness, float probability)
    {
        super(probability);
        this.surfaceBlock = surfaceBlock;
        this.radius = radius;
        this.rimWidth = rimWidth;
        this.rimSteepness = rimSteepness;
        this.floorHeight = floorHeight;
        this.smoothness = smoothness;
    }

    public BlockState getSurfaceBlock()
    {
        return surfaceBlock;
    }

    public WeightedRange getRadius()
    {
        return radius;
    }

    public WeightedRange getRimWidth()
    {
        return rimWidth;
    }

    public WeightedRange getRimSteepness()
    {
        return rimSteepness;
    }

    public WeightedRange getFloorHeight()
    {
        return floorHeight;
    }

    public float getSmoothness()
    {
        return smoothness;
    }
}
