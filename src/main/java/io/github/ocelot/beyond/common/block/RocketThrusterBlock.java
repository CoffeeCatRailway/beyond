package io.github.ocelot.beyond.common.block;

import io.github.ocelot.sonar.common.block.BaseBlock;
import io.github.ocelot.sonar.common.util.VoxelShapeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import sun.security.provider.SHA;

import java.util.Objects;

/**
 * @author Ocelot
 */
public class RocketThrusterBlock extends BaseBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShape[] SHAPES = generateShapes();

    public RocketThrusterBlock(Properties properties)
    {
        super(properties);
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
