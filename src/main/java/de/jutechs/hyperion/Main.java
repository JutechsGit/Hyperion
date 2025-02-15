package de.jutechs.hyperion;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.List;

public class Main extends JavaPlugin implements Listener, CommandExecutor {

    private static final NamespacedKey SWORD_KEY = new NamespacedKey("hyperion", "custom_sword");
    private static final int TELEPORT_DISTANCE = 12;
    private static final double DAMAGE_AMOUNT = 20;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("awardhyperion").setExecutor(this);
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() != Material.IRON_SWORD) return;
        if (!isHyperion(event.getItem())) return;

        event.setCancelled(true);
        useHyperion(event.getPlayer());
    }

    private boolean isHyperion(ItemStack item) {
        return item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(SWORD_KEY, PersistentDataType.BYTE);
    }

    private void useHyperion(Player player) {
        Location start = player.getLocation();
        Vector direction = start.getDirection().normalize();
        Location target = start.clone();

        for (int i = 1; i <= TELEPORT_DISTANCE; i++) {
            Location step = start.clone().add(direction.clone().multiply(i));
            if (!step.getBlock().isPassable()) {
                break;
            }
            target = step;
        }

        player.teleport(target);
        player.getWorld().spawnParticle(Particle.EXPLOSION, target, 10);
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1));
        //player.getWorld().playSound(target, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);

        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (entity instanceof org.bukkit.entity.LivingEntity) {  // Only process if it's a LivingEntity
                org.bukkit.entity.LivingEntity livingEntity = (org.bukkit.entity.LivingEntity) entity;
                if (entity.getType() != EntityType.PLAYER) {  // Ensuring the entity is not a player
                    livingEntity.damage(DAMAGE_AMOUNT, player);
                }
            }
        }
    }

    public static ItemStack createHyperion() {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.displayName(Component.text("Hyperion", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Ability: ", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false)
                        .append(Component.text("Wither Impact ", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false))
                        .append(Component.text("RIGHT CLICK", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)),
                Component.text("Teleport ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                        .append(Component.text("10 blocks", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false))
                        .append(Component.text(" ahead of you.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)),
                Component.text("Then implode dealing ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                        .append(Component.text("20", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
                        .append(Component.text(" damage to nearby enemies.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)),
                Component.text("Also applies the wither shield scroll ability", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("reducing damage taken and granting an absorption", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("shield for ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                        .append(Component.text("5", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                        .append(Component.text(" seconds.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)),
                Component.empty(),
                Component.text("LEGENDARY SWORD", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true)

        ));
        meta.getPersistentDataContainer().set(SWORD_KEY, PersistentDataType.BYTE, (byte) 1);
        sword.setItemMeta(meta);
        return sword;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("awardhyperion")) {
            if (!(sender instanceof Player) || !sender.hasPermission("hyperion.award")) {
                sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
                return true;
            }
            Player player = (Player) sender;
            player.getInventory().addItem(createHyperion());
            player.sendMessage(Component.text("You have been awarded the Hyperion!", NamedTextColor.GREEN));
            return true;
        }
        return false;
    }
}

