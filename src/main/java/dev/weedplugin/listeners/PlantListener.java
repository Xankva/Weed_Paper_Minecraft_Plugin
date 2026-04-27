package dev.weedplugin.listeners;

import dev.weedplugin.WeedPlugin;
import dev.weedplugin.managers.ConfigManager;
import dev.weedplugin.managers.GrowthManager;
import dev.weedplugin.managers.ItemManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class PlantListener implements Listener {

    private final WeedPlugin plugin;
    private final Random random = new Random();
    private final Set<UUID> plantCooldown = new HashSet<>();

    public PlantListener(WeedPlugin plugin) {
        this.plugin = plugin;
    }

    private Component msg(String legacy) {
        return LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlant(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Block clicked = event.getClickedBlock();
        if (item == null || clicked == null) return;
        if (!plugin.getItemManager().isSeed(item)) return;

        event.setCancelled(true);

        // Deduplicate double-fire
        UUID uid = player.getUniqueId();
        if (plantCooldown.contains(uid)) return;
        plantCooldown.add(uid);
        plugin.getServer().getScheduler().runTask(plugin, () -> plantCooldown.remove(uid));

        ConfigManager cm = plugin.getConfigManager();

        if (cm.getDisabledWorlds().contains(player.getWorld().getName())) {
            player.sendMessage(msg(cm.getMessage("no-permission"))); return;
        }
        if (!player.hasPermission("weedplugin.use")) {
            player.sendMessage(msg(cm.getMessage("no-permission"))); return;
        }
        if (event.getBlockFace() != BlockFace.UP) return;

        Block cropBlock = clicked.getRelative(BlockFace.UP);
        if (!cm.getValidSoil().contains(clicked.getType())) return;
        if (cropBlock.getType() != Material.AIR) return;

        cropBlock.setType(Material.WHEAT);
        plugin.getGrowthManager().plantCrop(cropBlock);

        if (player.getGameMode() != GameMode.CREATIVE) item.setAmount(item.getAmount() - 1);

        player.sendMessage(msg(cm.getMessage("planted")));
        playSound(player, cm.config().getString("sounds.plant", "BLOCK_CROP_BREAK"), cm);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHarvest(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        GrowthManager gm = plugin.getGrowthManager();
        if (!gm.isCannabisPlant(block)) return;

        event.setCancelled(true);
        event.setDropItems(false);

        ConfigManager cm = plugin.getConfigManager();
        ItemManager im = plugin.getItemManager();

        if (!gm.isFullyGrown(block)) {
            player.sendMessage(msg(cm.getMessage("too-early"))); return;
        }

        int seeds = cm.getMinSeeds() + random.nextInt(Math.max(1, cm.getMaxSeeds() - cm.getMinSeeds() + 1));
        int buds  = cm.getMinBuds()  + random.nextInt(Math.max(1, cm.getMaxBuds()  - cm.getMinBuds()  + 1));
        boolean premium = random.nextDouble() < cm.getPremiumBudChance();

        block.getWorld().dropItemNaturally(block.getLocation(), im.createSeed(seeds));
        block.getWorld().dropItemNaturally(block.getLocation(), premium ? im.createPremiumBud(buds) : im.createBud(buds));

        player.sendMessage(msg(premium
            ? cm.getMessage("premium-harvested", "{buds}", String.valueOf(buds), "{seeds}", String.valueOf(seeds))
            : cm.getMessage("harvested",          "{buds}", String.valueOf(buds), "{seeds}", String.valueOf(seeds))));

        gm.removePlant(block);
        if (cm.isAutoReplant()) { block.setType(Material.WHEAT); gm.plantCrop(block); }
        else block.setType(Material.AIR);

        playSound(player, cm.config().getString("sounds.harvest", "ENTITY_EXPERIENCE_ORB_PICKUP"), cm);
    }

    private void playSound(Player player, String name, ConfigManager cm) {
        if (!cm.areSoundsEnabled() || name == null) return;
        try { player.playSound(player.getLocation(), Sound.valueOf(name.toUpperCase()), 1f, 1f); }
        catch (Exception ignored) {}
    }
}
