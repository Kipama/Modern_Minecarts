# Forge 1.20.1 -> NeoForge 1.21 Port Summary

This file documents how the mod is structured on NeoForge 1.21 after the legacy 1.20.1 replacement-minecart layer was removed from this branch.

Use this as the reference when porting future Forge 1.20.1 changes into NeoForge 1.21.

## Core porting decision

The 1.20.1 version implemented much of the mod through custom minecart entity and item replacements in the `minecraft` namespace.

The NeoForge 1.21 port does **not** do that anymore.

Current 1.21 approach:

- Keep vanilla 1.21 minecart entities active at runtime.
- Add mod behavior through mixins into vanilla minecarts.
- Store extra state in NeoForge attachments.
- Put complex behavior into helpers under `util`.
- Use NeoForge events for interactions and lifecycle hooks.
- Keep the old implementations only on the `1.20.1` branch, not in this branch.

Practical rule:

- When porting an old change, do **not** recreate the old custom entity/item layer here.
- Port the behavior into the shared 1.21 runtime architecture instead.

## Main entry points

- Mod bootstrap:
  - [ModernMinecarts.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/ModernMinecarts.java)
- Active mixin config:
  - [modernminecarts.mixins.json](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/resources/modernminecarts.mixins.json)
- Global event hooks:
  - [ModEvents.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/event/ModEvents.java)

## Current NeoForge 1.21 architecture

### Registration and bootstrap

- Blocks:
  - [ModBlocks.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/block/ModBlocks.java)
- Items:
  - [ModItems.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/Item/ModItems.java)
- Entity attachment types:
  - [MinecartAttachmentTypes.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/attachment/MinecartAttachmentTypes.java)
- Menus/screens:
  - [ModMenus.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/inventory/ModMenus.java)
  - [FurnaceMinecartMenu.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/inventory/FurnaceMinecartMenu.java)
  - [FurnaceMinecartScreen.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/inventory/FurnaceMinecartScreen.java)
- Client/server link sync packet:
  - [ModernMinecartsPacketHandler.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/Proxy/ModernMinecartsPacketHandler.java)

### Active mixins

- [AbstractMinecartMixin.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/mixin/AbstractMinecartMixin.java)
  - Furnace minecart speed cap override
  - Powered detector rail motion behavior
  - Sloped rail jump behavior
  - Restored air drag and lateral air speed for minecart jumping
- [AbstractMinecartContainerMixin.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/mixin/AbstractMinecartContainerMixin.java)
  - Prevent crouch-right-click from opening chest/hopper minecart inventories
- [MinecartChestMixin.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/mixin/MinecartChestMixin.java)
  - Additional chest crouch-interaction guard
- [MinecartHopperMixin.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/mixin/MinecartHopperMixin.java)
  - Hopper minecart linked-inventory tick hook

### Attachments

The old custom minecart fields were replaced by NeoForge attachments.

- Chain link state:
  - [ChainMinecartData.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/attachment/ChainMinecartData.java)
- Furnace minecart extra state:
  - [FurnaceMinecartData.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/attachment/FurnaceMinecartData.java)

### Global event hooks

- [ModEvents.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/event/ModEvents.java)

This is now the home for:

- Chain item air-right-click reset
- Chain-based cart coupling interaction
- Per-tick train follower updates
- Unlink/drop cleanup when minecarts leave the level
- Furnace minecart ticking
- Runtime powered rail recipe replacement

## Feature map

### Chain-linked trains

Runtime home:

- [MinecartLinkHelper.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/util/MinecartLinkHelper.java)
- [ModEvents.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/event/ModEvents.java)
- [ModernMinecartsPacketHandler.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/Proxy/ModernMinecartsPacketHandler.java)
- [MinecartChainRenderer.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/renderer/MinecartChainRenderer.java)

Ported responsibilities:

- Parent/child train linking
- Circular-train prevention
- Client sync of linked cart ids
- Follower motion
- Chain drop on unlink/removal
- World-space chain rendering

### Furnace minecart inventory, speed, and chunkloading

Runtime home:

- [FurnaceMinecartHelper.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/util/FurnaceMinecartHelper.java)
- [AbstractMinecartMixin.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/mixin/AbstractMinecartMixin.java)
- [ModEvents.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/event/ModEvents.java)
- [FurnaceMinecartContainer.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/inventory/FurnaceMinecartContainer.java)
- [FurnaceMinecartDataAccess.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/inventory/FurnaceMinecartDataAccess.java)

Ported responsibilities:

- Furnace minecart GUI
- Fuel slot state
- Pulling matching fuel from linked inventories
- Train speed calculations
- Rail speed cap override
- Chunkloading while fueled

### Hopper and chest minecart linked inventory behavior

Runtime home:

- [HopperMinecartHelper.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/util/HopperMinecartHelper.java)
- [MinecartHopperMixin.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/mixin/MinecartHopperMixin.java)

Ported responsibilities:

- Hopper slots 1-4 push full stacks into linked chest minecarts
- Hopper slot 5 pulls a full reserve stack from linked chest minecarts

### Crouch interaction suppression on chest/hopper minecarts

Runtime home:

- [AbstractMinecartContainerMixin.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/mixin/AbstractMinecartContainerMixin.java)
- [MinecartChestMixin.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/mixin/MinecartChestMixin.java)

Ported responsibilities:

- Prevent inventory opening when crouch-right-clicking chest or hopper minecarts
- Preserve shift-right-click train chaining behavior

### Copper rails, weathering, waxing, and scraping

Runtime home:

- [CopperRailBlock.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/block/Custom/CopperRailBlock.java)
- [WaxedCopperRailBlock.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/block/Custom/WaxedCopperRailBlock.java)
- [WeatheringRailBlock.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/block/Custom/WeatheringRailBlock.java)
- [AdvancementHelper.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/util/AdvancementHelper.java)

Ported responsibilities:

- Weather progression
- Wax-on interaction
- Wax-off interaction
- Direct awarding of the vanilla `wax_on` / `wax_off` advancement criteria
- Rail speed by copper weather stage

### Sloped rail

Runtime home:

- [SlopedRailBlock.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/block/Custom/SlopedRailBlock.java)
- [AbstractMinecartMixin.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/mixin/AbstractMinecartMixin.java)

Ported responsibilities:

- Placement-direction slope selection
- Locked slope state via `CONST_SHAPE`
- Prevention of fallback into ordinary flat rail states
- Jump launch behavior off the ramp
- Restored 1.20.1-style air drag/jump distance support

### Rail crossing

Runtime home:

- [RailCrossingBlock.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/block/Custom/RailCrossingBlock.java)

Ported responsibilities:

- Crossing orientation derived from minecart motion
- Flat crossing behavior
- Speed cap based on configured copper speed

### Powered detector rail

Runtime home:

- [PoweredDetectorRailBlock.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/block/Custom/PoweredDetectorRailBlock.java)
- [AbstractMinecartMixin.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/mixin/AbstractMinecartMixin.java)

Ported responsibilities:

- Detector-style cart sensing
- Comparator output from command block and container minecarts
- Weight inversion / direction inversion logic
- Minecart-side powered detector acceleration and braking behavior

Important note:

- This feature is split between block-side logic and minecart-side motion logic.
- If it breaks, inspect both files above.

### Command block, spawner, and TNT minecarts

Runtime home:

- No custom replacements exist in this branch anymore.
- These minecart types now rely on vanilla 1.21 classes plus the shared hooks used by all minecarts:
  - [AbstractMinecartMixin.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/mixin/AbstractMinecartMixin.java)
  - [MinecartLinkHelper.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/util/MinecartLinkHelper.java)
  - [ModEvents.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/event/ModEvents.java)
  - [MinecartChainRenderer.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/renderer/MinecartChainRenderer.java)

Porting guidance:

- If a future 1.20.1 fix for one of these minecart types needs to be ported, compare against the old 1.20.1 branch implementation and then re-home that behavior into the current shared runtime structure.

## What was removed from this branch

The following old layers were intentionally deleted from this NeoForge branch:

- Custom minecart replacement entities
- Custom minecart replacement items
- Vanilla-namespace override registries
- The old custom minecart renderer
- Old proxy scaffolding used by the Forge-era architecture

Why:

- They were not part of the active 1.21 runtime anymore.
- Keeping them in-tree made it easier to accidentally patch dead code instead of the live 1.21 path.

Where to find them now:

- Check the Forge `1.20.1` branch when you need historical implementation details.

## Resource and data layout

Resources:

- [src/main/resources/assets/modernminecarts](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/resources/assets/modernminecarts)

Data:

- [src/main/resources/data/modernminecarts](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/resources/data/modernminecarts)

Special case:

- The vanilla powered rail recipe override is supplied from:
  - [src/main/resources/data/minecraft/recipe/powered_rail.json](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/resources/data/minecraft/recipe/powered_rail.json)
- It is injected into the live recipe manager by [ModEvents.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/event/ModEvents.java)

## Recommended workflow for future backports

When a 1.20.1 change still needs to be ported:

1. Find the old behavior on the Forge `1.20.1` branch.
2. Decide whether the new 1.21 home should be a mixin, helper, attachment, event hook, or block class.
3. Port the behavior into the current active architecture rather than restoring deleted replacement classes.
4. Verify the behavior against vanilla 1.21 sources under `build/neoForm/.../transformed` if method names or flow changed.

## Short index

- Bootstrap: [ModernMinecarts.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/ModernMinecarts.java)
- Mixins: [src/main/java/net/lordkipama/modernminecarts/mixin](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/mixin)
- Events: [ModEvents.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/event/ModEvents.java)
- Attachments: [src/main/java/net/lordkipama/modernminecarts/attachment](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/attachment)
- Train linking: [MinecartLinkHelper.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/util/MinecartLinkHelper.java)
- Furnace behavior: [FurnaceMinecartHelper.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/util/FurnaceMinecartHelper.java)
- Hopper behavior: [HopperMinecartHelper.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/util/HopperMinecartHelper.java)
- Rail blocks: [src/main/java/net/lordkipama/modernminecarts/block/Custom](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/block/Custom)
- Networking: [ModernMinecartsPacketHandler.java](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/Proxy/ModernMinecartsPacketHandler.java)
- GUI: [src/main/java/net/lordkipama/modernminecarts/inventory](C:/Users/Kilian%20Mayrhofer/AppData/Roaming/.minecraft_custom/Modding/Forge_1.20.1/src/main/java/net/lordkipama/modernminecarts/inventory)
