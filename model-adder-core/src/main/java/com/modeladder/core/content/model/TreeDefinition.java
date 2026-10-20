package com.modeladder.core.content.model;

import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.CustomTree;
import org.jetbrains.annotations.NotNull;

public final class TreeDefinition implements CustomTree {

    private final ContentKey key;
    private final String displayName;
    private final ContentKey saplingKey;
    private final ContentKey logKey;
    private final ContentKey leavesKey;
    private final int minHeight;
    private final int maxHeight;
    private final int canopyRadius;

    public TreeDefinition(
        @NotNull ContentKey key,
        @NotNull String displayName,
        @NotNull ContentKey saplingKey,
        @NotNull ContentKey logKey,
        @NotNull ContentKey leavesKey,
        int minHeight,
        int maxHeight,
        int canopyRadius
    ) {
        this.key = key;
        this.displayName = displayName;
        this.saplingKey = saplingKey;
        this.logKey = logKey;
        this.leavesKey = leavesKey;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.canopyRadius = canopyRadius;
    }

    @NotNull @Override public ContentKey getKey() { return key; }
    @NotNull @Override public String getDisplayName() { return displayName; }
    @NotNull @Override public ContentKey getSaplingKey() { return saplingKey; }
    @NotNull @Override public ContentKey getLogKey() { return logKey; }
    @NotNull @Override public ContentKey getLeavesKey() { return leavesKey; }
    @Override public int getMinHeight() { return minHeight; }
    @Override public int getMaxHeight() { return maxHeight; }
    @Override public int getCanopyRadius() { return canopyRadius; }
}
