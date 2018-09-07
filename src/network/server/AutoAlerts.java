package network.server;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import network.Network;
import network.customevents.TimeEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EventUtil;
import network.server.util.StringUtil;

public class AutoAlerts implements Listener {
	public class AutoAlert {
		private String id = null;
		private String text = null;
		private int howOften = 0;
		private int counter = 0;
		private boolean saved = false;
		
		public AutoAlert(String id) {
			this.id = id;
			if(alerts == null) {
				alerts = new HashMap<String, AutoAlert>();
			}
			alerts.put(getID(), this);
		}
		
		public String getID() {
			return this.id;
		}
		
		public String getText() {
			return this.text;
		}
		
		public void setText(String text) {
			this.text = text;
		}
		
		public int getHowOften() {
			return this.howOften;
		}
		
		public void setHowOften(int howOften) {
			this.howOften = howOften;
		}
		
		public int getCounter() {
			return this.counter;
		}
		
		public int incrementCounter() {
			return ++this.counter;
		}
		
		public String getInformation() {
			return "&eID: &c" + getID() + " &eHow often: &c" + getHowOften() + " &eText: &c\"" + getText() + "&c\" &eSaved: " + (isSaved() ? "&aYes" : "&cNo");
		}
		
		public void save() {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
//					if(DB.NETWORK_AUTO_ALERTS.isKeySet("text_id", getID())) {
//						DB.NETWORK_AUTO_ALERTS.updateInt("how_often", getHowOften(), "text_id", getID());
//						DB.NETWORK_AUTO_ALERTS.updateString("text", getText(), "text_id", getID());
//					} else {
//						DB.NETWORK_AUTO_ALERTS.insert("'" + getID() + "', '" + getHowOften() + "', '" + getText() + "'");
//					}
					saved = true;
				}
			});
		}
		
		public boolean isSaved() {
			return this.saved;
		}
		
		public void setSaved(boolean saved) {
			this.saved = saved;
		}
		
		public void delete() {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
//					DB.NETWORK_AUTO_ALERTS.delete("text_id", id);
					id = null;
					text = null;
					howOften = 0;
					counter = 0;
					saved = false;
				}
			});
		}
	}
	
	private static Map<String, AutoAlert> alerts = null;
	
	public AutoAlerts() {
		if(Network.getServerName().equals("HUB1")) {
			new CommandBase("aa", -1) {
				@Override
				public boolean execute(CommandSender sender, String [] arguments) {
					if(arguments.length == 2 && arguments[0].equalsIgnoreCase("create")) {
						String id = arguments[1];
						if(alerts != null && alerts.containsKey(id)) {
							MessageHandler.sendMessage(sender, "&cAn alert with the ID \"&e" + id + "&c\" already exists");
						} else {
							new AutoAlert(id);
							MessageHandler.sendMessage(sender, "Created auto alert with id \"&e" + id + "&a\"");
							MessageHandler.sendMessage(sender, "Next do &e/aa setHowOften " + id + " <minutes>");
						}
					} else if(arguments.length == 2 && arguments[0].equalsIgnoreCase("delete")) {
						String id = arguments[1];
						if(alerts != null && alerts.containsKey(id)) {
							alerts.get(id).delete();
							alerts.remove(id);
							MessageHandler.sendMessage(sender, "You have deleted auto alert \"&e" + id + "&a\"");
						} else {
							MessageHandler.sendMessage(sender, "&cThere is no auto alert with the ID \"&e" + id + "&c\"");
						}
					} else if(arguments.length == 3 && arguments[0].equalsIgnoreCase("setHowOften")) {
						String id = arguments[1];
						if(alerts != null && alerts.containsKey(id)) {
							try {
								int minutes = Integer.valueOf(arguments[2]);
								alerts.get(id).setHowOften(minutes);
								MessageHandler.sendMessage(sender, "The alert \"&e" + id + "&a\" will now run every " + minutes + " minute" + (minutes == 1 ? "" : "s"));
								MessageHandler.sendMessage(sender, "Next do &e/aa set " + id + " <text>");
							} catch(NumberFormatException e) {
								MessageHandler.sendMessage(sender, "&f/aa setHowOften " + id + " <minutes>");
							}
						} else {
							MessageHandler.sendMessage(sender, "&cThere is no auto alert with the ID \"&e" + id + "&c\"");
						}
					} else if(arguments.length == 2 && arguments[0].equalsIgnoreCase("getHowOften")) {
						String id = arguments[1];
						if(alerts != null && alerts.containsKey(id)) {
							int howOften = alerts.get(id).getHowOften();
							MessageHandler.sendMessage(sender, "The alert \"&e" + id + "&a\" alerts every " + howOften + " minute" + (howOften == 1 ? "" : "s"));
						} else {
							MessageHandler.sendMessage(sender, "&cThere is no auto alert with the ID \"&e" + id + "&c\"");
						}
					} else if(arguments.length >= 3 && arguments[0].equalsIgnoreCase("set")) {
						String id = arguments[1];
						if(alerts != null && alerts.containsKey(id)) {
							String text = "";
							for(int a = 2; a < arguments.length; ++a) {
								text += arguments[a] + " ";
							}
							text = StringUtil.color(text.substring(0, text.length() - 1));
							alerts.get(id).setText(text);
							MessageHandler.sendMessage(sender, "Set alert \"&e" + id + "&a\" to text \"" + text + "&a\"");
							MessageHandler.sendMessage(sender, "Next do &e/aa save " + id);
						} else {
							MessageHandler.sendMessage(sender, "&cThere is no auto alert with the ID \"&e" + id + "&c\"");
						}
					} else if(arguments.length == 2 && arguments[0].equalsIgnoreCase("save")) {
						String id = arguments[1];
						if(alerts != null && alerts.containsKey(id)) {
							alerts.get(id).save();
							MessageHandler.sendMessage(sender, "Saved alert \"&e" + id + "&a\"");
						} else {
							MessageHandler.sendMessage(sender, "&cThere is no auto alert with the ID \"&e" + id + "&c\"");
						}
					} else if(arguments.length == 1 && arguments[0].equalsIgnoreCase("list")) {
						if(alerts == null || alerts.isEmpty()) {
							MessageHandler.sendMessage(sender, "&cThere are currently no auto alerts");
						} else {
							for(AutoAlert alert : alerts.values()) {
								MessageHandler.sendMessage(sender, alert.getInformation());
							}
						}
					} else {
						MessageHandler.sendMessage(sender, "1) /aa create <text id>");
						MessageHandler.sendMessage(sender, "2) /aa setHowOften <text id> <minutes>");
						MessageHandler.sendMessage(sender, "3) /aa set <text id> <text>");
						MessageHandler.sendMessage(sender, "4) /aa save <text id>");
						MessageHandler.sendMessage(sender, "");
						MessageHandler.sendMessage(sender, "/aa help");
						MessageHandler.sendMessage(sender, "/aa list");
						MessageHandler.sendMessage(sender, "/aa delete <text id>");
						MessageHandler.sendMessage(sender, "/aa getHowOften <text id>");
					}
					return true;
				}
			}.setRequiredRank(Ranks.OWNER);
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
//					for(String id : DB.NETWORK_AUTO_ALERTS.getAllStrings("text_id")) {
//						AutoAlert alert = new AutoAlert(id);
//						alert.setHowOften(DB.NETWORK_AUTO_ALERTS.getInt("text_id", id, "how_often"));
//						alert.setText(DB.NETWORK_AUTO_ALERTS.getString("text_id", id, "text"));
//						alert.setSaved(true);
//					}
				}
			});
			EventUtil.register(this);
		} else {
			new CommandBase("aa", -1) {
				@Override
				public boolean execute(CommandSender sender, String [] arguments) {
					MessageHandler.sendMessage(sender, "&cYou can only run this on HUB 1 &e/hub 1");
					return true;
				}
			}.setRequiredRank(Ranks.OWNER);
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			if(alerts != null) {
				for(AutoAlert alert : alerts.values()) {
					if(alert.getHowOften() > 0) {
						if(alert.incrementCounter() % (alert.getHowOften() * 60) == 0) {
							AlertHandler.alert(alert.getText());
						}
					}
				}
			}
		}
	}
}
