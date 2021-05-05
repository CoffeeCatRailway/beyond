package io.github.ocelot.space.common.simulation.body;

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
                ResourceLocation mercury = create(bodies, "mercury", "Mercury", "mercury", builder ->
                {
                    builder.setScale(3.0F);
                    builder.setDistanceFactor(0.25F);
                    builder.setParent(sun);
                });
                ResourceLocation venus = create(bodies, "venus", "Venus", "venus", builder ->
                {
                    builder.setScale(3.0F);
                    builder.setDistanceFactor(0.5F);
                    builder.setParent(sun);
                });
                ResourceLocation earth = create(bodies, "earth", "Earth", "earth", builder ->
                {
                    builder.setScale(4.0F);
                    builder.setDistanceFactor(0.8F);
                    builder.setParent(sun);
                });
                ResourceLocation moon = create(bodies, "moon", "Moon", "moon", builder ->
                {
                    builder.setScale(2.0F);
                    builder.setParent(earth);
                });
                ResourceLocation mars = create(bodies, "mars", "Mars", "mars", builder ->
                {
                    builder.setScale(3.5F);
                    builder.setDistanceFactor(1.2F);
                    builder.setParent(sun);
                });
                ResourceLocation jupiter = create(bodies, "jupiter", "Jupiter", "jupiter", builder ->
                {
                    builder.setScale(12.0F);
                    builder.setDistanceFactor(5.0F);
                    builder.setParent(sun);
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
