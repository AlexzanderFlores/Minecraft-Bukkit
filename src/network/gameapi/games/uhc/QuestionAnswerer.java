package network.gameapi.games.uhc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import network.customevents.game.GameDeathEvent;
import network.gameapi.uhc.scenarios.scenarios.CutClean;
import network.player.MessageHandler;
import network.server.util.EventUtil;

public class QuestionAnswerer implements Listener {
	public class Question {
		private List<String> alreadyTold = null;
		private List<String> keyWords = null;
		private String message = null;
		
		public Question(String [] keyWords, String message) {
			this.keyWords = new ArrayList<String>();
			for(String keyWord : keyWords) {
				this.keyWords.add(keyWord.toLowerCase());
			}
			this.message = message;
			if(questions == null) {
				questions = new ArrayList<Question>();
			}
			questions.add(this);
			alreadyTold = new ArrayList<String>();
		}
		
		public boolean matches(String message) {
			for(String keyWord : keyWords) {
				if(message.toLowerCase().replace(" ", "").contains(keyWord.toLowerCase())) {
					return true;
				}
			}
			return false;
		}
		
		public boolean tell(Player player) {
			if(!alreadyTold.contains(player.getName())) {
				alreadyTold.add(player.getName());
				MessageHandler.sendMessage(player, "");
				if(message.startsWith("/")) {
					player.chat(message);
				} else {
					MessageHandler.sendMessage(player, message);
				}
				MessageHandler.sendMessage(player, "");
				return true;
			}
			return false;
		}
		
		public void remove(Player player) {
			alreadyTold.remove(player.getName());
		}
	}
	
	private static List<Question> questions = null;
	
	public QuestionAnswerer() {
		new Question(new String [] {
			"AppleRate"
		}, "/info");
		new Question(new String [] {
			"Nether"
		}, "/info");
		new Question(new String [] {
			"Godapple", "Notchapple"
		}, "/info");
		new Question(new String [] {
			"Shear"
		}, "Shears are &eEnabled");
		new Question(new String [] {
			"Horse",
		}, "/info");
		new Question(new String [] {
			"Stalkin"
		}, "Stalking is &eAllowed");
		new Question(new String [] {
			"Head",
		}, "Heads are &eEnabled");
		new Question(new String [] {
			"Crossteam"
		}, "Cross teaming is " + (OptionsHandler.getCrossTeaming() ? "&eALLOWED" : "&cNot Allowed"));
		new Question(new String [] {
			"Flint"
		}, "Flint Rates are &e" + (CutClean.getInstance().isEnabled() ? "50" : "10") + "&a%");
		new Question(new String [] {
			"Poke"
		}, "Pole holing is &eAllowed");
		new Question(new String [] {
			"Strip"
		}, "/rules");
		new Question(new String [] {
			"FriendlyFire", "TeamDamage", "TeamDmg", "FF"
		}, "Damaging team mates is &cDisabled");
		new Question(new String [] {
			"Heal"
		}, "Final heal is &cOff");
		new Question(new String [] {
			"Sounds"
		}, "Mining to sounds is &eEnabled");
		new Question(new String [] {
			"Staircas"
		}, "/rules");
		new Question(new String [] {
			"Rollercoaster"
		}, "/rules");
		new Question(new String [] {
			"Scenario"
		}, "/info");
		new Question(new String [] {
			"Relog"
		}, "Relog is &eEnabled &amax of &e5 &aminutes");
		new Question(new String [] {
			"1.7enchanting", "1.8enchanting"
		}, "Enchanting is &e1.7");
		new Question(new String [] {
			"Absor"
		}, "Absorption is " + (OptionsHandler.getAbsorption() ? "&eEnabled" : "&cDisabled"));
		new Question(new String [] {
			"iPVP"
		}, "/info");
		new Question(new String [] {
			"stats"
		}, "Stats will not be enabled");
		EventUtil.register(this);
	}
	
	public static boolean askQuestion(Player player, String text) {
		for(Question question : questions) {
			if(question.matches(text) && question.tell(player)) {
				MessageHandler.sendMessage(player, "&c&lNot what you asked? Resend your message");
				return true;
			}
		}
		return false;
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(askQuestion(event.getPlayer(), event.getMessage())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		for(Question question : questions) {
			question.remove(event.getPlayer());
		}
	}
}
