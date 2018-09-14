package network.server.servers.hub;

import network.customevents.TimeEvent;
import network.player.account.AccountHandler;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EventUtil;
import network.server.util.FileHandler;
import network.server.util.ImageMap;
import network.server.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

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

public abstract class RecentSupporters implements Listener {
	private List<ItemFrame> itemFrames = null;
	private List<ArmorStand> nameStands = null;
	private List<ArmorStand> packageStands = null;
	private List<String> packageNames = null;
	private Color color = null;
	private int updateDelayInMinutes = 5;

	public RecentSupporters(Location [] locations, Vector[] nameDistances, String [] titles, Color color, int updateDelayInMinutes) {
		itemFrames = new ArrayList<ItemFrame>();
		nameStands = new ArrayList<ArmorStand>();
		packageStands = new ArrayList<ArmorStand>();
		packageNames = Arrays.asList(titles);
		this.color = color;
		this.updateDelayInMinutes = updateDelayInMinutes;
		
		World world = Bukkit.getWorlds().get(0);

		for(Location location : locations) {
			itemFrames.add(ImageMap.getItemFrame(location));
		}

		try {
			for(int a = 0; a < 3; ++a) {
				Location location = itemFrames.get(a).getLocation();

				ArmorStand armorStand = (ArmorStand) world.spawnEntity(location.clone().add(nameDistances[a]), EntityType.ARMOR_STAND);
				setUpArmorStand(armorStand);
				packageStands.add(armorStand);

				armorStand = (ArmorStand) world.spawnEntity(location.clone().add(nameDistances[a + 3]), EntityType.ARMOR_STAND);
				setUpArmorStand(armorStand);
				nameStands.add(armorStand);
			}

			update();
			EventUtil.register(this);
		} catch(Exception e) {
			e.printStackTrace();
			Bukkit.getLogger().info("ARE ITEM FRAMES MISSING?");
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 * 60 * updateDelayInMinutes) {
			update();
		}
	}
	
	private void update() {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				List<UUID> uuids = getUUIDs();
				List<String> names = new ArrayList<String>();

				for(UUID uuid : uuids) {
					String name = AccountHandler.getName(uuid);
					names.add(name == null || name.equals("") ? "AlexzanderFlores" : name);
				}
				
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
//					new ImageMap(itemFrames.get(a), "Supporter " + a, loadImage(uuids.get(a), a, color), 3, 4);
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
	
	public static String loadImage(UUID uuid, int index, Color color) {
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

		// Download as a file so other servers on the same box can access it without an additional API call
		FileHandler.downloadImage(url, path);

		try {
			File file = new File(path);
			BufferedImage image = ImageIO.read(file);
			image = resizeImage(image);
			image = removeTransparency(image, color);

			ImageIO.write(image, "png", file);

			return file.getPath();
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static BufferedImage resizeImage(Image originalImage) {
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

	private static BufferedImage removeTransparency(BufferedImage image, Color color) {
		for(int y = 0; y < image.getHeight(); ++y) {
			for(int x = 0; x < image.getWidth(); ++x) {
				int argb = image.getRGB(x, y);

				if(color != null && ((argb >> 24) & 0xff) == 0) {
					image.setRGB(x, y, color.getRGB());
				} else {
					image.setRGB(x, y, argb);
				}
			}
		}

		return image;
	}

	public abstract List<UUID> getUUIDs();
}
