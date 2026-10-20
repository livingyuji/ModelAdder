package com.modeladder.api.content;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Immutable namespaced key uniquely identifying any custom content inside Model Adder.
 * Example format: "modeladder:ruby_sword"
 */
public final class ContentKey implements Comparable<ContentKey> {

    public static final String DEFAULT_NAMESPACE = "modeladder";
    private static final Pattern VALID_NAMESPACE_PATTERN = Pattern.compile("^[a-z0-9_.-]+$");
    private static final Pattern VALID_KEY_PATTERN = Pattern.compile("^[a-z0-9_./-]+$");

    private final String namespace;
    private final String key;
    private final String fullString;

    private ContentKey(@NotNull String namespace, @NotNull String key) {
        this.namespace = Objects.requireNonNull(namespace, "namespace cannot be null").toLowerCase();
        this.key = Objects.requireNonNull(key, "key cannot be null").toLowerCase();

        if (!VALID_NAMESPACE_PATTERN.matcher(this.namespace).matches()) {
            throw new IllegalArgumentException("Invalid namespace: '" + namespace + "'. Must match " + VALID_NAMESPACE_PATTERN.pattern());
        }
        if (!VALID_KEY_PATTERN.matcher(this.key).matches()) {
            throw new IllegalArgumentException("Invalid key: '" + key + "'. Must match " + VALID_KEY_PATTERN.pattern());
        }

        this.fullString = this.namespace + ":" + this.key;
    }

    @NotNull
    public static ContentKey of(@NotNull String namespace, @NotNull String key) {
        return new ContentKey(namespace, key);
    }

    @NotNull
    public static ContentKey of(@NotNull String rawKey) {
        Objects.requireNonNull(rawKey, "rawKey cannot be null");
        String trimmed = rawKey.trim();
        int colonIndex = trimmed.indexOf(':');
        if (colonIndex == -1) {
            return new ContentKey(DEFAULT_NAMESPACE, trimmed);
        }
        String ns = trimmed.substring(0, colonIndex);
        String k = trimmed.substring(colonIndex + 1);
        if (ns.isEmpty()) {
            ns = DEFAULT_NAMESPACE;
        }
        return new ContentKey(ns, k);
    }

    public static boolean isValid(@Nullable String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return false;
        }
        try {
            of(rawKey);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @NotNull
    public String getNamespace() {
        return namespace;
    }

    @NotNull
    public String getKey() {
        return key;
    }

    @NotNull
    public String asString() {
        return fullString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentKey that = (ContentKey) o;
        return fullString.equals(that.fullString);
    }

    @Override
    public int hashCode() {
        return fullString.hashCode();
    }

    @Override
    public String toString() {
        return fullString;
    }

    @Override
    public int compareTo(@NotNull ContentKey o) {
        return this.fullString.compareTo(o.fullString);
    }
}
