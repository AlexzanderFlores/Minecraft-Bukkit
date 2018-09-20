package network.server;

import java.util.UUID;

import network.server.servers.hub.crate.CrateTypes;
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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import network.customevents.player.InventoryItemClickEvent;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import network.server.util.Glow;
import network.server.util.ItemCreator;
import network.server.util.ItemUtil;
import npc.NPCEntity;

public class DailyRewards implements Listener {
	private String name = null;
	private String rewardsName = null;
	private String streakName = null;
	private final int coins = 20;
	
	public DailyRewards(Location location) {
		name = "Daily Rewards";
		rewardsName = "Rewards";
		streakName = "Streaks";
		Villager villager = (Villager) new NPCEntity(EntityType.VILLAGER, "&e" + name, location) {
			@Override
			public void onInteract(Player player) {
				open(player);
				EffectUtil.playSound(player, Sound.VILLAGER_IDLE);
			}
		}.getLivingEntity();
		villager.setProfession(Profession.LIBRARIAN);
		EventUtil.register(this);
	}
	
	private void open(Player player) {
		Inventory inventory = Bukkit.createInventory(player, 9 * 3, name);

		inventory.setItem(12, new ItemCreator(Material.DIAMOND).setName("&bVote").setLores(new String [] {
			"",
			"&eVote each day for cool rewards",
			"",
			"&7Left click - &aView voting links",
			"&7Right click - &aView voting rewards",
			""
		}).getItemStack());

		inventory.setItem(14, new ItemCreator(Material.DIAMOND).setName("&bVoting Streaks").setLores(new String [] {
			"",
			"&eVoting each day will create a streak",
			"&eStreaks multiply your rewards",
			"",
			"&7Click - &aView streak information",
			""
		}).getItemStack());

		player.openInventory(inventory);
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		Player player = event.getPlayer();
		if(event.getTitle().equals(name)) {
			int slot = event.getSlot();
			if(slot == 12) {
				if(event.getClickType() == ClickType.LEFT) {
					player.closeInventory();
					player.performCommand("vote");
					EffectUtil.playSound(player, Sound.LEVEL_UP);
				} else if(event.getClickType() == ClickType.RIGHT) {
					Inventory inventory = Bukkit.createInventory(player, 9 * 6, rewardsName);
					// Coins
					inventory.setItem(10, new ItemCreator(Material.GOLD_INGOT).setName("&bUHC Sky Wars Coins").setLores(new String [] {
						"",
						"&e+" + coins + " &aUHC Sky Wars Coins",
						""
					}).getItemStack());
					inventory.setItem(12, new ItemCreator(Material.GOLD_INGOT).setName("&bKit PVP Coins").setLores(new String [] {
						"",
						"&e+" + coins + " &aKit PVP Coins",
						""
					}).getItemStack());
					
					// Other
					inventory.setItem(14, new ItemCreator(Material.CHEST).setName("&bUHC Sky Wars Looter Passes").setLores(new String [] {
						"",
						"&e+3 &aUHC Sky Wars Looter Passes",
						"",
						"&7Break a chest to restock its contents",
						"&7Does not load if the Looter kit is selected",
						"&7Max of 1 use per game",
						""
					}).getItemStack());
					inventory.setItem(16, new ItemCreator(Material.EXP_BOTTLE).setName("&bNetwork Experience").setLores(new String [] {
						"",
						"&e+250 &aNetwork Experience",
						"&c(Coming soon)",
						""
					}).getItemStack());

					String voting = CrateTypes.VOTING.getDisplay();
					
					// Crate keys
					inventory.setItem(20, Glow.addGlow(new ItemCreator(Material.TRIPWIRE_HOOK).setName("&b" + voting + " Crate Key").setLores(new String [] {
						"",
						"&e+1 &aKey to the " + voting + " crate",
						""
					}).getItemStack()));
					inventory.setItem(22, Glow.addGlow(new ItemCreator(Material.TRIPWIRE_HOOK).setName("&b" + voting + " Crate Key Fragment").setLores(new String [] {
						"",
						"&e+1 &aKey Fragment to the " + voting + " crate",
						"",
						"&7Collect 3 of these for a full Key",
						"&7Click the Villager NPC near the crates",
						""
					}).getItemStack()));
					inventory.setItem(24, Glow.addGlow(new ItemCreator(Material.TRIPWIRE_HOOK).setName("&bUHC Sky Wars Crate Key").setLores(new String [] {
						"",
						"&e+1 &aKey to the UHC Sky Wars crate",
						"",
						"&7Open in the UHC Sky Wars shop",
						""
					}).getItemStack()));
					
					// Other
					inventory.setItem(30, new ItemCreator(Material.LEATHER_BOOTS).setName("&bHub Parkour Checkpoints").setLores(new String [] {
						"",
						"&e+10 &aHub Parkour Checkpoints",
						""
					}).getItemStack());
					inventory.setItem(32, new ItemCreator(Material.GOLDEN_APPLE).setName("&bUHC Rescatter Passes").setLores(new String [] {
						"",
						"&e+1 &aRescatter Pass",
						"",
						"&7Rescatter yourself with /rescatter",
						"&7Only works for the first 20 seconds",
						"",
						"&7Works for Speed UHC & Twitter UHCs",
						""
					}).getItemStack());
					inventory.setItem(inventory.getSize() - 5, new ItemCreator(Material.WOOD_DOOR).setName("&bBack").getItemStack());
					player.openInventory(inventory);
				}
			} else if(slot == 14) {
				Inventory inventory = Bukkit.createInventory(player, 9 * 4, streakName);
				player.openInventory(inventory);
				UUID uuid = player.getUniqueId();
				String name = player.getName();
				new AsyncDelayedTask(() -> {
					int streak = DB.PLAYERS_LIFETIME_VOTES.getInt("uuid", uuid.toString(), "streak");
					inventory.setItem(10, new ItemCreator(Material.NAME_TAG).setName("&bx1 Multiplier").setLores(new String [] {
						"",
						"&eVoting 1 - 10 days in a row",
						"",
						"&7All voting perk quantities",
						"&7are multiplied by 1",
						""
					}).setGlow(streak >= 1 && streak <= 10).getItemStack());

					inventory.setItem(12, new ItemCreator(Material.NAME_TAG).setName("&bx2 Multiplier").setAmount(2).setLores(new String [] {
						"",
						"&eVoting 11 - 20 days in a row",
						"",
						"&7All voting perk quantities",
						"&7are multiplied by 2",
						""
					}).setGlow(streak >= 11 && streak <= 20).getItemStack());

					inventory.setItem(14, new ItemCreator(Material.NAME_TAG).setName("&bx3 Multiplier").setAmount(3).setLores(new String [] {
						"",
						"&eVoting 21+ days in a row",
						"",
						"&7All voting perk quantities",
						"&7are multiplied by 3",
						""
					}).setGlow(streak >= 21).getItemStack());

					inventory.setItem(16, new ItemCreator(ItemUtil.getSkull(name)).setName("&bCurrent Streak: &a" + streak).getItemStack());
					inventory.setItem(inventory.getSize() - 5, new ItemCreator(Material.WOOD_DOOR).setName("&bBack").getItemStack());
				});
			}
			event.setCancelled(true);
		} else if(event.getTitle().equals(rewardsName)) {
			open(player);
		} else if(event.getTitle().equals(streakName)) {
			open(player);
		}
	}
}
