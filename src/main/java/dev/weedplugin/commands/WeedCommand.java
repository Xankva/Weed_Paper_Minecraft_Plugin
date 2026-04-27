package dev.weedplugin.commands;

import dev.weedplugin.WeedPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class WeedCommand implements CommandExecutor, TabCompleter {

    private final WeedPlugin plugin;

    public WeedCommand(WeedPlugin plugin) { this.plugin = plugin; }

    private Component mm(String legacy) {
        return LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        var cm = plugin.getConfigManager();

        if (args.length == 0) { sendHelp(sender, cm.getPrefix()); return true; }

        switch (args[0].toLowerCase()) {

            case "reload" -> {
                if (!sender.hasPermission("weedplugin.admin")) { sender.sendMessage(mm(cm.getMessage("no-permission"))); return true; }
                cm.reload();
                sender.sendMessage(mm(cm.getMessage("reload-success")));
            }

            case "give" -> {
                if (!sender.hasPermission("weedplugin.give")) { sender.sendMessage(mm(cm.getMessage("no-permission"))); return true; }
                if (args.length < 3) {
                    sender.sendMessage(mm(cm.getPrefix() + "&cUsage: /weed give <player> <item> [amount]"));
                    sender.sendMessage(mm(cm.getPrefix() + "&7Items: &fseed, bud, premium_bud, joint, premium_joint, edible"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sender.sendMessage(mm(cm.getPrefix() + "&cPlayer not found: " + args[1])); return true; }

                int amount = 1;
                if (args.length >= 4) try { amount = Math.max(1, Math.min(64, Integer.parseInt(args[3]))); } catch (NumberFormatException ignored) {}

                var im = plugin.getItemManager();
                var item = switch (args[2].toLowerCase()) {
                    case "seed"                      -> im.createSeed(amount);
                    case "bud"                       -> im.createBud(amount);
                    case "premium_bud","premiumbud"  -> im.createPremiumBud(amount);
                    case "joint"                     -> im.createJoint(amount);
                    case "premium_joint","premiumjoint" -> im.createPremiumJoint(amount);
                    case "edible","brownie"          -> im.createEdible(amount);
                    default -> null;
                };

                if (item == null) { sender.sendMessage(mm(cm.getPrefix() + "&cUnknown item.")); return true; }
                target.getInventory().addItem(item);
                sender.sendMessage(mm(cm.getPrefix() + "&aGave &e" + amount + "x &f" + args[2] + " &ato &e" + target.getName()));
                target.sendMessage(mm(cm.getPrefix() + "&aYou received &e" + amount + "x &f" + args[2] + " &afrom &e" + sender.getName() + "&a!"));
            }

            case "info" -> {
                sender.sendMessage(mm(cm.getPrefix() + "&aWeedPlugin &7v" + plugin.getDescription().getVersion()));
                sender.sendMessage(mm(cm.getPrefix() + "&7Growth time: &e" + cm.getGrowthTimeSeconds() + "s &7per stage"));
                sender.sendMessage(mm(cm.getPrefix() + "&7Premium chance: &e" + (int)(cm.getPremiumBudChance()*100) + "%"));
                sender.sendMessage(mm(cm.getPrefix() + "&7Auto-replant: &e" + cm.isAutoReplant()));
                sender.sendMessage(mm(cm.getPrefix() + "&7Active plants: &e" + plugin.getGrowthManager().getActivePlantCount()));
            }

            default -> sendHelp(sender, cm.getPrefix());
        }
        return true;
    }

    private void sendHelp(CommandSender s, String pre) {
        s.sendMessage(mm(pre + "&a--- WeedPlugin Commands ---"));
        s.sendMessage(mm("&e/weed give &f<player> <item> [amount] &7- Give weed items"));
        s.sendMessage(mm("&e/weed reload &7- Reload config.yml"));
        s.sendMessage(mm("&e/weed info &7- Show plugin stats"));
        s.sendMessage(mm("&7Items: &fseed, bud, premium_bud, joint, premium_joint, edible"));
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        if (args.length == 1) return Arrays.asList("give","reload","info");
        if (args.length == 2 && args[0].equalsIgnoreCase("give"))
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        if (args.length == 3 && args[0].equalsIgnoreCase("give"))
            return Arrays.asList("seed","bud","premium_bud","joint","premium_joint","edible");
        return List.of();
    }
}
