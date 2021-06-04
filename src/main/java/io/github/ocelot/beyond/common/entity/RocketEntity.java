package io.github.ocelot.beyond.common.entity;

import io.github.ocelot.beyond.common.blockentity.RocketControllerBlockEntity;
import io.github.ocelot.beyond.common.init.BeyondEntities;
import io.github.ocelot.beyond.common.rocket.LaunchContext;
import io.github.ocelot.beyond.common.rocket.RocketComponent;
import io.github.ocelot.beyond.event.ReloadRenderersEvent;
import io.github.ocelot.sonar.client.render.StructureTemplateRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Ocelot
 */
public class RocketEntity extends Entity implements IEntityAdditionalSpawnData
{
    private final Map<BlockPos, BlockState> components;
    private LaunchContext ctx;
    private EntityDimensions dimensions;
    private final Map<UUID, Vec3> players;

    @OnlyIn(Dist.CLIENT)
    private StructureTemplateRenderer templateRenderer;

    public RocketEntity(Level level, LaunchContext ctx, Map<UUID, Vec3> players)
    {
        super(BeyondEntities.ROCKET.get(), level);
        this.components = new HashMap<>();
        this.ctx = ctx;
        this.recalculateDimensions();
        this.players = players;
        this.noCulling = true;
        this.locateComponents();
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void recalculateDimensions()
    {
        Vec3i size = this.ctx.getTemplate().getSize();
        this.dimensions = EntityDimensions.fixed(Math.min(size.getX(), size.getZ()), size.getY());
        this.refreshDimensions();
    }

    private void locateComponents()
    {
        List<StructureTemplate.Palette> blockInfos = RocketControllerBlockEntity.getPalettes(this.ctx.getTemplate());
        for (StructureTemplate.Palette palette : blockInfos)
        {
            for (StructureTemplate.StructureBlockInfo info : palette.blocks())
            {
                if (info.state.getBlock() instanceof RocketComponent)
                    this.components.put(info.pos, info.state);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void freeRenderer()
    {
        if (this.templateRenderer != null)
        {
            this.templateRenderer.free();
            this.templateRenderer = null;
        }
    }

    @SubscribeEvent
    public void onReloadRenderers(ReloadRenderersEvent event)
    {
        this.freeRenderer();
    }

    @Override
    protected void defineSynchedData()
    {
    }

    @Override
    public void tick()
    {
        super.tick();

        this.setDeltaMovement(0, this.ctx.getThrust() / 20.0F, 0);
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (this.level.isClientSide())
        {
            for (Map.Entry<BlockPos, BlockState> entry : this.components.entrySet())
            {
                if (entry.getValue().getBlock() instanceof RocketComponent)
                {
                    BlockPos pos = entry.getKey();
                    Vec3i size = this.ctx.getTemplate().getSize();
                    ((RocketComponent) entry.getValue().getBlock()).addParticles(this.level, entry.getValue(), this.getX() - size.getX() / 2.0 + pos.getX(), this.getY() + pos.getY(), this.getZ() - size.getZ() / 2.0 + pos.getZ());
                }
            }
        }

//        this.setDeltaMovement(this.getDeltaMovement().add(0, 1.0F / 20.0F, 0));
    }

    @Override
    protected boolean canAddPassenger(Entity entity)
    {
        return this.players.containsKey(entity.getUUID());
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity entity)
    {
        if (this.players.containsKey(entity.getUUID()))
            return this.players.get(entity.getUUID()).add(this.getX(), this.getY() + 0.1, this.getZ());
        return this.players.getOrDefault(entity.getUUID(), super.getDismountLocationForPassenger(entity));
    }

    @Override
    public void positionRider(Entity passenger)
    {
        if (this.hasPassenger(passenger))
        {
            if (this.players.containsKey(passenger.getUUID()))
            {
                Vec3 pos = this.players.get(passenger.getUUID());
                passenger.setPos(this.getX() + pos.x(), this.getY() + pos.y(), this.getZ() + pos.z());
            }
            else
            {
                super.positionRider(passenger);
            }
        }
    }

    @Override
    public boolean shouldRiderSit()
    {
        return false;
    }

    @Override
    public boolean shouldRender(double x, double y, double z)
    {
        return true;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose)
    {
        return dimensions;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {
        this.ctx = LaunchContext.CODEC.parse(NbtOps.INSTANCE, nbt.get("LaunchData")).result().orElse(LaunchContext.DUMMY);
        this.recalculateDimensions();
        this.locateComponents();

        this.players.clear();
        ListTag playersNbt = nbt.getList("Players", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < playersNbt.size(); i++)
        {
            CompoundTag playerNbt = playersNbt.getCompound(i);
            this.players.put(playerNbt.getUUID("UUID"), new Vec3(playerNbt.getDouble("X"), playerNbt.getDouble("Y"), playerNbt.getDouble("Z")));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {
        nbt.put("LaunchData", LaunchContext.CODEC.encodeStart(NbtOps.INSTANCE, this.ctx).getOrThrow(false, LOGGER::error));

        ListTag playersNbt = new ListTag();
        this.players.forEach((id, pos) ->
        {
            CompoundTag playerNbt = new CompoundTag();
            playerNbt.putUUID("UUID", id);
            playerNbt.putDouble("X", pos.x());
            playerNbt.putDouble("Y", pos.y());
            playerNbt.putDouble("Z", pos.z());
            playersNbt.add(playerNbt);
        });
        nbt.put("Players", playersNbt);
    }

    @Override
    public Packet<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buf)
    {
        try
        {
            buf.writeWithCodec(LaunchContext.CODEC, this.ctx);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        buf.writeVarInt(this.players.size());
        for (Map.Entry<UUID, Vec3> entry : this.players.entrySet())
        {
            buf.writeUUID(entry.getKey());
            buf.writeDouble(entry.getValue().x());
            buf.writeDouble(entry.getValue().y());
            buf.writeDouble(entry.getValue().z());
        }
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buf)
    {
    }

    @Override
    public void onRemovedFromWorld()
    {
        super.onRemovedFromWorld();
        if (this.level.isClientSide())
            this.freeRenderer();
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @OnlyIn(Dist.CLIENT)
    public StructureTemplateRenderer getRenderer()
    {
        if (this.templateRenderer == null)
            this.templateRenderer = new StructureTemplateRenderer(CompletableFuture.completedFuture(this.ctx.getTemplate()), (pos, colorResolver) -> colorResolver.getColor(this.level.getBiome(pos), pos.getX(), pos.getZ()));
        return this.templateRenderer;
    }
}
