package network.server.servers.hub.crate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import network.customevents.player.InventoryItemClickEvent;
import network.player.MessageHandler;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import npc.NPCEntity;

public class KeyExchange implements Listener {
	private String name = null;
	
	public KeyExchange() {
		name = "Key Exchange";
		Villager villager = (Villager) new NPCEntity(EntityType.VILLAGER, "&e&n&k" + name, new Location(Bukkit.getWorlds().get(0), 1658, 5, -1277)) {
			@Override
			public void onInteract(Player player) {
				EffectUtil.playSound(player, Sound.VILLAGER_IDLE);
				MessageHandler.sendMessage(player, "&cComing soon");
			}
		}.getLivingEntity();
		villager.setProfession(Profession.LIBRARIAN);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			
			event.setCancelled(true);
		}
	}
}
