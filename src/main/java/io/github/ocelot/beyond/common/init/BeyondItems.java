package io.github.ocelot.beyond.common.init;

import io.github.ocelot.beyond.Beyond;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * @author Ocelot
 */
public class BeyondItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Beyond.MOD_ID);

    /* Registry Methods */

    /**
     * Registers a new item under the specified name.
     *
     * @param name The name of the item
     * @param item The item to register
     * @return The object created when registering the item
     */
    public static <T extends Item> RegistryObject<T> register(String name, Supplier<? extends T> item)
    {
        RegistryObject<T> object = ITEMS.register(name, item);
        Beyond.TAB.getOrderedItems().add(object);
        return object;
    }
}
