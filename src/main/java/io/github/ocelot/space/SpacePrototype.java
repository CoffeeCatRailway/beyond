package io.github.ocelot.space;

import io.github.ocelot.sonar.Sonar;
import io.github.ocelot.sonar.SonarModule;
import io.github.ocelot.space.client.SpacePrototypeClientRegistry;
import io.github.ocelot.space.common.init.SpaceMessages;
import io.github.ocelot.space.datagen.*;
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

@Mod(SpacePrototype.MOD_ID)
public class SpacePrototype
{
    public static final String MOD_ID = "space";

    public SpacePrototype()
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
//        BattlefieldsItems.ITEMS.register(modBus);
//        BattlefieldsProps.PROPS.register(modBus);
//        BattlefieldsBlocks.BLOCKS.register(modBus);
//        BattlefieldsEffects.EFFECTS.register(modBus);
//        BattlefieldsEntities.ENTITIES.register(modBus);
//        BattlefieldsBlocks.TILE_ENTITIES.register(modBus);
//        BattlefieldsParticles.PARTICLE_TYPES.register(modBus);
//        BattlefieldsBiomes.BIOMES.register(modBus);
//        BattlefieldsSounds.SOUNDS.register(modBus);
//        BattlefieldsFluids.FLUIDS.register(modBus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void attributeSetup(EntityAttributeCreationEvent event)
    {
    }

    private void init(FMLCommonSetupEvent event)
    {
        SpaceMessages.init();
//        BattlefieldsSyncedDataKeys.init();
//        ArgumentTypes.register(Battlefields.MOD_ID + ":notification_properties", NotificationPropertiesArgumentType.class, new ArgumentSerializer<>(NotificationPropertiesArgumentType::new));
//        ArgumentTypes.register(Battlefields.MOD_ID + ":color", ColorArgumentType.class, new ArgumentSerializer<>(ColorArgumentType::new));
//        ArgumentTypes.register(Battlefields.MOD_ID + ":time", TimeArgumentType.class, new TimeArgumentType.Serializer());
//        ArgumentTypes.register(Battlefields.MOD_ID + ":enum", BattlefieldsEnumArgument.class, new BattlefieldsEnumArgument.Serializer());
//        ArgumentTypes.register(Battlefields.MOD_ID + ":minimap_widget", GameMapWidgetArgumentType.class, new ArgumentSerializer<>(GameMapWidgetArgumentType::new));
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
