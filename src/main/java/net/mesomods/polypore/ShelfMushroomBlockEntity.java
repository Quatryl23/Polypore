package net.mesomods.polypore;

import com.mojang.math.OctahedralGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ListBackedContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShelfMushroomBlockEntity extends BlockEntity implements ListBackedContainer, ItemOwner {
    private final NonNullList<ItemStack> items;
    private VoxelShape itemShape;
    public VoxelShape shape;

    public ShelfMushroomBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(PolyporeBlockEntities.SHELF_MUSHROOM_BLOCK_ENTITY, worldPosition, blockState);
        ShelfMushroomBlockMixin shelfBlock = (ShelfMushroomBlockMixin) this.getBlockState().getBlock();
        this.shape = shelfBlock.polypore$getUnoccupiedShape(this.getBlockState());
        this.itemShape = Shapes.empty();
        items = NonNullList.withSize(2, ItemStack.EMPTY);
    }

    @Override
    public boolean canPlaceItem(final int slot, final @NonNull ItemStack itemStack) {
        return ListBackedContainer.super.canPlaceItem(slot, itemStack) && (slot == 0 || this.getBlockState().getValue(ShelfMushroomBlock.AGE) == 1);
    }

    @Override
    public int getMaxStackSize(@NonNull ItemStack itemStack) {
        return 1;
    }

    public ItemStack getFirstItem() {
        return this.getItem(0);
    }

    public ItemStack getSecondItem() {
        return this.getItem(1);
    }

    public boolean hasFirstItem() {
        return !this.getFirstItem().isEmpty();
    }

    public boolean hasSecondItem() {
        return !this.getSecondItem().isEmpty();
    }

    public void setFirstItem(ItemStack stack) {
        this.setItem(0, stack);
    }

    public void setSecondItem(ItemStack stack) {
        this.setItem(1, stack);
    }

    public void updateShapeOnAge(BlockState newBlockState) {
        this.itemShape = itemShape.move(0.125, 0, 0);
        joinShapes(newBlockState);
        this.setChanged();
    }

    private void joinShapes(BlockState state) {
        this.shape = Shapes.join(((ShelfMushroomBlockMixin) state.getBlock()).polypore$getUnoccupiedShape(state), this.itemShape, BooleanOp.OR);
    }

    public void updateLightLevel() {
        if (level == null) return;
        int i = 0;
        for (ItemStack stack : this.items) {
            if (stack.getItem() instanceof BlockItem b) {
                i += b.getBlock().defaultBlockState().getLightEmission();
            }
        }
        int lightLevel = Math.min(15, Math.round(i * 0.5f));
        level.setBlock(this.worldPosition, getBlockState().setValue(BlockStateProperties.LEVEL, lightLevel), 3);
    }

    public void updateShape() {
        ShelfMushroomBlockMixin shelfBlock = (ShelfMushroomBlockMixin) this.getBlockState().getBlock();
        this.itemShape = Shapes.empty();
        this.shape = shelfBlock.polypore$getUnoccupiedShape(this.getBlockState());
        if (this.getBlockState().getValue(ShelfMushroomBlock.AGE) == 1) {
            if (this.hasSecondItem()) {
                VoxelShape firstHitbox = getShape(getFirstItem(), new Vector3f(0.1875f, 0.6875f - getYOffset(shape), 0.1875f));
                VoxelShape secondHitbox = getShape(getSecondItem(), new Vector3f(-0.1875f, 0.6875f - getYOffset(shape), 0.1875f));
                this.itemShape = Shapes.join(firstHitbox, secondHitbox, BooleanOp.OR);

            } else {
                this.itemShape = getShape(getFirstItem(), new Vector3f(0, 0.6875f - getYOffset(shape), 0.1875f));
            }

        } else {
            this.itemShape = getShape(getFirstItem(), new Vector3f(0, 0.8125f - getYOffset(shape), 0.3125f));
        }
        this.shape = Shapes.join(this.shape, this.itemShape, BooleanOp.OR);
        this.setChanged();
    }

    private VoxelShape getShape(ItemStack stack, Vector3f translation) {
        Item item = stack.getItem();
        if (item instanceof BlockItem blockItem && this.level != null) {
            Block block = blockItem.getBlock();
            BlockState state = block.defaultBlockState();
            VoxelShape shape = block instanceof TransparentBlock ? state.getCollisionShape(this.level, getBlockPos()) : getVisualShape(state);
            if (Block.isShapeFullBlock(shape) || block instanceof SlabBlock || block instanceof CopperGolemStatueBlock
                    || block instanceof ChestBlock || block instanceof TrapDoorBlock || block instanceof CarpetBlock
                    || block instanceof SnowLayerBlock || block instanceof MossyCarpetBlock || block instanceof AzaleaBlock
                    || block instanceof CactusBlock || block instanceof ChorusFlowerBlock || block instanceof CauldronBlock
                    || block instanceof ChorusPlantBlock || block instanceof DriedGhastBlock || block instanceof SculkSensorBlock
                    || block instanceof SculkShriekerBlock || block instanceof EndRodBlock || block instanceof StonecutterBlock
                    || block instanceof ComposterBlock || block instanceof EnchantingTableBlock || block instanceof ScaffoldingBlock
                    || block instanceof LightningRodBlock || block instanceof DecoratedPotBlock || block instanceof LecternBlock
                    || block instanceof DragonEggBlock || block instanceof DaylightDetectorBlock) {
                return transformBlockShape(shape, translation, ItemTransforms.BLOCK);
            } else if (block instanceof StairBlock) {
                return transformBlockShape(shape, translation, ItemTransforms.STAIR_BLOCK);
            } else if (block instanceof FenceBlock) {
                return transformBlockShape(getVisualShape(state.setValue(FenceBlock.WEST, true).setValue(FenceBlock.EAST, true)), translation, ItemTransforms.BLOCK);
            } else if (block instanceof ShelfBlock) {
                return transformBlockShape(shape, translation, ItemTransforms.SHELF_BLOCK);
            } else if (block instanceof SkullBlock) {
                ItemTransforms transforms = (block instanceof PlayerHeadBlock || block.equals(Blocks.ZOMBIE_HEAD)) ? ItemTransforms.SPECIAL_MOB_HEAD : block.equals(Blocks.DRAGON_HEAD) ? ItemTransforms.DRAGON_HEAD : ItemTransforms.MOB_HEAD;
                return transformBlockShape(shape, translation, transforms);
            } else if (block instanceof GrindstoneBlock) {
                return transformBlockShape(getVisualShape(state.setValue(GrindstoneBlock.FACE, AttachFace.FLOOR)), translation, ItemTransforms.BLOCK);
            } else if (block instanceof AnvilBlock) {
                return transformBlockShape(getVisualShape(state.setValue(AnvilBlock.FACING, Direction.WEST)), translation, ItemTransforms.BLOCK);
            } else if (block instanceof FlowerPotBlock) {
                return transformBlockShape(shape, translation, ItemTransforms.FLOWER_POT);
            } else if (block instanceof LanternBlock) {
                return transformBlockShape(shape, translation, ItemTransforms.LANTERN);
            } else if (block instanceof WallBlock) {
                return transformBlockShape(shape, translation, ItemTransforms.WALL);
            } else if (block instanceof BedBlock || block instanceof StrawBedBlock) {
                boolean strawBed = block instanceof StrawBedBlock;
                VoxelShape headShape = transformBlockShape(getVisualShape(state.setValue(BedBlock.PART, BedPart.HEAD)), translation, strawBed ? ItemTransforms.STRAW_BED_HEAD : ItemTransforms.BED_HEAD);
                VoxelShape footShape = transformBlockShape(getVisualShape(state.setValue(BedBlock.PART, BedPart.FOOT)), translation, strawBed ? ItemTransforms.STRAW_BED_FOOT : ItemTransforms.BED_FOOT);
                return Shapes.join(headShape, footShape, BooleanOp.OR);
            } else if (block instanceof ShelfMushroomBlock) {
                return transformBlockShape(shape, translation, ItemTransforms.SHELF_MUSHROOM);
            } else if (block instanceof HeavyCoreBlock) {
                return transformBlockShape(shape, translation, ItemTransforms.HEAVY_CORE);
            } else if (block instanceof ConduitBlock) {
                return transformBlockShape(shape, translation, ItemTransforms.CONDUIT);
            } else if (block instanceof BigDripleafBlock) {
                return transformBlockShape(shape, translation, ItemTransforms.BIG_DRIPLEAF);
            }
        } else if (PolyporeItems.POTTED_FLOWERS.contains(item)) {
            return transformBlockShape(getVisualShape(Blocks.FLOWER_POT.defaultBlockState()), translation, ItemTransforms.FLOWER_POT);
        }
        return Shapes.empty();
    }

    private VoxelShape getVisualShape(BlockState state) {
        assert this.level != null;
        return state.getVisualShape(this.level, getBlockPos().below(1000), CollisionContext.empty());
    }

    private float getYOffset(VoxelShape shape) {
        return shape.isEmpty() ? 0 : (float) shape.bounds().minY;
    }

    private VoxelShape transformBlockShape(VoxelShape shape, Vector3f translation, ItemTransforms transforms) {
        float yRot = switch (this.getBlockState().getValue(ShelfMushroomBlock.FACING)) {
            case SOUTH -> 180;
            case EAST -> 90;
            case WEST -> 270;
            default -> 0;
        };
        return transformBlockShape(shape, new ItemTransforms(translation, new Vector3f(0, yRot, 0), new Vector3f(1f, 1f, 1f)), transforms);
    }

    private static VoxelShape transformBlockShape(VoxelShape shape, ItemTransforms blockTransforms, ItemTransforms itemTransforms) {
        Vec3 blockCenter = new Vec3(0.5f, 0.5f, 0.5f);
        shape = shape.move(blockTransforms.xTranslation, blockTransforms.yTranslation, blockTransforms.zTranslation);
        Vec3 center = blockCenter.add((float) blockTransforms.xTranslation, (float) blockTransforms.yTranslation, (float) blockTransforms.zTranslation);
        List<VoxelShape> scaledShapeParts = shape.toAabbs().stream().map(aabb -> {
            double minX = center.x - itemTransforms.xScale * (center.x - aabb.minX);
            double minY = center.y - itemTransforms.yScale * (center.y - aabb.minY);
            double minZ = center.z - itemTransforms.zScale * (center.z - aabb.minZ);
            double maxX = center.x - itemTransforms.xScale * (center.x - aabb.maxX);
            double maxY = center.y - itemTransforms.yScale * (center.y - aabb.maxY);
            double maxZ = center.z - itemTransforms.zScale * (center.z - aabb.maxZ);
            return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        }).map(Shapes::create).toList();
        shape = Shapes.empty();
        for (VoxelShape scaledShapePart : scaledShapeParts) {
            shape = Shapes.joinUnoptimized(shape, scaledShapePart, BooleanOp.OR);
        }
        shape = shape.optimize();
        shape = rotate(shape, itemTransforms.yRotation, Direction.Axis.Y, center);
        shape = rotate(shape, itemTransforms.xRotation, Direction.Axis.X, center);
        shape = rotate(shape, itemTransforms.zRotation, Direction.Axis.Z, center);
        shape = shape.move(itemTransforms.xTranslation * 0.5, itemTransforms.yTranslation * 0.5, itemTransforms.zTranslation * 0.5);
        scaledShapeParts = shape.toAabbs().stream().map(aabb -> {
            double minX = blockCenter.x - blockTransforms.xScale * (blockCenter.x - aabb.minX);
            double minY = blockCenter.y - blockTransforms.yScale * (blockCenter.y - aabb.minY);
            double minZ = blockCenter.z - blockTransforms.zScale * (blockCenter.z - aabb.minZ);
            double maxX = blockCenter.x - blockTransforms.xScale * (blockCenter.x - aabb.maxX);
            double maxY = blockCenter.y - blockTransforms.yScale * (blockCenter.y - aabb.maxY);
            double maxZ = blockCenter.z - blockTransforms.zScale * (blockCenter.z - aabb.maxZ);
            return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        }).map(Shapes::create).toList();
        shape = Shapes.empty();
        for (VoxelShape scaledShapePart : scaledShapeParts) {
            shape = Shapes.joinUnoptimized(shape, scaledShapePart, BooleanOp.OR);
        }
        shape = shape.optimize();
        shape = rotate(shape, blockTransforms.yRotation, Direction.Axis.Y);
        shape = rotate(shape, blockTransforms.xRotation, Direction.Axis.X);
        shape = rotate(shape, blockTransforms.zRotation, Direction.Axis.Z);
        return shape;
    }

    private static VoxelShape rotate(VoxelShape original, double rot, Direction.Axis axis) {
        return rotate(original, rot, axis, new Vec3(0.5, 0.5, 0.5));
    }

    private static VoxelShape rotate(VoxelShape original, double rot, Direction.Axis axis, Vec3 center) {
        rot = ((rot % 360) + 360) % 360;
        rot = Math.round(rot / 90);
        OctahedralGroup group = OctahedralGroup.IDENTITY;
        if (rot == 1) {
            group = switch (axis) {
                case X -> OctahedralGroup.BLOCK_ROT_X_90;
                case Y -> OctahedralGroup.BLOCK_ROT_Y_90;
                case Z -> OctahedralGroup.BLOCK_ROT_Z_90;
            };
        } else if (rot == 2) {
            group = switch (axis) {
                case X -> OctahedralGroup.BLOCK_ROT_X_180;
                case Y -> OctahedralGroup.BLOCK_ROT_Y_180;
                case Z -> OctahedralGroup.BLOCK_ROT_Z_180;
            };
        } else if (rot == 3) {
            group = switch (axis) {
                case X -> OctahedralGroup.BLOCK_ROT_X_270;
                case Y -> OctahedralGroup.BLOCK_ROT_Y_270;
                case Z -> OctahedralGroup.BLOCK_ROT_Z_270;
            };
        }
        return Shapes.rotate(original, group, center);
    }

    @Override @NonNull
    public NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    public void preRemoveSideEffects(final @NonNull BlockPos pos, final @NonNull BlockState state) {
        if (this.level != null) {
            for (int i = 0; i < 2; i++) {
                ItemStack stack = this.getItem(i);
                if (PolyporeItems.POTTED_FLOWERS.contains(stack.getItem())) {
                    Block flowerPot = BuiltInRegistries.BLOCK.getValue(BuiltInRegistries.ITEM.getKey(stack.getItem()));
                    if (flowerPot instanceof FlowerPotBlock b) {
                        Item flowerItem = b.getPotted().asItem();
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(flowerItem, stack.count()));
                        stack = new ItemStack(Items.FLOWER_POT, stack.count());
                    }
                }
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        saveVoxelShape(output, itemShape);
        output.putInt("facing", getBlockState().getValue(ShelfMushroomBlock.FACING).get3DDataValue());
    }

    private void saveVoxelShape(ValueOutput output, VoxelShape shape) {
        ValueOutput.ValueOutputList boxes = output.childrenList("contents_shape");
        shape.toAabbs().forEach(aabb -> {
            ValueOutput child = boxes.addChild();
            child.putDouble("min_x", aabb.minX);
            child.putDouble("min_y", aabb.minY);
            child.putDouble("min_z", aabb.minZ);
            child.putDouble("max_x", aabb.maxX);
            child.putDouble("max_y", aabb.maxY);
            child.putDouble("max_z", aabb.maxZ);
        });
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.items);
        if (getBlockState().getValue(ShelfMushroomBlock.AGE) == 0) {
            this.setSecondItem(ItemStack.EMPTY);
        }
        this.updateLightLevel();
        if (input.getIntOr("facing", -1) == getBlockState().getValue(ShelfMushroomBlock.FACING).get3DDataValue()) {
            this.updateShape();
        } else {
            VoxelShape itemShape = loadVoxelShape(input);
            if (itemShape != null) {
                this.itemShape = itemShape;
                this.joinShapes(getBlockState());
                if (this.level == null) return;
                getBlockState().updateNeighbourShapes(level, worldPosition, 3);
                level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            }
        }
    }

    private VoxelShape loadVoxelShape(ValueInput valueInput) {
        List<AABB> aabbs = new ArrayList<>();
        Optional<ValueInput.ValueInputList> shapeList = valueInput.childrenList("contents_shape");
        if (shapeList.isEmpty()) return null;
        shapeList.get().forEach(input -> aabbs.add(new AABB(
                input.getDoubleOr("min_x", 0),
                input.getDoubleOr("min_y", 0),
                input.getDoubleOr("min_z", 0),
                input.getDoubleOr("max_x", 0),
                input.getDoubleOr("max_y", 0),
                input.getDoubleOr("max_z", 0))));
        VoxelShape shape = Shapes.empty();
        for (AABB aabb : aabbs) {
            shape = Shapes.joinUnoptimized(shape, Shapes.create(aabb), BooleanOp.OR);
        }
        return shape.optimize();
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override @NonNull
    public CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override @NonNull
    public Level level() {
        assert this.level != null;
        return this.level;
    }

    @Override
    public @NonNull Vec3 position() {
        return Vec3.atCenterOf(this.getBlockPos());
    }

    @Override
    public float getVisualRotationYInDegrees() {
        return this.getBlockState().getValue(ShelfBlock.FACING).getOpposite().toYRot();
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return false;
    }

    public interface ShelfMushroomBlockMixin {
        VoxelShape polypore$getUnoccupiedShape(BlockState state);
    }

    public static class ItemTransforms {
        public static final ItemTransforms BLOCK = new ItemTransforms(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, 0f), new Vector3f(0.25f, 0.25f, 0.25f));
        public static final ItemTransforms STAIR_BLOCK = new ItemTransforms(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 90f, 0f), new Vector3f(0.25f, 0.25f, 0.25f));
        public static final ItemTransforms SHELF_BLOCK = new ItemTransforms(new Vector3f(0f, 0f, -0.25f), new Vector3f(0f, 0f, 0f), new Vector3f(0.25f, 0.25f, 0.25f));
        public static final ItemTransforms MOB_HEAD = new ItemTransforms(new Vector3f(0f, 0.25f, 0f), new Vector3f(0f, 0f, 0f), new Vector3f(0.5f, 0.5f, 0.5f));
        public static final ItemTransforms SPECIAL_MOB_HEAD = new ItemTransforms(new Vector3f(0f, 0.265f, 0f), new Vector3f(0f, 0f, 0f), new Vector3f(0.5f, 0.5f, 0.5f));
        public static final ItemTransforms DRAGON_HEAD = new ItemTransforms(new Vector3f(0f, 0.5f, 0f), new Vector3f(0f, 0f, 0f), new Vector3f(0.5f, 0.5f, 0.5f));
        public static final ItemTransforms FLOWER_POT = new ItemTransforms(new Vector3f(0f, 0.25f, 0f), new Vector3f(0f, 0f, 0f), new Vector3f(0.5f, 0.5f, 0.5f));
        public static final ItemTransforms LANTERN = new ItemTransforms(new Vector3f(0f, 0.25f, 0f), new Vector3f(0f, 0f, 0f), new Vector3f(0.5f, 0.5f, 0.5f));
        public static final ItemTransforms BED_HEAD = new ItemTransforms(new Vector3f(0f, 0.5f, -0.125f), new Vector3f(90f, 0f, 180f), new Vector3f(0.25f, 0.25f, 0.25f));
        public static final ItemTransforms BED_FOOT = new ItemTransforms(new Vector3f(0f, 0f, -0.125f), new Vector3f(90f, 0f, 180f), new Vector3f(0.25f, 0.25f, 0.25f));
        public static final ItemTransforms STRAW_BED_HEAD = new ItemTransforms(new Vector3f(0f, 0.59f, -0.1875f), new Vector3f(90f, 0f, 180f), new Vector3f(0.25f, 0.25f, 0.25f));
        public static final ItemTransforms STRAW_BED_FOOT = new ItemTransforms(new Vector3f(0f, 0.09f, -0.1875f), new Vector3f(90f, 0f, 0f), new Vector3f(0.25f, 0.25f, 0.25f));
        public static final ItemTransforms SHELF_MUSHROOM = new ItemTransforms(new Vector3f(0f, -0.25f, -0.4225f), new Vector3f(0f, 0f, 0f), new Vector3f(0.5f, 0.5f, 0.5f));
        public static final ItemTransforms CONDUIT = new ItemTransforms(new Vector3f(0f, -0.0625f, 0f), new Vector3f(0f, 0f, 0f), new Vector3f(0.5f, 0.5f, 0.5f));
        public static final ItemTransforms HEAVY_CORE = new ItemTransforms(new Vector3f(0f, 0.25f, 0f), new Vector3f(0f, 0f, 0f), new Vector3f(0.5f, 0.5f, 0.5f));
        public static final ItemTransforms WALL = new ItemTransforms(new Vector3f(0f, -0.09f, 0f), new Vector3f(0f, 0f, 0f), new Vector3f(0.25f, 0.17f, 0.25f));
        public static final ItemTransforms BIG_DRIPLEAF = new ItemTransforms(new Vector3f(0f, 0f, -0.0675f), new Vector3f(0f, 0f, 0f), new Vector3f(0.25f, 0.25f, 0.25f));
        final double xTranslation;
        final double yTranslation;
        final double zTranslation;
        final double xRotation;
        final double yRotation;
        final double zRotation;
        final double xScale;
        final double yScale;
        final double zScale;

        public ItemTransforms(Vector3fc translation, Vector3fc rotation, Vector3fc scale) {
            xTranslation = translation.x();
            yTranslation = translation.y();
            zTranslation = translation.z();
            xRotation = rotation.x();
            yRotation = rotation.y();
            zRotation = rotation.z();
            xScale = scale.x();
            yScale = scale.y();
            zScale = scale.z();
        }
    }
}