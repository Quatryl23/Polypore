package net.mesomods.polypore.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

public class ShelfMushroomRenderState extends BlockEntityRenderState {
    public Direction facing;
    public ItemArrangement arrangement;
    public boolean primaryIsBlock;
    public float primaryYOffset;
    public boolean secondaryIsBlock;
    public float secondaryYOffset;

    final ItemStackRenderState primaryItemState = new ItemStackRenderState();
    final ItemStackRenderState secondaryItemState = new ItemStackRenderState();

    public enum ItemArrangement {
        SMALL,
        LARGE_SINGLE,
        LARGE_DOUBLE
    }
}
