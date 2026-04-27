package dev.weedplugin;

import dev.weedplugin.commands.WeedCommand;
import dev.weedplugin.listeners.CropGrowthListener;
import dev.weedplugin.listeners.ItemUseListener;
import dev.weedplugin.listeners.PlantListener;
import dev.weedplugin.managers.ConfigManager;
import dev.weedplugin.managers.GrowthManager;
import dev.weedplugin.managers.ItemManager;
import org.bukkit.plugin.java.JavaPlugin;

public class WeedPlugin extends JavaPlugin {

    private static WeedPlugin instance;
    private ConfigManager configManager;
    private ItemManager itemManager;
    private GrowthManager growthManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        configManager = new ConfigManager(this);
        itemManager = new ItemManager(this);
        growthManager = new GrowthManager(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlantListener(this), this);
        getServer().getPluginManager().registerEvents(new CropGrowthListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemUseListener(this), this);

        // Register commands
        getCommand("weed").setExecutor(new WeedCommand(this));
        getCommand("weed").setTabCompleter(new WeedCommand(this));

        getLogger().info("🌿 WeedPlugin enabled! Time to grow...");
    }

    @Override
    public void onDisable() {
        if (growthManager != null) {
            growthManager.saveGrowthData();
        }
        getLogger().info("WeedPlugin disabled.");
    }

    public static WeedPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public GrowthManager getGrowthManager() {
        return growthManager;
    }
}
