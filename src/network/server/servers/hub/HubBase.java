package network.server.servers.hub;

import network.Network;
import network.ProPlugin;
import network.player.LevelHandler;
import network.player.TeamScoreboardHandler;
import network.server.DailyRewards;
import network.server.ServerLogger;
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

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

		Location [] locations = new Location [] {
				new Location(world, 1689, 8, -1260),
				new Location(world, 1685, 8, -1260),
				new Location(world, 1681, 8, -1260)
		};

		Vector [] nameDistances = new Vector [] {
				new Vector(-1, -2, -2.5),
				new Vector(-1, -2, -2.5),
				new Vector(-1, -2, -2.5),

				new Vector(-1, -2.3, -2.5),
				new Vector(-1, -2.3, -2.5),
				new Vector(-1, -2.3, -2.5)
		};

		new RecentSupporters(locations, nameDistances, new String [] {
				"Recent Customer &a/buy",
				"Recent Voter &a/vote",
				"Recently Joined Discord &a/discord"
		}, new Color(0x312117), 5) {
			@Override
			public List<UUID> getUUIDs() {
				List<UUID> uuids = new ArrayList<UUID>();
				uuids.add(UUID.fromString("ec286bfe-04ef-40d5-ab4c-e8d50148a499"));
				uuids.add(UUID.fromString("ec286bfe-04ef-40d5-ab4c-e8d50148a499"));
				uuids.add(UUID.fromString("ec286bfe-04ef-40d5-ab4c-e8d50148a499"));
				return uuids;
			}
		};

		if(hubNumber == 1) {
			new Server();
		}
	}
	
	public static int getHubNumber() {
		return hubNumber;
	}
	
	@Override
	public void disable() {
//		String container = Bukkit.getWorldContainer().getPath();
//		Bukkit.unloadWorld(Bukkit.getWorlds().get(0), false);
//		FileHandler.delete(new File(container + "/spawn"));
//		FileHandler.copyFolder(new File("/root/resources/maps/hub"), new File(container + "/spawn"));
		super.disable();
	}
}
