package network.server.servers.hub.items.features;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import network.player.MessageHandler;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;

public abstract class FeatureBase implements Listener {
	private static List<FeatureBase> features = null;
	private String name = null;
	private int slot = 0;
	private ItemStack itemStack = null;
	private String action = null;
	private String [] description = null;
	
	public FeatureBase(String name, int slot, ItemStack itemStack, String action, String [] description) {
		setName(name);
		setSlot(slot);
		setItemStack(itemStack);
		setAction(action);
		setDescription(description);
		if(features == null) {
			features = new ArrayList<FeatureBase>();
		}
		features.add(this);
		EventUtil.register(this);
	}
	
	public static List<FeatureBase> getFeatures() {
		return features;
	}
	
	public static FeatureBase getFeature(String name) {
		for(FeatureBase feature : getFeatures()) {
			if(feature.getName().equals(name)) {
				return feature;
			}
		}
		return null;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getSlot() {
		return this.slot;
	}
	
	public void setSlot(int slot) {
		this.slot = slot;
	}
	
	public ItemStack getItemStack(Player player) {
		int owned = getOwned(player);
		int max = getMax();
		int percentage = (int) (owned * 100.0 / max + 0.5);
		String [] description = new String [getDescription().length];
		for(int a = 0; a < description.length; ++a) {
			description[a] = this.description[a].replace("XX", owned + "").replace("YY", percentage + "").replace("ZZ", getAction());
		}
		return new ItemCreator(this.itemStack.clone()).setName("&b" + getName()).setLores(description).getItemStack();
	}
	
	public void setItemStack(ItemStack itemStack) {
		this.itemStack = itemStack;
	}
	
	public String getAction() {
		return this.action;
	}
	
	public void setAction(String action) {
		if(action == null) {
			action = "Reward Crates";
		}
		this.action = action;
	}
	
	public String [] getDescription() {
		return this.description;
	}
	
	public void setDescription(String [] description) {
		for(int a = 0; a < description.length; ++a) {
			description[a] = ChatColor.translateAlternateColorCodes('&', description[a]);
		}
		this.description = description;
	}
	
	public void displayLocked(Player player) {
		MessageHandler.sendMessage(player, "&cYou do not own that, unlock it in &e" + getAction());
	}
	
	public abstract int getOwned(Player player);
	public abstract int getMax();
	public abstract void display(Player player);
}
