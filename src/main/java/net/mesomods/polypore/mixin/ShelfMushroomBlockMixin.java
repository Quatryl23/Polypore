package net.mesomods.polypore.mixin;


import net.mesomods.polypore.PolyporeItems;
import net.mesomods.polypore.ShelfMushroomBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(ShelfMushroomBlock.class)
public abstract class ShelfMushroomBlockMixin extends HorizontalDirectionalBlock implements EntityBlock, ShelfMushroomBlockEntity.ShelfMushroomBlockMixin {

    @Shadow
    @Final
    private static List<Map<Direction, VoxelShape>> SHAPES;

    @Shadow
    @Final
    public static IntegerProperty AGE;

    protected ShelfMushroomBlockMixin(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new ShelfMushroomBlockEntity(pos, state);
    }

    @Unique
    public VoxelShape polypore$getUnoccupiedShape(BlockState state) {
        return SHAPES.get(state.getValue(AGE)).get(state.getValue(FACING));
    }

    @Inject(method = "createBlockStateDefinition", at = @At("HEAD"))
    protected void modifyBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(BlockStateProperties.LEVEL);
    }

    @Inject(method = "getShape", at = @At("RETURN"), cancellable = true)
    protected void getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ShelfMushroomBlockEntity shelfMushroomBlockEntity) {
            cir.setReturnValue(shelfMushroomBlockEntity.shape);
        }

    }

    @Inject(method = "performBonemeal", at = @At("HEAD"))
    protected void updateShapeAfterBonemealGrowth(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source, CallbackInfo ci) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ShelfMushroomBlockEntity shelfMushroomBlockEntity) {
            shelfMushroomBlockEntity.updateShapeOnAge(state.setValue(AGE, state.getValue(AGE) + 1));
        }
    }

    @Override
    protected @NonNull InteractionResult useItemOn(@NonNull ItemStack itemStack, BlockState state, @NonNull Level level,
                                                   BlockPos pos, @NonNull Player player, @NonNull InteractionHand hand, BlockHitResult hitResult) {
        Direction direction = state.getValue(ShelfMushroomBlock.FACING);
        Vec3 relativeHitLocation = hitResult.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
        double value;
        if (direction.getAxis() == Direction.Axis.X) {
            value = relativeHitLocation.z;
        } else {
            value = relativeHitLocation.x;
        }
        boolean hitRight = value > 0.5 ^ direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE ^ direction.getAxis() == Direction.Axis.X;
        Inventory inventory = player.getInventory();
        if ((itemStack.is(Items.BONE_MEAL) && state.getValue(AGE) == 0) || hand == InteractionHand.OFF_HAND) return InteractionResult.PASS;
        if (level.getBlockEntity(pos) instanceof ShelfMushroomBlockEntity smbe) {
            if (smbe.hasSecondItem()) {
                if (hitRight) {
                    itemStack = polypore$interactWithStoredItem(itemStack, true, smbe, false, true);
                } else {
                    itemStack = polypore$interactWithStoredItem(itemStack, false, smbe, false, false);
                }
            } else if (smbe.hasFirstItem()) {
                itemStack = polypore$interactWithStoredItem(itemStack, false, smbe, true, hitRight);
            } else {
                if (smbe.canPlaceItem(0, itemStack)) {
                    smbe.setFirstItem(itemStack.split(1));
                }
            }
            if (itemStack == null) return InteractionResult.PASS;
            inventory.setItem(inventory.getSelectedSlot(), itemStack);
            inventory.setChanged();
            smbe.updateShape();
            smbe.updateLightLevel();
            level.playSound(null, pos, SoundType.SHELF_MUSHROOM.getStepSound(), SoundSource.BLOCKS);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Unique
    @Nullable
    protected ItemStack polypore$interactWithStoredItem(ItemStack inHand, boolean isSecond, ShelfMushroomBlockEntity smbe, boolean hasEmptySlot, boolean hitRight) {
        ItemStack stored = isSecond ? smbe.getSecondItem() : smbe.getFirstItem();
        if (stored.is(Items.FLOWER_POT)) {
            Block filledPot = FlowerPotBlockAccessor.getPOTTED_BY_CONTENT().get(Block.byItem(inHand.getItem()));
            if (filledPot != null && filledPot != Blocks.FLOWER_POT) {
                if (isSecond) {
                    smbe.setSecondItem(new ItemStack(BuiltInRegistries.ITEM.getValue(BuiltInRegistries.BLOCK.getKey(filledPot)), 1));
                } else {
                    smbe.setFirstItem(new ItemStack(BuiltInRegistries.ITEM.getValue(BuiltInRegistries.BLOCK.getKey(filledPot)), 1));
                }
                inHand.shrink(1);
                return inHand;
            }
        }
        if (PolyporeItems.POTTED_FLOWERS.contains(stored.getItem()) && BuiltInRegistries.BLOCK.getValue(BuiltInRegistries.ITEM.getKey(stored.getItem())) instanceof FlowerPotBlock b) {
            Item flowerItem = b.getPotted().asItem();
            if (inHand.isEmpty()) {
                if (isSecond) {
                    smbe.setSecondItem(new ItemStack(Items.FLOWER_POT, 1));
                } else {
                    smbe.setFirstItem(new ItemStack(Items.FLOWER_POT, 1));
                }
                return new ItemStack(flowerItem, 1);
            } else if (inHand.count() == 1 && !inHand.is(flowerItem)) {
                Block filledPot = FlowerPotBlockAccessor.getPOTTED_BY_CONTENT().get(Block.byItem(inHand.getItem()));
                if (filledPot != null && filledPot != Blocks.FLOWER_POT) {
                    if (isSecond) {
                        smbe.setSecondItem(new ItemStack(BuiltInRegistries.ITEM.getValue(BuiltInRegistries.BLOCK.getKey(filledPot)), 1));
                    } else {
                        smbe.setFirstItem(new ItemStack(BuiltInRegistries.ITEM.getValue(BuiltInRegistries.BLOCK.getKey(filledPot)), 1));
                    }
                    return new ItemStack(flowerItem, 1);
                }
            }
            if (hasEmptySlot && smbe.canPlaceItem(1, inHand)) {
                if (hitRight) {
                    smbe.setSecondItem(inHand.split(1));
                } else {
                    smbe.setSecondItem(smbe.getFirstItem());
                    smbe.setFirstItem(inHand.split(1));
                }
                return inHand;
            }
            return null;
        }
        if (inHand.isEmpty()) {
            ItemStack item;
            if (isSecond) {
                item = smbe.getSecondItem();
                smbe.setSecondItem(ItemStack.EMPTY);
            } else {
                if (hasEmptySlot) {
                    item = smbe.getFirstItem();
                    smbe.setFirstItem(ItemStack.EMPTY);
                } else {
                    item = smbe.getFirstItem();
                    smbe.setFirstItem(smbe.getSecondItem());
                    smbe.setSecondItem(ItemStack.EMPTY);
                }

            }
            return item;
        } else if (hasEmptySlot && smbe.canPlaceItem(1, inHand)) {
            if (hitRight) {
                smbe.setSecondItem(inHand.split(1));
            } else {
                smbe.setSecondItem(smbe.getFirstItem());
                smbe.setFirstItem(inHand.split(1));
            }
            return inHand;
        } else if (inHand.count() == 1 && !inHand.is(stored.getItem())) {
            if (isSecond) {
                smbe.setSecondItem(inHand);
            } else {
                smbe.setFirstItem(inHand);
            }
            return stored;
        }
        return null;
    }
}
