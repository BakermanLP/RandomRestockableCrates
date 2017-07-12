package zairus.randomrestockablecrates.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import zairus.randomrestockablecrates.tileentity.TileEntityCrate;

public class ContainerCrate extends Container {
	private TileEntityCrate inventory;
	
	public ContainerCrate(TileEntityCrate crateInventory, EntityPlayer player) {
		InventoryPlayer playerInventory = player.inventory;
		this.inventory = crateInventory;
		this.inventory.openInventory(player);
		
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new Slot(inventory, i * 9 + j, 7 + j * 18, 20 + i * 18));
			}
		}
		
		for (int l = 0; l < 3; ++l) {
			for (int j1 = 0; j1 < 9; ++j1) {
				this.addSlotToContainer(new Slot(playerInventory, j1 + l * 9 + 9, 7 + j1 * 18, 91 + l * 18));
			}
		}
		
		for (int i1 = 0; i1 < 9; ++i1) {
			this.addSlotToContainer(new Slot(playerInventory, i1, 7 + i1 * 18, 149));
		}
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return inventory.isUsableByPlayer(player);
	}
	
	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		this.inventory.closeInventory(player);
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		ItemStack transferedStack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		
		if (slot != null && slot.getHasStack()) {
			ItemStack slotStack = slot.getStack();
			transferedStack = slotStack.copy();
			
			if (index < TileEntityCrate.SLOT_COUNT) {
				if (!this.mergeItemStack(slotStack, TileEntityCrate.SLOT_COUNT, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(slotStack, 0, TileEntityCrate.SLOT_COUNT, false)) {
				return ItemStack.EMPTY;
			}
			
			if (slotStack.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}
		
		return transferedStack;
	}
}
