# Fabric 26.1 Porting Plan

## Goal

Port the Fabric version of Modern Minecarts from `fabric/1.21.11` to a new `fabric/26.1` branch while preserving feature parity with the current Fabric 1.21.11 branch.

## Research Summary

### Target version alignment

- Mojang's official version manifest lists `26.1` as a release on `2026-03-24`.
- The same manifest also lists follow-up releases `26.1.1` on `2026-04-01` and `26.1.2` on `2026-04-09`.
- This repository's matching NeoForge branch is already named `neoforge/26.1` and targets base Minecraft `26.1`, so the Fabric branch should start by targeting `26.1` as well.

### Fabric ecosystem status

- Fabric API has dedicated builds for the `+26.1` line, so this Minecraft version is supported on Fabric.
- Fabric API also has separate follow-up lines for `+26.1.1` and `+26.1.2`, which means patch-level splits matter and should not be mixed accidentally.
- Fabric Loader metadata currently lists `0.19.3` as the newest release.
- Fabric Loom docs for the current generation describe `26.1+` as a non-obfuscated Minecraft line that uses the `net.fabricmc.fabric-loom` plugin id instead of the older remapping-oriented setup.

### Practical porting implications

- The existing `fabric/1.21.11` branch uses:
  - Minecraft `1.21.11`
  - Yarn `1.21.11+build.6`
  - Fabric Loader `0.19.3`
  - Fabric API `0.141.4+1.21.11`
  - Loom `1.14.10`
- For `26.1`, the version pins in `gradle.properties`, `build.gradle`, and `fabric.mod.json` will need to be updated together.
- The matching NeoForge `26.1` branch already moved its toolchain to Java 25, so the Fabric branch should be checked for the same requirement during the build update step.
- Fabric's live Yarn metadata does not publish any `26.x` mappings at all, and Mojang's `26.1` / `26.1.1` manifests do not publish `client_mappings` or `server_mappings` downloads either.
- That is consistent with Fabric's current Loom docs: `26.1+` should be treated as a non-obfuscated line, so the port should not depend on a Yarn or Mojang mappings coordinate in the old way.

## Porting Plan

### 1. Update build and version metadata

- Switch Fabric build files from `1.21.11` to `26.1`.
- Update:
  - `gradle.properties`
  - `build.gradle`
  - `fabric.mod.json`
- Pin explicit `26.1`-compatible versions for:
  - Minecraft
  - Fabric Loader
  - Fabric API
  - Fabric Loom
- Switch to the non-obfuscated Loom setup for `26.1+`:
  - use `net.fabricmc.fabric-loom`
  - remove the explicit mappings dependency
  - raise the Java toolchain to `25`

### 2. Restore a compiling baseline

- Run `compileJava` after the dependency/version bump.
- Fix mapping and API breakages caused by the `1.21.11 -> 26.1` jump.
- Expect likely failures in:
  - mixin targets
  - recipe serializers
  - resource condition code
  - client GUI classes
  - minecart behavior hooks

### 3. Reconcile data/resource format changes

- Check all recipe JSONs, advancements, tags, blockstates, and models against the `26.1` data format.
- Pay special attention to the areas that already changed between recent versions:
  - recipe ingredient syntax
  - recipe book advancement paths and triggers
  - conditional resource loading
  - feature-gated recipe availability

### 4. Re-port Fabric-specific gameplay features

- Ensure the following behavior survives the version jump:
  - recipe book unlocks after obtaining regular rails
  - configurable recipe yields
  - creative tab filtering for disabled features
  - feature-based recipe disabling
  - config screen and config persistence
  - rail jump placement behavior
  - rail jump neighbor updates
  - rail jump movement behavior
  - furnace minecart chunkloading

### 5. Validation pass

- Validate with `compileJava` first.
- Then validate with `runClient`.
- In-game smoke test checklist:
  - config screen opens
  - config names/toggles render correctly
  - speed configs apply
  - recipe yield configs apply
  - disabled features are hidden from creative inventory
  - disabled recipes are unavailable
  - obtaining vanilla rails unlocks modded rail recipes
  - rail jump crafts and behaves correctly
  - furnace minecart chunkloading still works

## Known Risks

- `26.1` is a larger jump than a normal patch update, so mixin targets and mapped method names are likely to move.
- The data pack/recipe parser may be stricter again, which can break crafting and recipe book unlocks even when code compiles.
- If the Fabric `26.1` base release is materially worse to support than `26.1.2`, we may need to explicitly decide whether the branch should stay on base `26.1` for parity with NeoForge or move to a later `26.1.x` patch.

## Current Status

- The original mappings blocker is now understood:
  - Yarn `26.x` mappings are not published on Fabric Maven.
  - Mojang `26.1` / `26.1.1` manifests do not expose official mapping downloads.
  - Fabric's own Loom docs indicate this is expected because `26.1+` is treated as a non-obfuscated line.
- After switching the branch toward the documented `26.1+` setup, `compileJava` now gets past the old mappings/tooling failure and reaches Java source compilation.
- The branch is therefore no longer blocked on mappings; the next blocker is source migration from the old obfuscated/Yarn namespace to the new `26.1` named API surface.

## Research Sources

- Mojang version manifest:
  - `https://piston-meta.mojang.com/mc/game/version_manifest_v2.json`
- Fabric Yarn metadata:
  - `https://maven.fabricmc.net/net/fabricmc/yarn/maven-metadata.xml`
- Fabric Loader metadata:
  - `https://maven.fabricmc.net/net/fabricmc/fabric-loader/maven-metadata.xml`
- Fabric API metadata:
  - `https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml`
- Fabric Loom metadata:
  - `https://maven.fabricmc.net/net/fabricmc/fabric-loom/maven-metadata.xml`
