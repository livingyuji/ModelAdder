# Model Adder — Resource Pack Engine & Hosting Guide

The Model Adder resource pack engine is an automated, deterministic pipeline designed specifically for Minecraft Java Edition **1.21.11**.

---

## Architecture & Directory Layout

```
plugins/ModelAdder/resourcepack/
├── imports/     # Drop ZIP files here to import via /ma pack import <file.zip>
├── assets/      # Source unpacked assets (textures, models, sounds, fonts)
├── staging/     # Active pack staging tree before compilation
├── published/   # Holds current pack.zip served to players
├── builds/      # Stored builds (build-YYYYMMDD-HHmmss.zip)
└── backups/     # Retained historical builds for rollback
```

---

## 1. Hosting Modes

### Mode A: Built-in HTTP Hosting (Default)
Model Adder embeds a lightweight HTTP daemon using standard Java 21 networking (`jdk.httpserver`).
- **Path Restriction:** Only `/pack.zip` is accessible. All other routes return HTTP 404.
- **Port:** Configured in `config.yml` (default `8085`).
- **Public URL:** The reachable URL players' clients connect to (e.g. `http://your-server-ip:8085/pack.zip` or reverse proxy HTTPS URL).

```yaml
resource-pack:
  hosting:
    mode: built-in
    built-in:
      enabled: true
      bind-address: "0.0.0.0"
      port: 8085
      public-url: "http://your-server-ip:8085/pack.zip"
```

### Mode B: External Hosting
If using an external CDN or web server:

```yaml
resource-pack:
  hosting:
    mode: external
    external:
      url: "https://cdn.example.com/pack.zip"
      expected-sha1: "da39a3ee5e6b4b0d3255bfef95601890afd80709"
```

---

## 2. Deterministic Packaging & SHA-1

To ensure reproducible builds:
- Entry paths are sorted lexicographically before writing to the ZIP output stream.
- File modification timestamps are normalized to epoch `0L`.
- The SHA-1 hash is computed using Java's `MessageDigest` and sent to the client via `player.setResourcePack(url, hashBytes)` for caching and integrity verification.

---

## 3. Backups & Rollback

- Every successful build is backed up into `resourcepack/backups/`.
- If an admin introduces broken assets, they can view backups with `/ma pack list` and instantly restore any previous build using:
  ```
  /ma pack rollback <build-id>
  ```
- Rollback is performed atomically without restarting the server.
