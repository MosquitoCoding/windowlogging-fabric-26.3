package mod.grimmauld.windowlogging.mixin;

import mod.grimmauld.windowlogging.WindowInABlockBlock;
import mod.grimmauld.windowlogging.WindowInABlockTileEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(ClientLevel.class)
public class ClientLevelMixin {
	@ModifyVariable(
			method = "addDestroyBlockEffect",
			at = @At("HEAD"),
			argsOnly = true
	)
	private BlockState modifyDestroyBlockEffectState(BlockState state) {
		return getShapeState(state, (ClientLevel) (Object) this, null);
	}

	@ModifyVariable(
			method = "addBreakingBlockEffects",
			at = @At("STORE"),
			ordinal = 0
	)
	private BlockState modifyBreakingBlockEffectsState(BlockState state) {
		return getShapeState(state, (ClientLevel) (Object) this, null);
	}

	@Unique
	private static BlockState getShapeState(BlockState state, BlockGetter level, BlockPos pos) {
		if (state.getBlock() instanceof WindowInABlockBlock wbb) {
			if (pos != null) {
				WindowInABlockTileEntity wte = wbb.getTileEntity(level, pos);
				if (wte != null && wte.hoveredBlock != Blocks.AIR.defaultBlockState())
					return wte.hoveredBlock;
			}
		}
		return state;
	}
}
