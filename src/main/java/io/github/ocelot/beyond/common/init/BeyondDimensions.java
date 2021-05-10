package io.github.ocelot.beyond.common.init;

import io.github.ocelot.beyond.Beyond;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * @author Ocelot
 */
public class BeyondDimensions
{
    public static final RegistryKey<World> MOON = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(Beyond.MOD_ID, "moon"));

    private static final Object2ObjectArrayMap<ResourceLocation, Object> PROPERTIES = new Object2ObjectArrayMap<>();

    public static synchronized void init()
    {
        registerSpaceProperties(MOON.location());
    }

    // TODO add properties

    /**
     * Registers space properties for the specified dimension.
     *
     * @param dimension The dimension to register for
     */
    public static synchronized void registerSpaceProperties(ResourceLocation dimension)
    {
        PROPERTIES.put(dimension, new Object());
    }

    /**
     * Retrieves the space properties for the specified dimension.
     *
     * @param dimension The dimension to register the properties for
     * @return The properties for that dimension
     */
    public static Optional<Object> getSpaceProperties(ResourceLocation dimension)
    {
        return Optional.ofNullable(PROPERTIES.get(dimension));
    }
}
