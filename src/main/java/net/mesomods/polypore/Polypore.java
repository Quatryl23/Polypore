package net.mesomods.polypore;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

public class Polypore implements ModInitializer {
	public static final String MOD_ID = "polypore";

	@Override
	public void onInitialize() {
		PolyporeBlockEntities.initialize();
		PolyporeItems.initialize();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
