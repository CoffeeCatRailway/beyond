package io.github.ocelot.beyond.common.advancement;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
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

/**
 * @author Ocelot
 */
public class LandRocketTrigger extends SimpleCriterionTrigger<LandRocketTrigger.TriggerInstance>
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation ID = new ResourceLocation(Beyond.MOD_ID, "rocket_land");

    @Override
    protected TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext context)
    {
        String dimensionId = GsonHelper.getAsString(jsonObject, "dimension");
        ResourceKey<Level> dimension = ResourceLocation.CODEC.parse(JsonOps.INSTANCE, jsonObject.get("dimension")).resultOrPartial(LOGGER::error).map((arg) -> ResourceKey.create(Registry.DIMENSION_REGISTRY, arg)).orElse(null);
        if (dimension == null)
            throw new JsonSyntaxException("Unknown dimension: " + dimensionId);
        return new TriggerInstance(composite, dimension);
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    public void trigger(ServerPlayer player)
    {
        this.trigger(player, instance -> player.level.dimension() == instance.dimension);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final ResourceKey<Level> dimension;

        public TriggerInstance(EntityPredicate.Composite composite, ResourceKey<Level> dimension)
        {
            super(ID, composite);
            this.dimension = dimension;
        }

        public static LandRocketTrigger.TriggerInstance land(ResourceKey<Level> dimension)
        {
            return new LandRocketTrigger.TriggerInstance(EntityPredicate.Composite.ANY, dimension);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context)
        {
            JsonObject jsonObject = super.serializeToJson(context);
            Level.RESOURCE_KEY_CODEC.encodeStart(JsonOps.INSTANCE, this.dimension).resultOrPartial(LOGGER::error).ifPresent((jsonElement) -> jsonObject.add("dimension", jsonElement));
            return jsonObject;
        }
    }
}
