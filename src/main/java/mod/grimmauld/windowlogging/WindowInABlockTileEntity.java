package mod.grimmauld.windowlogging;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockgetter.v2.RenderDataBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class WindowInABlockTileEntity extends BlockEntity implements RenderDataBlockEntity {
	private BlockState partialBlock = Blocks.AIR.defaultBlockState();
	private BlockState windowBlock = Blocks.AIR.defaultBlockState();
	private CompoundTag partialBlockTileData = new CompoundTag();
	private BlockEntity partialBlockTileEntity;
	@Environment(EnvType.CLIENT)
	public BlockState hoveredBlock = Blocks.AIR.defaultBlockState();

	public WindowInABlockTileEntity(BlockPos pos, BlockState blockState) {
		super(Windowlogging.WINDOW_IN_A_BLOCK_TILE_ENTITY, pos, blockState);
	}

	public CompoundTag getPartialBlockTileData() {
		return partialBlockTileData;
	}

	public void setPartialBlockTileData(CompoundTag partialBlockTileData) {
		this.partialBlockTileData = partialBlockTileData;
		this.partialBlockTileEntity = null;
	}

	@Override
	protected void loadAdditional(@NonNull ValueInput input) {
		super.loadAdditional(input);
		partialBlock = input.read("PartialBlock", BlockState.CODEC).orElse(Blocks.AIR.defaultBlockState());
		windowBlock = input.read("WindowBlock", BlockState.CODEC).orElse(Blocks.AIR.defaultBlockState());
		setPartialBlockTileData(input.read("PartialData", CompoundTag.CODEC).orElse(new CompoundTag()));
	}

	@Override
	protected void saveAdditional(@NonNull ValueOutput output) {
		super.saveAdditional(output);
		output.store("PartialBlock", BlockState.CODEC, partialBlock);
		output.store("WindowBlock", BlockState.CODEC, windowBlock);
		output.store("PartialData", CompoundTag.CODEC, partialBlockTileData);
	}

	public void updateWindowConnections() {
		if (level == null)
			return;

		for (Direction side : Direction.values()) {
			BlockPos offsetPos = worldPosition.relative(side);
			windowBlock = windowBlock.updateShape(
					level, level, worldPosition, side, offsetPos,
					level.getBlockState(offsetPos), level.getRandom());
		}

		level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2 | 16);
		setChanged();
	}

	@Override
	public Object getRenderData() {
		return this;
	}

	public void requestModelDataUpdate() {
		// Kept for compatibility with the old implementation; Fabric render data is queried directly.
	}

	public BlockState getPartialBlock() {
		return partialBlock;
	}

	public void setPartialBlock(BlockState partialBlock) {
		this.partialBlock = partialBlock;
		this.partialBlockTileEntity = null;
	}

	public BlockState getWindowBlock() {
		return windowBlock;
	}

	public void setWindowBlock(BlockState windowBlock) {
		this.windowBlock = windowBlock;
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Nullable
	public BlockEntity getPartialBlockTileEntityIfPresent() {
		if (!(partialBlock instanceof EntityBlock entityBlock) || level == null)
			return null;

		if (partialBlockTileEntity == null) {
			try {
				partialBlockTileEntity = entityBlock.newBlockEntity(worldPosition, partialBlock);
				if (partialBlockTileEntity != null) {
					partialBlockTileEntity.setBlockState(partialBlock);
					partialBlockTileEntity.loadWithComponents(
						NbtBridge.toValueInput(level.registryAccess(), partialBlockTileData));
					partialBlockTileEntity.setLevel(level);
				}
			} catch (Exception e) {
				partialBlockTileEntity = null;
			}
		}
		return partialBlockTileEntity;
	}
}
