# NeoForge 1.21.11 manual QA world

This datapack builds the manual test control station in the local development
world `ModernMinecarts-QA-NeoForge-1.21.11`.

Each button resets the nearest player by clearing their inventory, selecting
the required gamemode, and giving exactly the scenario's materials. It also
sets the scenario's random tick speed (3000 for copper aging, 3 otherwise).
The fixed crafting station and furnace-minecart rail layout are intentionally
not reset.

The datapack installs the station once when the world first ticks. If the
station is ever damaged, run:

```mcfunction
/function modernminecarts_qa:install
```
