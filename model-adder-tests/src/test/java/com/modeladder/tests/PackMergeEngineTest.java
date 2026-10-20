package com.modeladder.tests;

import com.modeladder.api.validation.ErrorCode;
import com.modeladder.api.validation.ValidationResult;
import com.modeladder.pack.merge.PackMergeEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PackMergeEngine Conflict Policy Tests")
class PackMergeEngineTest {

    @Test
    @DisplayName("FAIL_ON_CONFLICT reports error when duplicate path exists")
    void testFailOnConflict(@TempDir Path tempDir) throws IOException {
        Path src = tempDir.resolve("src");
        Path dest = tempDir.resolve("dest");
        Files.createDirectories(src);
        Files.createDirectories(dest);

        Files.writeString(src.resolve("item.json"), "{\"version\": 1}");
        Files.writeString(dest.resolve("item.json"), "{\"version\": 2}");

        PackMergeEngine engine = new PackMergeEngine(PackMergeEngine.ConflictPolicy.FAIL_ON_CONFLICT);
        ValidationResult result = engine.merge(src, dest);

        assertFalse(result.isValid(), "Merge with conflicting paths must fail under FAIL_ON_CONFLICT policy");
        assertTrue(result.getErrors().stream().anyMatch(e -> e.code() == ErrorCode.ASSET_CONFLICT));
    }

    @Test
    @DisplayName("OVERWRITE replaces destination file cleanly")
    void testOverwritePolicy(@TempDir Path tempDir) throws IOException {
        Path src = tempDir.resolve("src");
        Path dest = tempDir.resolve("dest");
        Files.createDirectories(src);
        Files.createDirectories(dest);

        Files.writeString(src.resolve("item.json"), "{\"version\": 2}");
        Files.writeString(dest.resolve("item.json"), "{\"version\": 1}");

        PackMergeEngine engine = new PackMergeEngine(PackMergeEngine.ConflictPolicy.OVERWRITE);
        ValidationResult result = engine.merge(src, dest);

        assertTrue(result.isValid());
        assertEquals("{\"version\": 2}", Files.readString(dest.resolve("item.json")));
        assertEquals(1, engine.getDetectedConflicts().size());
    }
}
