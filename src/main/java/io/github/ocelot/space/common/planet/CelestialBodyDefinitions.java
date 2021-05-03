package io.github.ocelot.space.common.planet;

import com.google.common.collect.ImmutableMap;
import io.github.ocelot.space.SpacePrototype;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

// TODO make data driven
public class CelestialBodyDefinitions
{
    public static final ImmutableMap<ResourceLocation, CelestialBody> SOLAR_SYSTEM;

    static
    {
        ImmutableMap.Builder<ResourceLocation, CelestialBody> bodies = new ImmutableMap.Builder<>();
        ResourceLocation sun = create(bodies, "sun", builder ->
        {
            builder.setScale(4.0F);
            builder.setShade(false);
        });
        ResourceLocation moon = create(bodies, "moon", builder -> builder.setParent(sun));
        SOLAR_SYSTEM = bodies.build();
    }

    private static ResourceLocation create(ImmutableMap.Builder<ResourceLocation, CelestialBody> bodies, String name, Consumer<CelestialBody.Builder> consumer)
    {
        ResourceLocation id = new ResourceLocation(SpacePrototype.MOD_ID, name);
        CelestialBody.Builder builder = CelestialBody.builder(id);
        consumer.accept(builder);
        bodies.put(id, builder.build());
        return id;
    }
}
