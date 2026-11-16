package up67.rolesmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.network.NetworkHooks;
import javax.annotation.Nullable;
import up67.rolesmod.menu.UpgraderMenu;

public class UpgraderBlock extends HorizontalDirectionalBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public UpgraderBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            MenuProvider provider = new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("menu.up67.upgrader");
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory inv, Player p) {
                    return new UpgraderMenu(containerId, inv);
                }
            };
            NetworkHooks.openScreen((net.minecraft.server.level.ServerPlayer) player, provider, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
