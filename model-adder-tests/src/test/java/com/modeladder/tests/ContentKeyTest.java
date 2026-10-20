package com.modeladder.tests;

import com.modeladder.api.content.ContentKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ContentKey Namespaced Identity Tests")
class ContentKeyTest {

    @Test
    @DisplayName("Valid namespace:key parsed successfully")
    void testValidKeys() {
        ContentKey key = ContentKey.of("modeladder:ruby_sword");
        assertEquals("modeladder", key.getNamespace());
        assertEquals("ruby_sword", key.getKey());
        assertEquals("modeladder:ruby_sword", key.asString());

        ContentKey subPathKey = ContentKey.of("custom_pack:weapons/fire_sword");
        assertEquals("custom_pack", subPathKey.getNamespace());
        assertEquals("weapons/fire_sword", subPathKey.getKey());
    }

    @Test
    @DisplayName("Default namespace assigned if missing")
    void testDefaultNamespace() {
        ContentKey key = ContentKey.of("ruby_sword");
        assertEquals(ContentKey.DEFAULT_NAMESPACE, key.getNamespace());
        assertEquals("ruby_sword", key.getKey());
        assertEquals("modeladder:ruby_sword", key.asString());
    }

    @Test
    @DisplayName("Invalid characters or empty parts throw IllegalArgumentException")
    void testInvalidKeys() {
        assertThrows(IllegalArgumentException.class, () -> ContentKey.of("invalid!ns:key"));
        assertThrows(IllegalArgumentException.class, () -> ContentKey.of("space in ns:key"));
        assertThrows(IllegalArgumentException.class, () -> ContentKey.of("ns:invalid@char"));
        assertFalse(ContentKey.isValid("invalid namespace:key"));
        assertFalse(ContentKey.isValid(""));
        assertFalse(ContentKey.isValid(null));
    }

    @Test
    @DisplayName("Equality and hashCode consistency")
    void testEquality() {
        ContentKey key1 = ContentKey.of("modeladder:ruby_sword");
        ContentKey key2 = ContentKey.of("MODELADDER:ruby_sword");
        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }
}
