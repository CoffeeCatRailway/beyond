package io.github.ocelot.space.common.simulation;

import com.google.common.collect.ImmutableMap;
import io.github.ocelot.space.SpacePrototype;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import java.util.Random;
import java.util.function.Consumer;

// TODO make data driven
public class CelestialBodyDefinitions
{
    public static final ImmutableMap<ResourceLocation, CelestialBody> SOLAR_SYSTEM;

    static
    {
        {
            ImmutableMap.Builder<ResourceLocation, CelestialBody> bodies = new ImmutableMap.Builder<>();
            ResourceLocation sun = create(bodies, "sun", "Sun", "sun", builder ->
            {
                builder.setScale(20.0F);
                builder.setShade(false);
            });
            ResourceLocation earth = create(bodies, "earth", "Earth", "earth", builder ->
            {
                builder.setScale(4.0F);
                builder.setParent(sun);
            });
            ResourceLocation moon = create(bodies, "moon", "Moon", "moon", builder ->
            {
                builder.setScale(2.0F);
                builder.setParent(earth);
            });
            SOLAR_SYSTEM = bodies.build();
        }
    }

    private static ResourceLocation create(ImmutableMap.Builder<ResourceLocation, CelestialBody> bodies, String name, String displayName, String texture, Consumer<CelestialBody.Builder> consumer)
    {
        ResourceLocation id = new ResourceLocation(SpacePrototype.MOD_ID, name);
        CelestialBody.Builder builder = CelestialBody.builder();
        builder.setTexture(new ResourceLocation(SpacePrototype.MOD_ID, texture));
        builder.setDisplayName(new StringTextComponent(displayName));
        consumer.accept(builder);
        bodies.put(id, builder.build());
        return id;
    }
}
