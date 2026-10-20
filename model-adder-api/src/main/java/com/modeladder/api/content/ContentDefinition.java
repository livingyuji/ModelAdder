package com.modeladder.api.content;

import org.jetbrains.annotations.NotNull;

/**
 * Base contract for any defined custom content in Model Adder.
 */
public interface ContentDefinition {

    /**
     * @return the unique namespaced key for this content.
     */
    @NotNull
    ContentKey getKey();

    /**
     * @return the content category/type.
     */
    @NotNull
    ContentType getType();

    /**
     * @return the human-readable display name or key if unset.
     */
    @NotNull
    String getDisplayName();
}
