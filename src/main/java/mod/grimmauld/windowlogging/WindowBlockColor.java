package mod.grimmauld.windowlogging;

import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class WindowBlockColor {
	private WindowBlockColor() {}

	public static void registerFor(WindowInABlockBlock block) {
		BlockColorRegistry.register(WindowBlockColor::collectTints, block);
	}

	private static void collectTints(BlockState state, BlockAndTintGetter level, BlockPos pos, IntList tintValues) {
		if (!(state.getBlock() instanceof WindowInABlockBlock wbb))
			return;

		BlockState surrounding = wbb.getSurroundingBlockState(level, pos);
		List<BlockTintSource> sources = Minecraft.getInstance().getBlockColors().getTintSources(surrounding);
		tintValues.size(sources.size());
		for (int i = 0; i < sources.size(); i++) {
			tintValues.set(i, sources.get(i).colorInWorld(surrounding, level, pos));
		}
	}
}
