package io.github.ocelot.beyond.datagen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.advancement.LandRocketTrigger;
import io.github.ocelot.beyond.common.advancement.LaunchRocketTrigger;
import io.github.ocelot.beyond.common.init.BeyondBlocks;
import io.github.ocelot.beyond.common.init.BeyondDimensions;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class AdvancementGen implements DataProvider
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    private final DataGenerator generator;
    private final List<Consumer<Consumer<Advancement>>> tabs = ImmutableList.of(new BeyondAdvancements());

    public AdvancementGen(DataGenerator arg)
    {
        this.generator = arg;
    }

    @Override
    public void run(HashCache arg)
    {
        Path path = this.generator.getOutputFolder();
        Set<ResourceLocation> set = Sets.newHashSet();
        Consumer<Advancement> consumer = (arg2) ->
        {
            if (!set.add(arg2.getId()))
            {
                throw new IllegalStateException("Duplicate advancement " + arg2.getId());
            }
            else
            {
                Path path2 = createPath(path, arg2);

                try
                {
                    DataProvider.save(GSON, arg, arg2.deconstruct().serializeToJson(), path2);
                }
                catch (IOException var6)
                {
                    LOGGER.error("Couldn't save advancement {}", path2, var6);
                }

            }
        };

        for (Consumer<Consumer<Advancement>> tab : this.tabs)
            tab.accept(consumer);
    }

    private static Path createPath(Path path, Advancement arg)
    {
        return path.resolve("data/" + arg.getId().getNamespace() + "/advancements/" + arg.getId().getPath() + ".json");
    }

    @Override
    public String getName()
    {
        return Beyond.MOD_ID + " Advancements";
    }

    private static Advancement.Builder get(ItemLike icon, String name, @Nullable ResourceLocation background, FrameType frameType, boolean showToast, boolean announceChat, boolean hidden)
    {
        return Advancement.Builder.advancement().display(icon, new TranslatableComponent("advancements." + Beyond.MOD_ID + "." + name + ".title"), new TranslatableComponent("advancements." + Beyond.MOD_ID + "." + name + ".description"), background, frameType, showToast, announceChat, hidden);
    }

    private static Advancement.Builder get(ItemStack icon, String name, @Nullable ResourceLocation background, FrameType frameType, boolean showToast, boolean announceChat, boolean hidden)
    {
        return Advancement.Builder.advancement().display(icon, new TranslatableComponent("advancements." + Beyond.MOD_ID + "." + name + ".title"), new TranslatableComponent("advancements." + Beyond.MOD_ID + "." + name + ".description"), background, frameType, showToast, announceChat, hidden);
    }

    private static Advancement save(Advancement.Builder builder, String name, Consumer<Advancement> consumer)
    {
        Advancement advancement = builder.build(new ResourceLocation(Beyond.MOD_ID, Beyond.MOD_ID + "/" + name));
        consumer.accept(advancement);
        return advancement;
    }

    private static class BeyondAdvancements implements Consumer<Consumer<Advancement>>
    {
        @Override
        public void accept(Consumer<Advancement> consumer)
        {
            Advancement root = save(get(BeyondBlocks.ROCKET_THRUSTER.get(), "root", new ResourceLocation(Beyond.MOD_ID, "textures/gui/advancements/backgrounds/" + Beyond.MOD_ID + ".png"), FrameType.TASK, false, false, false).addCriterion("has_rocket_thruster", InventoryChangeTrigger.TriggerInstance.hasItems(BeyondBlocks.ROCKET_THRUSTER.get())), "root", consumer);
            Advancement launchRocket = save(get(BeyondBlocks.ROCKET_CONTROLLER.get(), "launch_rocket", null, FrameType.TASK, true, true, false).parent(root).addCriterion("launch_rocket", LaunchRocketTrigger.TriggerInstance.launch(false)), "launch_rocket", consumer);
            Advancement atlas = save(get(getEarth(), "atlas", null, FrameType.CHALLENGE, true, true, true).parent(launchRocket).addCriterion("rocket_too_large", LaunchRocketTrigger.TriggerInstance.launch(true, Level.OVERWORLD)), "atlas", consumer);
            Advancement moonTravel = save(get(BeyondBlocks.MOON_ROCK.get(), "moon_travel", null, FrameType.TASK, true, true, false).parent(launchRocket).addCriterion("moon_travel", LandRocketTrigger.TriggerInstance.land(BeyondDimensions.MOON)), "moon_travel", consumer);
        }

        private static ItemStack getEarth()
        {
            ItemStack stack = new ItemStack(Items.PLAYER_HEAD);

            GameProfile profile = new GameProfile(UUID.fromString("e3ae97fb-b688-4dfd-8ee6-247790f22ecd"), null);
            profile.getProperties().put("textures", new Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTI4OWQ1YjE3ODYyNmVhMjNkMGIwYzNkMmRmNWMwODVlODM3NTA1NmJmNjg1YjVlZDViYjQ3N2ZlODQ3MmQ5NCJ9fX0="));

            CompoundTag skullOwnerNbt = new CompoundTag();
            NbtUtils.writeGameProfile(skullOwnerNbt, profile);
            stack.getOrCreateTag().put("SkullOwner", skullOwnerNbt);

            return stack;
        }
    }
}
