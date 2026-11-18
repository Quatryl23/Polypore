package net.mesomods.polypore.mixin;

import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.level.block.FlowerPotBlock.class)
public interface FlowerPotBlockAccessor {
    @Accessor
    static Map<Block, Block> getPOTTED_BY_CONTENT() {
        throw new UnsupportedOperationException();
    }
}
