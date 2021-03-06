package vazkii.skillable.base;

import java.io.File;

import net.minecraft.advancements.Advancement;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import vazkii.skillable.lib.LibMisc;
import vazkii.skillable.skill.Skills;

public class ConfigHandler {

	public static Configuration config;

	private static boolean firstLoad;
	public static int baseXPCost = 4;
	public static int xpIncrease = 1;
	public static int xpIncreaseStagger = 1;
	public static int skillPointInterval = 2;
	public static int levelCap = 32;
	public static boolean disableSheepWool = true;
	public static boolean enforceFakePlayers = true;
	public static boolean enableTabs = true;
	
	public static void init(File configFile) {
		config = new Configuration(configFile);

		firstLoad = true;
		config.load();
		load();

		MinecraftForge.EVENT_BUS.register(ChangeListener.class);
	}

	public static void load() {
		baseXPCost = loadPropInt("Base XP Cost", "", baseXPCost);
		xpIncrease = loadPropInt("XP Increase Per Level", "", 1);
		xpIncreaseStagger = loadPropInt("XP Increase Stagger", "Between how many levels should XP costs increase?\nDefault is 1, which means it increases cost every level. 3 would make the cost increase every 3 levels.", 1);
		skillPointInterval = loadPropInt("Levels per Skill Point", "", skillPointInterval);
		levelCap = loadPropInt("Level Cap", "", levelCap);
		disableSheepWool = loadPropBool("Disable Sheep Dropping Wool on Death", "", disableSheepWool);
		enforceFakePlayers = loadPropBool("Enforce requirements on Fake Players", "", true);
		enableTabs = loadPropBool("Enable Skillable Tabs", "Set this to false if you don't want to use skills, just the advancement locks", true);
		
		String desc = "Set requirements for items in this list. Each entry is composed of the item key and the requirements\n"
				+ "The item key is in the simple mod:item_id format. Optionally, it can be in mod:item_id:metadata, if you want to match metadata.\n"
				+ "The requirements are in a comma separated list, each in a key|value format. For example, to make an iron pickaxe require 5 mining\n"
				+ "and 5 building, you'd use the following string:\n"
				+ "\"minecraft:iron_pickaxe=mining|5,building|5\"\n\n"
				+ "Item usage can also be locked behind an advancement, by using adv|id. For example, to make the elytra require the \"Acquire Hardware.\" advancement\n"
				+ "you'd use the following string:\n"
				+ "\"minecraft:elytra=adv|minecraft:story/smelt_iron\"\n\n"
				+ "Skill requirements and advancements can be mixed and matched, so you can make an item require both, if you want.\n"
				+ "You can also lock placed blocks from being used or broken, in the same manner.\n\n"
				+ "Locks defined here apply to all the following cases: Right clicking an item, placing a block, breaking a block, using a block that's placed,\n"
				+ "left clicking an item, using an item to break any block, and equipping an armor item.\n\n"
				+ "You can lock entire mods by just using their name as the left argument. You can then specify specific items to not be locked,\n"
				+ "by defining their lock in the normal way. If you want an item to not be locked in this way, use \"none\" after the =";
		String[] locks = config.getStringList("Skill Locks", Configuration.CATEGORY_GENERAL, LevelLockHandler.DEFAULT_SKILL_LOCKS, desc);
		
		LevelLockHandler.loadFromConfig(locks);
		if(!firstLoad)
			LevelLockHandler.setupLocks();
		
		Skills.ALL_UNLOCKABLES.clear();
		Skills.ALL_SKILLS.values().forEach((skill) -> {
			skill.getUnlockables().clear();
			skill.initUnlockables();	
		});
		
		if(config.hasChanged())
			config.save();
		
		firstLoad = false;
	}

	public static int loadPropInt(String propName, String desc, int default_) {
		Property prop = config.get(Configuration.CATEGORY_GENERAL, propName, default_);
		prop.setComment(desc);

		return prop.getInt(default_);
	}

	public static double loadPropDouble(String propName, String desc, double default_) {
		Property prop = config.get(Configuration.CATEGORY_GENERAL, propName, default_);
		prop.setComment(desc);

		return prop.getDouble(default_);
	}

	public static boolean loadPropBool(String propName, String desc, boolean default_) {
		Property prop = config.get(Configuration.CATEGORY_GENERAL, propName, default_);
		prop.setComment(desc);

		return prop.getBoolean(default_);
	}
	

	public static class ChangeListener {

		@SubscribeEvent
		public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
			if(eventArgs.getModID().equals(LibMisc.MOD_ID))
				load();
		}

	}

	
}
