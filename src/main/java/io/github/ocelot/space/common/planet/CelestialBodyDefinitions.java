package io.github.ocelot.space.common.planet;

import com.google.common.collect.ImmutableMap;
import io.github.ocelot.space.SpacePrototype;
import net.minecraft.util.ResourceLocation;

import java.util.Random;
import java.util.function.Consumer;

// TODO make data driven
public class CelestialBodyDefinitions
{
    public static final ImmutableMap<ResourceLocation, CelestialBody> SOLAR_SYSTEM;
    public static final ImmutableMap<ResourceLocation, CelestialBody> LARGE_SOLAR_SYSTEM;

    static
    {
        {
            ImmutableMap.Builder<ResourceLocation, CelestialBody> bodies = new ImmutableMap.Builder<>();
            ResourceLocation sun = create(bodies, "sun", "sun", builder ->
            {
                builder.setScale(5.0F);
                builder.setShade(false);
            });
            ResourceLocation earth = create(bodies, "earth", "earth", builder -> builder.setParent(sun));
            ResourceLocation moon = create(bodies, "moon", "moon", builder ->
            {
                builder.setScale(0.5F);
                builder.setParent(earth);
            });
            SOLAR_SYSTEM = bodies.build();
        }

        {
            Random random = new Random();
            ImmutableMap.Builder<ResourceLocation, CelestialBody> bodies = new ImmutableMap.Builder<>();
            ResourceLocation sun = create(bodies, "sun", "sun", builder ->
            {
                builder.setScale(5.0F);
                builder.setShade(false);
            });
            for (int i = 0; i < 10; i++)
            {
                ResourceLocation earth = create(bodies, "earth" + i, "earth", builder ->
                {
                    builder.setScale((float) (1.0F + random.nextGaussian() / 2F));
                    builder.setParent(sun);
                });
                for (int j = 0; j < random.nextInt(8); j++)
                {
                    ResourceLocation moon = create(bodies, "moon" + i + "_" + j, "moon", builder ->
                    {
                        builder.setScale((float) (1.0F + random.nextGaussian() / 2F));
                        builder.setParent(earth);
                    });
                    for (int k = 0; k < random.nextInt(5); k++)
                    {
                        ResourceLocation moon1 = create(bodies, "smallmoon" + i + "_" + j + "_" + k, "moon", builder ->
                        {
                            builder.setScale((float) (1.0F + random.nextGaussian() / 2F));
                            builder.setParent(moon);
                        });
                    }
                }
            }
            LARGE_SOLAR_SYSTEM = bodies.build();
        }
    }

    private static ResourceLocation create(ImmutableMap.Builder<ResourceLocation, CelestialBody> bodies, String name, String texture, Consumer<CelestialBody.Builder> consumer)
    {
        ResourceLocation id = new ResourceLocation(SpacePrototype.MOD_ID, name);
        CelestialBody.Builder builder = CelestialBody.builder(new ResourceLocation(SpacePrototype.MOD_ID, texture));
        consumer.accept(builder);
        bodies.put(id, builder.build());
        return id;
    }
}
