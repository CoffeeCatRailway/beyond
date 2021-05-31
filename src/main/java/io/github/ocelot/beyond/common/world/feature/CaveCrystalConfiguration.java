package io.github.ocelot.beyond.common.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;

/**
 * <p>Configuration for placing cave crystals inside caves.</p>
 *
 * @author Ocelot
 */
public class CaveCrystalConfiguration implements FeatureConfiguration
{
    public static final Codec<CaveCrystalConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("state").forGetter(CaveCrystalConfiguration::getState),
            BlockState.CODEC.listOf().fieldOf("place_on").forGetter(CaveCrystalConfiguration::getPlaceOn),
            BlockState.CODEC.listOf().fieldOf("place_in").forGetter(CaveCrystalConfiguration::getPlaceIn),
            Codec.INT.fieldOf("max_y").forGetter(CaveCrystalConfiguration::getMaxY),
            Codec.INT.fieldOf("min_length").forGetter(CaveCrystalConfiguration::getMinLength),
            Codec.INT.fieldOf("max_length").forGetter(CaveCrystalConfiguration::getMaxLength),
            Codec.INT.fieldOf("min_radius").forGetter(CaveCrystalConfiguration::getMinRadius),
            Codec.INT.fieldOf("max_radius").forGetter(CaveCrystalConfiguration::getMaxRadius)
    ).apply(instance, CaveCrystalConfiguration::new));

    private final BlockState state;
    private final List<BlockState> placeOn;
    private final List<BlockState> placeIn;
    private final int maxY;
    private final int minLength;
    private final int maxLength;
    private final int minRadius;
    private final int maxRadius;

    public CaveCrystalConfiguration(BlockState state, List<BlockState> placeOn, List<BlockState> placeIn, int maxY, int minLength, int maxLength, int minRadius, int maxRadius)
    {
        this.state = state;
        this.placeOn = placeOn;
        this.placeIn = placeIn;
        this.maxY = maxY;
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    public BlockState getState()
    {
        return state;
    }

    public List<BlockState> getPlaceOn()
    {
        return placeOn;
    }

    public List<BlockState> getPlaceIn()
    {
        return placeIn;
    }

    public int getMaxY()
    {
        return maxY;
    }

    public int getMinLength()
    {
        return minLength;
    }

    public int getMaxLength()
    {
        return maxLength;
    }

    public int getMinRadius()
    {
        return minRadius;
    }

    public int getMaxRadius()
    {
        return maxRadius;
    }
}
