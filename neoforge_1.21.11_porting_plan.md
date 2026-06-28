# NeoForge 1.21.11 Porting Plan

This branch now starts from the merged `neoforge/1.21.3-1.21.4` state, not from the older shared `1.21` / `1.21.1` baseline.

That matters because the earlier `1.21.1 -> 1.21.3/1.21.4` source/API migration work has already been carried over into this branch. The remaining job is the final hop from that working intermediate port to `1.21.11`, plus any later-series NeoGradle/runtime adjustments required by the newer NeoForge toolchain.

## Current branch baseline

- Minecraft: `1.21.3`
- Minecraft range: `[1.21.3,1.21.5)`
- NeoForge: `21.3.94`
- NeoForge range: `[21.3.94,21.4)`
- Parchment mappings: `2024.12.07`
- Mod version: `1.1.0-1.21.x`

## Target version

- Minecraft: `1.21.11`
- NeoForge: `21.11.42`
- Parchment mappings: `2025.12.20`

## Current status

- `gradle.properties` has been moved to the `1.21.11` / `21.11.42` toolchain.
- The branch now compiles successfully on NeoForge `1.21.11`.
- `./gradlew build` succeeds on this branch.
- The source migration work includes the late-line API changes hit during the port:
  - minecart package moves
  - attachment serialization updates
  - `Identifier` migration
  - GUI rendering/input updates
  - detector rail signature updates
  - chain rendering event updates
  - chunk ticket and furnace display state updates
- `runClient` has intentionally not been executed in this environment.

## Remaining work

1. Manual in-game validation on `1.21.11`.
2. Dedicated-server startup validation if you want parity checked beyond client/build success.
3. Any gameplay bug fixes discovered during hands-on testing.

## Official references used

- NeoForge 1.21.11 primer:
  - https://docs.neoforged.net/primer/docs/1.21.11/
- NeoForge 1.21.11 networking payload docs:
  - https://docs.neoforged.net/docs/1.21.11/networking/payload/
- NeoForge 1.21.11 attachments docs:
  - https://docs.neoforged.net/docs/1.21.11/datastorage/attachments/
- NeoForge artifact metadata:
  - https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml
- Parchment 1.21.11 metadata:
  - https://maven.parchmentmc.org/org/parchmentmc/data/parchment-1.21.11/maven-metadata.xml

## Practical meaning

The port no longer needs an intermediate implementation checkpoint.

What remains is a later `1.21.x` upgrade with two likely workstreams:

1. Build/runtime migration:
   - update the branch from the current `1.21.3` / NeoForge `21.3.94` setup to the `1.21.11` toolchain
   - resolve later-series NeoGradle run configuration changes such as the previously observed `The run type 'data' was not found` failure

2. Final source/API validation:
   - recompile against `1.21.11`
   - fix any mapping drift, signature drift, mixin target drift, resource validation breakage, or behavior regressions introduced between `1.21.3/1.21.4` and `1.21.11`

## Porting goals

1. Upgrade the mod from the merged `1.21.3/1.21.4` baseline to `1.21.11`.
2. Preserve the current NeoForge architecture documented in `forge2neoforge_summary.md`.
3. Reach a clean build, working data generation, and a feature-complete in-game validation pass on `1.21.11`.

## Work plan

### 1. Confirm and preserve the new starting point

- Treat the merged `neoforge/1.21.3-1.21.4` state as the canonical base for this branch.
- Avoid reintroducing pre-port code paths that were already removed during the `1.21.3/1.21.4` work.
- Keep the current architecture intact:
  - vanilla minecart entities
  - mixin-based behavior patches
  - NeoForge attachments for extra minecart state
  - helper-driven gameplay logic under `util`
  - event-driven interaction and ticking hooks

### 2. Move the build to 1.21.11

- Update `gradle.properties` to:
  - `minecraft_version=1.21.11`
  - a matching `minecraft_version_range`
  - `neo_version=21.11.42`
  - updated `neo_version_range`
  - updated parchment mapping coordinates
  - an updated mod version suffix if needed
- Recheck:
  - `build.gradle`
  - `settings.gradle`
  - `src/main/resources/META-INF/neoforge.mods.toml`
  - `src/main/resources/pack.mcmeta`

### 3. Update late-line NeoGradle / run configuration

- Rework the current `runs { ... }` setup if required by NeoForge / NeoGradle `21.11.x`.
- Confirm the final setup supports:
  - client runs
  - server runs
  - game test server runs if still applicable
  - data generation
- Specifically recheck the failure previously seen on later versions:
  - `The run type 'data' was not found`
 - Status:
   - build tasks now work on the `1.21.11` toolchain
   - manual run-profile validation still remains because `runClient` was not executed here

### 4. Rebuild and catalog 1.21.11 breakage

- Run a clean compile on the new target before making broad behavioral changes.
- Bucket failures into:
  - renamed or moved Minecraft classes, methods, or fields
  - NeoForge API changes
  - mixin target or descriptor drift
  - client menu/screen/renderer issues
  - resource, codec, recipe, or data-pack validation issues
 - Status:
   - completed for the current compile/build pass
   - remaining regressions, if any, are now expected to be behavioral rather than compile-time

### 5. Re-audit gameplay-critical code on 1.21.11

- Recheck:
  - `src/main/java/net/lordkipama/modernminecarts/event/ModEvents.java`
  - `src/main/java/net/lordkipama/modernminecarts/mixin/*`
  - `src/main/java/net/lordkipama/modernminecarts/util/*`
  - `src/main/java/net/lordkipama/modernminecarts/block/Custom/*`
  - `src/main/java/net/lordkipama/modernminecarts/inventory/*`
  - `src/main/java/net/lordkipama/modernminecarts/renderer/*`
- Prioritize behavior-sensitive systems:
  - minecart speed hooks
  - slope jump / air drag logic
  - train follower motion
  - chain linking and unlinking
  - furnace minecart GUI and fuel behavior
  - hopper/chest linked inventory behavior
  - powered detector rail logic

### 6. Re-audit networking and attachments

- Recheck:
  - `src/main/java/net/lordkipama/modernminecarts/Proxy/ModernMinecartsPacketHandler.java`
  - `src/main/java/net/lordkipama/modernminecarts/attachment/*`
- Validate that payload registration, codecs, and attachment serialization still match `1.21.11` expectations.

### 7. Run data generation and repair resource issues

- Run the data generator on `1.21.11`.
- Resolve any pack format, codec, recipe, loot, tag, item model, or data map failures.
- Reconfirm the powered rail recipe replacement flow still works.

### 8. Test the major features in-game

- Client startup
- Dedicated server startup
- Chain linking and unlinking
- Chain rendering
- Train follower movement
- Furnace minecart GUI and fuel behavior
- Hopper/chest linked inventory behavior
- Sloped rail jumping
- Rail crossing behavior
- Powered detector rail behavior
- Copper rail weathering, waxing, and scraping
 - Status:
   - still required
   - this is now the primary remaining milestone

### 9. Final cleanup

- Remove temporary debugging
- Update supported-version text in docs and metadata
- Record any known residual issues for the final `1.21.11` branch

## Expected first-pass touch points

- `gradle.properties`
- `build.gradle`
- `settings.gradle`
- `src/main/resources/META-INF/neoforge.mods.toml`
- `src/main/resources/pack.mcmeta`
- `src/main/java/net/lordkipama/modernminecarts/Proxy/ModernMinecartsPacketHandler.java`
- `src/main/java/net/lordkipama/modernminecarts/attachment/*`
- `src/main/java/net/lordkipama/modernminecarts/event/ModEvents.java`
- `src/main/java/net/lordkipama/modernminecarts/mixin/*`
- `src/main/java/net/lordkipama/modernminecarts/util/*`
- `src/main/java/net/lordkipama/modernminecarts/block/Custom/*`
- `src/main/java/net/lordkipama/modernminecarts/inventory/*`
- `src/main/java/net/lordkipama/modernminecarts/renderer/*`
- generated/data/resource files as required by compile and datagen results

## Stop point

This branch now has the later `1.21.3/1.21.4` work merged in, the build has been upgraded to `1.21.11` / `21.11.42`, and the mod reaches a successful `compileJava` and `build`.

Recommended next action:

1. Run manual in-game validation on the built `1.21.11` port.
2. Verify the major rail, chain, train, and furnace-minecart features.
3. Fix any runtime or gameplay regressions discovered during that testing.
