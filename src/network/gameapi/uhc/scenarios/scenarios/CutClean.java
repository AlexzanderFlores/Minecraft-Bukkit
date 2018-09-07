package network.gameapi.uhc.scenarios.scenarios;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

import network.gameapi.uhc.scenarios.Scenario;

public class CutClean extends Scenario {
	private static CutClean instance = null;
	
	public CutClean() {
		super("CutClean", "CC", Material.IRON_INGOT);
		instance = this;
		setInfo("Ores auto smelt and food auto cooks. Breaking the base of a tree breaks the whole tree.");
		setPrimary(true);
		enable(true);
		new Timber();
	}
	
	public static CutClean getInstance() {
		if(instance == null) {
			new CutClean();
		}
		return instance;
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		Material material = event.getEntity().getItemStack().getType();
		Entity entity = event.getEntity();
		World world = entity.getWorld();
		if(material == Material.GOLD_ORE) {
			world.dropItem(event.getLocation(), new ItemStack(Material.GOLD_INGOT));
			ExperienceOrb exp = (ExperienceOrb) world.spawnEntity(event.getLocation(), EntityType.EXPERIENCE_ORB);
			exp.setExperience(1);
			entity.remove();
		} else if(material == Material.IRON_ORE) {
			world.dropItem(event.getLocation(), new ItemStack(Material.IRON_INGOT));
			ExperienceOrb exp = (ExperienceOrb) world.spawnEntity(event.getLocation(), EntityType.EXPERIENCE_ORB);
			exp.setExperience(1);
			entity.remove();
		} else if(material == Material.RAW_BEEF) {
			world.dropItem(event.getLocation(), new ItemStack(Material.COOKED_BEEF));
			entity.remove();
		} else if(material == Material.RAW_CHICKEN) {
			world.dropItem(event.getLocation(), new ItemStack(Material.COOKED_CHICKEN));
			entity.remove();
		} else if(material == Material.RAW_FISH) {
			world.dropItem(event.getLocation(), new ItemStack(Material.COOKED_FISH));
			entity.remove();
		} else if(material == Material.POTATO) {
			world.dropItem(event.getLocation(), new ItemStack(Material.BAKED_POTATO));
			entity.remove();
		} else if(material == Material.PORK) {
			world.dropItem(event.getLocation(), new ItemStack(Material.GRILLED_PORK));
			entity.remove();
		} else if(material == Material.GRAVEL && new Random().nextBoolean()) {
			world.dropItem(event.getLocation(), new ItemStack(Material.FLINT));
			entity.remove();
		}
	}
}