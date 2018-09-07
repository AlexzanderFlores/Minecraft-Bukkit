package network.server.servers.hub.crate;

import network.customevents.player.InventoryItemClickEvent;
import network.server.DB;
import network.server.tasks.AsyncDelayedTask;
import network.server.tasks.DelayedTask;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;
import npc.NPCEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KeyFragments implements Listener {
	private String name = null;
	private List<String> delayed = null;
	
	public KeyFragments() {
		name = "Key Fragments";
		delayed = new ArrayList<String>();
		Villager villager = (Villager) new NPCEntity(EntityType.VILLAGER, "&e&n" + name, new Location(Bukkit.getWorlds().get(0), 1658, 5, -1284)) {
			@Override
			public void onInteract(Player player) {
				if(!delayed.contains(player.getName())) {
					final String playerName = player.getName();
					delayed.add(playerName);
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							delayed.remove(playerName);
						}
					}, 20 * 2);
					EffectUtil.playSound(player, Sound.VILLAGER_IDLE);
					open(player);
				}
			}
		}.getLivingEntity();
		villager.setProfession(Profession.LIBRARIAN);
		EventUtil.register(this);
	}
	
	public static void give(Player player, int toAdd) {
		give(player.getUniqueId(), toAdd);
	}
	
	public static void give(final UUID uuid, final int toAdd) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				int amount = DB.PLAYERS_KEY_FRAGMENTS.getInt("uuid", uuid.toString(), "amount") + toAdd;
				if(DB.PLAYERS_KEY_FRAGMENTS.isUUIDSet(uuid)) {
					DB.PLAYERS_KEY_FRAGMENTS.updateInt("amount", amount, "uuid", uuid.toString());
				} else {
					DB.PLAYERS_KEY_FRAGMENTS.insert("'" + uuid.toString() + "', '" + amount + "'");
				}
			}
		});
	}
	
	private void open(Player player) {
		final Inventory inventory = Bukkit.createInventory(player, 9 * 5, name);
		player.openInventory(inventory);
		UUID uuid = player.getUniqueId();
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				int owned = DB.PLAYERS_KEY_FRAGMENTS.getInt("uuid", uuid.toString(), "amount");
				int [] slots = new int [] {11, 13, 15};
				String [] lores = new String [] {
					"",
					"&7Turn 3 key fragments into a key",
					"&7To get these be the best in your",
					"&7game in kills or other objectives",
					""
				};
				String getString = "&bGet Key Fragments by playing games";
				for(int a = 0; a < slots.length; ++a) {
					if(owned > a) {
						inventory.setItem(slots[a], new ItemCreator(Material.TRIPWIRE_HOOK).setName("&bKey Fragment &7(&e" + (a + 1) + "&7/&e3&7)").setGlow(true).getItemStack());
					} else {
						inventory.setItem(slots[a], new ItemCreator(Material.INK_SACK, 8).setName(getString).setLores(lores).getItemStack());
					}
				}
				if(owned >= 3) {
					inventory.setItem(inventory.getSize() - 14, new ItemCreator(Material.EMERALD_BLOCK).setName("&bExchange for a key").setLores(new String [] {
						"",
						"&7Click to exchange 3 key",
						"&7fragments for one crate key",
						""
					}).setGlow(true).getItemStack());
				} else {
					inventory.setItem(inventory.getSize() - 14, new ItemCreator(Material.REDSTONE_BLOCK).setName(getString).setLores(lores).getItemStack());
				}
			}
		});
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			ItemStack item = event.getItem();
			if(item.getType() == Material.EMERALD_BLOCK) {
				event.getInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
				int owned = DB.PLAYERS_KEY_FRAGMENTS.getInt("uuid", player.getUniqueId().toString(), "amount") - 3;
				if(owned <= 0) {
					DB.PLAYERS_KEY_FRAGMENTS.deleteUUID(player.getUniqueId());
				} else {
					DB.PLAYERS_KEY_FRAGMENTS.updateInt("amount", owned, "uuid", player.getUniqueId().toString());
				}
				open(player);
				Beacon.giveKey(player.getUniqueId(), 1, Crate.getVoting().getType());
				EffectUtil.playSound(player, Sound.LEVEL_UP);
			} else {
				EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
			}
			event.setCancelled(true);
		}
	}
}
