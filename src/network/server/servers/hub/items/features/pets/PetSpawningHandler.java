package network.server.servers.hub.items.features.pets;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityLiving;
import network.ProPlugin;
import network.server.servers.hub.items.features.pets.Pets.PetTypes;
import network.server.servers.hub.items.features.pets.entities.*;
import network.server.util.EventUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.HashMap;

public class PetSpawningHandler implements Listener {
    private static HashMap<String, EntityType> spawningQueue = null;

    public PetSpawningHandler() {
        EventUtil.register(this);
    }

    public static Entity spawn(Player player, EntityType entityType) {
        return spawn(player, entityType, player.getLocation());
    }

    public static Entity spawn(Player player, EntityType entityType, Location location) {
        if(spawningQueue == null) {
            spawningQueue = new HashMap<String, EntityType>();
        }
        spawningQueue.put(player.getName(), entityType);
        return player.getWorld().spawnEntity(location, entityType);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if(spawningQueue != null) {
            Player player = null;
            EntityType entityType = event.getEntityType();
            for(String name : spawningQueue.keySet()) {
                if(spawningQueue.get(name) == entityType) {
                    player = ProPlugin.getPlayer(name);
                    break;
                }
            }
            if(!event.isCancelled() && event.getSpawnReason() == SpawnReason.CUSTOM && player != null) {
                Entity entity = event.getEntity();
                Location location = event.getLocation();
                World world = location.getWorld();
                CraftWorld craftWorld = (CraftWorld) world;
                CraftEntity craftEntity = (CraftEntity) entity;
                EntityLiving entityLiving = null;
                if(entityType == EntityType.COW && !(craftEntity.getHandle() instanceof CowPet)) {
                    entityLiving = new CowPet(craftWorld.getHandle());
                } else if(entityType == EntityType.PIG && !(craftEntity.getHandle() instanceof PigPet)) {
                    entityLiving = new PigPet(craftWorld.getHandle());
                } else if(entityType == EntityType.CHICKEN && !(craftEntity.getHandle() instanceof ChickenPet)) {
                    entityLiving = new ChickenPet(craftWorld.getHandle());
                } else if(entityType == EntityType.WOLF && !(craftEntity.getHandle() instanceof WolfPet)) {
                    entityLiving = new WolfPet(craftWorld.getHandle());
                } else if(entityType == EntityType.MUSHROOM_COW && !(craftEntity.getHandle() instanceof MushroomCowPet)) {
                    entityLiving = new MushroomCowPet(craftWorld.getHandle());
                } else if(entityType == EntityType.OCELOT && !(craftEntity.getHandle() instanceof OcelotPet)) {
                    entityLiving = new OcelotPet(craftWorld.getHandle());
                } else if(entityType == EntityType.SHEEP && !(craftEntity.getHandle() instanceof SheepPet)) {
                    entityLiving = new SheepPet(craftWorld.getHandle());
                } else if(entityType == EntityType.HORSE && !(craftEntity.getHandle() instanceof HorsePet)) {
                    entityLiving = new HorsePet(craftWorld.getHandle());
                } else if(entityType == EntityType.SLIME && !(craftEntity.getHandle() instanceof SlimePet)) {
                    entityLiving = new SlimePet(craftWorld.getHandle());
                } else if(entityType == EntityType.MAGMA_CUBE && !(craftEntity.getHandle() instanceof MagmaCubePet)) {
                    entityLiving = new MagmaCubePet(craftWorld.getHandle());
                } else if(entityType == EntityType.SQUID && !(craftEntity.getHandle() instanceof SquidPet)) {
                    entityLiving = new SquidPet(craftWorld.getHandle());
                } else if(entityType == EntityType.SNOWMAN && !(craftEntity.getHandle() instanceof Snowman)) {
                    entityLiving = new SnowmanPet(craftWorld.getHandle());
                }
                if(entityLiving == null) {
                    spawningQueue.remove(player.getName());
                } else {
                    LivingEntity livingEntity = (LivingEntity) entityLiving.getBukkitEntity();
                    livingEntity.setRemoveWhenFarAway(false);
                    livingEntity.setCustomNameVisible(true);
                    livingEntity.setCustomName(ChatColor.GOLD + player.getName() + "'s Pet");
                    entityLiving.setPosition(location.getX(), location.getY(), location.getZ());
                    spawningQueue.remove(player.getName());
                    new Pet(player, PetTypes.valueOf(entityType.toString()), entityLiving);
                    craftWorld.getHandle().removeEntity(craftEntity.getHandle());
                    craftWorld.getHandle().addEntity(entityLiving, SpawnReason.CUSTOM);
                }
            }
        }
    }
}
