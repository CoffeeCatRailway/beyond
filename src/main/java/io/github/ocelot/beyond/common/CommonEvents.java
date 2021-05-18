package io.github.ocelot.beyond.common;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.world.space.DimensionSpaceSettings;
import io.github.ocelot.beyond.common.world.space.DimensionSpaceSettingsManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Beyond.MOD_ID)
public class CommonEvents
{
    private static final UUID GRAVITY_ID = UUID.fromString("E35698B9-EF2C-4312-AE44-2CB0A59D1F17");

    @SubscribeEvent
    public static void onEvent(LivingEvent.LivingUpdateEvent event)
    {
        LivingEntity entity = event.getEntityLiving();
        Level level = entity.level;

        AttributeInstance attribute = entity.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
        if (!entity.isNoGravity() && attribute != null)
        {
            DimensionSpaceSettings settings = DimensionSpaceSettingsManager.get(level.isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER).getSettings(level.dimension().location());
            AttributeModifier modifier = attribute.getModifier(GRAVITY_ID);
            if (settings.getGravityMultiplier() == 1.0)
            {
                if (modifier != null)
                    attribute.removeModifier(modifier);
                return;
            }
            if (settings.getGravityMultiplier() < 1.0)
                entity.fallDistance = 0.0F;
            if (modifier != null && modifier.getAmount() == settings.getGravityMultiplier())
                return;
            if (settings.getGravityMultiplier() != 1.0)
            {
                attribute.removeModifier(GRAVITY_ID);
                attribute.addTransientModifier(new AttributeModifier(GRAVITY_ID, "Gravity acceleration reduction", -(1.0 - settings.getGravityMultiplier()), AttributeModifier.Operation.MULTIPLY_BASE));
            }
        }
    }
}
