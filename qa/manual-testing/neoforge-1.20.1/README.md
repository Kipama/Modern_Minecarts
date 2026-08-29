# NeoForge 1.20.1 manual QA world

This datapack builds the manual test control station in the local development
world `ModernMinecarts-QA-NeoForge-1.20.1`.

Each button clears the nearest player's inventory, selects the scenario's
gamemode, and gives its materials. The copper test sets random tick speed to
2000; the other scenarios use 3. The fixed stations are not reset. A separate
directed-powered-rail area contains opposing powered lines for stationary-cart
starts and direction reversal checks.

The datapack installs the station once on the first world tick. If the station
is damaged, run:

```mcfunction
/function modernminecarts_qa:install
```
