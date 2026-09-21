package mod.grimmauld.windowlogging;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockgetter.v2.FabricBlockGetter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
@MethodsReturnNonnullByDefault
public class WindowInABlockModel implements BlockStateModel {
	private final BlockStateModel wrapped;

	public WindowInABlockModel(BlockStateModel wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
		wrapped.collectParts(random, parts);
	}

	@Override
	public Material.Baked particleMaterial() {
		return wrapped.particleMaterial();
	}

	@Override
	public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		WindowInABlockTileEntity wte = renderDataOf(level, pos);
		if (wte == null || wte.hoveredBlock.is(Blocks.AIR))
			return wrapped.particleMaterial(level, pos, state);
		return modelOf(wte.hoveredBlock).particleMaterial(level, pos, state);
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state,
	                      RandomSource random, Predicate<@Nullable Direction> cullTest) {
		WindowInABlockTileEntity wte = renderDataOf(level, pos);
		if (wte == null) {
			wrapped.emitQuads(emitter, level, pos, state, random, cullTest);
			return;
		}

		BlockState partialState = wte.getPartialBlock();
		BlockState windowState = wte.getWindowBlock();

		BlockStateModel partialModel = modelOf(partialState);
		BlockStateModel windowModel = modelOf(windowState);

		random.setSeed(state.getSeed(pos));
		partialModel.emitQuads(emitter, level, pos, partialState, random, cullTest);

		random.setSeed(state.getSeed(pos));
		emitter.pushTransform(quad -> {
			Direction face = quad.lightFace();
			if (hasSolidSide(partialState, level, pos, face))
				return false;
			fightZfighting(quad, face);
			return true;
		});
		windowModel.emitQuads(emitter, level, pos, windowState, random, cullTest);
		emitter.popTransform();
	}

	@Override
	@Nullable
	public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
		WindowInABlockTileEntity wte = renderDataOf(level, pos);
		if (wte == null)
			return wrapped.createGeometryKey(level, pos, state, random);

		BlockStateModel partialModel = modelOf(wte.getPartialBlock());
		BlockStateModel windowModel = modelOf(wte.getWindowBlock());

		random.setSeed(state.getSeed(pos));
		Object partialKey = partialModel.createGeometryKey(level, pos, wte.getPartialBlock(), random);
		random.setSeed(state.getSeed(pos));
		Object windowKey = windowModel.createGeometryKey(level, pos, wte.getWindowBlock(), random);

		if (partialKey == null || windowKey == null)
			return null;

		return new GeometryKey(partialKey, windowKey);
	}

	@Override
	public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
		WindowInABlockTileEntity wte = renderDataOf(level, pos);
		if (wte == null || wte.hoveredBlock.is(Blocks.AIR))
			return wrapped.particleMaterial(level, pos, state, random);
		return modelOf(wte.hoveredBlock).particleMaterial(level, pos, state, random);
	}

	@Override
	@BakedQuad.MaterialFlags
	public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
		WindowInABlockTileEntity wte = renderDataOf(level, pos);
		if (wte == null)
			return wrapped.materialFlags(level, pos, state, random);
		return modelOf(wte.getPartialBlock()).materialFlags(level, pos, wte.getPartialBlock(), random)
				| modelOf(wte.getWindowBlock()).materialFlags(level, pos, wte.getWindowBlock(), random);
	}

	@Override
	public @BakedQuad.MaterialFlags int materialFlags() {
		return wrapped.materialFlags() | BakedQuad.FLAG_ANIMATED | BakedQuad.FLAG_TRANSLUCENT;
	}

	private static BlockStateModel modelOf(BlockState state) {
		return net.minecraft.client.Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
	}

	private static void fightZfighting(MutableQuadView quad, Direction dir) {
		float dx = dir.getStepX() / 512f;
		float dy = dir.getStepY() / 512f;
		float dz = dir.getStepZ() / 512f;
		for (int v = 0; v < 4; ++v)
			quad.pos(v, quad.x(v) - dx, quad.y(v) - dy, quad.z(v) - dz);
	}

	private static boolean hasSolidSide(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		return !state.is(BlockTags.LEAVES) && Block.isFaceFull(state.getBlockSupportShape(world, pos), side);
	}

	@Nullable
	private static WindowInABlockTileEntity renderDataOf(BlockAndTintGetter blockView, BlockPos pos) {
		if (!(blockView instanceof FabricBlockGetter view))
			return null;
		Object data = view.getBlockEntityRenderData(pos);
		return data instanceof WindowInABlockTileEntity wte ? wte : null;
	}

	private record GeometryKey(Object partial, Object window) {}
}
