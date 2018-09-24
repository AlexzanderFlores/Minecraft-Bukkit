package network.server;

import network.player.VoteHandler;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.Listener;

import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import npc.NPCEntity;

public class DailyRewards implements Listener {
	public DailyRewards(Location location, Location target) {
		Villager villager = (Villager) new NPCEntity(EntityType.VILLAGER, "&eDaily Rewards", location, target) {
			@Override
			public void onInteract(Player player) {
				VoteHandler.open(player);
				EffectUtil.playSound(player, Sound.VILLAGER_IDLE);
			}
		}.getLivingEntity();

		villager.setProfession(Profession.LIBRARIAN);
		EventUtil.register(this);
	}
}
