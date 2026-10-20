package com.modeladder.api.content;

import org.jetbrains.annotations.NotNull;

/**
 * Custom tree definition specifying sapling, trunk logs, and leaves.
 */
public interface CustomTree extends ContentDefinition {

    @Override
    default @NotNull ContentType getType() {
        return ContentType.TREE;
    }

    /**
     * @return sapling item key.
     */
    @NotNull
    ContentKey getSaplingKey();

    /**
     * @return custom block key used for trunk logs.
     */
    @NotNull
    ContentKey getLogKey();

    /**
     * @return custom block key used for leaves.
     */
    @NotNull
    ContentKey getLeavesKey();

    /**
     * @return minimum trunk height in blocks.
     */
    int getMinHeight();

    /**
     * @return maximum trunk height in blocks.
     */
    int getMaxHeight();

    /**
     * @return radius of foliage canopy.
     */
    int getCanopyRadius();
}
