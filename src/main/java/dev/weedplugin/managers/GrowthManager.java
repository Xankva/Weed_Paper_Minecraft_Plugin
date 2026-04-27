package dev.weedplugin.managers;

import dev.weedplugin.WeedPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks cannabis plant growth entirely in memory.
 * growthdata.yml has been removed — plants reset on server restart intentionally,
 * which is simpler and avoids stale data bugs.
 */
public class GrowthManager {

    private final WeedPlugin plugin;
    private final Map<String, Long> plantedTimes = new HashMap<>();
    private final Map<String, Integer> growthStages = new HashMap<>();
    private BukkitTask growthTask;

    public static final int MAX_STAGE = 3;

    public GrowthManager(WeedPlugin plugin) {
        this.plugin = plugin;
        startGrowthTask();
    }

    private void startGrowthTask() {
        growthTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickGrowth, 20L * 10, 20L * 10);
    }

    private void tickGrowth() {
        long now = System.currentTimeMillis();
        int growthMs = plugin.getConfigManager().getGrowthTimeSeconds() * 1000;

        for (String key : Map.copyOf(plantedTimes).keySet()) {
            int currentStage = growthStages.getOrDefault(key, 0);
            if (currentStage >= MAX_STAGE) {
                if (plugin.getConfigManager().areParticlesEnabled()) spawnReadyParticle(key);
                continue;
            }
            long elapsed = now - plantedTimes.get(key);
            int expectedStage = (int) Math.min(elapsed / growthMs, MAX_STAGE);
            if (expectedStage > currentStage) {
                growthStages.put(key, expectedStage);
                updateBlockVisual(key, expectedStage);
            }
        }
    }

    private void updateBlockVisual(String key, int stage) {
        Location loc = keyToLocation(key);
        if (loc == null) return;
        Block block = loc.getBlock();
        if (block.getBlockData() instanceof Ageable ageable) {
            ageable.setAge(Math.min(stage * 2, ageable.getMaximumAge()));
            block.setBlockData(ageable);
        }
    }

    private void spawnReadyParticle(String key) {
        Location loc = keyToLocation(key);
        if (loc == null) return;
        try {
            Particle p = Particle.valueOf(plugin.getConfigManager().getReadyParticle());
            loc.getWorld().spawnParticle(p, loc.clone().add(0.5, 1.2, 0.5), 3, 0.2, 0.1, 0.2, 0);
        } catch (Exception ignored) {}
    }

    public void plantCrop(Block block) {
        String key = locationKey(block.getLocation());
        plantedTimes.put(key, System.currentTimeMillis());
        growthStages.put(key, 0);
        if (block.getBlockData() instanceof Ageable ageable) {
            ageable.setAge(0);
            block.setBlockData(ageable);
        }
    }

    public boolean isCannabisPlant(Block block) {
        return plantedTimes.containsKey(locationKey(block.getLocation()));
    }

    public int getGrowthStage(Block block) {
        return growthStages.getOrDefault(locationKey(block.getLocation()), 0);
    }

    public boolean isFullyGrown(Block block) {
        return getGrowthStage(block) >= MAX_STAGE;
    }

    public void removePlant(Block block) {
        String key = locationKey(block.getLocation());
        plantedTimes.remove(key);
        growthStages.remove(key);
    }

    public void advanceStageByBonemeal(Block block) {
        String key = locationKey(block.getLocation());
        if (!plantedTimes.containsKey(key)) return;
        int stages = plugin.getConfigManager().getBonemealStages();
        int newStage = Math.min(growthStages.getOrDefault(key, 0) + stages, MAX_STAGE);
        growthStages.put(key, newStage);
        long adjustedPlanted = System.currentTimeMillis() - ((long) newStage * plugin.getConfigManager().getGrowthTimeSeconds() * 1000L);
        plantedTimes.put(key, adjustedPlanted);
        updateBlockVisual(key, newStage);
    }

    public int getActivePlantCount() { return plantedTimes.size(); }

    // No-op kept so WeedPlugin.onDisable() still compiles
    public void saveGrowthData() {}

    private String locationKey(Location loc) {
        return loc.getWorld().getUID() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private Location keyToLocation(String key) {
        try {
            String[] p = key.split(",");
            var world = Bukkit.getWorld(UUID.fromString(p[0]));
            if (world == null) return null;
            return new Location(world, Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
        } catch (Exception e) { return null; }
    }
}
