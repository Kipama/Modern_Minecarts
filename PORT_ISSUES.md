# Modern Minecarts Port Issues

Last updated: 2026-06-27

This file tracks the known blockers for the NeoForge 1.21 port until the port is complete.

## Active issues

- [ ] Vanilla minecart feature parity is currently broken.
    - verify shift-right-click no longer opens chest and hopper minecarts when chain interactions should take priority
    - verify hopper minecarts correctly distribute items across the train, especially alongside chest minecarts
  - Relevant files:
    - `src/main/java/net/lordkipama/modernminecarts/ModernMinecarts.java`
    - `src/main/java/net/lordkipama/modernminecarts/event/ModEvents.java`
    - `src/main/java/net/lordkipama/modernminecarts/util/MinecartLinkHelper.java`
    - `src/main/java/net/lordkipama/modernminecarts/util/FurnaceMinecartHelper.java`
    - `src/main/java/net/lordkipama/modernminecarts/renderer/MinecartChainRenderer.java`
    - `src/main/java/net/lordkipama/modernminecarts/attachment/MinecartAttachmentTypes.java`
    - `src/main/java/net/lordkipama/modernminecarts/entity/CustomAbstractMinecartEntity.java`
    - `src/main/java/net/lordkipama/modernminecarts/entity/CustomMinecartFurnaceEntity.java`

- [ ] Sloped rail blockstate/model definitions are incomplete in 1.21.
  - The logs currently report many missing variants for `modernminecarts:blockstates/sloped_rail.json`.
  - Current symptom: repeated `BlockStateModelLoader` warnings for missing `sloped_rail` variants. Not gamebreaking but needs to be adressed for clean console logs.
  - The sloped rail block also doesnt work yet. The logic for the "jump" needs to be ported.

## 1.21 minecart port direction

The current best path is to stop replacing vanilla minecart registry entries and instead move the custom behavior onto vanilla minecarts.

### Why this direction

- NeoForge 1.21 now assumes dense built-in registries during creative tab rebuilding and registry validation.
- Replacing vanilla `minecraft:*` minecart items/entities currently leaves registry holes and causes client and server crashes.
- The mod's real value is in minecart behavior, not in owning separate registry identities for those minecarts.

### Proposed implementation strategy

- [ ] Keep vanilla `AbstractMinecart` and `MinecartFurnace` entity registrations intact.
- [ ] Move chain-link state off the custom entity classes and onto vanilla minecarts.
  - Best candidate: a mixin or attached state layer targeting `AbstractMinecart`.
  - Needed state: parent UUID, child UUID, client entity ids, refresh hooks.
  - Current progress: done for the first 1.21 pass using attachments plus event-driven linking/ticking against vanilla `AbstractMinecart`.
  - Compatibility target: this should also let many modded minecarts participate as long as they inherit from `AbstractMinecart`.
- [ ] Port the shared train physics from `CustomAbstractMinecartEntity` into helper methods that can run against vanilla minecarts.
  - Target behaviors:
    - chain coupling / uncoupling
    - boosted custom rail speed handling
    - sloped rail launch behavior
    - modified slowdown / off-track motion
    - chain item drops when links break
- [ ] Port furnace minecart behavior onto vanilla furnace minecarts instead of spawning `CustomMinecartFurnaceEntity`.
  - Best candidate: targeted mixin/hooks on `MinecartFurnace`.
  - Needed behavior:
    - one-slot fuel inventory
    - train-aware fuel usage
    - linked inventory scavenging
    - chunkloading config hook
    - custom menu opening
    - display block lit state sync
  - Current progress: done for the first 1.21 pass with a vanilla `MinecartFurnace` wrapper menu/container, attachment-backed fuel slot state, speedometer calibration, and train-speed clamping logic.
- [ ] Rewrite entity interaction code in `ModEvents` to operate on vanilla minecart instances instead of `CustomAbstractMinecartEntity`.
  - Current progress: done for the first 1.21 pass, including furnace-cart menu opening and chain interactions on vanilla minecarts.
- [ ] Update rendering so chain visuals render for vanilla minecarts when linked.
  - Current progress: first pass implemented with a `RenderLevelStageEvent` world render hook instead of replacing the entity renderer registration.
- [ ] Only keep custom registry entries where the mod truly owns the object, such as custom rail blocks and menus.

### Suggested migration order

1. Introduce mixin support or another equivalent vanilla-hook mechanism for 1.21.
2. Extract chain-link data + helper logic away from the custom entity inheritance tree.
3. Port chain linking to vanilla `AbstractMinecart`.
4. Port furnace minecart inventory/menu behavior to vanilla `MinecartFurnace`.
5. Rework train rendering and interaction code.
6. Delete the now-obsolete vanilla registry replacement path.

## Notes

- `CustomAbstractMinecartEntity` is still the best source of truth for the intended behavior, even if the final 1.21 solution no longer uses custom minecart entity registrations.
- The current port should prefer smaller, targeted hooks into vanilla behavior over another attempt to override `minecraft` registry entries.
