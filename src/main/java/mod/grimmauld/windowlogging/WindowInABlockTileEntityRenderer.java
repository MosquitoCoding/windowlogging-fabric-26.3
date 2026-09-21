package mod.grimmauld.windowlogging;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WindowInABlockTileEntityRenderer implements BlockEntityRenderer<WindowInABlockTileEntity, WindowInABlockRenderState> {
	private final BlockEntityRendererProvider.Context context;

	public WindowInABlockTileEntityRenderer(BlockEntityRendererProvider.Context context) {
		this.context = context;
	}

	@Override
	public WindowInABlockRenderState createRenderState() {
		return new WindowInABlockRenderState();
	}

	@Override
	public void extractRenderState(WindowInABlockTileEntity blockEntity, WindowInABlockRenderState state,
	                               float partialTick, Vec3 cameraPos,
	                               ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
		BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
		state.partialBlockEntity = blockEntity.getPartialBlockTileEntityIfPresent();
		state.partialTick = partialTick;
	}

	@Override
	public void submit(WindowInABlockRenderState state, PoseStack poseStack,
	                   SubmitNodeCollector submitNodeCollector,
	                   CameraRenderState cameraRenderState) {
		BlockEntity partialTE = state.partialBlockEntity;
		if (partialTE == null)
			return;

		BlockEntityRenderer<?, ?> renderer = context.blockEntityRenderDispatcher().getRenderer(partialTE);
		if (renderer == null)
			return;

		try {
			submitNested(renderer, partialTE, state.partialTick, poseStack, submitNodeCollector, cameraRenderState);
		} catch (Exception ignored) {
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends BlockEntity, S extends BlockEntityRenderState> void submitNested(
			BlockEntityRenderer<?, ?> renderer, BlockEntity partialTE, float partialTick,
			PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
			CameraRenderState cameraRenderState) {
		BlockEntityRenderer<T, S> typedRenderer = (BlockEntityRenderer<T, S>) renderer;
		T typedTE = (T) partialTE;
		S nestedState = typedRenderer.createRenderState();
		typedRenderer.extractRenderState(
				typedTE, nestedState, partialTick,
				Vec3.atCenterOf(partialTE.getBlockPos()), null);
		typedRenderer.submit(nestedState, poseStack, submitNodeCollector, cameraRenderState);
	}
}
