package net.mesomods.polypore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mesomods.polypore.OnShelfMushroomModels;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import net.mesomods.polypore.ShelfMushroomBlockEntity;

public class ShelfMushroomRenderer implements BlockEntityRenderer<ShelfMushroomBlockEntity, ShelfMushroomRenderState> {
    private final ItemModelResolver itemModelResolver;

    public ShelfMushroomRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override @NonNull
    public ShelfMushroomRenderState createRenderState() {
        return new ShelfMushroomRenderState();
    }

    @Override
    public void extractRenderState(@NonNull ShelfMushroomBlockEntity blockEntity, @NonNull ShelfMushroomRenderState state, float partialTicks, @NonNull Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        boolean isLarge = blockEntity.getBlockState().getValue(BlockStateProperties.AGE_1) == 1;
        state.primaryIsBlock = false;
        itemModelResolver.updateForTopItem(state.primaryItemState, OnShelfMushroomModels.withCorrectModel(blockEntity.getFirstItem()), ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
        AABB bounds = state.primaryItemState.getModelBoundingBox();
        state.primaryYOffset = (1f/32);
        if (bounds.getZsize() != 0.0625) {
            state.primaryIsBlock = true;
            state.primaryYOffset = - (float) bounds.minY;
        }
        if (isLarge) {
            ItemStack secondItem = blockEntity.getSecondItem();
            if (secondItem != ItemStack.EMPTY) {
                itemModelResolver.updateForTopItem(state.secondaryItemState, OnShelfMushroomModels.withCorrectModel(secondItem), ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
                bounds = state.secondaryItemState.getModelBoundingBox();
                state.secondaryYOffset = (1f/32);
                if (bounds.getZsize() != 0.0625) {
                    state.secondaryIsBlock = true;
                    state.secondaryYOffset = - (float) bounds.minY;
                }
                state.arrangement = ShelfMushroomRenderState.ItemArrangement.LARGE_DOUBLE;
            } else {
                state.arrangement = ShelfMushroomRenderState.ItemArrangement.LARGE_SINGLE;
            }
        } else {
            state.arrangement = ShelfMushroomRenderState.ItemArrangement.SMALL;
        }

    }

    @Override
    public void submit(ShelfMushroomRenderState state, PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, @NonNull CameraRenderState camera) {
        poseStack.pushPose();

        poseStack.translate(0.5f, 0.5f, 0.5f);

        float angle = (float) switch (state.facing) {
            case SOUTH -> Math.toRadians(180);
            case WEST -> Math.toRadians(90);
            case EAST -> Math.toRadians(270);
            default -> Math.toRadians(0);
        };
        poseStack.rotate(Axis.YP, angle);

        poseStack.scale(0.5f, 0.5f, 0.5f);

        switch (state.arrangement) {
            case SMALL -> {
                poseStack.translate(0f, 0.375f, 0.625f);
                this.renderItem(state.primaryIsBlock, state.primaryYOffset, state.primaryItemState, poseStack, submitNodeCollector, state.lightCoords);
            }
            case LARGE_SINGLE -> {
                poseStack.translate(0f, 0.375f, 0.375f);
                this.renderItem(state.primaryIsBlock, state.primaryYOffset, state.primaryItemState, poseStack, submitNodeCollector, state.lightCoords);
            }
            case LARGE_DOUBLE -> {
                poseStack.translate(0.375f, 0.375f, 0.375f);
                poseStack.pushPose();
                this.renderItem(state.primaryIsBlock, state.primaryYOffset, state.primaryItemState, poseStack, submitNodeCollector, state.lightCoords);
                poseStack.popPose();
                poseStack.translate(-0.75f, 0f, 0f);
                this.renderItem(state.secondaryIsBlock, state.secondaryYOffset, state.secondaryItemState, poseStack, submitNodeCollector, state.lightCoords);
            }
        }

        poseStack.popPose();
    }

    private void renderItem(boolean isBlock, float yOffset, ItemStackRenderState itemRenderState, PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, int lightCoords) {
        if (!isBlock) {
            poseStack.rotate(Axis.XP, (float) Math.toRadians(90));
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
        poseStack.translate(0, yOffset, 0);
        itemRenderState.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
    }
}
