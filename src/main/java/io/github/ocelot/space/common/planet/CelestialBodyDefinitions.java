package io.github.ocelot.space.common.planet;

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
    public static final ImmutableMap<ResourceLocation, CelestialBody> LARGE_SOLAR_SYSTEM;
    public static final ImmutableMap<ResourceLocation, CelestialBody> BINARY_SOLAR_SYSTEM;

    static
    {
        {
            ImmutableMap.Builder<ResourceLocation, CelestialBody> bodies = new ImmutableMap.Builder<>();
            ResourceLocation sun = create(bodies, "sun", "Sun", "sun", builder ->
            {
                builder.setScale(5.0F);
                builder.setShade(false);
            });
            ResourceLocation earth = create(bodies, "earth", "Earth", "earth", builder -> builder.setParent(sun));
            ResourceLocation moon = create(bodies, "moon", "Moon", "moon", builder ->
            {
                builder.setScale(0.5F);
                builder.setParent(earth);
            });
            SOLAR_SYSTEM = bodies.build();
        }

        {
            Random random = new Random();
            ImmutableMap.Builder<ResourceLocation, CelestialBody> bodies = new ImmutableMap.Builder<>();
            ResourceLocation sun = create(bodies, "sun", "Sun", "sun", builder ->
            {
                builder.setScale(5.0F);
                builder.setShade(false);
            });
            for (int i = 0; i < 10; i++)
            {
                ResourceLocation earth = create(bodies, "earth" + i, "Earth " + (i + 1), "earth", builder ->
                {
                    builder.setScale((float) (1.0F + random.nextGaussian() / 2F));
                    builder.setParent(sun);
                });
                for (int j = 0; j < random.nextInt(8); j++)
                {
                    ResourceLocation moon = create(bodies, "moon" + i + "_" + j, "Moon " + (j + 1), "moon", builder ->
                    {
                        builder.setScale((float) (1.0F + random.nextGaussian() / 2F));
                        builder.setParent(earth);
                    });
                    for (int k = 0; k < random.nextInt(5); k++)
                    {
                        ResourceLocation moon1 = create(bodies, "smallmoon" + i + "_" + j + "_" + k, "Moon Satellite " + (k + 1), "moon", builder ->
                        {
                            builder.setScale((float) (1.0F + random.nextGaussian() / 2F));
                            builder.setParent(moon);
                        });
                    }
                }
            }
            LARGE_SOLAR_SYSTEM = bodies.build();
        }

        {
            ImmutableMap.Builder<ResourceLocation, CelestialBody> bodies = new ImmutableMap.Builder<>();
            ResourceLocation sun1 = create(bodies, "sun1", "Sun 1", "sun", builder ->
            {
                builder.setParent(new ResourceLocation(SpacePrototype.MOD_ID, "sun2"));
                builder.setScale(5.0F);
                builder.setShade(false);
            });
            ResourceLocation sun2 = create(bodies, "sun2", "Sun 2", "sun", builder ->
            {
                builder.setParent(new ResourceLocation(SpacePrototype.MOD_ID, "sun1"));
                builder.setScale(5.0F);
                builder.setShade(false);
            });
            ResourceLocation earth = create(bodies, "earth", "Earth", "earth", builder -> builder.setParent(sun1));
            BINARY_SOLAR_SYSTEM = bodies.build();
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
