package network.gameapi;

import network.Network;
import network.ProPlugin;
import network.anticheat.events.PlayerBanEvent;
import network.customevents.AutoRestartEvent;
import network.customevents.TimeEvent;
import network.customevents.game.*;
import network.customevents.player.PlayerLeaveEvent;
import network.gameapi.MiniGame.GameStates;
import network.gameapi.competitive.StatsHandler;
import network.player.TitleDisplayer;
import network.server.DB;
import network.server.tasks.AsyncDelayedTask;
import network.server.tasks.DelayedTask;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import npc.NPCRegistrationHandler.NPCs;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.List;
import java.util.UUID;

public class MiniGameEvents implements Listener {
	public MiniGameEvents() {
		EventUtil.register(this);
	}
	
	private MiniGame getMiniGame() {
		return Network.getMiniGame();
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			MiniGame miniGame = getMiniGame();
			GameStates gameState = miniGame.getGameState();
			if(gameState == GameStates.WAITING) {
				int waitingFor = miniGame.getRequiredPlayers() - ProPlugin.getPlayers().size();
				if(waitingFor <= 0) {
					miniGame.setGameState(GameStates.VOTING);
				}
			} else if(gameState == GameStates.VOTING) {
				if(miniGame.getCounter() <= 0) {
					getMiniGame().setMap(VotingHandler.loadWinningWorld());
					miniGame.setGameState(GameStates.STARTING);
				} else {
					if(miniGame.getCounter() <= 5) {
						for(Player player : Bukkit.getOnlinePlayers()) {
							new TitleDisplayer(player, "&2Voting Ends", miniGame.getCounterAsString()).setFadeIn(0).setStay(15).setFadeOut(60).display();
						}
					}
					if(miniGame.getCounter() <= 3) {
						EffectUtil.playSound(Sound.CLICK);
					}
				}
			} else if(gameState == GameStates.STARTING) {
				if(miniGame.getCounter() == 10) {
					if(StatsHandler.isEnabled()) {
						for(Player player : ProPlugin.getPlayers()) {
							try {
								StatsHandler.loadStats(player);
							} catch(Exception e) {
								
							}
						}
					}
				}
				if(miniGame.getCounter() <= 0) {
					miniGame.setGameState(GameStates.STARTED);
				} else {
					if(miniGame.getCounter() <= 5) {
						for(Player player : Bukkit.getOnlinePlayers()) {
							new TitleDisplayer(player, "&2Starting", miniGame.getCounterAsString()).setFadeIn(0).setStay(15).setFadeOut(60).display();
						}
					}
					if(miniGame.getCounter() <= 3) {
						EffectUtil.playSound(Sound.CLICK);
					}
				}
			} else if(gameState == GameStates.STARTED) {
				
			} else if(gameState == GameStates.ENDING) {
				if(miniGame.getCounter() <= 0) {
					ProPlugin.restartServer();
				} else {
					for(Player player : Bukkit.getOnlinePlayers()) {
						new TitleDisplayer(player, "&bServer Restarting", miniGame.getCounterAsString());
					}
				}
			}
			Network.getSidebar().update();
			if(getMiniGame().getCounter() > 0) {
				CounterDecrementEvent counterDecrementEvent = new CounterDecrementEvent();
				Bukkit.getPluginManager().callEvent(counterDecrementEvent);
				if(!counterDecrementEvent.isCancelled()) {
					getMiniGame().decrementCounter();
				}
			}
		} else if(ticks == 20 * 60 * 5 && Bukkit.getOnlinePlayers().isEmpty()) {
			AutoRestartEvent autoRestartEvent = new AutoRestartEvent();
			Bukkit.getPluginManager().callEvent(autoRestartEvent);
			if(!autoRestartEvent.isCancelled()) {
				ProPlugin.restartServer();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onGameWaiting(GameWaitingEvent event) {
		MiniGame miniGame = getMiniGame();
		miniGame.resetFlags();
		World lobby = miniGame.getLobby();
		if(lobby != null) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(!player.getWorld().getName().equals(lobby.getName())) {
					player.teleport(lobby.getSpawnLocation());
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onGameVoting(GameVotingEvent event) {
		getMiniGame().setCounter(getMiniGame().getVotingCounter());
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onGameStarting(GameStartingEvent event) {
		getMiniGame().setCounter(getMiniGame().getStartingCounter());
		for(NPCs npc : NPCs.values()) {
			npc.unregister();
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onGameStart(GameStartEvent event) {
		getMiniGame().setCounter(0);
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				for(Player player : ProPlugin.getPlayers()) {
					UUID uuid = player.getUniqueId();
					if(DB.NETWORK_ANTI_CHEAT_BAN_QUEUE.isUUIDSet(uuid)) {
						Bukkit.getPluginManager().callEvent(new PlayerBanEvent(uuid, DB.NETWORK_ANTI_CHEAT_BAN_QUEUE.getString("cheat", "uuid", uuid.toString())));
					} else {
						player.getInventory().clear();
						player.getInventory().setHeldItemSlot(0);
					}
				}
			}
		});
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onGameEnding(GameEndingEvent event) {
		getMiniGame().resetFlags();
		getMiniGame().setCounter(getMiniGame().getEndingCounter());
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		MiniGame miniGame = getMiniGame();
		if(miniGame.getJoiningPreGame()) {
			GameStates gameState = miniGame.getGameState();
			Player player = event.getPlayer();
			List<Player> players = ProPlugin.getPlayers();
			if(gameState == GameStates.STARTING && miniGame.getCanJoinWhileStarting() && players.size() > 0) {
				player.teleport(players.get(0).getWorld().getSpawnLocation().clone().add(0.5, 0, 0.5));
			} else {
				player.teleport(miniGame.getLobby().getSpawnLocation().clone().add(0.5, 0, 0.5));
			}
			if(gameState == GameStates.WAITING && players.size() >= miniGame.getRequiredPlayers()) {
				miniGame.setGameState(GameStates.VOTING);
			}
			players.clear();
			players = null;
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerLeave(final PlayerLeaveEvent event) {
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				MiniGame miniGame = getMiniGame();
				GameStates gameState = miniGame.getGameState();
				List<Player> players = ProPlugin.getPlayers();
				int playing = players.size();
				Player leaving = event.getPlayer();
				if(gameState == GameStates.VOTING && playing < miniGame.getRequiredPlayers()) {
					for(Player player : players) {
						new TitleDisplayer(player, "&eWaiting for Players").display();
					}
					miniGame.setGameState(GameStates.WAITING);
				} else if(gameState == GameStates.STARTING && playing == 1) {
					for(Player player : players) {
						new TitleDisplayer(player, "&eWaiting for Players").display();
					}
					miniGame.setGameState(GameStates.WAITING);
				}
				if(gameState == GameStates.STARTING || gameState == GameStates.STARTED) {
					if(playing == 1 && miniGame.getRestartWithOnePlayerLeft()) {
						Player winner = players.get(0);
						if(winner.getName().equals(leaving.getName())) {
							winner = players.get(1);
						}
						Bukkit.getPluginManager().callEvent(new GameWinEvent(winner));
					} else if(playing == 0) {
						miniGame.setGameState(GameStates.ENDING);
					}
				}
				players.clear();
				players = null;
			}
		});
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		MiniGame miniGame = getMiniGame();
		if(miniGame.getPlayersHaveOneLife()) {
			Bukkit.getPluginManager().callEvent(new GameLossEvent(event.getPlayer()));
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					List<Player> players = ProPlugin.getPlayers();
					if(players.size() == 1 && Network.getMiniGame().getRestartWithOnePlayerLeft()) {
						Bukkit.getPluginManager().callEvent(new GameWinEvent(players.get(0)));
					} else if(players.size() == 0) {
						Network.getMiniGame().setGameState(GameStates.ENDING);
					}
					players.clear();
					players = null;
				}
			});
		}
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		if(event.getEndServer() && getMiniGame().getGameState() != GameStates.ENDING) {
			getMiniGame().setGameState(GameStates.ENDING);
		}
		getMiniGame().setAllowEntityDamage(false);
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if(event.getSpawnReason() != SpawnReason.CUSTOM && event.getEntity().getWorld().equals(getMiniGame().getLobby())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onAutoRestart(AutoRestartEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(getMiniGame().getJoiningPreGame() && event.getCause() == DamageCause.VOID) {
			event.getEntity().teleport(event.getEntity().getWorld().getSpawnLocation());
		}
	}
}
