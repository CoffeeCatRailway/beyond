package io.github.ocelot.beyond.common.rocket;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.ocelot.beyond.common.space.satellite.PlayerRocket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * <p>Context for a rocket launching.</p>
 *
 * @author Ocelot
 */
public class LaunchContext
{
    public static final LaunchContext DUMMY = new LaunchContext(new StructureTemplate(), 0.0F, BlockPos.ZERO, BlockPos.ZERO);

    public static final Codec<LaunchContext> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerRocket.TEMPLATE_CODEC.fieldOf("template").forGetter(LaunchContext::getTemplate),
            Codec.FLOAT.fieldOf("thrust").forGetter(LaunchContext::getThrust),
            BlockPos.CODEC.fieldOf("min").forGetter(LaunchContext::getMin),
            BlockPos.CODEC.fieldOf("max").forGetter(LaunchContext::getMax)
    ).apply(instance, LaunchContext::new));

    private final StructureTemplate template;
    private final float thrust;
    private final BlockPos min;
    private final BlockPos max;

    public LaunchContext(StructureTemplate template, float thrust, BlockPos min, BlockPos max)
    {
        this.template = template;
        this.thrust = thrust;
    this.min = min;
    this.max = max;
    }

    /**
     * @return The structure blocks part of the rocket
     */
    public StructureTemplate getTemplate()
    {
        return template;
    }

    /**
     * @return The amount of thrust the rocket has
     */
    public float getThrust()
    {
        return thrust;
    }

    public BlockPos getMin()
    {
        return min;
    }

    public BlockPos getMax()
    {
        return max;
    }
}
