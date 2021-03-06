package io.github.ocelot.beyond.common.init;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.block.RocketControllerBlock;
import io.github.ocelot.beyond.common.block.RocketThrusterBlock;
import io.github.ocelot.beyond.common.blockentity.RocketControllerBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Ocelot
 */
public class BeyondBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Beyond.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Beyond.MOD_ID);

    public static final RegistryObject<Block> MOON_ROCK = register("moon_rock", () -> new Block(BlockBehaviour.Properties.copy(Blocks.END_STONE)));
    public static final RegistryObject<Block> LANDING_PAD = register("landing_pad", () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Block> ROCKET_CONTROLLER = register("rocket_controller", () -> new RocketControllerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).isRedstoneConductor((state, level, pos) -> true).strength(6.0F, 9.0F)));
    public static final RegistryObject<Block> ROCKET_THRUSTER = register("rocket_thruster", () -> new RocketThrusterBlock(1.0F, BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(6.0F, 9.0F)));
    public static final RegistryObject<Block> CREATIVE_ROCKET_THRUSTER = register("creative_rocket_thruster", () -> new RocketThrusterBlock(Float.MAX_VALUE, BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(-1.0F, 3600000.0F).noDrops()));

    public static final RegistryObject<BlockEntityType<RocketControllerBlockEntity>> ROCKET_CONTROLLER_BE = BLOCK_ENTITIES.register("rocket_controller", () -> BlockEntityType.Builder.of(RocketControllerBlockEntity::new, ROCKET_CONTROLLER.get()).build(null));

    /* Registry Methods */

    /**
     * Registers the specified block with a bound {@link BlockItem} under the specified id.
     *
     * @param name  The id of the block
     * @param block The block to register
     * @return The registry reference
     */
    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block)
    {
        return register(name, block, new Item.Properties().tab(Beyond.TAB));
    }

    /**
     * Registers the specified block with a bound {@link BlockItem} under the specified id.
     *
     * @param name           The id of the block
     * @param block          The block to register
     * @param itemProperties The properties of the block item to register
     * @return The registry reference
     */
    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block, Item.Properties itemProperties)
    {
        return register(name, block, object -> new BlockItem(object.get(), itemProperties));
    }

    /**
     * Registers the specified block with a bound item under the specified id.
     *
     * @param name  The id of the block
     * @param block The block to register
     * @param item  The item to register or null for no item
     * @return The registry reference
     */
    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block, Function<RegistryObject<T>, Item> item)
    {
        RegistryObject<T> object = BLOCKS.register(name, block);
        BeyondItems.ITEMS.register(name, () -> item.apply(object));
        return object;
    }
}
