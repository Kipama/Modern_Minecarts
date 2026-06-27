# NeoForge 1.21.1 Porting Plan

This branch starts from `neoforge/1.21`, which is already using the NeoForge-era runtime architecture described in `forge2neoforge_summary.md`.

That means this port is expected to be an in-place `1.21 -> 1.21.1` upgrade, not a second architecture rewrite. The main job is to move the build and source set to Minecraft `1.21.1`, align with a matching NeoForge `21.1.x` release, then fix any API, mapping, mixin, or data breakage revealed by compile and runtime testing.

## Official references used

- NeoForge 1.21.1 primer:
  - https://docs.neoforged.net/primer/docs/1.21.1/
- NeoForge 1.21.1 networking payload docs:
  - https://docs.neoforged.net/docs/1.21.1/networking/payload/
- NeoForge 1.21.1 attachments docs:
  - https://docs.neoforged.net/docs/1.21.1/datastorage/attachments/

## Porting goals

1. Upgrade the mod from Minecraft `1.21` / NeoForge `21.0.x` to Minecraft `1.21.1` / a compatible NeoForge `21.1.x` release.
2. Preserve the current 1.21 architecture:
   - vanilla minecart entities
   - mixin-based behavior patches
   - attachment-backed extra state
   - event-driven interaction and ticking
3. Reach a clean build, working data generation, and a basic in-game verification pass for all major mod features.

## Work plan

### 1. Upgrade build and metadata

- Update `gradle.properties`:
  - `minecraft_version=1.21.1`
  - `minecraft_version_range` to a `1.21.1`-appropriate range
  - `neo_version` to a matching NeoForge `21.1.x` build
  - parchment version if required for the chosen toolchain
  - mod version suffix from `1.21` to `1.21.1`
- Verify `build.gradle` still matches the recommended NeoGradle / NeoForge setup for the chosen `21.1.x` version.
- Reconfirm `settings.gradle`, wrapper compatibility, and Java 21 requirements.
- Check `src/main/resources/META-INF/neoforge.mods.toml` and `pack.mcmeta` placeholders after the version bump.

### 2. Rebuild and catalog source breakage

- Run a clean compile on the new target and record all failures before changing behavior.
- Separate failures into buckets:
  - renamed or moved Minecraft classes/methods/fields
  - NeoForge API changes
  - mixin target/method signature drift
  - data/resource validation errors
  - client rendering or menu registration issues

### 3. Fix bootstrap, registration, and metadata issues

- Validate the mod entrypoint in `src/main/java/net/lordkipama/modernminecarts/ModernMinecarts.java`.
- Recheck deferred registration usage in:
  - `src/main/java/net/lordkipama/modernminecarts/block/ModBlocks.java`
  - `src/main/java/net/lordkipama/modernminecarts/Item/ModItems.java`
  - `src/main/java/net/lordkipama/modernminecarts/entity/ModEntities.java`
  - `src/main/java/net/lordkipama/modernminecarts/inventory/ModMenus.java`
- Confirm client-only menu screen registration still matches NeoForge 1.21.1 expectations.

### 4. Verify attachments against 1.21.1

- Recheck attachment registration and serialization in:
  - `src/main/java/net/lordkipama/modernminecarts/attachment/MinecartAttachmentTypes.java`
  - `src/main/java/net/lordkipama/modernminecarts/attachment/ChainMinecartData.java`
  - `src/main/java/net/lordkipama/modernminecarts/attachment/FurnaceMinecartData.java`
- Confirm entity attachment access patterns still compile and serialize correctly on `1.21.1`.
- Verify no attachment builder or registry key changes are required.

### 5. Verify networking and packet registration

- Recheck `src/main/java/net/lordkipama/modernminecarts/Proxy/ModernMinecartsPacketHandler.java` against NeoForge 1.21.1 payload registration rules.
- Confirm these pieces still line up with the docs and compile target:
  - `RegisterPayloadHandlersEvent`
  - `PayloadRegistrar`
  - `CustomPacketPayload`
  - `StreamCodec`
  - main-thread handling strategy
- Test that chain-link sync still updates parent/child ids client-side.

### 6. Repair mixins against 1.21.1 mappings

- Validate `src/main/resources/modernminecarts.mixins.json`.
- Re-audit every injected target in:
  - `src/main/java/net/lordkipama/modernminecarts/mixin/AbstractMinecartMixin.java`
  - `src/main/java/net/lordkipama/modernminecarts/mixin/AbstractMinecartContainerMixin.java`
  - `src/main/java/net/lordkipama/modernminecarts/mixin/MinecartChestMixin.java`
  - `src/main/java/net/lordkipama/modernminecarts/mixin/MinecartHopperMixin.java`
- Compare failures against transformed 1.21.1 vanilla sources if method names, descriptors, or control flow changed.
- Prioritize logic-sensitive mixins first:
  - rail speed hooks
  - jump/air-drag behavior
  - chest/hopper crouch interaction suppression
  - hopper linked-inventory ticking

### 7. Recheck gameplay helpers and event hooks

- Validate global event handlers in `src/main/java/net/lordkipama/modernminecarts/event/ModEvents.java`.
- Reconfirm compatibility for:
  - right-click item/entity interaction events
  - entity tick events
  - entity leave level cleanup
  - reload listener and recipe replacement flow
- Recheck helper classes for mapping drift and vanilla behavior changes:
  - `src/main/java/net/lordkipama/modernminecarts/util/MinecartLinkHelper.java`
  - `src/main/java/net/lordkipama/modernminecarts/util/FurnaceMinecartHelper.java`
  - `src/main/java/net/lordkipama/modernminecarts/util/HopperMinecartHelper.java`
  - `src/main/java/net/lordkipama/modernminecarts/util/AdvancementHelper.java`

### 8. Recheck blocks, menus, rendering, and resources

- Rebuild all custom rail block classes against 1.21.1 block and rail APIs.
- Verify the furnace minecart menu/screen path:
  - container creation
  - slot logic
  - data access
  - screen registration
- Validate the chain renderer in `src/main/java/net/lordkipama/modernminecarts/renderer/MinecartChainRenderer.java` against any client rendering API drift.
- Validate resources and data:
  - blockstates
  - item/block models
  - lang
  - loot tables
  - recipes
  - tags
  - NeoForge data maps

### 9. Run data generation and fix generated/resource issues

- Run the data generator on `1.21.1`.
- Resolve any pack format, codec, recipe, loot, tag, or data map errors.
- Verify the custom powered rail recipe replacement still loads and overrides correctly.

### 10. Test the major features in-game

- Smoke-test startup on client and dedicated server.
- Validate:
  - chain linking and unlinking
  - chain rendering
  - train follower movement
  - furnace minecart GUI and fuel handling
  - furnace-powered speed and chunkloading behavior
  - hopper/chest linked inventory behavior
  - sloped rail launch behavior
  - rail crossing behavior
  - powered detector rail sensing and acceleration/braking logic
  - copper rail weathering, waxing, and scraping

### 11. Final cleanup

- Remove temporary logging or debug code added during the port.
- Update `README.md` and any version text if the port changes supported versions.
- Run a final build and record any known residual issues before merge.

## Expected first-pass touch points

- `gradle.properties`
- `build.gradle`
- `src/main/resources/META-INF/neoforge.mods.toml`
- `src/main/java/net/lordkipama/modernminecarts/ModernMinecarts.java`
- `src/main/java/net/lordkipama/modernminecarts/Proxy/ModernMinecartsPacketHandler.java`
- `src/main/java/net/lordkipama/modernminecarts/attachment/*`
- `src/main/java/net/lordkipama/modernminecarts/event/ModEvents.java`
- `src/main/java/net/lordkipama/modernminecarts/mixin/*`
- `src/main/java/net/lordkipama/modernminecarts/util/*`
- rail block, menu, renderer, and data/resource files as needed by compile/runtime results

## Stop point

This branch and plan are ready for the next step. The next action should be to start with step 1, pick the exact NeoForge `21.1.x` target version, and run the first compile pass on Minecraft `1.21.1`.
