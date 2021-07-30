package io.github.ocelot.beyond.common.init;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.entity.RocketEntity;
import io.github.ocelot.beyond.common.rocket.LaunchContext;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Ocelot
 */
public class BeyondEntities
{
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Beyond.MOD_ID);

    public static final RegistryObject<EntityType<RocketEntity>> ROCKET = ENTITIES.register("rocket", () -> EntityType.Builder.<RocketEntity>of(RocketEntity::new, MobCategory.MISC).sized(0.0F, 0.0F).fireImmune().noSummon().clientTrackingRange(10).updateInterval(100).setCustomClientFactory((spawnEntity, level) ->
    {
        FriendlyByteBuf buf = spawnEntity.getAdditionalData();
        LaunchContext ctx;
        try
        {
            ctx = buf.readWithCodec(LaunchContext.CODEC);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        Map<Integer, Vec3> players = new Int2ObjectArrayMap<>();
        int playersCount = buf.readVarInt();
        for (int i = 0; i < playersCount; i++)
            players.put(buf.readVarInt(), new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        return new RocketEntity(level, ctx, players);
    }).build(Beyond.MOD_ID + ":rocket"));
}
