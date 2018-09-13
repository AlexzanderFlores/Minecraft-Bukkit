package network.staff;

import network.ProPlugin;
import network.anticheat.AntiCheatBase;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.ChatClickHandler;
import network.server.DB;
import network.server.util.EventUtil;
import network.staff.ban.BanHandler;
import network.staff.ban.BanListener;
import network.staff.ban.UnBanHandler;
import network.staff.mute.MuteHandler;
import network.staff.mute.ServerMute;
import network.staff.mute.ShadowMuteHandler;
import network.staff.mute.UnMuteHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.UUID;

public class Punishment implements Listener {
	public enum ChatViolations {
		DISRESPECT(0, 1),
		DEATH_COMMENTS(0, 1),
		INAPPROPRIATE(0, 1),
		SPAM(0, 1),
		ADVERTISEMENT(0, 1),
		DDOS_THREATS(1, 0);
		
		private int days = 0;
		private int hours = 0;
		
		private ChatViolations(int days, int hours) {
			this.days = days;
			this.hours = hours;
		}
		
		public int getDays() {
			return this.days;
		}
		
		public int getHours() {
			return this.hours;
		}
	}
	
	public class PunishmentExecuteReuslts {
		private boolean valid = false;
		private UUID uuid = null;
		
		public PunishmentExecuteReuslts(boolean valid, UUID uuid) {
			this.valid = valid;
			this.uuid = uuid;
		}
		
		public boolean isValid() {
			return this.valid;
		}
		
		public UUID getUUID() {
			return this.uuid;
		}
	}
	
	public static final String appeal = "Coming soon";
	private String name = null;
	
	public Punishment() {
		new BanHandler();
		new UnBanHandler();
		new MuteHandler();
		new UnMuteHandler();
		new SpamPrevention();
		new ReportHandler();
		new ViolationPrevention();
		new CommandLogger();
		new StaffMode();
		new ShadowMuteHandler();
		new BanListener();
		new StaffChat();
		new AntiCheatBase();
		new ServerMute();
		new BadNameHandler();
		new ClearChat();
	}
	
	public Punishment(String name) {
		this.name = name;
		EventUtil.register(this);
	}
	
	protected String getName() {
		return this.name;
	}
	
	protected String getReason(Ranks rank, String [] arguments, String reason, PunishmentExecuteReuslts result) {
		return getReason(rank, arguments, reason, result, false);
	}
	
	protected String getReason(Ranks rank, String [] arguments, String reason, PunishmentExecuteReuslts result, boolean reversingPunishment) {
		Ranks playerRank = AccountHandler.getRank(result.getUUID());
		String message = playerRank.getColor() + arguments[0] + "&x was &c" + getName();
		if(reason != null && !reason.equals("")) {
			message += "&x for " + reason;
		}
		return message;
	}
	
	protected PunishmentExecuteReuslts executePunishment(CommandSender sender, String [] arguments, boolean reversingPunishment) {
		UUID uuid = null;
		Player player = ProPlugin.getPlayer(arguments[0]);
		if(player == null) {
			uuid = AccountHandler.getUUID(arguments[0]);
		} else {
			uuid = player.getUniqueId();
		}
		if(uuid == null) {
			MessageHandler.sendMessage(sender, "&cNo player data found for " + arguments[0]);
		} else {
			if(!reversingPunishment && Bukkit.getPlayer(uuid) == null && DB.PLAYERS_LOCATIONS.isUUIDSet(uuid)) {
				String server = DB.PLAYERS_LOCATIONS.getString("uuid", uuid.toString(), "location");
				if(sender instanceof Player) {
					Player staff = (Player) sender;
					ChatClickHandler.sendMessageToRunCommand(staff, "&6" + server, "Click to be sent to " + server, "/join " + server, "&cThat player is connected to ");
				} else {
					MessageHandler.sendMessage(sender, "&cThat player is connected to &6" + server);
				}
			} else {
				return new PunishmentExecuteReuslts(true, uuid);
			}
		}
		return new PunishmentExecuteReuslts(false, null);
	}
}
