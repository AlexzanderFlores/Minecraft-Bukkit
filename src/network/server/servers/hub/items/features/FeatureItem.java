package network.server.servers.hub.items.features;

import java.util.ArrayList;
import java.util.List;

import network.server.servers.hub.crate.CrateTypes;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import network.server.servers.hub.crate.Beacon;
import network.server.servers.hub.crate.KeyFragments;
import network.server.servers.hub.items.Features.Rarity;
import network.server.servers.hub.items.features.Armor.PlayerArmor;
import network.server.servers.hub.items.features.particles.ArrowTrails.ArrowTrailParticleTypes;
import network.server.servers.hub.items.features.particles.HaloParticles.HaloParticleTypes;
import network.server.servers.hub.items.features.pets.Pets.PetTypes;
import network.server.util.ItemCreator;

public class FeatureItem {
	private static List<FeatureItem> items = null;
	private String name = null;
	private ItemStack itemStack = null;
	private Rarity rarity = null;
	private FeatureType type = null;
	public enum FeatureType {REWARD_CRATE, SKY_WARS, SPEED_UHC};
	
	public FeatureItem(String name, ItemStack itemStack, Rarity rarity, FeatureType type) {
		this.name = name;
		if(name.equals(ChatColor.stripColor(name))) {
			this.itemStack = new ItemCreator(itemStack).setName("&b" + name).getItemStack();
		} else {
			this.itemStack = itemStack;
		}
		this.rarity = rarity;
		this.type = type;
		if(items == null) {
			items = new ArrayList<FeatureItem>();
		}
		items.add(this);
	}
	
	public static List<FeatureItem> getItems(FeatureType type) {
		List<FeatureItem> items = new ArrayList<FeatureItem>();
		for(FeatureItem item : FeatureItem.items) {
			if(type == item.getType()) {
				items.add(item);
			}
		}
		return items;
	}
	
	public static FeatureItem getItem(ItemStack itemStack) {
		for(FeatureItem featureItem : items) {
			if(featureItem.getItemStack().equals(itemStack)) {
				return featureItem;
			}
		}
		return null;
	}
	
	public String getName() {
		return this.name;
	}
	
	public ItemStack getItemStack() {
		return this.itemStack;
	}
	
	public Rarity getRarity() {
		return this.rarity;
	}
	
	public FeatureType getType() {
		return this.type;
	}
	
	public void give(Player player) {
		if(getName().equals(Beacon.getKeyFragmentName())) {
			KeyFragments.give(player, 1);
			return;
		}
		if(getName().equals(Beacon.getThreeKeys())) {
			Beacon.giveKey(player.getUniqueId(), 3, CrateTypes.VOTING);
			return;
		}
		for(HaloParticleTypes halo : HaloParticleTypes.values()) {
			if(halo.getName().equals(getName())) {
				halo.give(player);
				return;
			}
		}
		for(PetTypes petType : PetTypes.values()) {
			if(petType.getName().equals(getName())) {
				petType.give(player);
				return;
			}
		}
		for(PlayerArmor armor : PlayerArmor.values()) {
			if(armor.getName().equals(getName())) {
				armor.give(player);
				return;
			}
		}
		for(ArrowTrailParticleTypes trail : ArrowTrailParticleTypes.values()) {
			if(trail.getName().equals(getName())) {
				trail.give(player);
				return;
			}
		}
		/*for(WinEffect effect : WinEffect.values()) {
			if(effect.getDisplay().equals(getDisplay())) {
				effect.give(player);
				return;
			}
		}
		for(SpinBlock block : SpinBlock.values()) {
			if(block.getDisplay().equals(getDisplay())) {
				block.give(player);
				return;
			}
		}*/
	}
}
