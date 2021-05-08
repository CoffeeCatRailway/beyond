package io.github.ocelot.beyond.common.init;

import io.github.ocelot.beyond.Beyond;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

/**
 * @author Ocelot
 */
public class BeyondDimensions
{
    public static final RegistryKey<World> MOON = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(Beyond.MOD_ID, "moon"));

    public static synchronized void init()
    {
    }
}
