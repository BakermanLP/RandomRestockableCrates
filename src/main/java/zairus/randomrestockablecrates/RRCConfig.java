package zairus.randomrestockablecrates;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class RRCConfig {
	public static Configuration config;
	
	public static int tier1RestockTime = 6000;
	public static int tier2RestockTime = 24000;
	public static int tier3RestockTime = 24000;
	public static int tier4RestockTime = 24000;
	
	public static String tier1DefaultTable = "minecraft:chests/simple_dungeon";
	public static String tier2DefaultTable = "minecraft:chests/village_blacksmith";
	public static String tier3DefaultTable = "minecraft:chests/stronghold_library";
	public static String tier4DefaultTable = "minecraft:chests/end_city_treasure";
	
	public static void init(File file) {
		config = new Configuration(file);
		
		config.load();
		
		config.setCategoryComment("CRATE_RESTOCK_TIMES", "Restock time in ticks, 24000 ticks is one Minecraft day.");
		
		tier1RestockTime = config.getInt("tier1RestockTime", "CRATE_RESTOCK_TIMES", tier1RestockTime, 10, 300000, "Ticks for tier 1 crate.");
		tier2RestockTime = config.getInt("tier2RestockTime", "CRATE_RESTOCK_TIMES", tier2RestockTime, 10, 300000, "Ticks for tier 2 crate.");
		tier3RestockTime = config.getInt("tier3RestockTime", "CRATE_RESTOCK_TIMES", tier3RestockTime, 10, 300000, "Ticks for tier 3 crate.");
		tier4RestockTime = config.getInt("tier4RestockTime", "CRATE_RESTOCK_TIMES", tier4RestockTime, 10, 300000, "Ticks for tier 4 crate.");
		
		config.setCategoryComment("CRATE_DEFAULT_TABLE", "Default loot table for crates by type");
		
		tier1DefaultTable = config.getString("tier1DefaultLootTable", "CRATE_DEFAULT_TABLE", tier1DefaultTable, "Loot table for tier 1 crate");
		tier2DefaultTable = config.getString("tier2DefaultLootTable", "CRATE_DEFAULT_TABLE", tier2DefaultTable, "Loot table for tier 2 crate");
		tier3DefaultTable = config.getString("tier3DefaultLootTable", "CRATE_DEFAULT_TABLE", tier3DefaultTable, "Loot table for tier 3 crate");
		tier4DefaultTable = config.getString("tier4DefaultLootTable", "CRATE_DEFAULT_TABLE", tier4DefaultTable, "Loot table for tier 4 crate");
		
		config.save();
	}
}
