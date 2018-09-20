package network.gameapi.competitive;

import java.util.List;

import com.sainttx.holograms.api.Hologram;
import com.sainttx.holograms.api.line.TextLine;
import org.bukkit.Location;

import network.gameapi.competitive.StatsHandler.StatTimes;
import network.server.util.StringUtil;

public class StatDisplayer {
	public StatDisplayer(List<Location> locations) {
		for(int a = 0; a < StatTimes.values().length; ++a) {
			StatTimes time = StatTimes.values()[a];
			Location location = locations.get(a).clone();
			Hologram hologram = new Hologram("stats_" + time, location);
			hologram.addLine(new TextLine(hologram, StringUtil.color("&e" + (time == StatTimes.LIFETIME ? "Top 10 &7(&bLifetime" : time == StatTimes.MONTHLY ? "Top 10 &7(&bMonthly" : "Top 10 &7(&bWeekly") + "&7)")));
			for(String top : StatsHandler.getTop10(time)) {
				hologram.addLine(new TextLine(hologram, StringUtil.color(top)));
			}
		}
	}
}