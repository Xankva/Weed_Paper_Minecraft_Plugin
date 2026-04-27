package dev.weedplugin.managers;

import dev.weedplugin.WeedPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ItemManager {

    private final WeedPlugin plugin;
    public final NamespacedKey KEY_ITEM_TYPE;

    public static final String SEED          = "weed_seed";
    public static final String BUD           = "cannabis_bud";
    public static final String PREMIUM_BUD   = "premium_bud";
    public static final String JOINT         = "joint";
    public static final String PREMIUM_JOINT = "premium_joint";
    public static final String EDIBLE        = "edible";

    private final NamespacedKey MODEL_SEED, MODEL_BUD, MODEL_PREMIUM_BUD,
                                MODEL_JOINT, MODEL_PREMIUM_JOINT, MODEL_EDIBLE;

    public ItemManager(WeedPlugin plugin) {
        this.plugin = plugin;
        KEY_ITEM_TYPE       = new NamespacedKey(plugin, "weed_item_type");
        MODEL_SEED          = new NamespacedKey(plugin, "weed_seed");
        MODEL_BUD           = new NamespacedKey(plugin, "cannabis_bud");
        MODEL_PREMIUM_BUD   = new NamespacedKey(plugin, "premium_bud");
        MODEL_JOINT         = new NamespacedKey(plugin, "joint");
        MODEL_PREMIUM_JOINT = new NamespacedKey(plugin, "premium_joint");
        MODEL_EDIBLE        = new NamespacedKey(plugin, "edible");
    }

    public ItemStack createSeed(int amount)         { return build(Material.WHEAT_SEEDS, SEED,          "seed",          MODEL_SEED,          amount); }
    public ItemStack createBud(int amount)          { return build(Material.PAPER,       BUD,           "bud",           MODEL_BUD,           amount); }
    public ItemStack createPremiumBud(int amount)   { return build(Material.PAPER,       PREMIUM_BUD,   "premium-bud",   MODEL_PREMIUM_BUD,   amount); }
    public ItemStack createJoint(int amount)        { return build(Material.PAPER,       JOINT,         "joint",         MODEL_JOINT,         amount); }
    public ItemStack createPremiumJoint(int amount) { return build(Material.PAPER,       PREMIUM_JOINT, "premium-joint", MODEL_PREMIUM_JOINT, amount); }
    public ItemStack createEdible(int amount)       { return build(Material.COOKIE,      EDIBLE,        "edible",        MODEL_EDIBLE,        amount); }

    private ItemStack build(Material mat, String typeId, String cfgKey, NamespacedKey modelKey, int amount) {
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        var cm = plugin.getConfigManager();

        // Use Adventure API for name and lore — proper Component, no legacy codes
        String rawName = cm.getItemName(cfgKey);  // returns §-coded string
        Component name = LegacyComponentSerializer.legacySection().deserialize(rawName);
        meta.displayName(name);

        List<String> rawLore = cm.getItemLore(cfgKey);
        List<Component> lore = rawLore.stream()
            .map(l -> (Component) LegacyComponentSerializer.legacySection().deserialize(l))
            .toList();
        meta.lore(lore);

        meta.setItemModel(modelKey);
        meta.getPersistentDataContainer().set(KEY_ITEM_TYPE, PersistentDataType.STRING, typeId);
        item.setItemMeta(meta);
        return item;
    }

    public String getItemType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        var meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(KEY_ITEM_TYPE, PersistentDataType.STRING);
    }

    public boolean isSeed(ItemStack item)         { return SEED.equals(getItemType(item)); }
    public boolean isBud(ItemStack item)          { return BUD.equals(getItemType(item)); }
    public boolean isPremiumBud(ItemStack item)   { return PREMIUM_BUD.equals(getItemType(item)); }
    public boolean isJoint(ItemStack item)        { return JOINT.equals(getItemType(item)); }
    public boolean isPremiumJoint(ItemStack item) { return PREMIUM_JOINT.equals(getItemType(item)); }
    public boolean isEdible(ItemStack item)       { return EDIBLE.equals(getItemType(item)); }
    public boolean isWeedItem(ItemStack item)     { return getItemType(item) != null; }
}
