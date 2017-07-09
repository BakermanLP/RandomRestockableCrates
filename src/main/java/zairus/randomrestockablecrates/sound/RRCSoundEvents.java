package zairus.randomrestockablecrates.sound;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

import net.minecraftforge.fml.common.registry.ForgeRegistries;

import zairus.randomrestockablecrates.RRCConstants;

public class RRCSoundEvents {
	public static SoundEvent CRATE_OPEN;
	
	public static SoundEvent registerSound(ResourceLocation location) {
		SoundEvent event = new SoundEvent(location);
		event.setRegistryName(location);
		ForgeRegistries.SOUND_EVENTS.register(event);
		
		return event;
	}
	
	private static SoundEvent registerSound(String location) {
		return registerSound(new ResourceLocation(RRCConstants.MODID, location));
	}
	
	public static void registerSounds() {
		
		CRATE_OPEN = registerSound("crate_open");
	}
}
