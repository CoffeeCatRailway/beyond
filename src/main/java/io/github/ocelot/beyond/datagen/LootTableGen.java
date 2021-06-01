package io.github.ocelot.beyond.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.init.BeyondBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.ForgeLootTableProvider;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LootTableGen extends ForgeLootTableProvider
{
    private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> tables = ImmutableList.of(Pair.of(FishingProvider::new, LootContextParamSets.FISHING), Pair.of(ChestProvider::new, LootContextParamSets.CHEST), Pair.of(EntityProvider::new, LootContextParamSets.ENTITY), Pair.of(BlockProvider::new, LootContextParamSets.BLOCK), Pair.of(PiglinBarteringProvider::new, LootContextParamSets.PIGLIN_BARTER), Pair.of(GiftProvider::new, LootContextParamSets.GIFT));

    public LootTableGen(DataGenerator generator)
    {
        super(generator);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables()
    {
        return tables;
    }

    private static class FishingProvider extends FishingLoot
    {
        @Override
        public void accept(BiConsumer<ResourceLocation, LootTable.Builder> registry)
        {
        }
    }

    private static class ChestProvider extends ChestLoot
    {
        @Override
        public void accept(BiConsumer<ResourceLocation, LootTable.Builder> registry)
        {
        }
    }

    private static class EntityProvider extends EntityLoot
    {
        @Override
        protected void addTables()
        {
        }

        @Override
        protected Iterable<EntityType<?>> getKnownEntities()
        {
            return ForgeRegistries.ENTITIES.getValues().stream().filter(entityType -> entityType.getRegistryName() != null && Beyond.MOD_ID.equals(entityType.getRegistryName().getNamespace())).collect(Collectors.toSet());
        }
    }

    private static class BlockProvider extends BlockLoot
    {
        @Override
        protected void addTables()
        {
            this.dropSelf(BeyondBlocks.MOON_ROCK.get());
            this.dropSelf(BeyondBlocks.ROCKET_CONSTRUCTION_PLATFORM.get());
            this.dropSelf(BeyondBlocks.ROCKET_CONTROLLER.get());
            this.dropSelf(BeyondBlocks.ROCKET_THRUSTER.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks()
        {
            return ForgeRegistries.BLOCKS.getValues().stream().filter(entityType -> entityType.getRegistryName() != null && Beyond.MOD_ID.equals(entityType.getRegistryName().getNamespace())).collect(Collectors.toSet());
        }
    }

    private static class PiglinBarteringProvider extends PiglinBarterLoot
    {
        @Override
        public void accept(BiConsumer<ResourceLocation, LootTable.Builder> registry)
        {
        }
    }

    private static class GiftProvider extends GiftLoot
    {
        @Override
        public void accept(BiConsumer<ResourceLocation, LootTable.Builder> registry)
        {
        }
    }
}