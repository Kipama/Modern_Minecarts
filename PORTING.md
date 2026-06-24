# Porting Modern Minecarts

The project separates stable gameplay rules from Minecraft-version adapters:

- `logic/` contains version-neutral calculations and tuning values.
- `util/TrainInventoryUtil` contains cycle-safe train traversal.
- `network/ChainLinkSync` contains the transport-neutral chain sync message.
- `mixin/`, screen registration, rendering, NBT, and packet transport are version adapters.

## Expected port workflow

1. Update Gradle, Loom, Minecraft, mappings, Loader, Fabric API, and Java.
2. Compile before changing gameplay logic.
3. Update registry and identifier construction.
4. Update packet transport while retaining `ChainLinkSync`.
5. Update NBT/entity serialization adapters.
6. Re-target mixins against the new vanilla minecart methods.
7. Update the chain renderer for the current rendering pipeline.
8. Migrate resource paths and item model definitions.
9. Validate screens, train persistence, multiplayer tracking, and every rail type in game.

## High-risk adapters

- `MinecartMixin`: overwrites vanilla rail movement and slowdown methods.
- `MinecartEntityRendererMixin`: depends directly on the entity render method and vertex API.
- `EntityTrackerEntryMixin`: depends on server entity-tracking internals.
- `SyncChainedMinecartPacket`: Fabric networking APIs changed after 1.20.1.
- Furnace inventory persistence: entity serialization signatures change between versions.

Do not duplicate tuning formulas in version branches. Add stable rules to `logic/` and keep
version branches focused on translating Minecraft state into those rules.
