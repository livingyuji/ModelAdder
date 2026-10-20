package com.modeladder.core.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Validated configuration container for Model Adder.
 */
public final class ModelAdderConfig {

    private final String hostingMode;
    private final boolean builtInEnabled;
    private final String bindAddress;
    private final int httpPort;
    private final String publicUrl;

    private final boolean externalEnabled;
    private final String externalUrl;
    private final String externalSha1;

    private final boolean promptOnJoin;
    private final boolean packRequired;

    private final boolean backupsEnabled;
    private final int maxBuilds;

    private final long maxArchiveBytes;
    private final long maxExpandedBytes;
    private final int maxEntries;
    private final boolean debugMode;

    public ModelAdderConfig(@NotNull FileConfiguration config) {
        this.hostingMode = config.getString("resource-pack.hosting.mode", "built-in");

        this.builtInEnabled = config.getBoolean("resource-pack.hosting.built-in.enabled", true);
        this.bindAddress = config.getString("resource-pack.hosting.built-in.bind-address", "0.0.0.0");
        this.httpPort = config.getInt("resource-pack.hosting.built-in.port", 8085);
        this.publicUrl = config.getString("resource-pack.hosting.built-in.public-url", "");

        this.externalEnabled = "external".equalsIgnoreCase(hostingMode);
        this.externalUrl = config.getString("resource-pack.hosting.external.url", "");
        this.externalSha1 = config.getString("resource-pack.hosting.external.expected-sha1", "");

        this.promptOnJoin = config.getBoolean("resource-pack.delivery.prompt-on-join", true);
        this.packRequired = config.getBoolean("resource-pack.delivery.required", false);

        this.backupsEnabled = config.getBoolean("resource-pack.backups.enabled", true);
        this.maxBuilds = Math.max(1, config.getInt("resource-pack.backups.max-builds", 10));

        this.maxArchiveBytes = config.getLong("security.limits.max-archive-mb", 100) * 1024 * 1024;
        this.maxExpandedBytes = config.getLong("security.limits.max-expanded-mb", 250) * 1024 * 1024;
        this.maxEntries = Math.max(100, config.getInt("security.limits.max-entries", 10000));
        this.debugMode = config.getBoolean("debug", false);
    }

    public String getHostingMode() { return hostingMode; }
    public boolean isBuiltInEnabled() { return builtInEnabled && "built-in".equalsIgnoreCase(hostingMode); }
    public String getBindAddress() { return bindAddress; }
    public int getHttpPort() { return httpPort; }
    public String getPublicUrl() { return publicUrl; }

    public boolean isExternalEnabled() { return externalEnabled; }
    public String getExternalUrl() { return externalUrl; }
    public String getExternalSha1() { return externalSha1; }

    public boolean isPromptOnJoin() { return promptOnJoin; }
    public boolean isPackRequired() { return packRequired; }

    public boolean isBackupsEnabled() { return backupsEnabled; }
    public int getMaxBuilds() { return maxBuilds; }

    public long getMaxArchiveBytes() { return maxArchiveBytes; }
    public long getMaxExpandedBytes() { return maxExpandedBytes; }
    public int getMaxEntries() { return maxEntries; }
    public boolean isDebugMode() { return debugMode; }
}
