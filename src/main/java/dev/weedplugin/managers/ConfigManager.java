package dev.weedplugin.managers;

import dev.weedplugin.WeedPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final WeedPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(WeedPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public FileConfiguration config() { return config; }

    // ── Settings ─────────────────────────────────────────────────────────────────
    public int getGrowthTimeSeconds()    { return config.getInt("settings.growth-time-seconds", 120); }
    public boolean isBonemealAllowed()   { return config.getBoolean("settings.allow-bonemeal", true); }
    public int getBonemealStages()       { return config.getInt("settings.bonemeal-stages", 1); }
    public List<String> getDisabledWorlds() { return config.getStringList("settings.disabled-worlds"); }

    public List<Material> getValidSoil() {
        List<Material> list = new ArrayList<>();
        for (String s : config.getStringList("settings.valid-soil")) {
            try { list.add(Material.valueOf(s.toUpperCase())); }
            catch (IllegalArgumentException e) { plugin.getLogger().warning("Invalid soil: " + s); }
        }
        if (list.isEmpty()) { list.add(Material.FARMLAND); list.add(Material.DIRT); }
        return list;
    }

    // ── Harvest ──────────────────────────────────────────────────────────────────
    public int getMinSeeds()           { return config.getInt("harvest.min-seeds", 1); }
    public int getMaxSeeds()           { return config.getInt("harvest.max-seeds", 3); }
    public int getMinBuds()            { return config.getInt("harvest.min-buds", 1); }
    public int getMaxBuds()            { return config.getInt("harvest.max-buds", 4); }
    public double getPremiumBudChance(){ return config.getDouble("harvest.premium-bud-chance", 0.15); }
    public boolean isAutoReplant()     { return config.getBoolean("harvest.auto-replant", false); }

    // ── Items ────────────────────────────────────────────────────────────────────
    public String getItemName(String key) {
        return legacyColor(config.getString("items." + key + ".name", "&fItem"));
    }
    public List<String> getItemLore(String key) {
        List<String> out = new ArrayList<>();
        for (String l : config.getStringList("items." + key + ".lore"))
            out.add(legacyColor(l));
        return out;
    }

    // ── Effects ──────────────────────────────────────────────────────────────────
    public record EffectEntry(PotionEffectType type, int amplifier) {}

    public List<EffectEntry> getJointEffects()        { return loadEffects("joint-effects.effects"); }
    public int getJointDuration()                     { return config.getInt("joint-effects.duration-seconds", 30) * 20; }
    public List<EffectEntry> getPremiumJointEffects() { return loadEffects("premium-joint-effects.effects"); }
    public int getPremiumJointDuration()              { return config.getInt("premium-joint-effects.duration-seconds", 60) * 20; }
    public List<EffectEntry> getEdibleEffects()       { return loadEffects("edible-effects.effects"); }
    public int getEdibleDuration()                    { return config.getInt("edible-effects.duration-seconds", 120) * 20; }
    public int getEdibleDelaySeconds()                { return config.getInt("edible-effects.delay-seconds", 10); }

    private List<EffectEntry> loadEffects(String path) {
        List<EffectEntry> list = new ArrayList<>();
        for (var map : config.getMapList(path)) {
            String typeName = String.valueOf(map.get("type"));
            int amp = map.containsKey("amplifier") ? ((Number) map.get("amplifier")).intValue() : 0;
            PotionEffectType type = resolveEffectType(typeName);
            if (type != null) list.add(new EffectEntry(type, amp));
            else plugin.getLogger().warning("Unknown effect: " + typeName);
        }
        return list;
    }

    @SuppressWarnings("deprecation")
    private PotionEffectType resolveEffectType(String name) {
        if (name == null || name.isBlank()) return null;
        try {
            PotionEffectType t = Registry.EFFECT.get(NamespacedKey.minecraft(name.toLowerCase()));
            if (t != null) return t;
        } catch (Exception ignored) {}
        try {
            var f = PotionEffectType.class.getField(name.toUpperCase());
            Object v = f.get(null);
            if (v instanceof PotionEffectType t) return t;
        } catch (Exception ignored) {}
        return null;
    }

    // ── Messages ─────────────────────────────────────────────────────────────────
    /** Returns a §-formatted string (legacy color codes) ready for Adventure deserialization */
    public String getPrefix() {
        return legacyColor(config.getString("messages.prefix", "&8[&aWeed&8] "));
    }
    public String getMessage(String key) {
        return getPrefix() + legacyColor(config.getString("messages." + key, "&cMissing message: " + key));
    }
    public String getMessage(String key, String... replacements) {
        String msg = getMessage(key);
        for (int i = 0; i + 1 < replacements.length; i += 2)
            msg = msg.replace(replacements[i], replacements[i + 1]);
        return msg;
    }

    // ── Misc ─────────────────────────────────────────────────────────────────────
    public boolean areParticlesEnabled()  { return config.getBoolean("particles.enabled", true); }
    public boolean areSoundsEnabled()     { return config.getBoolean("sounds.enabled", true); }
    public String getReadyParticle()      { return config.getString("particles.ready-particle", "HAPPY_VILLAGER"); }
    public int getSmokePuffCount()        { return config.getInt("smoking-animation.puff-count", 6); }
    public int getSmokePuffIntervalTicks(){ return config.getInt("smoking-animation.puff-interval-ticks", 7); }

    /** Convert &-codes to §-codes */
    public String legacyColor(String s) {
        return s == null ? "" : s.replace("&", "\u00a7");
    }
}
