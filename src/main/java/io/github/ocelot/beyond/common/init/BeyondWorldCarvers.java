package io.github.ocelot.beyond.common.init;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.world.carver.CraterCarver;
import io.github.ocelot.beyond.common.world.carver.MoonCaveCarver;
import io.github.ocelot.beyond.common.world.carver.CraterConfig;
import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author Ocelot
 */
public class BeyondWorldCarvers
{
    public static final DeferredRegister<WorldCarver<?>> CARVERS = DeferredRegister.create(ForgeRegistries.WORLD_CARVERS, Beyond.MOD_ID);

    public static final RegistryObject<WorldCarver<ProbabilityConfig>> MOON_CAVE = CARVERS.register("moon_cave", () -> new MoonCaveCarver(ProbabilityConfig.CODEC));
    public static final RegistryObject<WorldCarver<CraterConfig>> CRATER = CARVERS.register("crater", () -> new CraterCarver(CraterConfig.CODEC));
}
