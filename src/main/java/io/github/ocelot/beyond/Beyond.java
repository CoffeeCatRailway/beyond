package io.github.ocelot.beyond;

import io.github.ocelot.beyond.client.BeyondClientRegistry;
import io.github.ocelot.beyond.common.init.*;
import io.github.ocelot.beyond.common.space.SpaceManager;
import io.github.ocelot.beyond.common.world.space.DimensionSpaceSettingsLoader;
import io.github.ocelot.beyond.datagen.*;
import io.github.ocelot.sonar.Sonar;
import io.github.ocelot.sonar.SonarModule;
import io.github.ocelot.sonar.common.util.SortedItemGroup;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Beyond.MOD_ID)
public class Beyond
{
    public static final String MOD_ID = "beyond";

    public static final SortedItemGroup TAB = new SortedItemGroup(MOD_ID)
    {
        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(BeyondBlocks.MOON_ROCK.get());
        }
    };

    public Beyond()
    {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        Sonar.init(modBus, SonarModule.INBUILT_NETWORK);
        modBus.addListener(this::init);
        modBus.addListener(this::dataSetup);
        modBus.addListener(this::attributeSetup);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
        {
            BeyondClientRegistry.init(modBus);
            modBus.addListener(BeyondClientRegistry::setup);
        });
        BeyondBlocks.BLOCKS.register(modBus);
        BeyondBlocks.BLOCK_ENTITIES.register(modBus);
        BeyondFeatures.FEATURES.register(modBus);
        BeyondItems.ITEMS.register(modBus);
        BeyondEntities.ENTITIES.register(modBus);
        BeyondWorldCarvers.CARVERS.register(modBus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onEvent(FMLServerStartedEvent event)
    {
        SpaceManager.load(event.getServer());
    }

    @SubscribeEvent
    public void onEvent(FMLServerStoppedEvent event)
    {
        SpaceManager.unload();
    }

    private void attributeSetup(EntityAttributeCreationEvent event)
    {
    }

    private void init(FMLCommonSetupEvent event)
    {
        event.enqueueWork(BeyondDimensions::init);
        BeyondMessages.init();
        BeyondTriggers.init();
    }

    private void dataSetup(GatherDataEvent event)
    {
        DataGenerator dataGenerator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        BlockTagGen blockTagGen = new BlockTagGen(dataGenerator, existingFileHelper);
        dataGenerator.addProvider(new RecipeGen(dataGenerator));
        dataGenerator.addProvider(new LootTableGen(dataGenerator));
        dataGenerator.addProvider(blockTagGen);
        dataGenerator.addProvider(new ItemTagGen(dataGenerator, blockTagGen, existingFileHelper));
        dataGenerator.addProvider(new FluidTagGen(dataGenerator, existingFileHelper));
        dataGenerator.addProvider(new EntityTypeTagGen(dataGenerator, existingFileHelper));
        dataGenerator.addProvider(new ItemModelGen(dataGenerator));
        dataGenerator.addProvider(new LanguageGen(dataGenerator));
        dataGenerator.addProvider(new ConfiguredFeatureGen(dataGenerator));
        dataGenerator.addProvider(new AdvancementGen(dataGenerator));
    }

    @SubscribeEvent
    public void onEvent(AddReloadListenerEvent event)
    {
        event.addListener(DimensionSpaceSettingsLoader.INSTANCE);
    }
}
