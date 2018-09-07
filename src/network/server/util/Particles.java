package network.server.util;

import de.slikey.effectlib.util.ParticleEffect;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import network.ProPlugin;
import network.customevents.TimeEvent;
import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.PlayerAFKEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.gameapi.SpectatorHandler;
import network.player.MessageHandler;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Particles implements Listener {
	private static String name = null;
	private static Map<String, ParticleTypes> types = null;
	
	public enum ParticleTypes {
		FLAME(Material.BLAZE_ROD, Effect.MOBSPAWNER_FLAMES),
		SMOKE(Material.WEB, Effect.SMOKE),
		FIREWORK_SPARK(Material.FIREWORK, ParticleEffect.FIREWORKS_SPARK),
		MOB_SPELL(Material.POTION, ParticleEffect.SPELL_MOB),
		SPELL(Material.POTION, ParticleEffect.SPELL),
		INSTANT_SPELL(Material.POTION, ParticleEffect.SPELL_INSTANT),
		WITCH_MAGIC(Material.POTION, ParticleEffect.SPELL_WITCH),
		NOTE(Material.JUKEBOX, ParticleEffect.NOTE),
		PORTAL(Material.PORTAL, ParticleEffect.PORTAL),
		EXPLODE(Material.TNT, ParticleEffect.EXPLOSION_NORMAL),
		LAVA(Material.LAVA_BUCKET, ParticleEffect.LAVA),
		LARGE_SMOKE(Material.WEB, ParticleEffect.SMOKE_LARGE),
		CLOUD(Material.WEB, ParticleEffect.CLOUD),
		RED_DUST(Material.REDSTONE, ParticleEffect.REDSTONE),
		SNOWBALL_POOF(Material.SNOW_BALL, ParticleEffect.SNOWBALL),
		DRIP_WATER(Material.WATER, ParticleEffect.DRIP_WATER),
		DRIP_LAVA(Material.LAVA, ParticleEffect.DRIP_LAVA),
		SNOW_SHOVEL(Material.IRON_SPADE, ParticleEffect.SNOW_SHOVEL),
		SLIME(Material.SLIME_BALL, ParticleEffect.SLIME),
		HEART(Material.RED_ROSE, ParticleEffect.HEART),
		ANGRY_VILLAGER(Material.REDSTONE_BLOCK, ParticleEffect.VILLAGER_HAPPY),
		HAPPY_VILLAGER(Material.EMERALD_BLOCK, ParticleEffect.VILLAGER_ANGRY);
		
		private Effect effect = null;
		private ParticleEffect particlEffect = null;
		private ItemStack item = null;
		
		private ParticleTypes(Material material, Effect effect) {
			this.item = new ItemCreator(material).setName("&a" + getName()).getItemStack();
			this.effect = effect;
		}
		
		private ParticleTypes(Material material, ParticleEffect particleEffect) {
			this.item = new ItemCreator(material).setName("&a" + getName()).getItemStack();
			this.particlEffect = particleEffect;
		}
		
		private String getName() {
			return toString().substring(0, 1).toUpperCase() + toString().substring(1, toString().length()).toLowerCase().replace("_", " ");
		}
		
		public ItemStack getItem() {
			return item;
		}
		
		public void display(Location location) {
			if(particlEffect != null) {
				try {
					ParticleEffect.valueOf(particlEffect.toString().toUpperCase()).display(1, 0, 1, 0, 3, location.add(0, 2, 0), 20);
				} catch(Exception e) {
					
				}
			} else if(effect != null) {
				EffectUtil.playEffect(effect, location.add(0, 1, 0));
			}
		}
		
		public void displaySpiral(Location location) {
			displaySpiral(location, 5);
		}
		
		public void displaySpiral(Location location, double height) {
			displaySpiral(location, 5, 2);
		}
		
		public void displaySpiral(Location location, double height, double radius) {
//			String particle = effect == null ? particlEffect.getName() : toString().toLowerCase();
			EnumParticle particle = EnumParticle.valueOf((effect == null ? particlEffect.getName() : toString()).toUpperCase());
			for(double y = 0; y < height; y += 0.05) {
				double x = radius * Math.cos(y);
				double z = radius * Math.sin(y);
				PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particle, false, (float) (location.getX() + x), (float) (location.getY() + y), (float) (location.getZ() + z), 0, 0, 0, 0, 1);
				for(Player player : Bukkit.getOnlinePlayers()) {
					CraftPlayer craftPlayer = (CraftPlayer) player;
					craftPlayer.getHandle().playerConnection.sendPacket(packet);
				}
			}
		}
	}
	
	public Particles() {
		types = new HashMap<String, ParticleTypes>();
		EventUtil.register(this);
	}
	
	public static void setType(Player player, ParticleTypes type) {
		types.put(player.getName(), type);
	}
	
	public static Inventory getParticlesMenu(Player player, String title) {
		int size = ItemUtil.getInventorySize(ParticleTypes.values().length + 1);
		Inventory inventory = Bukkit.createInventory(player, size, title == null ? ChatColor.stripColor(getName()) : ChatColor.stripColor(title));
		inventory.addItem(new ItemCreator(Material.WATER_BUCKET).setName("&bRemove particles").getItemStack());
		for(ParticleTypes types: ParticleTypes.values()) {
			inventory.addItem(types.getItem());
		}
		return inventory;
	}
	
	public static String getName() {
		if(name == null) {
			name = ChatColor.AQUA + "Particle Selector";
		}
		return name;
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().startsWith(ChatColor.stripColor(getName()))) {
			Player player = event.getPlayer();
			String name = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());
			if(name.equals("Remove particles")) {
				types.remove(player.getName());
			} else {
				ParticleTypes type = ParticleTypes.valueOf(name.toUpperCase().replace(" ", "_"));
				types.put(player.getName(), type);
				MessageHandler.sendMessage(player, "You selected &b" + name);
			}
			player.closeInventory();
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 10) {
			if(types != null && !types.isEmpty()) {
				for(String name : types.keySet()) {
					Player player = ProPlugin.getPlayer(name);
					if(player != null && !PlayerAFKEvent.isAFK(player) && !SpectatorHandler.contains(player)) {
						types.get(name).display(player.getLocation());
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(types != null) {
			types.remove(event.getPlayer().getName());
		}
	}
}