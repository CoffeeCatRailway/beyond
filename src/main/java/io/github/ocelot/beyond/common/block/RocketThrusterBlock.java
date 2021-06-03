package io.github.ocelot.beyond.common.block;

import io.github.ocelot.beyond.common.rocket.RocketThruster;
import io.github.ocelot.sonar.common.block.BaseBlock;
import io.github.ocelot.sonar.common.util.VoxelShapeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Objects;
import java.util.Random;

/**
 * @author Ocelot
 */
public class RocketThrusterBlock extends BaseBlock implements RocketThruster
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShape[] SHAPES = generateShapes();

    private final float thrust;

    public RocketThrusterBlock(float thrust, Properties properties)
    {
        super(properties);
        this.thrust = thrust;
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public void tickLaunch(Level level, BlockPos pos)
    {
        if (level.isClientSide())
        {
            Direction facing = level.getBlockState(pos).getValue(FACING);
            for (int i = 0; i < 2; i++)
            {
                Random random = level.getRandom();
                double randomX = (1.0 - Math.abs(facing.getStepX())) * random.nextGaussian() * 0.25;
                double randomY = (1.0 - Math.abs(facing.getStepY())) * random.nextGaussian() * 0.25;
                double randomZ = (1.0 - Math.abs(facing.getStepZ())) * random.nextGaussian() * 0.25;
                double x = pos.getX() + 0.5 + facing.getStepX() * 0.5 + randomX;
                double y = pos.getY() + 0.5 + facing.getStepY() * 0.5 + randomY;
                double z = pos.getZ() + 0.5 + facing.getStepZ() * 0.5 + randomZ;
                level.addParticle(ParticleTypes.CLOUD, true, x, y, z, facing.getStepX() * 0.5, facing.getStepY() * 0.5, facing.getStepZ() * 0.5);
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPES[state.getValue(FACING).get3DDataValue()];
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockState state = Objects.requireNonNull(super.getStateForPlacement(context));
        return state.setValue(FACING, context.getClickedFace());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Override
    public float getThrust()
    {
        return thrust;
    }

    private static VoxelShape[] generateShapes()
    {
        VoxelShapeHelper.Builder builder = new VoxelShapeHelper.Builder().append(
                box(1, 0, 1, 15, 2, 15),
                box(5, 2, 5, 11, 4, 11),
                box(3, 4, 3, 13, 9, 13),
                box(1, 9, 1, 15, 16, 15)
        ).rotate(Direction.UP);

        VoxelShape[] shapes = new VoxelShape[Direction.values().length];
        for (Direction direction : Direction.values())
            shapes[direction.get3DDataValue()] = builder.rotate(direction).build();
        return shapes;
    }
}
