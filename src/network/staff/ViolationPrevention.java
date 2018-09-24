package network.staff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import network.Network;
import network.customevents.player.AsyncPlayerLeaveEvent;
import network.player.account.AccountHandler.Ranks;
import network.server.DB;
import network.server.util.EventUtil;

public class ViolationPrevention implements Listener {
	private static List<String> mutes = null;
	private static List<String> extentions = null;
	private List<String> hasHadTicketCreated = null;
	private Map<String, List<String>> blockedMessages = null;

	public ViolationPrevention() {
		mutes = new ArrayList<String>();
		mutes.add("do your job");
		mutes.add("do ur job");
		mutes.add("doyourjob");
		mutes.add("dourjob");
		mutes.add("bad staff");
		mutes.add("badstaff");
		mutes.add("horrible staff");
		mutes.add("horriblestaff");
		mutes.add("autistic");
		mutes.add("autism");
		mutes.add("autist");
		mutes.add("cancer");
		mutes.add("kys");
		mutes.add("sperm");
		mutes.add("kill your self");
		mutes.add("kill yourself");
		mutes.add("kill urself");
		mutes.add("kill self");
		mutes.add("killself");
		mutes.add("killur self");
		mutes.add("killurself");
		mutes.add("kill ur self");
		mutes.add("hang your self");
		mutes.add("hang yourself");
		mutes.add("hang urself");
		mutes.add("hang self");
		mutes.add("hangself");
		mutes.add("hangur self");
		mutes.add("hangurself");
		mutes.add("hang ur self");
		mutes.add("bleach");
		mutes.add("blaech");
		mutes.add("bastard");
		mutes.add("jack off");
		mutes.add("jackoff");
		mutes.add("jacking off");
		mutes.add("jackingoff");
		mutes.add("cunt");
		mutes.add("die");
		mutes.add("hoe");
		mutes.add("hoebag");
		mutes.add("faget");
		mutes.add("fagget");
		mutes.add("faggot");
		mutes.add("faqqot");
		mutes.add("faqqet");
		mutes.add("fag");
		mutes.add("feg");
		mutes.add("fagt");
		mutes.add("fagi");
		mutes.add("fg");
		mutes.add("fgt");
		mutes.add("bitch");
		mutes.add("bish");
		mutes.add("bitchy");
		mutes.add("btch");
		mutes.add("btchy");
		mutes.add("betch");
		mutes.add("betchy");
		mutes.add("diet");
		mutes.add("whore");
		mutes.add("slut");
		mutes.add("asshole");
		mutes.add("douche");
		mutes.add("douchebag");
		mutes.add("cut");
		mutes.add("stfu");
		mutes.add("kkk");
		mutes.add("nig");
		mutes.add("nigger");
		mutes.add("nigr");
		mutes.add("nigg");
		mutes.add("nigga");
		mutes.add("niga");
		mutes.add("niqqa");
		mutes.add("negro");
		mutes.add("neggro");
		mutes.add("pussy");
		mutes.add("pussie");
		mutes.add("pussay");
		mutes.add("dick");
		mutes.add("dik");
		mutes.add("dic");
		mutes.add("anal");
		mutes.add("orgasm");
		mutes.add("clit");
		mutes.add("cum");
		mutes.add("fist me");
		mutes.add("fu");
		mutes.add("f u");
		mutes.add("fk u");
		mutes.add("fc u");
		mutes.add("fck u");
		mutes.add("f you");
		mutes.add("fk you");
		mutes.add("fc you");
		mutes.add("fck you");
		mutes.add("dumbass");
		mutes.add("cock");
		mutes.add("eat me");
		mutes.add("retard");
		mutes.add("retart");
		mutes.add("smd");
		mutes.add("smfd");
		mutes.add("thot");
		mutes.add("reach");
		mutes.add("runover");
		mutes.add("ranover");
		mutes.add("run over");
		mutes.add("ran over");
		mutes.add("scumbag");
		mutes.add("scum bag");
		mutes.add("finger me");
		mutes.add("fingerme");
		mutes.add("fuckhead");
		mutes.add("do your job");
		mutes.add("do ur job");
		mutes.add("do you're job");
		mutes.add("do youre job");
		mutes.add("ur throat");
		mutes.add("your throat");
		mutes.add("your throat");
		mutes.add("youre throat");
		mutes.add("you're throat");
		mutes.add("urthroat");
		mutes.add("yourthroat");
		mutes.add("yourthroat");
		mutes.add("yourethroat");
		mutes.add("you'rethroat");
		mutes.add("choad");
		mutes.add("homosexual");
		mutes.add("gay");
		mutes.add("ghey");
		mutes.add("homo");
		mutes.add("trash");
		mutes.add("fgot");
		mutes.add("queer");
		mutes.add("arse");
		mutes.add("fap");
		mutes.add("lube");
		mutes.add("chode");
		mutes.add("rekt");
		mutes.add("recked");
		mutes.add("rkt");
		mutes.add("r ekt");
		mutes.add("reekt");
		mutes.add("re kt");
		mutes.add("ez");
		mutes.add("e z");
		mutes.add("ezpz");
		mutes.add("ez pz");
		mutes.add("ks");
		mutes.add("gg10");
		mutes.add("10heart");
		mutes.add("10 heart");
		mutes.add("killaura");
		mutes.add("kill aura");
		mutes.add("kms");
		mutes.add("rape");
		mutes.add("rap");
		mutes.add("rapp");
		mutes.add("raip");
		mutes.add("whalecum");
		mutes.add("noob");
		mutes.add("suck");
		mutes.add("suk");
		mutes.add("suc");
		mutes.add("yousuck");
		mutes.add("you mad");
		mutes.add("u mad");
		mutes.add("ffs");
		mutes.add("try hard");
		mutes.add("tryhard");
		mutes.add("scrub");
		mutes.add("skrub");
		mutes.add("randy");
		mutes.add("randie");
		mutes.add("relt");
		mutes.add("not hard");
		mutes.add("ur bad");
		mutes.add("your bad");
		mutes.add("youre bad");
		mutes.add("so bad");
		mutes.add("they suck");
		mutes.add("you sux");
		mutes.add("u sux");
		mutes.add("ltop");
		mutes.add("learn to play");
		mutes.add("learn to pvp");
		mutes.add("stank");
		mutes.add("cleanme");
		mutes.add("clean me");
		mutes.add("clean up");
		mutes.add("cleanup");
		mutes.add("clean upper");
		mutes.add("clean uper");
		mutes.add("cleanupper");
		mutes.add("cleanuper");
		mutes.add("cleanning up");
		mutes.add("cleaning up");
		mutes.add("cleanningup");
		mutes.add("cleaningup");
		mutes.add("cleaned");
		mutes.add("dildo");
		mutes.add("dicked");
		mutes.add("i was low");
		mutes.add("go after me");
		mutes.add("shutup");
		mutes.add("shut up");
		mutes.add("tov1");
		mutes.add("3v1");
		mutes.add("4v1");
		mutes.add("5v1");
		mutes.add("6v1");
		mutes.add("fatty");
		mutes.add("nudes");
		mutes.add("sma");
		mutes.add("smfa");
		mutes.add("gtfo");
		mutes.add("salty");
		mutes.add("salt");
		mutes.add("omfg");
		mutes.add("getskill");
		mutes.add("get skill");
		mutes.add("fuk off");
		mutes.add("fuc off");
		mutes.add("f off");
		mutes.add("ping");
		mutes.add("spam");
		mutes.add("spamm");
		mutes.add("bowspam");
		mutes.add("bowspamm");
		mutes.add("bow spam");
		mutes.add("bow spamm");
		mutes.add("idiot");
		mutes.add("nub");
		mutes.add("frickoff");
		mutes.add("frick off");
		mutes.add("ass");
		mutes.add("queef");
		mutes.add("rek");
		mutes.add("ur dumb");
		mutes.add("your dumb");
		mutes.add("youre dumb");
		mutes.add("killsteal");
		mutes.add("kill steal");
		mutes.add("shit");
		mutes.add("shat");
		mutes.add("fuck");
		mutes.add("fuc");
		mutes.add("fuk");
		mutes.add("fukc");
		mutes.add("fucking");
		mutes.add("fucker");
		mutes.add("fek");
		mutes.add("fak");
		mutes.add("motherfucker");
		mutes.add("grow up");
		mutes.add("downy");
		mutes.add("smartass");
		mutes.add("get a life");
		mutes.add("go choke");
		mutes.add("cvnt");
		mutes.add("cnt");
		mutes.add("fisting");
		mutes.add("bite me");
		mutes.add("pinq");
		mutes.add("rek");
		mutes.add("screwoff");
		mutes.add("screw");
		mutes.add("getbanned");
		mutes.add("getbaned");
		mutes.add("youbanned");
		mutes.add("youbaned");
		mutes.add("ubanned");
		mutes.add("ubaned");
		mutes.add("get banned");
		mutes.add("get baned");
		mutes.add("you banned");
		mutes.add("you baned");
		mutes.add("u banned");
		mutes.add("u baned");
		mutes.add("hacs");
		mutes.add("rodspamm");
		mutes.add("rod spamm");
		mutes.add("rodspam");
		mutes.add("rod spam");
		mutes.add("fuckboy");
		mutes.add("tit");
		mutes.add("tits");
		mutes.add("tities");
		mutes.add("titties");
		mutes.add("crusty");
		mutes.add("penis");
		mutes.add("vagina");
		mutes.add("getgood");
		mutes.add("get good");
		mutes.add("getgud");
		mutes.add("get gud");
		mutes.add("antikb");
		mutes.add("anti kb");
		mutes.add("nokb");
		mutes.add("no kb");
		mutes.add("burn in hell");
		mutes.add("porn");
		mutes.add("ddos");
		mutes.add("toggle");
		mutes.add("bullshit");
		mutes.add("garb");
		mutes.add("garbage");
		mutes.add("gold dig");
		mutes.add("gold diger");
		mutes.add("gold digger");
		mutes.add("pedo");
		mutes.add("pedophile");
		mutes.add("blockhit");
		mutes.add("block hit");
		mutes.add("kick me");
		mutes.add("mute me");
		mutes.add("ban me");
		mutes.add("kickme");
		mutes.add("muteme");
		mutes.add("banme");
		mutes.add("sex");
		mutes.add("seck");
		mutes.add("aids");
		mutes.add("horny");
		mutes.add("aimbot");
		mutes.add("macro");
		extentions = new ArrayList<String>();
		extentions.add("s");
		extentions.add("ist");
		extentions.add("in");
		extentions.add("ing");
		extentions.add("er");
		extentions.add("ers");
		extentions.add("ed");
		extentions.add("es");
		hasHadTicketCreated = new ArrayList<String>();
		blockedMessages = new HashMap<String, List<String>>();
		EventUtil.register(this);
	}

	public static boolean contains(String msg) {
		for(String block : mutes) {
			if(msg.contains("target") || msg.equals(block) || msg.startsWith(block + " ") || msg.contains(" " + block + " ") || msg.endsWith(" " + block)) {
				return true;
			}
			for(String extention : extentions) {
				extention = block + extention;
				if(msg.equals(extention) || msg.startsWith(extention + " ") || msg.contains(" " + extention + " ") || msg.endsWith(" " + extention)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isAdvertisement(String msg) {
		return msg.contains("(dot)") || msg.contains("(.)") || (msg.replaceAll("\\D+", "").length() >= 8 && StringUtils.countMatches(msg, ".") == 3)
				|| (msg.contains(":") && msg.split(":").length == 2 && msg.split(":")[1].split(" ")[0].replaceAll("\\D+", "").length() == 5);
	}

	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String msg = ChatColor.stripColor(event.getMessage().toLowerCase().replace("?", "").replace("!", "").replace(".", "").replace("=", "")
				.replace(",", "").replace("\"", "").replace("*", "").replace("\'", "").replace("`", "").replace("$", "s")
				.replace("1", "i").replace("3", "e").replace("0", "o").replace("-", "").replace("/", "").replace("_", "").replace("2", "to").replace("too", "to").replace("two", "to"));
		if(contains(msg)) {
			List<String> messages = blockedMessages.get(player.getName());
			if(messages == null) {
				messages = new ArrayList<String>();
			}
			messages.add(msg);
			blockedMessages.put(player.getName(), messages);
			if(!Ranks.isStaff(player)) {
				for(Player online : Bukkit.getOnlinePlayers()) {
					if(!online.getName().equals(player.getName())) {
						event.getRecipients().remove(online);
					}
				}
			}
			return;
		}
		if(isAdvertisement(msg) && !Ranks.isStaff(player)) {
			for(Player online : Bukkit.getOnlinePlayers()) {
				if(!online.getName().equals(player.getName())) {
					event.getRecipients().remove(online);
				}
			}
			if(!hasHadTicketCreated.contains(player.getName())) {
				hasHadTicketCreated.add(player.getName());
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ticket create " + player.getName() + " CHAT_FILTER_DETECTION " + event.getMessage().replace(" ", "_"));
			}
		}
		boolean link = false;
		if(Pattern.compile("http[s]{0,1}://[a-zA-Z0-9\\./\\?=_%&#-+$@'\"\\|,!*]*").matcher(msg).find() && !msg.contains("promcgames")) {
			link = true;
		} else {
			for(String prefix : new String [] {".", ",", "*"}) {
				if(link) {
					break;
				}
				for(String option : new String [] {"com", "net", "org", "dj", "tk", "tv", "co", "uk", "ninja"}) {
					if(event.getMessage().contains(prefix + option)) {
						link = true;
						break;
					}
				}
			}
		}
		if(link && !Ranks.YOUTUBER.hasRank(player)) {
			for(Player online : Bukkit.getOnlinePlayers()) {
				if(!online.getName().equals(player.getName())) {
					event.getRecipients().remove(online);
				}
			}
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		UUID uuid = event.getUUID();
		String name = event.getName();
		hasHadTicketCreated.remove(name);
		if(blockedMessages.containsKey(name)) {
			List<String> messages = blockedMessages.get(name);
			if(messages != null && !messages.isEmpty()) {
				for(String message : messages) {
					DB.PLAYERS_BLOCKED_MESSAGES.insert("'" + uuid.toString() + "', '" + Network.getServerName() + "', '" + message + "'");
				}
				messages.clear();
				messages = null;
				blockedMessages.remove(name);
			}
		}
	}
}
