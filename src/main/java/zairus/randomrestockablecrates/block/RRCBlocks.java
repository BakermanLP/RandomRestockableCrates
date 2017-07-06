package zairus.randomrestockablecrates.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import net.minecraftforge.fml.common.registry.GameRegistry;

import zairus.randomrestockablecrates.RandomRestockableCrates;
import zairus.randomrestockablecrates.tileentity.TileEntityCrate;

public class RRCBlocks
{
	public static final Block crate;
	public static final Block crate2;
	public static final Block crate3;
	public static final Block crate4;
	
	static
	{
		crate = new BlockCrate(Material.WOOD, 0);
		crate2 = new BlockCrate(Material.WOOD, 1);
		crate3 = new BlockCrate(Material.ANVIL, 2);
		crate4 = new BlockCrate(Material.ANVIL, 3);
	}
	
	public static void init()
	{
		RandomRestockableCrates.proxy.registerBlock(crate, ((BlockCrate)crate).getModName());
		RandomRestockableCrates.proxy.registerBlock(crate2, ((BlockCrate)crate2).getModName());
		RandomRestockableCrates.proxy.registerBlock(crate3, ((BlockCrate)crate3).getModName());
		RandomRestockableCrates.proxy.registerBlock(crate4, ((BlockCrate)crate4).getModName());
		
		GameRegistry.registerTileEntity(TileEntityCrate.class, "tileEntityCrate");
		RandomRestockableCrates.proxy.initTESR();
	}
	
	public static void initModels()
	{
		RandomRestockableCrates.proxy.registerBlockModel(crate, 0, ((BlockCrate)crate).getModName());
		RandomRestockableCrates.proxy.registerBlockModel(crate2, 0, ((BlockCrate)crate2).getModName());
		RandomRestockableCrates.proxy.registerBlockModel(crate3, 0, ((BlockCrate)crate3).getModName());
		RandomRestockableCrates.proxy.registerBlockModel(crate4, 0, ((BlockCrate)crate4).getModName());
	}
}
