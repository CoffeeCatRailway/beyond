package io.github.ocelot.beyond.common.advancement;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.github.ocelot.beyond.Beyond;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

/**
 * @author Ocelot
 */
public class LaunchRocketTrigger extends SimpleCriterionTrigger<LaunchRocketTrigger.TriggerInstance>
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation ID = new ResourceLocation(Beyond.MOD_ID, "rocket_launch");

    @Override
    protected TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext context)
    {
        boolean tooLarge = GsonHelper.getAsBoolean(jsonObject, "tooLarge");
        ResourceKey<Level> dimension = jsonObject.has("dimension") ? ResourceLocation.CODEC.parse(JsonOps.INSTANCE, jsonObject.get("dimension")).resultOrPartial(LOGGER::error).map((arg) -> ResourceKey.create(Registry.DIMENSION_REGISTRY, arg)).orElse(null) : null;
        return new TriggerInstance(composite, tooLarge, dimension);
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    public void trigger(ServerPlayer player, boolean tooLarge)
    {
        this.trigger(player, instance -> instance.tooLarge == tooLarge && (instance.dimension == null || player.level.dimension() == instance.dimension));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final boolean tooLarge;
        private final ResourceKey<Level> dimension;

        public TriggerInstance(EntityPredicate.Composite composite, boolean tooLarge, @Nullable ResourceKey<Level> dimension)
        {
            super(ID, composite);
            this.tooLarge = tooLarge;
            this.dimension = dimension;
        }

        public static LaunchRocketTrigger.TriggerInstance launch(boolean tooLarge)
        {
            return new LaunchRocketTrigger.TriggerInstance(EntityPredicate.Composite.ANY, tooLarge, null);
        }

        public static LaunchRocketTrigger.TriggerInstance launch(boolean tooLarge, ResourceKey<Level> dimension)
        {
            return new LaunchRocketTrigger.TriggerInstance(EntityPredicate.Composite.ANY, tooLarge, dimension);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context)
        {
            JsonObject jsonObject = super.serializeToJson(context);
            jsonObject.addProperty("tooLarge", this.tooLarge);
            if (this.dimension != null)
                Level.RESOURCE_KEY_CODEC.encodeStart(JsonOps.INSTANCE, this.dimension).resultOrPartial(LOGGER::error).ifPresent((jsonElement) -> jsonObject.add("dimension", jsonElement));
            return jsonObject;
        }
    }
}
