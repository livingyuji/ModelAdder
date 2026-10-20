package com.modeladder.api.registry;

import com.modeladder.api.content.ContentDefinition;
import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.ContentType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

/**
 * Thread-safe registry for a specific category of custom content definitions.
 *
 * @param <T> specific ContentDefinition type
 */
public interface ContentRegistry<T extends ContentDefinition> {

    @NotNull
    ContentType getType();

    void register(@NotNull T definition);

    boolean unregister(@NotNull ContentKey key);

    @NotNull
    Optional<T> get(@NotNull ContentKey key);

    @NotNull
    default Optional<T> get(@NotNull String rawKey) {
        if (!ContentKey.isValid(rawKey)) {
            return Optional.empty();
        }
        return get(ContentKey.of(rawKey));
    }

    boolean contains(@NotNull ContentKey key);

    @NotNull
    Collection<T> getAll();

    int size();

    void clear();
}
