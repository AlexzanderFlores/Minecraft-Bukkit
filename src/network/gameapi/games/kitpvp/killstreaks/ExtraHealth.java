package network.gameapi.games.kitpvp.killstreaks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;

public class ExtraHealth extends Killstreak implements Listener {
	private static ExtraHealth instance = null;
	
	public ExtraHealth() {
		super(new ItemCreator(Material.GOLDEN_APPLE).setName("Extra Health").getItemStack());
		instance = this;
		EventUtil.register(this);
	}
	
	public static ExtraHealth getInstance() {
		if(instance == null) {
			new ExtraHealth();
		}
		return instance;
	}
	
	@Override
	public void execute(Player player) {
		player.setMaxHealth(30.0d);
		player.setHealth(player.getMaxHealth());
		MessageHandler.alert(AccountHandler.getPrefix(player) + " &6opened \"&e" + getName() + "&6\" from the killstreak selector");
		MessageHandler.sendMessage(player, "&b" + getName() + ": &a+5 extra hearts until you die");
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if(player.getMaxHealth() == 30.0d) {
			player.setMaxHealth(20.0d);
		}
	}
}