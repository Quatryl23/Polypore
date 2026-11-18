package net.mesomods.polypore.mixin;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Blocks.class)
public class BlocksMixin {
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;fallDistanceReduction(F)Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;", ordinal = 0))
    private static BlockBehaviour.Properties redirectShelfBlock(BlockBehaviour.Properties instance, float fallDistanceReduction) {
        return instance.fallDistanceReduction(fallDistanceReduction).dynamicShape().lightLevel((state) -> state.getValue(BlockStateProperties.LEVEL));
    }
}
