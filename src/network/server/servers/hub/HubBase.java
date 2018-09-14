package network.server.servers.hub;

import network.Network;
import network.ProPlugin;
import network.customevents.player.AsyncPlayerJoinEvent;
import network.player.LevelHandler;
import network.player.MessageHandler;
import network.player.TeamScoreboardHandler;
import network.player.account.AccountHandler;
import network.server.CommandBase;
import network.server.DailyRewards;
import network.server.ServerLogger;
import network.server.effects.images.DisplayImage;
import network.server.effects.images.DisplaySkin;
import network.server.servers.hub.crate.CrateTypes;
import network.server.servers.hub.crate.KeyFragments;
import network.server.servers.hub.items.Features;
import network.server.servers.hub.items.GameSelector;
import network.server.servers.hub.items.HubSelector;
import network.server.servers.hub.items.Profile;
import network.server.servers.hub.parkours.EndlessParkour;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.ImageMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;
import org.inventivetalent.animatedframes.AnimatedFrame;
import org.inventivetalent.animatedframes.AnimatedFramesPlugin;
import org.inventivetalent.animatedframes.Callback;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class HubBase extends ProPlugin {
	private static int hubNumber = 0;
	private boolean hasPlayerJoined = false;
	
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

//		Location [] locations = new Location [] {
//				new Location(world, 1689, 8, -1260),
//				new Location(world, 1685, 8, -1260),
//				new Location(world, 1681, 8, -1260)
//		};
//
//		Vector [] nameDistances = new Vector [] {
//				new Vector(-1, -2, -2.5),
//				new Vector(-1, -2, -2.5),
//				new Vector(-1, -2, -2.5),
//
//				new Vector(-1, -2.3, -2.5),
//				new Vector(-1, -2.3, -2.5),
//				new Vector(-1, -2.3, -2.5)
//		};
//
//		new RecentSupporters(locations, nameDistances, new String [] {
//				"Recent Customer &a/buy",
//				"Recent Voter &a/vote",
//				"Recently Joined Discord &a/discord"
//		}, new Color(0x312117), 5) {
//			@Override
//			public List<UUID> getUUIDs() {
//				List<UUID> uuids = new ArrayList<UUID>();
//
//				String rez = "11603007-81b5-45fe-b17e-91ea8972143d";
//				String leet = "ec286bfe-04ef-40d5-ab4c-e8d50148a499";
//
//				uuids.add(UUID.fromString(new Random().nextBoolean() ? leet : rez));
//				uuids.add(UUID.fromString(new Random().nextBoolean() ? leet : rez));
//				uuids.add(UUID.fromString(new Random().nextBoolean() ? leet : rez));
//
//				return uuids;
//			}
//		};

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

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(hasPlayerJoined) {
			return;
		}
		hasPlayerJoined = true;

		World world = event.getPlayer().getWorld();

		Map<Integer, Location []> locations = new HashMap<Integer, Location []>();

		locations.put(1, new Location [] {
				new Location(world, 1689, 5, -1260),
				new Location(world, 1687, 8, -1260)
		});

		locations.put(2, new Location [] {
				new Location(world, 1685, 5, -1260),
				new Location(world, 1683, 8, -1260)
		});

		locations.put(3, new Location [] {
				new Location(world, 1681, 5, -1260),
				new Location(world, 1679, 8, -1260),
		});

		new DisplaySkin(
				locations.get(1)[0],
				locations.get(1)[1],
				event.getPlayer().getUniqueId(),
				new Color(0x312117)
		).display();

		new DisplaySkin(
				locations.get(2)[0],
				locations.get(2)[1],
				UUID.fromString("6330b959-8daa-4cb0-9112-cf28f5185384"),
				new Color(0x312117)
		).display();

		new DisplaySkin(
				locations.get(3)[0],
				locations.get(3)[1],
				UUID.fromString("10924f26-1c86-4025-8f9d-8b3a86b83810"),
				new Color(0x312117)
		).display();

		// /setSkin <uuid> <1 - 3>
		new CommandBase("setSkin", 2) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				UUID uuid = UUID.fromString(arguments[0]);
				int index = Integer.valueOf(arguments[1]);

				new DisplaySkin(
						locations.get(index)[0],
						locations.get(index)[1],
						uuid,
						new Color(0x312117)
				).display();
				return true;
			}
		};
	}
}
