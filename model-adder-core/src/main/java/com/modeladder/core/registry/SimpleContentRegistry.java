package com.modeladder.core.registry;

import com.modeladder.api.content.ContentDefinition;
import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.ContentType;
import com.modeladder.api.registry.ContentRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe concurrent content registry implementation.
 */
public final class SimpleContentRegistry<T extends ContentDefinition> implements ContentRegistry<T> {

    private final ContentType type;
    private final Map<ContentKey, T> definitions = new ConcurrentHashMap<>();

    public SimpleContentRegistry(@NotNull ContentType type) {
        this.type = Objects.requireNonNull(type, "type cannot be null");
    }

    @NotNull
    @Override
    public ContentType getType() {
        return type;
    }

    @Override
    public void register(@NotNull T definition) {
        Objects.requireNonNull(definition, "definition cannot be null");
        definitions.put(definition.getKey(), definition);
    }

    @Override
    public boolean unregister(@NotNull ContentKey key) {
        return definitions.remove(key) != null;
    }

    @NotNull
    @Override
    public Optional<T> get(@NotNull ContentKey key) {
        return Optional.ofNullable(definitions.get(key));
    }

    @Override
    public boolean contains(@NotNull ContentKey key) {
        return definitions.containsKey(key);
    }

    @NotNull
    @Override
    public Collection<T> getAll() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    @Override
    public int size() {
        return definitions.size();
    }

    @Override
    public void clear() {
        definitions.clear();
    }
}
