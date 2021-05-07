package io.github.ocelot.beyond;

import io.github.ocelot.sonar.Sonar;
import io.github.ocelot.sonar.SonarModule;
import io.github.ocelot.beyond.client.SpacePrototypeClientRegistry;
import io.github.ocelot.beyond.common.init.BeyondMessages;
import io.github.ocelot.beyond.datagen.*;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Beyond.MOD_ID)
public class Beyond
{
    public static final String MOD_ID = "beyond";

    public Beyond()
    {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        Sonar.init(modBus, SonarModule.INBUILT_NETWORK);
        modBus.addListener(this::init);
        modBus.addListener(this::dataSetup);
        modBus.addListener(this::attributeSetup);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
        {
            SpacePrototypeClientRegistry.init(modBus);
            modBus.addListener(SpacePrototypeClientRegistry::setup);
        });
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void attributeSetup(EntityAttributeCreationEvent event)
    {
    }

    private void init(FMLCommonSetupEvent event)
    {
        BeyondMessages.init();
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
    }
}
