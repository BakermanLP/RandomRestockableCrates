package zairus.randomrestockablecrates.proxy;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;

import net.minecraftforge.client.model.ModelLoader;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import zairus.randomrestockablecrates.RRCConstants;
import zairus.randomrestockablecrates.client.renderer.tileentity.TileEntityCrateRenderer;
import zairus.randomrestockablecrates.tileentity.TileEntityCrate;

public class ClientProxy extends CommonProxy {
	public static final Minecraft mc = Minecraft.getMinecraft();
	
	@Override
	public void preInit(FMLPreInitializationEvent e) {
	}
	
	@Override
	public void init(FMLInitializationEvent e) {
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent e) {
	}
	
	@Override
	public void registerItem(Item item, String name) {
		super.registerItem(item, name);
	}
	
	@Override
	public void registerItemModel(Item item, int meta) {
		String itemId = RRCConstants.MODID + ":"; // + item.getModName();
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(itemId, "inventory"));
	}
	
	@Override
	public void registerItemModel(Item item, int meta, String texture) {
		String itemId = RRCConstants.MODID + ":" + texture;
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(itemId, "inventory"));
	}
	
	@Override
	public void registerBlockModel(Block block, int meta, String modName) {
		Item item = Item.getItemFromBlock(block);
		
		if (item != null) {
			registerItemModel(item, meta, modName);
		}
	}
	
	@Override
	public void initTESR() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrate.class, new TileEntityCrateRenderer());
	}
}
