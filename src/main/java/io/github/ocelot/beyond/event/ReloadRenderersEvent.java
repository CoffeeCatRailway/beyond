package io.github.ocelot.beyond.event;

import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.world.WorldEvent;

import javax.annotation.Nullable;

/**
 * @author Ocelot
 */
public class ReloadRenderersEvent extends WorldEvent
{
    public ReloadRenderersEvent(@Nullable LevelAccessor world)
    {
        super(world);
    }

    @Nullable
    @Override
    public LevelAccessor getWorld()
    {
        return super.getWorld();
    }
}
