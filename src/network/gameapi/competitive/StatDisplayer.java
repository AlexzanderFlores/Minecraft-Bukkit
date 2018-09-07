package network.gameapi.competitive;

import java.util.List;

import org.bukkit.Location;

import de.inventivegames.hologram.Hologram;
import de.inventivegames.hologram.HologramAPI;
import network.gameapi.competitive.StatsHandler.StatTimes;
import network.server.util.StringUtil;

public class StatDisplayer {
	public StatDisplayer(List<Location> locations) {
		for(int a = 0; a < StatTimes.values().length; ++a) {
			StatTimes time = StatTimes.values()[a];
			Location location = locations.get(a).clone();
			HologramAPI.createHologram(location, StringUtil.color("&e" + (time == StatTimes.LIFETIME ? "Top 10 &7(&bLifetime" : time == StatTimes.MONTHLY ? "Top 10 &7(&bMonthly" : "Top 10 &7(&bWeekly") + "&7)")).spawn();
			for(String top : StatsHandler.getTop10(time)) {
				location = location.add(0, -0.35, 0);
				Hologram hologram = HologramAPI.createHologram(location, StringUtil.color(top));
				hologram.spawn();
			}
		}
	}
}