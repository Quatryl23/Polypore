package net.mesomods.polypore.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.mesomods.polypore.PolyporeBlockEntities;

public class PolyporeClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRenderers.register(PolyporeBlockEntities.SHELF_MUSHROOM_BLOCK_ENTITY,ShelfMushroomRenderer::new);
	}
}