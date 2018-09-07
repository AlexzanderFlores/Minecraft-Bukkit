package network.gameapi.games.uhcskywars.cages;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import network.Network.Plugins;
import network.gameapi.kit.KitBase;
import network.server.servers.hub.items.Features.Rarity;
import network.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public abstract class Cage extends KitBase {
	private static List<Cage> cages = null;
	private List<Block> blocks = null;
	//private String playerName = null;
	private Material material = Material.GLASS;
	private byte data = 0;
	
	public Cage(ItemStack icon, Rarity rarity, int slot) {
		super(Plugins.UHCSW, icon, rarity, -1, slot);
		if(cages == null) {
			cages = new ArrayList<Cage>();
		}
		cages.add(this);
	}
	
	public static void createCages() {
		String [] names = new String [] {
			"White Cage", "Orange Cage", "Magenta Cage", "Light Blue Cage", "Yellow Cage", "Lime Cage", "Pink Cage", "Gray Cage",
			"Light Gray Cage", "Cyan Cage", "Purple Cage", "Blue Cage", "Brown Cage", "Green Cage", "Red Cage", "Black Cage"};
		new SmallCage(new ItemCreator(Material.STAINED_GLASS, 0).setName("Small " + names[0]).setLores(new String [] {
			"",
			"&7Unlocked in &bSky Wars Crate",
			"&7Rarity: " + Rarity.COMMON.getName()
		}).getItemStack(), 18);
		for(int a = 1; a < 9; ++a) {
			new SmallCage(new ItemCreator(Material.STAINED_GLASS, a).setName("Small " + names[a]).setLores(new String [] {
				"",
				"&7Unlocked in &bSky Wars Crate",
				"&7Rarity: " + Rarity.COMMON.getName()
			}).getItemStack());
		}
		new SmallCage(new ItemCreator(Material.STAINED_GLASS, 9).setName("Small " + names[9]).setLores(new String [] {
			"",
			"&7Unlocked in &bSky Wars Crate",
			"&7Rarity: " + Rarity.COMMON.getName()
		}).getItemStack(), 28);
		for(int a = 10; a < names.length; ++a) {
			new SmallCage(new ItemCreator(Material.STAINED_GLASS, a).setName("Small " + names[a]).setLores(new String [] {
				"",
				"&7Unlocked in &bSky Wars Crate",
				"&7Rarity: " + Rarity.COMMON.getName()
			}).getItemStack());
		}
		new BigCage(new ItemCreator(Material.STAINED_GLASS, 0).setName("Big " + names[0]).setLores(new String [] {
			"",
			"&7Unlocked in &bSky Wars Crate",
			"&7Rarity: " + Rarity.UNCOMMON.getName()
		}).getItemStack(), 18);
		for(int a = 1; a < 9; ++a) {
			new BigCage(new ItemCreator(Material.STAINED_GLASS, a).setName("Big " + names[a]).setLores(new String [] {
				"",
				"&7Unlocked in &bSky Wars Crate",
				"&7Rarity: " + Rarity.UNCOMMON.getName()
			}).getItemStack());
		}
		new BigCage(new ItemCreator(Material.STAINED_GLASS, 9).setName("Big " + names[9]).setLores(new String [] {
			"",
			"&7Unlocked in &bSky Wars Crate",
			"&7Rarity: " + Rarity.UNCOMMON.getName()
		}).getItemStack(), 28);
		for(int a = 10; a < names.length; ++a) {
			new BigCage(new ItemCreator(Material.STAINED_GLASS, a).setName("Big " + names[a]).setLores(new String [] {
				"",
				"&7Unlocked in &bSky Wars Crate",
				"&7Rarity: " + Rarity.UNCOMMON.getName()
			}).getItemStack());
		}
	}
	
	public static List<Cage> getCages() {
		return cages;
	}
	
	public void setMaterial(Material material) {
		this.material = material;
	}
	
	public void setMaterial(Material material, byte data) {
		this.material = material;
		this.data = data;
	}
	
	public void placeBlock(Location location) {
		placeBlock(location.getBlock());
	}
	
	public void placeBlock(Block block) {
		block.setType(material);
		block.setData(data);
		if(blocks == null) {
			blocks = new ArrayList<Block>();
		}
		blocks.add(block);
	}
	
	public List<Block> getBlocks() {
		return blocks;
	}
	
	public void remove() {
		if(blocks != null) {
			for(Block block : getBlocks()) {
				block.setType(Material.AIR);
				block.setData((byte) 0);
			}
			blocks.clear();
			blocks = null;
		}
	}
	
	protected void teleport(Player player) {
		player.teleport(getBlocks().get(0).getLocation().clone().add(0.5, 1, 0.5));
	}
}