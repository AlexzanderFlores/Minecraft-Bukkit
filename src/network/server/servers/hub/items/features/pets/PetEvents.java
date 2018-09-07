package network.server.servers.hub.items.features.pets;

import org.bukkit.ChatColor;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Squid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffectType;

import network.ProPlugin;
import network.customevents.TimeEvent;
import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.PlayerAFKEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.player.MessageHandler;
import network.server.DB;
import network.server.util.EventUtil;
import npc.NPCEntity;

public class PetEvents implements Listener {
	public PetEvents() {
        EventUtil.register(this);
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();
        if(ticks == 20 * 2) {
            if(Pet.playersPets != null) {
                for(String name : Pet.playersPets.keySet()) {
                    Pet pet = Pet.playersPets.get(name);
                    pet.walkTo(ProPlugin.getPlayer(name), 0.0f);
                }
            }
        }
    }

    // Disable air damage for squids
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Squid) {
            event.setCancelled(true);
        }
    }

    // Open options inventory
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof LivingEntity && !(event.getEntity() instanceof Player)) {
            LivingEntity livingEntity = (LivingEntity) event.getEntity();
            if(event.getDamager() instanceof Player && Pet.playersPets != null && !NPCEntity.isNPC(livingEntity)) {
                Player player = (Player) event.getDamager();
                if(Pet.playersPets.containsKey(player.getName())) {
                    Pet pet = Pet.playersPets.get(player.getName());
                    if(pet.isEntity(event.getEntity())) {
                    	Inventory inventory = pet.getOptionsInventory(player, null);
                        if(inventory != null) {
                            player.openInventory(inventory);
                        }
                        event.setCancelled(true);
                    }
                } else {
                	MessageHandler.sendMessage(player, "&cCannot interact with a pet that you do not own");
                }
            }
        }
    }

    // Open options inventory
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if(Pet.playersPets != null && event.getRightClicked() instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) event.getRightClicked();
            if(!(event.getRightClicked() instanceof Player) && !NPCEntity.isNPC(livingEntity)) {
                if(Pet.playersPets.containsKey(player.getName())) {
                    Pet pet = Pet.playersPets.get(player.getName());
                    if(pet.isEntity(event.getRightClicked())) {
                    	LivingEntity entity = (LivingEntity) pet.getLivingEntity();
                        if(player.isSneaking() && event.getRightClicked() == entity && Pet.playersPets.get(player.getName()).getLivingEntity() instanceof Slime) {
                        	if(event.getRightClicked() instanceof Slime) {
                                Slime slime = (Slime) entity;
                                int newSize = slime.getSize() + 1;
                                if(newSize > 5) {
                                    slime.setSize(1);
                                } else {
                                    slime.setSize(slime.getSize() + 1);
                                }
                            }
                        } else {
                            Inventory inventory = pet.getOptionsInventory(player, null);
                            if(inventory != null) {
                                player.openInventory(inventory);
                            }
                            event.setCancelled(true);
                        }
                    }
                } else {
                	MessageHandler.sendMessage(player, "&cCannot interact with a pet that you do not own");
                }
            }
        } else {
            event.setCancelled(true);
        }
    }

    // Worn by owner, toggle pet staying, toggle baby/adult, toggle pet sounds, remove pet, and custom options
    @EventHandler
    public void onInventoryItemClick(InventoryItemClickEvent event) {
        Player player = event.getPlayer();
        if(event.getTitle().equals(ChatColor.GOLD + player.getName() + "'s Pet") && Pet.playersPets != null && Pet.playersPets.containsKey(player.getName())) {
            Pet pet = Pet.playersPets.get(player.getName());
            String clicked = event.getItem().getItemMeta().getDisplayName();
            if(clicked.equals(ChatColor.YELLOW + "Toggle pet staying")) {
                pet.togglePetStaying(player);
            } else if(clicked.equals(ChatColor.YELLOW + "Toggle pet sounds")) {
                pet.togglePetSounds(player);
            } else if(clicked.equals(ChatColor.YELLOW + "Toggle to Adult")) {
            	Ageable ageable = (Ageable) pet.getLivingEntity();
                ageable.setAdult();
            } else if(clicked.equals(ChatColor.YELLOW + "Toggle to Baby")) {
            	Ageable ageable = (Ageable) pet.getLivingEntity();
                ageable.setBaby();
            } else if(clicked.equals(ChatColor.YELLOW + "Ride pet")) {
            	pet.getLivingEntity().removePotionEffect(PotionEffectType.SLOW);
                pet.getLivingEntity().setPassenger(player);
            } else if(clicked.equals(ChatColor.RED + "Remove your pet")) {
                pet.remove(player);
                if(DB.HUB_PETS.isUUIDSet(player.getUniqueId())) {
                    DB.HUB_PETS.deleteUUID(player.getUniqueId());
                }
            } else {
                pet.clickedOnCustomOption(player, event.getItem());
            }
            player.closeInventory();
            event.setCancelled(true);
        }
    }

    // Remove pet
    @EventHandler
    public void onPlayerAFK(PlayerAFKEvent event) {
        if(event.getAFK() && Pet.playersPets != null && Pet.playersPets.containsKey(event.getPlayer().getName())) {
            Pet.playersPets.get(event.getPlayer().getName()).remove(event.getPlayer());
        }
    }

    // Remove pet
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLeave(PlayerLeaveEvent event) {
        Player player = event.getPlayer();
        if(Pet.playersPets != null && Pet.playersPets.containsKey(player.getName())) {
            Pet.playersPets.get(player.getName()).remove(player);
        }
    }
}
