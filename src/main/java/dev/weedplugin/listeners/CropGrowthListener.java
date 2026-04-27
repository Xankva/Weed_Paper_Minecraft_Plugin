package dev.weedplugin.listeners;

import dev.weedplugin.WeedPlugin;
import dev.weedplugin.managers.GrowthManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class CropGrowthListener implements Listener {

    private final WeedPlugin plugin;

    public CropGrowthListener(WeedPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * THE MAIN FIX: Wheat has vanilla neighbor-update physics that break it
     * when the block below is not farmland, OR when an adjacent block changes.
     * This fires every time a nearby block is placed/broken.
     * We intercept it and cancel for all cannabis plants so they never pop off.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        // Only care about wheat blocks
        if (block.getType() != Material.WHEAT) return;
        // If it's our cannabis plant, kill the physics check
        if (plugin.getGrowthManager().isCannabisPlant(block)) {
            event.setCancelled(true);
        }
    }

    /**
     * Also cancel the physics on the SOURCE block that triggered the update —
     * when you place wheat next to existing wheat, the existing wheat fires
     * physics on itself. Cancelling on the changed block too covers that case.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onSourcePhysics(BlockPhysicsEvent event) {
        Block source = event.getSourceBlock();
        if (source == null) return;
        if (source.getType() != Material.WHEAT) return;
        if (plugin.getGrowthManager().isCannabisPlant(source)) {
            event.setCancelled(true);
        }
    }

    /**
     * Cancel vanilla random-tick growth — we manage growth ourselves via timer.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockGrow(BlockGrowEvent event) {
        if (plugin.getGrowthManager().isCannabisPlant(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    /**
     * Bonemeal support via right-click.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBonemeal(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.BONE_MEAL) return;

        GrowthManager gm = plugin.getGrowthManager();
        if (!gm.isCannabisPlant(block)) return;

        event.setCancelled(true);
        if (!plugin.getConfigManager().isBonemealAllowed()) return;
        if (gm.isFullyGrown(block)) return;

        gm.advanceStageByBonemeal(block);

        Player player = event.getPlayer();
        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }
        block.getWorld().spawnParticle(
            org.bukkit.Particle.HAPPY_VILLAGER,
            block.getLocation().clone().add(0.5, 0.5, 0.5),
            5, 0.3, 0.3, 0.3, 0
        );
    }

    /**
     * Bonemeal from dispensers.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onFertilize(BlockFertilizeEvent event) {
        GrowthManager gm = plugin.getGrowthManager();
        if (!gm.isCannabisPlant(event.getBlock())) return;
        event.setCancelled(true);
        if (plugin.getConfigManager().isBonemealAllowed()) {
            gm.advanceStageByBonemeal(event.getBlock());
        }
    }
}
