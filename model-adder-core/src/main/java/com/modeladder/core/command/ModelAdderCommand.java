package com.modeladder.core.command;

import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.CustomBlock;
import com.modeladder.api.content.CustomItem;
import com.modeladder.api.content.CustomMob;
import com.modeladder.api.pack.PackManager;
import com.modeladder.api.registry.RegistryManager;
import com.modeladder.api.validation.ValidationResult;
import com.modeladder.core.ModelAdderPlugin;
import com.modeladder.core.diagnostics.DiagnosticsService;
import com.modeladder.core.item.ItemFactory;
import com.modeladder.core.mob.MobManager;
import com.modeladder.core.permission.Permissions;
import com.modeladder.pack.importer.PackImporter;
import com.modeladder.pack.validation.PackAssetValidator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main command handler for /modeladder and /ma.
 */
public final class ModelAdderCommand implements CommandExecutor, TabCompleter {

    private final ModelAdderPlugin plugin;
    private final RegistryManager registryManager;
    private final PackManager packManager;
    private final MobManager mobManager;
    private final DiagnosticsService diagnostics;

    public ModelAdderCommand(
        @NotNull ModelAdderPlugin plugin,
        @NotNull RegistryManager registryManager,
        @NotNull PackManager packManager,
        @NotNull MobManager mobManager,
        @NotNull DiagnosticsService diagnostics
    ) {
        this.plugin = plugin;
        this.registryManager = registryManager;
        this.packManager = packManager;
        this.mobManager = mobManager;
        this.diagnostics = diagnostics;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            sendHelp(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "version" -> {
                sender.sendMessage("§6Model Adder §7v" + plugin.getDescription().getVersion() + " §8(Target: Minecraft Java 1.21.11)");
                return true;
            }
            case "status" -> {
                if (!Permissions.has(sender, Permissions.STATUS)) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }
                sender.sendMessage(diagnostics.generateSummary());
                return true;
            }
            case "reload" -> {
                if (!Permissions.has(sender, Permissions.RELOAD)) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }
                sender.sendMessage("§eReloading Model Adder configuration and content...");
                plugin.reloadAll();
                sender.sendMessage("§aModel Adder reloaded successfully!");
                return true;
            }
            case "debug" -> {
                if (!Permissions.has(sender, Permissions.DEBUG)) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }
                if (args.length > 1 && "export".equalsIgnoreCase(args[1])) {
                    try {
                        Path logsFolder = plugin.getDataFolder().toPath().resolve("logs");
                        Path exported = diagnostics.exportSanitizedReport(logsFolder);
                        sender.sendMessage("§aSanitized debug report exported to: §f" + exported.getFileName());
                    } catch (Exception e) {
                        sender.sendMessage("§cFailed to export debug report: " + e.getMessage());
                    }
                } else {
                    sender.sendMessage(diagnostics.generateSummary());
                }
                return true;
            }
            case "item" -> {
                handleItemCommand(sender, args);
                return true;
            }
            case "block" -> {
                handleBlockCommand(sender, args);
                return true;
            }
            case "mob" -> {
                handleMobCommand(sender, args);
                return true;
            }
            case "pack" -> {
                handlePackCommand(sender, args);
                return true;
            }
            default -> {
                sender.sendMessage("§cUnknown subcommand. Use §e/" + label + " help§c for available commands.");
                return true;
            }
        }
    }

    private void handleItemCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /ma item <list|give|info>");
            return;
        }
        String action = args[1].toLowerCase();

        if ("list".equalsIgnoreCase(action)) {
            if (!Permissions.has(sender, Permissions.ITEM_LIST)) {
                sender.sendMessage("§cNo permission.");
                return;
            }
            Collection<CustomItem> items = registryManager.getItems().getAll();
            sender.sendMessage("§6=== Registered Custom Items (" + items.size() + ") ===");
            for (CustomItem item : items) {
                sender.sendMessage("§7- §e" + item.getKey() + " §7(" + item.getDisplayName() + "§7)");
            }
        } else if ("give".equalsIgnoreCase(action)) {
            if (!Permissions.has(sender, Permissions.ITEM_GIVE)) {
                sender.sendMessage("§cNo permission.");
                return;
            }
            if (args.length < 4) {
                sender.sendMessage("§cUsage: /ma item give <player> <item-id> [amount]");
                return;
            }
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found: " + args[2]);
                return;
            }
            String rawId = args[3];
            if (!ContentKey.isValid(rawId)) {
                sender.sendMessage("§cInvalid item ID: " + rawId);
                return;
            }
            Optional<CustomItem> itemOpt = registryManager.getItems().get(ContentKey.of(rawId));
            if (itemOpt.isEmpty()) {
                sender.sendMessage("§cNo custom item found with ID: " + rawId);
                return;
            }
            int amount = 1;
            if (args.length >= 5) {
                try {
                    amount = Math.max(1, Integer.parseInt(args[4]));
                } catch (NumberFormatException ignored) {}
            }
            ItemStack stack = ItemFactory.create(itemOpt.get(), amount);
            target.getInventory().addItem(stack);
            sender.sendMessage("§aGave " + amount + "x " + rawId + " to " + target.getName());
        } else if ("info".equalsIgnoreCase(action)) {
            if (!Permissions.has(sender, Permissions.ITEM_INFO)) {
                sender.sendMessage("§cNo permission.");
                return;
            }
            if (args.length < 3) {
                sender.sendMessage("§cUsage: /ma item info <item-id>");
                return;
            }
            String rawId = args[2];
            Optional<CustomItem> itemOpt = registryManager.getItems().get(rawId);
            if (itemOpt.isEmpty()) {
                sender.sendMessage("§cItem not found: " + rawId);
                return;
            }
            CustomItem i = itemOpt.get();
            sender.sendMessage("§6Item: §e" + i.getKey());
            sender.sendMessage("§7Display: §f" + i.getDisplayName());
            sender.sendMessage("§7Base Material: §f" + i.getBaseMaterial());
            sender.sendMessage("§7Model Asset: §f" + i.getModelAsset());
            sender.sendMessage("§7Abilities: §f" + i.getAbilities());
        }
    }

    private void handleBlockCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /ma block <list|info>");
            return;
        }
        String action = args[1].toLowerCase();

        if ("list".equalsIgnoreCase(action)) {
            if (!Permissions.has(sender, Permissions.BLOCK_LIST)) {
                sender.sendMessage("§cNo permission.");
                return;
            }
            Collection<CustomBlock> blocks = registryManager.getBlocks().getAll();
            sender.sendMessage("§6=== Registered Custom Blocks (" + blocks.size() + ") ===");
            for (CustomBlock b : blocks) {
                sender.sendMessage("§7- §e" + b.getKey() + " §7(" + b.getDisplayName() + "§7)");
            }
        } else if ("info".equalsIgnoreCase(action)) {
            if (!Permissions.has(sender, Permissions.BLOCK_INFO)) {
                sender.sendMessage("§cNo permission.");
                return;
            }
            if (args.length < 3) {
                sender.sendMessage("§cUsage: /ma block info <block-id>");
                return;
            }
            Optional<CustomBlock> bOpt = registryManager.getBlocks().get(args[2]);
            if (bOpt.isEmpty()) {
                sender.sendMessage("§cBlock not found: " + args[2]);
                return;
            }
            CustomBlock b = bOpt.get();
            sender.sendMessage("§6Block: §e" + b.getKey());
            sender.sendMessage("§7Display: §f" + b.getDisplayName());
            sender.sendMessage("§7Material: §f" + b.getBaseMaterial());
            sender.sendMessage("§7Hardness: §f" + b.getHardness());
            sender.sendMessage("§7Drops: §f" + b.getDrops().size() + " configured");
        }
    }

    private void handleMobCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /ma mob <list|spawn>");
            return;
        }
        String action = args[1].toLowerCase();

        if ("list".equalsIgnoreCase(action)) {
            if (!Permissions.has(sender, Permissions.MOB_LIST)) {
                sender.sendMessage("§cNo permission.");
                return;
            }
            Collection<CustomMob> mobs = registryManager.getMobs().getAll();
            sender.sendMessage("§6=== Registered Custom Mobs (" + mobs.size() + ") ===");
            for (CustomMob m : mobs) {
                sender.sendMessage("§7- §e" + m.getKey() + " §7(" + m.getDisplayName() + "§7)");
            }
        } else if ("spawn".equalsIgnoreCase(action)) {
            if (!Permissions.has(sender, Permissions.MOB_SPAWN)) {
                sender.sendMessage("§cNo permission.");
                return;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players can spawn mobs at their location.");
                return;
            }
            if (args.length < 3) {
                sender.sendMessage("§cUsage: /ma mob spawn <mob-id>");
                return;
            }
            Optional<CustomMob> mobOpt = registryManager.getMobs().get(args[2]);
            if (mobOpt.isEmpty()) {
                sender.sendMessage("§cMob not found: " + args[2]);
                return;
            }
            mobManager.spawn(player.getLocation(), mobOpt.get());
            sender.sendMessage("§aSpawned custom mob: " + args[2]);
        }
    }

    private void handlePackCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /ma pack <import|validate|build|status|list|rollback|send>");
            return;
        }
        String action = args[1].toLowerCase();

        switch (action) {
            case "status" -> {
                if (!Permissions.has(sender, Permissions.PACK_STATUS)) {
                    sender.sendMessage("§cNo permission.");
                    return;
                }
                sender.sendMessage("§6=== Resource Pack Status ===");
                sender.sendMessage("§7Current Build: §e" + packManager.getCurrentBuildId().orElse("None"));
                sender.sendMessage("§7SHA-1: §f" + packManager.getCurrentSha1().orElse("None"));
                sender.sendMessage("§7Download URL: §f" + packManager.getPublicDownloadUrl().orElse("Not configured"));
                sender.sendMessage("§7Published file: §f" + (packManager.getPublishedPackFile().isPresent() ? "Present" : "Missing"));
            }
            case "validate" -> {
                if (!Permissions.has(sender, Permissions.PACK_VALIDATE)) {
                    sender.sendMessage("§cNo permission.");
                    return;
                }
                Path staging = plugin.getDataFolder().toPath().resolve("resourcepack").resolve("staging");
                ValidationResult val = PackAssetValidator.validatePackDirectory(staging);
                if (val.isValid()) {
                    sender.sendMessage("§aResource pack staging assets are valid! (Warnings: " + val.getWarnings().size() + ")");
                } else {
                    sender.sendMessage("§cValidation failed with " + val.getErrors().size() + " errors:");
                    val.getErrors().forEach(e -> sender.sendMessage("§7- §c" + e));
                }
            }
            case "build" -> {
                if (!Permissions.has(sender, Permissions.PACK_BUILD)) {
                    sender.sendMessage("§cNo permission.");
                    return;
                }
                sender.sendMessage("§eStarting resource pack build...");
                packManager.buildPack().thenAccept(res -> {
                    if (res.success()) {
                        sender.sendMessage("§aPack build successful! ID: §f" + res.buildId() + " §a(SHA1: " + res.sha1() + ")");
                    } else {
                        sender.sendMessage("§cPack build failed: " + res.errorMessage());
                    }
                });
            }
            case "list" -> {
                if (!Permissions.has(sender, Permissions.PACK_LIST)) {
                    sender.sendMessage("§cNo permission.");
                    return;
                }
                List<String> backups = packManager.getAvailableBackups();
                sender.sendMessage("§6=== Available Resource Pack Backups (" + backups.size() + ") ===");
                backups.forEach(b -> sender.sendMessage("§7- §e" + b));
            }
            case "rollback" -> {
                if (!Permissions.has(sender, Permissions.PACK_ROLLBACK)) {
                    sender.sendMessage("§cNo permission.");
                    return;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /ma pack rollback <build-id>");
                    return;
                }
                boolean ok = packManager.rollbackTo(args[2]);
                if (ok) {
                    sender.sendMessage("§aSuccessfully rolled back resource pack to: " + args[2]);
                } else {
                    sender.sendMessage("§cRollback failed. Build not found or file error.");
                }
            }
            case "send" -> {
                if (!Permissions.has(sender, Permissions.PACK_SEND)) {
                    sender.sendMessage("§cNo permission.");
                    return;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /ma pack send <player>");
                    return;
                }
                Player p = Bukkit.getPlayer(args[2]);
                if (p == null) {
                    sender.sendMessage("§cPlayer not found: " + args[2]);
                    return;
                }
                boolean sent = packManager.sendPack(p);
                if (sent) {
                    sender.sendMessage("§aDispatched resource pack to " + p.getName());
                } else {
                    sender.sendMessage("§cCould not send pack. Is hosting URL configured?");
                }
            }
            case "import" -> {
                if (!Permissions.has(sender, Permissions.PACK_IMPORT)) {
                    sender.sendMessage("§cNo permission.");
                    return;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /ma pack import <filename.zip>");
                    return;
                }
                File zip = plugin.getDataFolder().toPath().resolve("resourcepack").resolve("imports").resolve(args[2]).toFile();
                if (!zip.exists()) {
                    sender.sendMessage("§cImport file not found in resourcepack/imports/: " + args[2]);
                    return;
                }
                Path staging = plugin.getDataFolder().toPath().resolve("resourcepack").resolve("staging");
                PackImporter importer = new PackImporter();
                ValidationResult res = importer.importArchive(zip, staging, true);
                if (res.isValid()) {
                    sender.sendMessage("§aSuccessfully imported archive: " + args[2]);
                } else {
                    sender.sendMessage("§cArchive import rejected: " + res.getErrors());
                }
            }
        }
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage("§6=== Model Adder Commands ===");
        sender.sendMessage("§e/" + label + " help §7- Show command help");
        sender.sendMessage("§e/" + label + " version §7- Show version and compatibility info");
        sender.sendMessage("§e/" + label + " status §7- Show system and pack status");
        sender.sendMessage("§e/" + label + " reload §7- Safely reload configs and content");
        sender.sendMessage("§e/" + label + " item <list|give|info> §7- Manage custom items");
        sender.sendMessage("§e/" + label + " block <list|info> §7- Manage custom blocks");
        sender.sendMessage("§e/" + label + " mob <list|spawn> §7- Manage custom mobs");
        sender.sendMessage("§e/" + label + " pack <import|validate|build|status|list|rollback|send> §7- Resource pack pipeline");
        sender.sendMessage("§e/" + label + " debug [export] §7- Performance metrics and sanitized log export");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(List.of("help", "version", "status", "reload", "item", "block", "mob", "pack", "debug"), args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            return switch (sub) {
                case "item" -> filter(List.of("list", "give", "info"), args[1]);
                case "block" -> filter(List.of("list", "info"), args[1]);
                case "mob" -> filter(List.of("list", "spawn"), args[1]);
                case "pack" -> filter(List.of("import", "validate", "build", "status", "list", "rollback", "send"), args[1]);
                case "debug" -> filter(List.of("export"), args[1]);
                default -> Collections.emptyList();
            };
        }

        if (args.length == 3) {
            if ("item".equalsIgnoreCase(args[0])) {
                if ("give".equalsIgnoreCase(args[1])) {
                    return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[2]);
                } else if ("info".equalsIgnoreCase(args[1])) {
                    return filter(registryManager.getItems().getAll().stream().map(i -> i.getKey().asString()).toList(), args[2]);
                }
            } else if ("block".equalsIgnoreCase(args[0]) && "info".equalsIgnoreCase(args[1])) {
                return filter(registryManager.getBlocks().getAll().stream().map(b -> b.getKey().asString()).toList(), args[2]);
            } else if ("mob".equalsIgnoreCase(args[0]) && "spawn".equalsIgnoreCase(args[1])) {
                return filter(registryManager.getMobs().getAll().stream().map(m -> m.getKey().asString()).toList(), args[2]);
            } else if ("pack".equalsIgnoreCase(args[0])) {
                if ("send".equalsIgnoreCase(args[1])) {
                    return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[2]);
                } else if ("rollback".equalsIgnoreCase(args[1])) {
                    return filter(packManager.getAvailableBackups(), args[2]);
                }
            }
        }

        if (args.length == 4 && "item".equalsIgnoreCase(args[0]) && "give".equalsIgnoreCase(args[1])) {
            return filter(registryManager.getItems().getAll().stream().map(i -> i.getKey().asString()).toList(), args[3]);
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase();
        return options.stream().filter(o -> o.toLowerCase().startsWith(lower)).collect(Collectors.toList());
    }
}
