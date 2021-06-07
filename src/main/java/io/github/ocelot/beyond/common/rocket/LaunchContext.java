package io.github.ocelot.beyond.common.rocket;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.ocelot.beyond.common.space.satellite.PlayerRocket;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * <p>Context for a rocket launching.</p>
 *
 * @author Ocelot
 */
public class LaunchContext
{
    public static final LaunchContext DUMMY = new LaunchContext(new StructureTemplate(), 0.0F);

    public static final Codec<LaunchContext> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerRocket.TEMPLATE_CODEC.fieldOf("template").forGetter(LaunchContext::getTemplate),
            Codec.FLOAT.fieldOf("lift").forGetter(LaunchContext::getLift)
    ).apply(instance, LaunchContext::new));

    private final StructureTemplate template;
    private final float lift;

    public LaunchContext(StructureTemplate template, float lift)
    {
        this.template = template;
        this.lift = lift;
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
    public float getLift()
    {
        return lift;
    }
}
