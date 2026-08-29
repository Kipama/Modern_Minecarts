# Test direct copper-to-powered-rail redstone connectivity.
clear @p
gamemode creative @p
tp @p 0.5 121 -20.5
give @p modernminecarts:copper_rail 2
give @p minecraft:powered_rail 2
give @p minecraft:redstone_torch 1
tellraw @p {"text":"Copper/powered rail compatibility test prepared: creative mode, two copper rails, two powered rails, and a redstone torch.","color":"gold"}
