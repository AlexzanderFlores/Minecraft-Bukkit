package network.server.servers.hub;

import network.Network;
import network.ProPlugin;
import network.player.LevelHandler;
import network.player.TeamScoreboardHandler;
import network.server.*;
import network.server.servers.hub.crate.CrateTypes;
import network.server.servers.hub.crate.KeyFragments;
import network.server.servers.hub.items.Features;
import network.server.servers.hub.items.GameSelector;
import network.server.servers.hub.items.HubSelector;
import network.server.servers.hub.items.Profile;
import network.server.servers.hub.parkours.EndlessParkour;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

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

		CrateTypes.VOTING.setBeacon(world.getBlockAt(1651, 6, -1278), new Vector(0.85, 2.5, 0.5));
		CrateTypes.PREMIUM.setBeacon(world.getBlockAt(1651, 6, -1284), new Vector(0.85, 2.5, 0.5));

		new Events();
		new KeyFragments();
		new GameSelector();
		new Features();
		new Profile();
		new HubSelector();
		new ServerLogger();
		new DailyRewards(new Location(world, 1658, 5, -1277));
		new TeamScoreboardHandler();
		new ParkourNPC();
		new EndlessParkour();
		new Flag();
	}
	
	public static int getHubNumber() {
		return hubNumber;
	}
	
//	@Override
//	public void disable() {
//		String container = Bukkit.getWorldContainer().getPath();
//		Bukkit.unloadWorld(Bukkit.getWorlds().get(0), false);
//		FileHandler.delete(new File(container + "/spawn"));
//		FileHandler.copyFolder(new File("/root/resources/maps/hub"), new File(container + "/spawn"));
//		super.disable();
//	}
}
