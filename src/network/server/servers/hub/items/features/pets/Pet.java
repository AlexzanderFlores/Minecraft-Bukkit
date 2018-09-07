package network.server.servers.hub.items.features.pets;

import net.minecraft.server.v1_8_R3.EntityLiving;
import network.player.MessageHandler;
import network.server.servers.hub.items.features.pets.Pets.PetTypes;
import network.server.util.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pet implements EntityPet {
    public static Map<String, Pet> playersPets = null;
    public EntityPet entityPet = null;
    private EntityLiving entityLiving = null;
    private List<String> movedTowards = null;
    private List<String> stayingPets = null;
    private List<String> enabledPetSounds = null;

    public Pet(Player player, PetTypes petType, EntityLiving entityLiving) {
        if(playersPets == null) {
            playersPets = new HashMap<String, Pet>();
        }
        this.entityLiving = entityLiving;
        this.entityPet = (EntityPet) entityLiving;
        if(playersPets.containsKey(player.getName())) {
            playersPets.get(player.getName()).remove(player);
        }
        playersPets.put(player.getName(), this);
        onSpawn(player);
    }

    public LivingEntity getLivingEntity() {
        return (LivingEntity) entityLiving.getBukkitEntity();
    }

    public String getType() {
        return getLivingEntity().getType().toString();
    }

    public boolean isEntity(Entity entity) {
        if(entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            return livingEntity.getCustomName() != null && livingEntity.getCustomName().equals(getLivingEntity().getCustomName());
        } else {
            return false;
        }
    }

    @Override
    public void onSpawn(Player player) {
        MessageHandler.sendMessage(player, "Spawned in your pet! Click it for options");
        entityPet.onSpawn(player);
        if(getLivingEntity() instanceof Ageable) {
            Ageable ageable = (Ageable) getLivingEntity();
            ageable.setBaby();
            ageable.setAgeLock(true);
        }
        entityPet.makeSound(player);
        if(movedTowards == null) {
            movedTowards = new ArrayList<String>();
        }
        movedTowards.add(player.getName());
    }

    @Override
    public void walkTo(Player player, float speed) {
        if((stayingPets == null || !stayingPets.contains(player.getName())) && getLivingEntity().getPassenger() == null) {
            LivingEntity livingEntity = (LivingEntity) entityLiving.getBukkitEntity();
            double x1 = livingEntity.getLocation().getX();
            double z1 = livingEntity.getLocation().getZ();
            double x2 = player.getLocation().getX();
            double z2 = player.getLocation().getZ();
            if(livingEntity.getPassenger() == null) {
                if(movedTowards != null && Math.sqrt((x1 - x2) * (x1 - x2) + (z1 - z2) * (z1 - z2)) >= 3.0d) {
                    movedTowards.remove(player.getName());
                    livingEntity.removePotionEffect(PotionEffectType.SLOW);
                    entityPet.walkTo(player, 1.65f);
                } else {
                    if(movedTowards == null || !movedTowards.contains(player.getName())) {
                        if(movedTowards == null) {
                            movedTowards = new ArrayList<String>();
                        }
                        movedTowards.add(player.getName());
                    }
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999999, 10));
                }
            }
        }
    }

    @Override
    public Inventory getOptionsInventory(Player player, Inventory inventory) {
        if(inventory == null) {
            inventory = Bukkit.createInventory(player, 9, ChatColor.GOLD + player.getName() + "'s Pet");
        }
        inventory.addItem(new ItemCreator(Material.LEATHER_HELMET).setName("&eWear your pet").getItemStack());
        inventory.addItem(new ItemCreator(Material.SULPHUR).setName("&eToggle pet staying").getItemStack());
        inventory.addItem(new ItemCreator(Material.JUKEBOX).setName("&eToggle pet sounds").getItemStack());
        if(entityLiving.getBukkitEntity() instanceof Ageable) {
            Ageable ageable = (Ageable) entityLiving.getBukkitEntity();
            if(ageable.isAdult()) {
                inventory.addItem(new ItemCreator(Material.BLAZE_POWDER).setName("&eToggle to Baby").getItemStack());
            } else {
                inventory.addItem(new ItemCreator(Material.BLAZE_POWDER).setName("&eToggle to Adult").getItemStack());
            }
        }
        inventory.addItem(new ItemCreator(Material.SADDLE).setName("&eRide pet").getItemStack());
        inventory.addItem(new ItemCreator(Material.TNT).setName("&cRemove your pet").getItemStack());
        inventory = entityPet.getOptionsInventory(player, inventory);
        return inventory;
    }

    @Override
    public void clickedOnCustomOption(Player player, ItemStack clicked) {
        entityPet.clickedOnCustomOption(player, clicked);
    }

    @Override
    public void togglePetStaying(Player player) {
        if(stayingPets != null && stayingPets.contains(player.getName())) {
            stayingPets.remove(player.getName());
            MessageHandler.sendMessage(player, "Your pet will now move again");
        } else {
            if(stayingPets == null) {
                stayingPets = new ArrayList<String>();
            }
            stayingPets.add(player.getName());
            MessageHandler.sendMessage(player, "You told your pet to stay");
        }
        makeSound(player);
        entityPet.togglePetStaying(player);
    }

    @Override
    public void togglePetSounds(Player player) {
        entityPet.togglePetSounds(player);
        if(enabledPetSounds != null && enabledPetSounds.contains(player.getName())) {
            enabledPetSounds.remove(player.getName());
            MessageHandler.sendMessage(player, "Pet sounds &cdisabled");
        } else {
            if(enabledPetSounds == null) {
                enabledPetSounds = new ArrayList<String>();
            }
            enabledPetSounds.add(player.getName());
            MessageHandler.sendMessage(player, "Pet sounds &eenabled");
            makeSound(player);
        }
    }

    @Override
    public void makeSound(Player player) {
        if(enabledPetSounds != null && enabledPetSounds.contains(player.getName())) {
            entityPet.makeSound(player);
        }
    }

    @Override
    public void makeHurtSound(Player player) {
        if(enabledPetSounds != null && enabledPetSounds.contains(player.getName())) {
            entityPet.makeHurtSound(player);
        }
    }

    @Override
    public void remove(Player player) {
        MessageHandler.sendMessage(player, "Removed your pet");
        entityPet.remove(player);
        getLivingEntity().remove();
        playersPets.remove(player.getName());
        if(movedTowards != null) {
            movedTowards.remove(player.getName());
        }
        if(stayingPets != null) {
            stayingPets.remove(player.getName());
        }
        if(enabledPetSounds != null) {
            enabledPetSounds.remove(player.getName());
        }
    }
}
