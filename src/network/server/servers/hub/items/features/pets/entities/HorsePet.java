package network.server.servers.hub.items.features.pets.entities;

import net.minecraft.server.v1_8_R3.EntityHorse;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.World;
import network.player.MessageHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.servers.hub.items.features.pets.EntityPet;
import network.server.servers.hub.items.features.pets.PathfinderGoalWalkToOwner;
import network.server.util.ItemCreator;
import network.server.util.ReflectionUtil;
import network.server.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;

@SuppressWarnings("deprecation")
public class HorsePet extends EntityHorse implements EntityPet {
    public HorsePet(World world) {
        super(world);
        try {
            for(String fieldName : new String[]{"b", "c"}) {
                Field field = ReflectionUtil.getDeclaredField(PathfinderGoalSelector.class, fieldName);
                field.setAccessible(true);
                field.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
                field.set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSpawn(Player player) {
        Horse horse = (Horse) getBukkitEntity();
        horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
    }

    @Override
    public void walkTo(Player player, float speed) {
        this.goalSelector.a(0, new PathfinderGoalWalkToOwner(this, speed, player));
    }

    @Override
    public Inventory getOptionsInventory(Player player, Inventory inventory) {
        Inventory newInventory = Bukkit.createInventory(player, 9 * 4, inventory.getTitle());
        ItemStack remove = null;
        for(ItemStack itemStack : inventory.getContents()) {
            if(itemStack != null && itemStack.getType() != Material.AIR) {
                if(itemStack.getType() == Material.TNT) {
                    remove = itemStack;
                } else {
                    newInventory.addItem(itemStack);
                }
            }
        }
        newInventory.addItem(new ItemCreator(new ItemStack(Material.SKULL_ITEM, 1, (byte) 3)).setName("&eNormal Horse ").getItemStack());
        newInventory.addItem(new ItemCreator(new ItemStack(Material.SKULL_ITEM, 1, (byte) 0)).setName("&eSkeleton Horse").getItemStack());
        newInventory.addItem(new ItemCreator(new ItemStack(Material.SKULL_ITEM, 1, (byte) 2)).setName("&eZombie Horse").getItemStack());
        newInventory.addItem(remove);
        int counter = 9 * 1;
        for(Color color : Color.values()) {
            newInventory.setItem(counter++, new ItemCreator(Material.HAY_BLOCK).setName("&aColor " + StringUtil.getFirstLetterCap(color.toString())).getItemStack());
        }
        counter = 9 * 2;
        for(Style style : Style.values()) {
            newInventory.setItem(counter++, new ItemCreator(Material.HAY_BLOCK).setName("&aStyle " + StringUtil.getFirstLetterCap(style.toString())).getItemStack());
        }
        counter = 9 * 3;
        newInventory.setItem(counter++, new ItemCreator(Material.GOLD_BARDING).setName("&6Gold Horse Armor").getItemStack());
        newInventory.setItem(counter++, new ItemCreator(Material.IRON_BARDING).setName("&7Iron Horse Armor").getItemStack());
        newInventory.setItem(counter++, new ItemCreator(Material.DIAMOND_BARDING).setName("&bDiamond Horse Armor").getItemStack());
        newInventory.setItem(counter++, new ItemCreator(Material.LEATHER).setName("&aNo Armor").getItemStack());
        return newInventory;
    }

    @Override
    public void clickedOnCustomOption(Player player, ItemStack clicked) {
        if(Ranks.PRO_PLUS.hasRank(player)) {
            Horse horse = (Horse) getBukkitEntity();
            if(clicked.getType() == Material.SADDLE) {
                horse.removePotionEffect(PotionEffectType.SLOW);
                getBukkitEntity().setPassenger(player);
            } else if(clicked.getType() == Material.SKULL_ITEM) {
                if(Ranks.PRO_PLUS.hasRank(player)) {
                    if(clicked.getData().getData() == (byte) 3) {
                        horse.setVariant(Variant.HORSE);
                        setTame(true);
                        MessageHandler.sendMessage(player, "&aYou have changed your horse into a Normal Horse");
                    } else if(clicked.getData().getData() == (byte) 0) {
                        horse.setVariant(Variant.SKELETON_HORSE);
                        setTame(true);
                        MessageHandler.sendMessage(player, "&aYou have changed your horse into a Skeleton Horse");
                    } else if(clicked.getData().getData() == (byte) 2) {
                        horse.setVariant(Variant.UNDEAD_HORSE);
                        setTame(true);
                        MessageHandler.sendMessage(player, "&aYou have changed your horse into a Zombie Horse");
                    }
                } else {
                    MessageHandler.sendMessage(player, Ranks.PRO_PLUS.getNoPermission());
                }
            } else {
                String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                if(name.startsWith("Color")) {
                    horse.setColor(Color.valueOf(name.split("Color ")[1].toUpperCase().replace(" ", "_")));
                } else if(name.startsWith("Style")) {
                    horse.setStyle(Style.valueOf(name.split("Style ")[1].toUpperCase().replace(" ", "_")));
                } else {
                    if(name.equals("NO_ARMOR")) {
                        horse.getInventory().setArmor(new ItemStack(Material.AIR));
                    } else {
                        horse.getInventory().setArmor(clicked);
                    }
                }
            }
        } else {
            MessageHandler.sendMessage(player, Ranks.PRO_PLUS.getNoPermission());
        }
    }

    @Override
    public void togglePetStaying(Player player) {

    }

    @Override
    public void togglePetSounds(Player player) {

    }

    @Override
    public void makeSound(Player player) {
//        makeSound(super.t(), this.bf(), this.bg());
    }

    @Override
    public void makeHurtSound(Player player) {
//        makeSound(super.aT(), this.bf(), this.bg());
    }

    @Override
    public void remove(Player player) {

    }

//    @Override
//    protected String t() {
//        return null;
//    }
//
//    @Override
//    protected String aT() {
//        return null;
//    }
//
//    @Override
//    protected String aU() {
//        return null;
//    }
//
//    @Override
//    protected void a(int i, int j, int k, Block block) {
//
//    }
//
//    @Override
//    public void aD() {
//        super.aD();
//        this.getAttributeInstance(GenericAttributes.b).setValue(1000.0D);
//    }
}
