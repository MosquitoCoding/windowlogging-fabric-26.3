package mod.grimmauld.windowlogging;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

@Environment(EnvType.CLIENT)
public class WindowloggingClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRenderers.register(Windowlogging.WINDOW_IN_A_BLOCK_TILE_ENTITY, WindowInABlockTileEntityRenderer::new);
		WindowBlockColor.registerFor(Windowlogging.WINDOW_IN_A_BLOCK);
		ModelLoadingPlugin.register(new WindowloggingModelLoadingPlugin());
	}
}
