package network.server.servers.hub;

import network.Network;
import network.ProPlugin;
import network.player.LevelHandler;
import network.player.TeamScoreboardHandler;
import network.server.DailyRewards;
import network.server.ServerLogger;
import network.server.servers.hub.crate.Crate;
import network.server.servers.hub.crate.KeyExchange;
import network.server.servers.hub.crate.KeyFragments;
import network.server.servers.hub.items.Features;
import network.server.servers.hub.items.GameSelector;
import network.server.servers.hub.items.HubSelector;
import network.server.servers.hub.items.Profile;
import network.server.servers.hub.parkours.EndlessParkour;
import network.server.util.FileHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;

public class HubBase extends ProPlugin {
	private static int hubNumber = 0;
	
	public HubBase(String name) {
		super(name);
		addGroup("24/7");
		addGroup("hub");
		setAllowItemSpawning(true);
		setAllowInventoryClicking(true);
		resetWorld(12250);
		hubNumber = Integer.valueOf(Network.getServerName().replaceAll("[^\\d.]", ""));
		LevelHandler.enable();
		World world = Bukkit.getWorlds().get(0);
		new Events();
		new Crate();
		new KeyExchange();
		new KeyFragments();
		new GameSelector();
		new Features();
		new Profile();
		new HubSelector();
		new ServerLogger();
		new DailyRewards(new Location(world, 1684.5, 5, -1295.5));
		new TeamScoreboardHandler();
		new ParkourNPC();
		new EndlessParkour();
		new RecentSupporters();
		new Flag();
		if(hubNumber == 1) {
			new Server();
		}
	}
	
	public static int getHubNumber() {
		return hubNumber;
	}
	
	@Override
	public void disable() {
		String container = Bukkit.getWorldContainer().getPath();
		Bukkit.unloadWorld(Bukkit.getWorlds().get(0), false);
		FileHandler.delete(new File(container + "/spawn"));
		FileHandler.copyFolder(new File("/root/resources/maps/hub"), new File(container + "/spawn"));
		super.disable();
	}
}
