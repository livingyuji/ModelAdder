package com.modeladder.tests;

import com.modeladder.api.pack.PackManager.PackBuildResult;
import com.modeladder.pack.builder.PackBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Deterministic Pack Build Tests")
class PackBuilderDeterministicTest {

    @Test
    @DisplayName("Identical staging content produces identical deterministic SHA-1 hash")
    void testDeterministicHash(@TempDir Path tempDir) throws IOException, InterruptedException {
        Path staging = tempDir.resolve("staging");
        Path builds = tempDir.resolve("builds");
        Path backups = tempDir.resolve("backups");
        File published = tempDir.resolve("published").resolve("pack.zip").toFile();

        // Populate staging with sample assets
        Path modelPath = staging.resolve("assets").resolve("modeladder").resolve("models").resolve("item");
        Files.createDirectories(modelPath);
        Files.writeString(modelPath.resolve("ruby_sword.json"), "{\"parent\":\"item/generated\"}", StandardCharsets.UTF_8);

        PackBuilder builder1 = new PackBuilder(staging, builds, backups, published, 5);
        PackBuildResult res1 = builder1.build();
        assertTrue(res1.success(), "First build must succeed: " + res1.errorMessage());
        assertNotNull(res1.sha1());

        // Wait a brief moment to ensure wall-clock time changes
        Thread.sleep(100);

        PackBuilder builder2 = new PackBuilder(staging, builds, backups, published, 5);
        PackBuildResult res2 = builder2.build();
        assertTrue(res2.success(), "Second build must succeed: " + res2.errorMessage());

        // SHA-1 must be strictly identical due to normalized entry timestamps and sorted ordering
        assertEquals(res1.sha1(), res2.sha1(), "Pack builds must be 100% deterministic and reproducible");
        assertTrue(published.exists(), "Published pack file must exist");
    }
}
