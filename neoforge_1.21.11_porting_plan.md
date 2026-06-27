# NeoForge 1.21.11 Porting Plan

This branch starts from the current shared `1.21` / `1.21.1` codebase on `neoforge/1.21.1`.

That codebase is already using the modern NeoForge-era runtime architecture described in `forge2neoforge_summary.md`:

- vanilla minecart entities
- mixin-based minecart behavior patches
- NeoForge attachments for extra minecart state
- helper-driven gameplay logic under `util`
- event-driven interaction and ticking hooks

So this is not a Forge-to-NeoForge rewrite. It is a later `1.21.x` migration across a version range where both gameplay APIs and build/runtime setup have changed.

## Target version

- Minecraft: `1.21.11`
- NeoForge: `21.11.42`
- Parchment mappings: `2025.12.20`

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

## Research findings

### Current branch compatibility checkpoints

From the current shared `1.21` / `1.21.1` source:

- `1.21` compiles and runs.
- `1.21.1` compiles and runs.
- `1.21.3` no longer compiles due to significant source/API breakage.
- `1.21.8`, `1.21.10`, and `1.21.11` fail even earlier during Gradle/NeoGradle run configuration because the current `runs { data { ... } }` setup is no longer accepted as-is.

### Practical meaning

There are at least two separate migration layers between `1.21.1` and `1.21.11`:

1. Source/API migration:
   - starts by `1.21.3`
   - affects block interaction APIs, rail helpers, menu/screen classes, recipe access, minecart methods, and several helper/mixin touchpoints

2. Build/runtime migration:
   - appears by the later `1.21.x` line
   - the current NeoGradle run configuration fails with:
     - `The run type 'data' was not found`

## Recommendation: direct vs iterative

### Recommended approach

Port iteratively, but not patch-by-patch.

Best path:

1. First port to the earliest breaking post-`1.21.1` checkpoint:
   - recommended intermediate target: `1.21.3`
2. Once the source compiles and gameplay behavior is restored there, do a second hop to:
   - `1.21.11`

### Why this makes more sense than a direct jump

- A direct `1.21.1 -> 1.21.11` port mixes two different categories of work:
  - gameplay/source/API rewrites
  - build and NeoGradle run-configuration rewrites
- `1.21.3` already exposes the broad source breakage without the extra late-series run-configuration failure.
- Fixing the source/API layer first should make the later `1.21.11` hop much easier to reason about.
- Porting through every intermediate patch does **not** look worthwhile.

### Suggested stepping-stone policy

- Do **not** port every version from `1.21.3` through `1.21.11`.
- Use one intermediate code/API checkpoint, then jump to the final target.
- Default intermediate checkpoint: `1.21.3`
- Optional second checkpoint only if needed during implementation:
  - a late-line checkpoint such as `1.21.8` or `1.21.10` to isolate NeoGradle/run-config updates before the final `1.21.11` jump

## Porting goals

1. Upgrade the mod from the current shared `1.21` / `1.21.1` state to `1.21.11`.
2. Preserve the current architecture documented in `forge2neoforge_summary.md`.
3. Reach a clean build, working data generation, and a feature-complete in-game validation pass on `1.21.11`.

## Work plan

### 1. Create an intermediate migration checkpoint

- Use `1.21.3` as the first implementation target.
- Override the build to:
  - Minecraft `1.21.3`
  - NeoForge `21.3.94`
  - matching parchment mappings
- Capture and categorize the source-level failures before making behavior changes.

### 2. Repair early 1.21.x source/API breakage

- Expect changes in these areas first:
  - block item interaction return types and hooks
  - rail shape helpers and ascending checks
  - comparator/query methods on minecarts
  - neighbor update signatures
  - recipe holder / recipe manager APIs
  - furnace fuel checks
  - menu, recipe-book, and screen APIs
  - mixin targets for changed minecart methods
- Primary touch areas likely include:
  - `src/main/java/net/lordkipama/modernminecarts/block/Custom/*`
  - `src/main/java/net/lordkipama/modernminecarts/inventory/*`
  - `src/main/java/net/lordkipama/modernminecarts/event/ModEvents.java`
  - `src/main/java/net/lordkipama/modernminecarts/mixin/AbstractMinecartMixin.java`
  - `src/main/java/net/lordkipama/modernminecarts/util/FurnaceMinecartHelper.java`

### 3. Revalidate gameplay architecture at the intermediate checkpoint

- Confirm the current architectural decisions still hold:
  - keep vanilla minecart entities
  - keep attachment-backed custom state
  - keep packet-based client link sync
  - keep event-driven runtime logic
- Do not reintroduce deleted replacement entity/item layers from the old Forge branch.

### 4. Move the build to 1.21.11

- Update `gradle.properties` to:
  - `minecraft_version=1.21.11`
  - a matching `minecraft_version_range`
  - `neo_version=21.11.42`
  - updated parchment mapping coordinates
  - updated mod version suffix
- Recheck:
  - `build.gradle`
  - `settings.gradle`
  - `src/main/resources/META-INF/neoforge.mods.toml`
  - `pack.mcmeta`

### 5. Update late-line NeoGradle / run configuration

- Fix the later `1.21.x` build/runtime issue where the current configuration fails with:
  - `The run type 'data' was not found`
- Recheck the `runs { ... }` block in `build.gradle`.
- Confirm current NeoGradle expectations for:
  - client
  - server
  - game test server
  - data generation
- Ensure the final setup supports both:
  - `compileJava`
  - `runData`

### 6. Re-audit networking and attachments on 1.21.11

- Recheck:
  - `src/main/java/net/lordkipama/modernminecarts/Proxy/ModernMinecartsPacketHandler.java`
  - `src/main/java/net/lordkipama/modernminecarts/attachment/*`
- Validate that packet registration, codecs, and attachment serialization still match `1.21.11` expectations.

### 7. Re-audit mixins against 1.21.11 mappings

- Validate:
  - `src/main/resources/modernminecarts.mixins.json`
  - `src/main/java/net/lordkipama/modernminecarts/mixin/*`
- Compare target method names and descriptors against transformed `1.21.11` sources if needed.
- Prioritize:
  - minecart speed hooks
  - air drag / jump logic
  - chest/hopper crouch interaction suppression
  - hopper linked-inventory transfer timing

### 8. Recheck helpers, blocks, menus, and rendering

- Rebuild and validate:
  - all custom rail block classes
  - furnace minecart menu and screen flow
  - chain rendering
  - helper classes under `util`
- Pay special attention to any late-series client API drift in the screen and renderer code.

### 9. Run data generation and repair data/resource issues

- Run the data generator on `1.21.11`.
- Resolve any pack format, codec, recipe, loot, tag, or data map failures.
- Reconfirm the powered rail recipe replacement flow still works.

### 10. Test the major features in-game

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

### 11. Final cleanup

- Remove temporary debugging
- Update supported-version text in docs and metadata
- Record any known residual issues for the final `1.21.11` branch

## Expected first-pass touch points

- `gradle.properties`
- `build.gradle`
- `src/main/resources/META-INF/neoforge.mods.toml`
- `src/main/resources/pack.mcmeta`
- `src/main/java/net/lordkipama/modernminecarts/block/Custom/*`
- `src/main/java/net/lordkipama/modernminecarts/inventory/*`
- `src/main/java/net/lordkipama/modernminecarts/event/ModEvents.java`
- `src/main/java/net/lordkipama/modernminecarts/mixin/*`
- `src/main/java/net/lordkipama/modernminecarts/util/*`
- `src/main/java/net/lordkipama/modernminecarts/Proxy/ModernMinecartsPacketHandler.java`
- `src/main/java/net/lordkipama/modernminecarts/attachment/*`
- renderer and generated/data files as required by the later compile and datagen passes

## Stop point

This branch and plan are ready for the next step.

Recommended next action:

1. Start implementation on the intermediate `1.21.3` checkpoint.
2. Fix the source/API layer there first.
3. Only then move the branch forward to `1.21.11`.
