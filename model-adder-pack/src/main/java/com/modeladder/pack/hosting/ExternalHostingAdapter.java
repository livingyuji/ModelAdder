package com.modeladder.pack.hosting;

import com.modeladder.api.validation.ErrorCode;
import com.modeladder.api.validation.ValidationResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * Adapter for external resource pack hosting (e.g. self-hosted HTTPS server, CDN, or object store).
 */
public final class ExternalHostingAdapter {

    private final String externalUrl;
    private final String expectedSha1;

    public ExternalHostingAdapter(@NotNull String externalUrl, @Nullable String expectedSha1) {
        this.externalUrl = externalUrl;
        this.expectedSha1 = (expectedSha1 != null && !expectedSha1.isBlank()) ? expectedSha1.trim().toLowerCase() : null;
    }

    @NotNull
    public ValidationResult testReachability(int timeoutMs) {
        ValidationResult result = ValidationResult.ok();

        if (externalUrl.isBlank()) {
            result.addError(ErrorCode.PACK_HOST_UNREACHABLE, "External URL is not configured");
            return result;
        }

        try {
            URI uri = URI.create(externalUrl);
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                result.addError(ErrorCode.PACK_HOST_UNREACHABLE, "Invalid URL scheme (must be http or https): " + externalUrl);
                return result;
            }

            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setInstanceFollowRedirects(true);

            int responseCode = conn.getResponseCode();
            if (responseCode < 200 || responseCode >= 400) {
                result.addError(ErrorCode.PACK_HOST_UNREACHABLE,
                    "External host returned HTTP " + responseCode + " on HEAD check: " + externalUrl);
            }
        } catch (Exception e) {
            result.addError(ErrorCode.PACK_HOST_UNREACHABLE,
                "Failed connecting to external URL: " + e.getMessage());
        }

        return result;
    }

    @NotNull
    public String getExternalUrl() {
        return externalUrl;
    }

    @Nullable
    public String getExpectedSha1() {
        return expectedSha1;
    }
}
