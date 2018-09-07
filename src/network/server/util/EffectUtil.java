package network.server.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import network.gameapi.SpectatorHandler;

@SuppressWarnings("deprecation")
public class EffectUtil {
	public static void launchFireworks() {
		for(Player player : Bukkit.getOnlinePlayers()) {
			launchFirework(player.getLocation());
		}
	}
	
	public static void launchFirework(ArrayList<Entity> entities) {
		for(Entity entity : entities) {
			launchFirework(entity);
		}
	}
	
	public static Firework launchFirework(Entity entity) {
		if(entity instanceof Player) {
			Player player = (Player) entity;
			if(SpectatorHandler.contains(player)) {
				return null;
			}
		}
		return launchFirework(entity.getLocation());
	}
	
	public static void launchFirework(List<Location> locations) {
		for(Location location : locations) {
			launchFirework(location);
		}
	}
	
	public static Firework launchFirework(Location location) {
		Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
		FireworkMeta meta = firework.getFireworkMeta();
		FireworkEffect.Type type = FireworkEffect.Type.values()[new Random().nextInt(FireworkEffect.Type.values().length)];
	    Color [] colors = {
	    	Color.fromRGB(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255)), Color.fromRGB(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255))
	    };
	    FireworkEffect effect = FireworkEffect.builder().flicker(new Random().nextBoolean()).withColor(colors[0]).withFade(colors[1]).with(type).trail(new Random().nextBoolean()).build();
	    meta.addEffect(effect);
	    meta.setPower(new Random().nextInt(2) + 1);
	    firework.setFireworkMeta(meta);
	    return firework;
	}
	
	public static void playFireworkEffect(FireworkEffect effect) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			playFireworkEffect(player.getLocation(), effect);
		}
	}
	
	public static void playFireworkEffect(ArrayList<Entity> entities, FireworkEffect effect) {
		for(Entity entity : entities) {
			playFireworkEffect(entity, effect);
		}
	}
	
	public static void playFireworkEffect(Entity entity, FireworkEffect effect) {
		if(entity instanceof Player) {
			Player player = (Player) entity;
			if(SpectatorHandler.contains(player)) {
				return;
			}
		}
		playFireworkEffect(entity.getLocation(), effect);
	}
	
	public static void playFireworkEffect(List<Location> locations, FireworkEffect effect) {
		for(Location location : locations) {
			playFireworkEffect(location, effect);
		}
	}
	
	public static void playFireworkEffect(Location location, FireworkEffect effect) {
	    if(effect == null) {
	    	FireworkEffect.Type type = FireworkEffect.Type.values()[new Random().nextInt(FireworkEffect.Type.values().length)];
		    Color [] colors = {
		    	Color.fromRGB(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255)), Color.fromRGB(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255))
		    };
	    	effect = FireworkEffect.builder().flicker(new Random().nextBoolean()).withColor(colors[0]).withFade(colors[1]).with(type).trail(new Random().nextBoolean()).build();
	    }
	    Firework firework = (Firework) location.getWorld().spawn(location, Firework.class);
	    Method world_getHandle = null;
        Method nms_world_broadcastEntityEffect = null;
        Method firework_getHandle = null;
        Object nms_world = null;
        Object nms_firework = null;
        if(world_getHandle == null) {
            world_getHandle = getMethod(location.getWorld().getClass(), "getHandle");
            firework_getHandle = getMethod(firework.getClass(), "getHandle");
        }
        try {
        	nms_world = world_getHandle.invoke(location.getWorld(), (Object []) null);
            nms_firework = firework_getHandle.invoke(firework, (Object []) null);
            if(nms_world_broadcastEntityEffect == null) {
                nms_world_broadcastEntityEffect = getMethod(nms_world.getClass(), "broadcastEntityEffect");
            }
            FireworkMeta data = (FireworkMeta) firework.getFireworkMeta();
            data.clearEffects();
            data.setPower(1);
            data.addEffect(effect);
            firework.setFireworkMeta(data);
            nms_world_broadcastEntityEffect.invoke(nms_world, new Object[] {nms_firework, (byte) 17});
            
        } catch(Exception e) {
        	e.printStackTrace();
        }
        firework.remove();
	}
	
	public static void displayParticles(Material block, Location location) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			/*if(ProMcGames.getPlugin() == Plugins.HUB && PlayerVanisher.vanishedEnabled(player)) {
				continue;
			}*/
			player.playEffect(location == null ? player.getLocation() : location, Effect.STEP_SOUND, block);
		}
	}
	
	public static void displayDeath(final Location location) {
		for(int a = 1; a <= 5; ++a) {
			EffectUtil.displayParticles(Material.NETHER_WARTS, location);
			EffectUtil.displayParticles(Material.NETHER_WARTS, location.add(0, 1, 0));
		}
	}
	
	public static void playSound(Sound sound) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
		}
	}
	
	public static void playSound(Sound sound, Location location) {
		playSound(sound, location, 1.0f);
	}
	
	public static void playSound(Sound sound, Location location, float volume) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.playSound(location == null ? player.getLocation() : location, sound, volume, 1.0f);
		}
	}
	
	public static void playSound(Sound sound, Location location, float volume, float pitch) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.playSound(location == null ? player.getLocation() : location, sound, volume, pitch);
		}
	}
	
	public static void playSound(Player player, Sound sound) {
		player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
	}
	
	public static void playSound(Player player, Sound sound, float pitch) {
		player.playSound(player.getLocation(), sound, 1.0f, pitch);
	}
	
	public static void playSound(Player player, Location location, Sound sound) {
		player.playSound(location == null ? player.getLocation() : location, sound, 1.0f, 1.0f);
	}
	
	public static void playEffect(Effect effect) {
		playEffect(effect, 0);
	}
	
	public static void playEffect(Effect effect, int data) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.playEffect(player.getLocation(), effect, data);
		}
	}
	
	public static void playEffect(Player player, Effect effect) {
		playEffect(player, effect, player.getLocation(), 0);
	}
	
	public static void playEffect(Player player, Effect effect, int data) {
		playEffect(player, effect, player.getLocation(), data);
	}
	
	public static void playEffect(Player player, Effect effect, Location location) {
		player.playEffect(location, effect, 0);
	}
	
	public static void playEffect(Player player, Effect effect, Location location, int data) {
		player.playEffect(location, effect, data);
	}
	
	public static void playEffect(Effect effect, Location location) {
		playEffect(effect, location, 0);
	}
	
	public static void playEffect(Effect effect, Location location, int data) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.playEffect(location, effect, data);
		}
	}
	
	private static Method getMethod(Class<?> cl, String method) {
	    for(Method m : cl.getMethods()) {
	    	if(m.getName().equals(method)) {
	    		return m;
	    	}
	    }
	    return null;
	}
}
