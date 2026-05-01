package net.appleseed.appleseed.client.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class DietMenu extends AbstractContainerMenu {

    private final Player player;

    public DietMenu(int containerId, Inventory playerInventory) {
        super(null, containerId);
        this.player = playerInventory.player;
    }

    public DietMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public Player getPlayer() {
        return player;
    }
}
