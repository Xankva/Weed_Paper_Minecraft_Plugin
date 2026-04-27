package dev.weedplugin.listeners;

import dev.weedplugin.WeedPlugin;
import dev.weedplugin.managers.ConfigManager;
import dev.weedplugin.managers.ItemManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ItemUseListener implements Listener {

    private final WeedPlugin plugin;
    private final Set<UUID> smokingPlayers = new HashSet<>();
    // Tracks players already handled THIS tick — blocks the OFF_HAND duplicate event
    private final Set<UUID> handledThisTick = new HashSet<>();

    public ItemUseListener(WeedPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemUse(PlayerInteractEvent event) {
        // Ignore off-hand entirely — Paper fires one event per hand
        if (event.getHand() != EquipmentSlot.HAND) {
            event.setCancelled(true);
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        UUID uid = player.getUniqueId();

        // Always read from inventory — event.getItem() can be stale after cancellation
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() == Material.AIR) return;

        ItemManager im = plugin.getItemManager();
        String type = im.getItemType(held);
        if (type == null) return;

        // Block duplicate handling within the same tick
        if (handledThisTick.contains(uid)) {
            event.setCancelled(true);
            return;
        }
        handledThisTick.add(uid);
        plugin.getServer().getScheduler().runTask(plugin, () -> handledThisTick.remove(uid));

        event.setCancelled(true); // Cancel ALL weed item right-clicks unconditionally

        ConfigManager cm = plugin.getConfigManager();

        switch (type) {
            case ItemManager.BUD          -> handleRollJoint(player, held, false, cm, im);
            case ItemManager.PREMIUM_BUD  -> handleRollJoint(player, held, true,  cm, im);
            case ItemManager.JOINT        -> handleSmokeJoint(player, held, false, cm, im);
            case ItemManager.PREMIUM_JOINT -> handleSmokeJoint(player, held, true, cm, im);
            case ItemManager.EDIBLE       -> handleEatEdible(player, held, cm, im);
        }
    }

    // ── Roll Joint ───────────────────────────────────────────────────────────────

    private void handleRollJoint(Player player, ItemStack held, boolean premium,
                                  ConfigManager cm, ItemManager im) {
        if (!hasVanillaPaper(player, 1)) {
            player.sendMessage(c(cm.getMessage("need-paper")));
            return;
        }

        boolean creative = player.getGameMode() == GameMode.CREATIVE;

        // Remove paper
        if (!creative) removeVanillaPaper(player, 1);

        // Consume the bud — re-read from main hand to get live stack reference
        if (!creative) {
            ItemStack current = player.getInventory().getItemInMainHand();
            if (im.isWeedItem(current)) {
                int newAmt = current.getAmount() - 1;
                if (newAmt <= 0) {
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                } else {
                    current.setAmount(newAmt);
                }
            }
        }

        // Give joint
        giveItem(player, premium ? im.createPremiumJoint(1) : im.createJoint(1));
        player.sendMessage(c(cm.getMessage(premium ? "rolling-premium" : "rolling-joint")));
        playSound(player, "ENTITY_CAT_PURR", cm);
    }

    // ── Smoke Joint ──────────────────────────────────────────────────────────────

    private void handleSmokeJoint(Player player, ItemStack held, boolean premium,
                                   ConfigManager cm, ItemManager im) {
        if (!player.hasPermission("weedplugin.use")) {
            player.sendMessage(c(cm.getMessage("no-permission"))); return;
        }
        if (smokingPlayers.contains(player.getUniqueId())) return;

        boolean creative = player.getGameMode() == GameMode.CREATIVE;
        if (!creative) {
            ItemStack current = player.getInventory().getItemInMainHand();
            if (im.isWeedItem(current)) {
                int newAmt = current.getAmount() - 1;
                if (newAmt <= 0) player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                else current.setAmount(newAmt);
            }
        }

        player.sendMessage(c(cm.getMessage(premium ? "smoked-premium" : "smoked-joint")));
        playSound(player, cm.config().getString("sounds.smoke", "ENTITY_CAT_PURR"), cm);

        var effects  = premium ? cm.getPremiumJointEffects() : cm.getJointEffects();
        int duration = premium ? cm.getPremiumJointDuration() : cm.getJointDuration();
        for (var fx : effects)
            player.addPotionEffect(new PotionEffect(fx.type(), duration, fx.amplifier(), false, true, true));

        if (effects.isEmpty())
            plugin.getLogger().warning("[WeedPlugin] No effects for " + (premium ? "premium " : "") + "joint — check config.");

        startSmokingAnimation(player, premium, cm);
    }

    // ── Smoking Animation ────────────────────────────────────────────────────────

    private void startSmokingAnimation(Player player, boolean premium, ConfigManager cm) {
        smokingPlayers.add(player.getUniqueId());
        int puffCount    = cm.getSmokePuffCount();
        int puffInterval = cm.getSmokePuffIntervalTicks();

        new BukkitRunnable() {
            int puffs = 0;
            @Override public void run() {
                if (!player.isOnline() || puffs >= puffCount) {
                    smokingPlayers.remove(player.getUniqueId());
                    cancel(); return;
                }
                var eye    = player.getEyeLocation();
                var origin = eye.clone().add(eye.getDirection().normalize().multiply(0.6)).add(0, 0.1, 0);
                player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, origin, 3, 0.05, 0.05, 0.05, 0.015);
                if (premium)
                    player.getWorld().spawnParticle(Particle.END_ROD, origin.clone().add(0, 0.1, 0), 2, 0.05, 0.05, 0.05, 0.01);
                if (puffs % 2 == 0 && cm.areSoundsEnabled())
                    player.playSound(player.getLocation(), Sound.ENTITY_CAT_PURR, 0.4f, premium ? 0.7f : 1.0f);
                puffs++;
            }
        }.runTaskTimer(plugin, 0L, puffInterval);
    }

    // ── Eat Edible ───────────────────────────────────────────────────────────────

    private void handleEatEdible(Player player, ItemStack held,
                                  ConfigManager cm, ItemManager im) {
        if (!player.hasPermission("weedplugin.use")) {
            player.sendMessage(c(cm.getMessage("no-permission"))); return;
        }

        boolean creative = player.getGameMode() == GameMode.CREATIVE;
        if (!creative) {
            ItemStack current = player.getInventory().getItemInMainHand();
            if (im.isWeedItem(current)) {
                int newAmt = current.getAmount() - 1;
                if (newAmt <= 0) player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                else current.setAmount(newAmt);
            }
        }

        player.sendMessage(c(cm.getMessage("ate-edible")));
        playSound(player, cm.config().getString("sounds.eat", "ENTITY_PLAYER_BURP"), cm);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0,1.5,0), 5, 0.2, 0.2, 0.2, 0);

        new BukkitRunnable() {
            @Override public void run() {
                if (!player.isOnline()) return;
                player.sendMessage(c(cm.getMessage("edible-kicked-in")));
                var effects  = cm.getEdibleEffects();
                int duration = cm.getEdibleDuration();
                for (var fx : effects)
                    player.addPotionEffect(new PotionEffect(fx.type(), duration, fx.amplifier(), false, true, true));
                if (effects.isEmpty())
                    plugin.getLogger().warning("[WeedPlugin] No edible effects — check config.");
                player.getWorld().spawnParticle(Particle.WITCH,        player.getLocation().add(0,1,0),   25, 0.6, 0.6, 0.6, 0.1);
                player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0,1.5,0), 15, 0.4, 0.3, 0.4, 0);
            }
        }.runTaskLater(plugin, cm.getEdibleDelaySeconds() * 20L);
    }

    // ── Vanilla Paper helpers ─────────────────────────────────────────────────────

    private boolean hasVanillaPaper(Player player, int amount) {
        int count = 0;
        for (ItemStack i : player.getInventory().getContents()) {
            if (i == null || i.getType() != Material.PAPER) continue;
            if (plugin.getItemManager().isWeedItem(i)) continue;
            count += i.getAmount();
            if (count >= amount) return true;
        }
        return false;
    }

    private void removeVanillaPaper(Player player, int amount) {
        int remaining = amount;
        for (ItemStack i : player.getInventory().getContents()) {
            if (remaining <= 0) break;
            if (i == null || i.getType() != Material.PAPER) continue;
            if (plugin.getItemManager().isWeedItem(i)) continue;
            if (i.getAmount() <= remaining) { remaining -= i.getAmount(); i.setAmount(0); }
            else { i.setAmount(i.getAmount() - remaining); remaining = 0; }
        }
    }

    // Give item; if inventory full, drop at feet
    private void giveItem(Player player, ItemStack item) {
        var leftover = player.getInventory().addItem(item);
        leftover.values().forEach(drop ->
            player.getWorld().dropItemNaturally(player.getLocation(), drop));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private Component c(String legacy) {
        return LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    private void playSound(Player player, String name, ConfigManager cm) {
        if (!cm.areSoundsEnabled() || name == null) return;
        try { player.playSound(player.getLocation(), Sound.valueOf(name.toUpperCase()), 1f, 1f); }
        catch (Exception ignored) {}
    }
}
