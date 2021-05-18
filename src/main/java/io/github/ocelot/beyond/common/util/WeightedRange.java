package io.github.ocelot.beyond.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.ocelot.beyond.common.MagicMath;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.Validate;

import java.util.Random;

/**
 * @author Ocelot
 */
public class WeightedRange
{
    public static final Codec<WeightedRange> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("min").forGetter(WeightedRange::getMin),
            Codec.FLOAT.fieldOf("max").forGetter(WeightedRange::getMax),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("bias", 0.0F).forGetter(WeightedRange::getBias)
    ).apply(instance, WeightedRange::new));

    private static final int CONSTANT_FLAG = 0x1;
    private static final int LINEAR_FLAG = 0x2;

    private final float min;
    private final float max;
    private final float bias;
    private final byte flags;

    public WeightedRange(float min, float max, float bias)
    {
        Validate.isTrue(min <= max, "Min must be less than or equal to max, was " + min + "-" + max);
        Validate.inclusiveBetween(0.0F, 1.0F, bias, "Bias must be between 0.0 and 1.0, was " + bias);
        this.min = min;
        this.max = max;
        this.bias = bias;
        this.flags = (byte) ((Float.compare(min, max) == 0 ? CONSTANT_FLAG : 0) | (bias == 0 ? LINEAR_FLAG : 0));
    }

    /**
     * Wrapper for {@link #getValue(float)} that automatically generates a random float value from the random.
     *
     * @param random The random instance to use
     * @return A random value between this min and max
     */
    public float getRandomValue(Random random)
    {
        if ((this.flags & CONSTANT_FLAG) != 0)
            return this.min;
        return this.getValue(random.nextFloat());
    }

    /**
     * Calculates a weighted value between this min and max based on the provided input.
     *
     * @param input The input value between 0.0 and 1.0
     * @return A weighted value based on this range's bias
     */
    public float getValue(float input)
    {
        if ((this.flags & CONSTANT_FLAG) != 0)
            return this.min;
        if ((this.flags & LINEAR_FLAG) != 0)
            return Mth.lerp(input, this.min, this.max);
        return Mth.lerp(MagicMath.bias(input, this.bias), this.min, this.max);
    }

    /**
     * @return The minimum value this range can be
     */
    public float getMin()
    {
        return min;
    }

    /**
     * @return The maximum value this range can be
     */
    public float getMax()
    {
        return max;
    }

    /**
     * @return The bias to apply. A bias of 0.0 is a linear line and 1.0 is a square angle
     */
    public float getBias()
    {
        return bias;
    }
}
