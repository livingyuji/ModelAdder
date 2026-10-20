package com.modeladder.tests;

import com.modeladder.api.validation.ErrorCode;
import com.modeladder.api.validation.ValidationError;
import com.modeladder.api.validation.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationResult Error Aggregation Tests")
class ValidationResultTest {

    @Test
    @DisplayName("Empty result is valid")
    void testOkResult() {
        ValidationResult result = ValidationResult.ok();
        assertTrue(result.isValid());
        assertFalse(result.hasWarnings());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Adding errors invalidates result and records metadata")
    void testErrorAddition() {
        ValidationResult result = ValidationResult.ok();
        result.addError(ErrorCode.JSON_PARSE_ERROR, "Unexpected token", "assets/model.json", "elements");
        result.addWarning("Non-fatal deprecation warning");

        assertFalse(result.isValid());
        assertTrue(result.hasWarnings());
        assertEquals(1, result.getErrors().size());
        assertEquals(1, result.getWarnings().size());

        ValidationError error = result.getErrors().get(0);
        assertEquals(ErrorCode.JSON_PARSE_ERROR, error.code());
        assertEquals("assets/model.json", error.sourcePath());
        assertEquals("elements", error.field());
    }

    @Test
    @DisplayName("Merge combines errors and warnings from multiple results")
    void testMerge() {
        ValidationResult res1 = ValidationResult.ofError(ErrorCode.PACK_INVALID_ZIP, "Bad zip");
        ValidationResult res2 = ValidationResult.ok();
        res2.addWarning("Warning 1");

        res1.merge(res2);
        assertFalse(res1.isValid());
        assertEquals(1, res1.getErrors().size());
        assertEquals(1, res1.getWarnings().size());
    }
}
