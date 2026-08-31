# Test direct copper-to-powered-rail redstone connectivity.
clear @p
gamemode creative @p
tp @p 0.5 57 -20.5
give @p modernminecarts:copper_rail 2
give @p minecraft:powered_rail 2
give @p modernminecarts:directed_powered_rail 2
give @p minecraft:redstone_torch 1
tellraw @p {"text":"Rail compatibility test prepared: creative mode, copper, powered, and directed powered rails plus a redstone torch.","color":"gold"}
