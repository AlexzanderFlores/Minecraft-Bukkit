package network.gameapi.games.uhcskywars.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import network.Network.Plugins;
import network.gameapi.kit.KitBase;
import network.gameapi.shops.SkyWarsShop;
import network.server.servers.hub.items.Features.Rarity;
import network.server.util.ItemCreator;
import network.server.util.UnicodeUtil;

public class Enderman extends KitBase {
	private static boolean enabled = false;
	
	public Enderman() {
		super(Plugins.UHCSW, new ItemCreator(Material.ENDER_PEARL).setName("Enderman").setLores(new String [] {
			"",
			"&7Abilities:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aTake no Enderpearl Damage",
			"",
			"&7Unlocked in &bSky Wars Crate",
			"&7Rarity: " + getRarity().getName()
		}).getItemStack(), getRarity(), -1);
	}
	
	public static Rarity getRarity() {
		return Rarity.UNCOMMON;
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(SkyWarsShop.getInstance().getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		enabled = true;
	}
	
	@Override
	public void execute(Player player) {
		
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(enabled && event.getCause() == TeleportCause.ENDER_PEARL && has(event.getPlayer())) {
			event.getPlayer().teleport(event.getTo());
			event.setCancelled(true);
		}
	}
}