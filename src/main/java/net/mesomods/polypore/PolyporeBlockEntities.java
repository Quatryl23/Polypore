package net.mesomods.polypore;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PolyporeBlockEntities {
    public static final BlockEntityType<ShelfMushroomBlockEntity> SHELF_MUSHROOM_BLOCK_ENTITY = register("shelf_mushroom", ShelfMushroomBlockEntity::new, Blocks.SHELF_MUSHROOM);

    @SuppressWarnings("SameParameterValue")
    private static <T extends BlockEntity> BlockEntityType<T> register(String name, FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory, Block block) {
        Identifier id = Polypore.id(name);
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(entityFactory, block).build());
    }

    public static void initialize() {
    }
}
