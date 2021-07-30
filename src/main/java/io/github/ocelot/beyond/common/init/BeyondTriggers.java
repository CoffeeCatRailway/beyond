package io.github.ocelot.beyond.common.init;

import io.github.ocelot.beyond.common.advancement.LandRocketTrigger;
import io.github.ocelot.beyond.common.advancement.LaunchRocketTrigger;
import net.minecraft.advancements.CriteriaTriggers;

/**
 * @author Ocelot
 */
public class BeyondTriggers
{
    public static final LaunchRocketTrigger LAUNCH_ROCKET = CriteriaTriggers.register(new LaunchRocketTrigger());
    public static final LandRocketTrigger LAND_ROCKET = CriteriaTriggers.register(new LandRocketTrigger());

    public static void init()
    {
    }
}
