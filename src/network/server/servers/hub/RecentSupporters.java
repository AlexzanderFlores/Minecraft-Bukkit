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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class RecentSupporters implements Listener {
	private List<ItemFrame> itemFrames = null;
	private List<ArmorStand> nameStands = null;
	private List<ArmorStand> packageStands = null;
	private List<String> packageNames = null;

	RecentSupporters() {
		itemFrames = new ArrayList<ItemFrame>();
		nameStands = new ArrayList<ArmorStand>();
		packageStands = new ArrayList<ArmorStand>();
		packageNames = Arrays.asList("Recent Customer &a/buy", "Recent Voter &a/vote", "Recently Joined Discord &a/discord");
		
		World world = Bukkit.getWorlds().get(0);
		
		itemFrames.add(ImageMap.getItemFrame(world, 1689, 8, -1260));
		itemFrames.add(ImageMap.getItemFrame(world, 1685, 8, -1260));
		itemFrames.add(ImageMap.getItemFrame(world, 1681, 8, -1260));
		
		try {
			packageStands.add((ArmorStand) world.spawnEntity(itemFrames.get(0).getLocation().clone().add(-1, -2, -2.5), EntityType.ARMOR_STAND));
			packageStands.add((ArmorStand) world.spawnEntity(itemFrames.get(1).getLocation().clone().add(-1, -2, -2.5), EntityType.ARMOR_STAND));
			packageStands.add((ArmorStand) world.spawnEntity(itemFrames.get(2).getLocation().clone().add(-1, -2, -2.5), EntityType.ARMOR_STAND));
			for(ArmorStand armorStand : packageStands) {
				setUpArmorStand(armorStand);
			}

			nameStands.add((ArmorStand) world.spawnEntity(packageStands.get(0).getLocation().clone().add(0, .3, 0), EntityType.ARMOR_STAND));
			nameStands.add((ArmorStand) world.spawnEntity(packageStands.get(1).getLocation().clone().add(0, .3, 0), EntityType.ARMOR_STAND));
			nameStands.add((ArmorStand) world.spawnEntity(packageStands.get(2).getLocation().clone().add(0, .3, 0), EntityType.ARMOR_STAND));
			for(ArmorStand armorStand : nameStands) {
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
		if(ticks == 20 * 60 * 5) {
			update();
		}
	}
	
	private void update() {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				List<UUID> uuids = new ArrayList<UUID>();
				List<String> names = new ArrayList<String>();

				uuids.add(UUID.fromString("ec286bfe-04ef-40d5-ab4c-e8d50148a499"));
				uuids.add(UUID.fromString("ec286bfe-04ef-40d5-ab4c-e8d50148a499"));
				uuids.add(UUID.fromString("ec286bfe-04ef-40d5-ab4c-e8d50148a499"));

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
//					names.add(AccountHandler.getDisplay(uuid));
//				}
				for(int a = 0; a < 3; ++a) {
					new ImageMap(itemFrames.get(a), "Supporter " + a, loadImage(uuids.get(a), a), 3, 4);
					nameStands.get(a).setCustomName(StringUtil.color("&a" + names.get(a)));
					packageStands.get(a).setCustomName(StringUtil.color("&b" + packageNames.get(a)));
				}
				
				uuids.clear();
				names.clear();
			}
		}, 20 * 3);
	}
	
	private void setUpArmorStand(ArmorStand armorStand) {
		armorStand.setGravity(false);
		armorStand.setVisible(false);
		armorStand.setCustomNameVisible(true);
	}
	
	private BufferedImage loadImage(UUID uuid, int index) {
		String url;
		int scale = 10;

		switch(index) {
		case 0:
			url = "https://crafatar.com/renders/body/" + uuid + "?scale=" + scale;
			break;
		case 1:
			url = "https://crafatar.com/renders/body/" + uuid + "?scale=" + scale;
			break;
		case 2:
			url = "https://crafatar.com/renders/body/" + uuid + "?scale=" + scale;
			break;
		default:
			return null;
		}

		String path = Bukkit.getWorldContainer().getPath() + "/plugins/" + index + ".png";

		// Download as a file so other hubs on the same box can access it without an additional API call
		FileHandler.downloadImage(url, path);

		try {
			BufferedImage image = ImageIO.read(new File(path));
			image = resizeImage(image);
			image = removeTransparency(image);

			return image;
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private BufferedImage resizeImage(Image originalImage) {
		int originalHeight = originalImage.getHeight(null);
		int originalWidth = originalImage.getWidth(null);

		int size = 128;
		int height = size * 4;
		int width = size * 3;

		BufferedImage scaledBI = new BufferedImage(width, height, TYPE_INT_ARGB);
		Graphics2D g = scaledBI.createGraphics();
		g.drawImage(originalImage, (width - originalWidth) / 2, (height - originalHeight) / 2, originalWidth, originalHeight, null);
		g.dispose();
		return scaledBI;
	}

	private BufferedImage removeTransparency(BufferedImage image) {
		for(int y = 0; y < image.getHeight(); ++y) {
			for(int x = 0; x < image.getWidth(); ++x) {
				int argb = image.getRGB(x, y);

				if(((argb >> 24) & 0xff) == 0) {
					image.setRGB(x, y, new Color(0x312117).getRGB());
				} else {
					image.setRGB(x, y, argb);
				}
			}
		}

		return image;
	}
}
