package zairus.randomrestockablecrates.client.gui.inventory;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import zairus.randomrestockablecrates.RRCConstants;
import zairus.randomrestockablecrates.inventory.ContainerCrate;
import zairus.randomrestockablecrates.tileentity.TileEntityCrate;

@SideOnly(Side.CLIENT)
public class GuiCrate extends GuiContainer {
	private static final ResourceLocation GUI_BACKGROUND = new ResourceLocation(RRCConstants.MODID, "textures/gui/gui_crate.png");
	
	private TileEntityCrate inventory;
	private InventoryPlayer inventoryPlayer;
	
	public GuiCrate(TileEntityCrate inventorySlots, EntityPlayer player) {
		super(new ContainerCrate(inventorySlots, player));
		this.inventory = inventorySlots;
		this.inventoryPlayer = player.inventory;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRendererObj.drawString(inventory.getDisplayName().getUnformattedText(), 8, 6, 4210752);
		this.fontRendererObj.drawString(inventoryPlayer.getDisplayName().getUnformattedText(), 8, this.ySize - 88, 4210752);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(GUI_BACKGROUND);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize + 10);
	}
}
