package network.server.servers.hub;

import network.customevents.TimeEvent;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EventUtil;
import network.server.util.FileHandler;
import network.server.util.ImageMap;
import network.server.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RecentSupporters implements Listener {
	private List<ItemFrame> itemFrames = null;
	private List<ArmorStand> nameStands = null;
	private List<ArmorStand> packageStands = null;
	
	public RecentSupporters() {
		itemFrames = new ArrayList<ItemFrame>();
		nameStands = new ArrayList<ArmorStand>();
		packageStands = new ArrayList<ArmorStand>();
		
		World world = Bukkit.getWorlds().get(0);
		
		itemFrames.add(ImageMap.getItemFrame(world, 1689, 8, -1260));
		itemFrames.add(ImageMap.getItemFrame(world, 1685, 8, -1260));
		itemFrames.add(ImageMap.getItemFrame(world, 1681, 8, -1260));
		
		try {
			nameStands.add((ArmorStand) world.spawnEntity(itemFrames.get(0).getLocation().clone().add(1, 6, -1), EntityType.ARMOR_STAND));
			nameStands.add((ArmorStand) world.spawnEntity(itemFrames.get(1).getLocation().clone().add(1, 6, -1), EntityType.ARMOR_STAND));
			nameStands.add((ArmorStand) world.spawnEntity(itemFrames.get(2).getLocation().clone().add(1, 6, -1), EntityType.ARMOR_STAND));
			for(ArmorStand armorStand : nameStands) {
				setUpArmorStand(armorStand);
			}

			packageStands.add((ArmorStand) world.spawnEntity(nameStands.get(0).getLocation().clone().add(0, 5, -1), EntityType.ARMOR_STAND));
			packageStands.add((ArmorStand) world.spawnEntity(nameStands.get(1).getLocation().clone().add(0, 5, -1), EntityType.ARMOR_STAND));
			packageStands.add((ArmorStand) world.spawnEntity(nameStands.get(2).getLocation().clone().add(0, 5, -1), EntityType.ARMOR_STAND));
			for(ArmorStand armorStand : packageStands) {
				setUpArmorStand(armorStand);
			}

			update();
			EventUtil.register(this);
		} catch(Exception e) {
			e.printStackTrace();
			Bukkit.getLogger().info("ARE THE RECENT SUPPORTER BLOCKS ARE MISSING?");
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 * 60 * 10) {
			update();
		}
	}
	
	private void update() {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				List<UUID> uuids = new ArrayList<UUID>();
				List<String> packageNames = new ArrayList<String>();
				List<String> names = new ArrayList<String>();

				uuids.add(UUID.fromString("ec286bfe-04ef-40d5-ab4c-e8d50148a499"));
				uuids.add(UUID.fromString("ec286bfe-04ef-40d5-ab4c-e8d50148a499"));
				uuids.add(UUID.fromString("ec286bfe-04ef-40d5-ab4c-e8d50148a499"));
				packageNames.add("VIP+");
				packageNames.add("VIP+");
				packageNames.add("VIP+");
				names.add("AlexzanderFlores");
				names.add("AlexzanderFlores");
				names.add("AlexzanderFlores");
				
//				ResultSet resultSet = null;
//				try {
//					resultSet = DB.Databases.NETWORK.getConnection().prepareStatement("SELECT uuid,package FROM recent_supporters ORDER BY id DESC LIMIT 3").executeQuery();
//					while(resultSet.next()) {
//						uuids.add(UUID.fromString(resultSet.getString("uuid")));
//						packageNames.add(resultSet.getString("package"));
//					}
//				} catch(SQLException e) {
//					Bukkit.getLogger().info(e.getMessage());
//				} finally {
//					DB.close(resultSet);
//				}
				
//				for(UUID uuid : uuids) {
//					names.add(AccountHandler.getName(uuid));
//				}
				for(int a = 0; a < 3; ++a) {
					new ImageMap(itemFrames.get(a), "Supporter " + a, loadImage(uuids.get(a), a), 3, 4);
					nameStands.get(a).setCustomName(StringUtil.color("&a&l" + names.get(a)));
					packageStands.get(a).setCustomName(StringUtil.color("&b&l" + packageNames.get(a)));
				}
				
				uuids.clear();
				packageNames.clear();
				names.clear();
			}
		}, 20 * 3);
	}
	
	private void setUpArmorStand(ArmorStand armorStand) {
		armorStand.setGravity(false);
		armorStand.setVisible(false);
		armorStand.setCustomNameVisible(true);
	}
	
	private String loadImage(UUID uuid, int index) {
		String url = "";
		switch(index) {
		case 0:
			url = "https://crafatar.com/renders/body/" + uuid + "?scale=10";
			break;
		case 1:
			url = "https://crafatar.com/renders/body/" + uuid + "?scale=10";
			break;
		case 2:
			url = "https://crafatar.com/renders/body/" + uuid + "?scale=10";
			break;
		default:
			return null;
		}
		String path = Bukkit.getWorldContainer().getPath() + "/plugins/" + index + ".png";
		FileHandler.downloadImage(url, path);
		return path;
	}
}
