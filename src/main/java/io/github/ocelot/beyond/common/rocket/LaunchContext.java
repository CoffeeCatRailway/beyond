package io.github.ocelot.beyond.common.rocket;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * <p>Context for a rocket launching.</p>
 *
 * @author Ocelot
 */
public class LaunchContext
{
    private final StructureTemplate template;
    private final float thrust;

    public LaunchContext(StructureTemplate template, float thrust)
    {
        this.template = template;
        this.thrust = thrust;
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
}
