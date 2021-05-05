package io.github.ocelot.space.common.simulation;

import io.github.ocelot.space.SpacePrototype;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

// TODO make data driven
public class CelestialBodyDefinitions
{
    public static final Supplier<Map<ResourceLocation, CelestialBody>> SOLAR_SYSTEM;

    static
    {
        {
            SOLAR_SYSTEM = () -> Util.make(new HashMap<>(), bodies ->
            {
                Random random = new Random();
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
                for (int i = 0; i < 1000; i++)
                {
                    ResourceLocation asteroid = create(bodies, "asteroid" + i, "Asteroid", "asteroid", builder ->
                    {
                        builder.setScale(1.0F + random.nextFloat() - 0.5F);
                        builder.setDistanceFactor(2.0F + random.nextFloat());
                        builder.setParent(sun);
                    });
                }
            });
        }
    }

    private static ResourceLocation create(Map<ResourceLocation, CelestialBody> bodies, String name, String displayName, String texture, Consumer<CelestialBody.Builder> consumer)
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
