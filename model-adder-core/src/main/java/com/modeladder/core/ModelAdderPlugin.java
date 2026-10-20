package com.modeladder.core;

import com.modeladder.api.ModelAdderAPI;
import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.CustomBlock;
import com.modeladder.api.content.CustomItem;
import com.modeladder.api.pack.PackManager;
import com.modeladder.api.registry.RegistryManager;
import com.modeladder.api.validation.ValidationResult;
import com.modeladder.core.block.BlockListener;
import com.modeladder.core.block.BlockStorage;
import com.modeladder.core.command.ModelAdderCommand;
import com.modeladder.core.config.ModelAdderConfig;
import com.modeladder.core.content.loader.ContentLoader;
import com.modeladder.core.diagnostics.DiagnosticsService;
import com.modeladder.core.furniture.FurnitureListener;
import com.modeladder.core.item.AbilityExecutor;
import com.modeladder.core.item.ItemFactory;
import com.modeladder.core.item.ItemListener;
import com.modeladder.core.mob.MobListener;
import com.modeladder.core.mob.MobManager;
import com.modeladder.core.recipe.RecipeManager;
import com.modeladder.core.registry.DefaultRegistryManager;
import com.modeladder.core.world.CropListener;
import com.modeladder.pack.DefaultPackManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.modeladder.editor.listener.GuiListener;
import com.modeladder.integrations.IntegrationManager;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Main plugin class for Model Adder custom content and resource-pack engine.
 */
public final class ModelAdderPlugin extends JavaPlugin implements ModelAdderAPI, Listener {

    private ModelAdderConfig config;
    private DefaultRegistryManager registryManager;
    private DefaultPackManager packManager;
    private BlockStorage blockStorage;
    private MobManager mobManager;
    private AbilityExecutor abilityExecutor;
    private RecipeManager recipeManager;
    private DiagnosticsService diagnosticsService;
    private IntegrationManager integrationManager;

    @Override
    public void onEnable() {
        // Banner
        getLogger().info("==================================================");
        getLogger().info("           MODEL ADDER — Java 1.21.11             ");
        getLogger().info("  Original Custom Content & Resource-Pack Engine   ");
        getLogger().info("==================================================");

        saveDefaultConfig();
        this.config = new ModelAdderConfig(getConfig());

        // 1. Initialize API Singleton
        ModelAdderAPI.setInstance(this);

        // 2. Initialize Subsystems
        Path dataPath = getDataFolder().toPath();
        this.registryManager = new DefaultRegistryManager();
        this.packManager = new DefaultPackManager(dataPath, getLogger());
        this.blockStorage = new BlockStorage(dataPath);
        this.mobManager = new MobManager(registryManager);
        this.abilityExecutor = new AbilityExecutor();
        this.recipeManager = new RecipeManager(registryManager, getLogger());
        this.diagnosticsService = new DiagnosticsService(getDescription().getVersion(), registryManager, packManager);

        // 3. Configure Resource Pack Hosting
        packManager.configureHosting(
            config.isBuiltInEnabled(),
            config.getBindAddress(),
            config.getHttpPort(),
            config.getPublicUrl(),
            config.isExternalEnabled(),
            config.getExternalUrl(),
            config.getExternalSha1()
        );

        // 4. Load Content Definitions
        ContentLoader contentLoader = new ContentLoader(dataPath.resolve("content"), registryManager, getLogger());
        ValidationResult loadResult = contentLoader.loadAll();
        if (!loadResult.isValid()) {
            getLogger().warning("[ModelAdder] Some content definitions had errors during loading:");
            loadResult.getErrors().forEach(err -> getLogger().warning("  - " + err));
        }

        getLogger().info("[ModelAdder] Loaded " + registryManager.getTotalContentCount() + " custom content definitions.");

        // 5. Register Custom Recipes
        recipeManager.registerAll();

        // 6. Register Event Listeners
        getServer().getPluginManager().registerEvents(new ItemListener(registryManager, abilityExecutor), this);
        getServer().getPluginManager().registerEvents(new BlockListener(registryManager, blockStorage), this);
        getServer().getPluginManager().registerEvents(new FurnitureListener(registryManager), this);
        getServer().getPluginManager().registerEvents(new CropListener(registryManager), this);
        getServer().getPluginManager().registerEvents(new MobListener(registryManager, mobManager), this);
        getServer().getPluginManager().registerEvents(new GuiListener(), this);
        getServer().getPluginManager().registerEvents(this, this);

        // 7. Initialize Integrations
        this.integrationManager = new IntegrationManager(getLogger());
        integrationManager.init();

        // 8. Register Commands
        PluginCommand cmd = getCommand("modeladder");
        if (cmd != null) {
            ModelAdderCommand executor = new ModelAdderCommand(this, registryManager, packManager, mobManager, diagnosticsService);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        getLogger().info("[ModelAdder] Successfully enabled Model Adder v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("[ModelAdder] Disabling Model Adder...");

        if (integrationManager != null) {
            integrationManager.shutdown();
        }
        if (recipeManager != null) {
            recipeManager.unregisterAll();
        }
        if (blockStorage != null) {
            blockStorage.save();
        }
        if (packManager != null) {
            packManager.shutdown();
        }
        if (abilityExecutor != null) {
            abilityExecutor.clearAll();
        }

        ModelAdderAPI.setInstance(null);
        getLogger().info("[ModelAdder] Disabled cleanly.");
    }

    public synchronized void reloadAll() {
        reloadConfig();
        this.config = new ModelAdderConfig(getConfig());

        // Reload hosting configuration
        packManager.configureHosting(
            config.isBuiltInEnabled(),
            config.getBindAddress(),
            config.getHttpPort(),
            config.getPublicUrl(),
            config.isExternalEnabled(),
            config.getExternalUrl(),
            config.getExternalSha1()
        );

        // Reload content definitions
        ContentLoader contentLoader = new ContentLoader(getDataFolder().toPath().resolve("content"), registryManager, getLogger());
        contentLoader.loadAll();

        // Re-register recipes
        recipeManager.registerAll();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (config.isPromptOnJoin()) {
            // Slight delay so the player completes connection before receiving pack prompt
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (event.getPlayer().isOnline()) {
                    packManager.sendPack(event.getPlayer());
                }
            }, 20L);
        }
    }

    // --- ModelAdderAPI Implementation ---

    @NotNull
    @Override
    public RegistryManager getRegistries() {
        return registryManager;
    }

    @NotNull
    @Override
    public PackManager getPackManager() {
        return packManager;
    }

    @Override
    public boolean isCustomItem(@Nullable ItemStack item) {
        return ItemFactory.isCustomItem(item);
    }

    @NotNull
    @Override
    public Optional<ContentKey> getItemKey(@Nullable ItemStack item) {
        return ItemFactory.getItemKey(item);
    }

    @NotNull
    @Override
    public Optional<CustomItem> getCustomItem(@Nullable ItemStack item) {
        Optional<ContentKey> keyOpt = getItemKey(item);
        return keyOpt.flatMap(contentKey -> registryManager.getItems().get(contentKey));
    }

    @NotNull
    @Override
    public Optional<ItemStack> createItemStack(@NotNull ContentKey key, int amount) {
        Optional<CustomItem> itemOpt = registryManager.getItems().get(key);
        return itemOpt.map(customItem -> ItemFactory.create(customItem, amount));
    }

    @Override
    public boolean isCustomBlock(@Nullable Block block) {
        if (block == null) return false;
        return blockStorage.isCustomBlock(block);
    }

    @NotNull
    @Override
    public Optional<ContentKey> getBlockKey(@Nullable Block block) {
        if (block == null) return Optional.empty();
        return Optional.ofNullable(blockStorage.getBlockKey(block));
    }

    @NotNull
    @Override
    public Optional<CustomBlock> getCustomBlock(@Nullable Block block) {
        Optional<ContentKey> keyOpt = getBlockKey(block);
        return keyOpt.flatMap(contentKey -> registryManager.getBlocks().get(contentKey));
    }
}
