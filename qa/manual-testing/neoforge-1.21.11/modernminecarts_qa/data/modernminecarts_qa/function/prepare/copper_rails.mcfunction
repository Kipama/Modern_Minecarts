# Exercise copper rail weathering, waxing, unwaxing, and minecart behavior.
clear @p
gamemode survival @p
tp @p 0.5 121 40.5
give @p minecraft:diamond_axe 1
give @p minecraft:honeycomb 64
give @p modernminecarts:copper_rail 16
give @p modernminecarts:exposed_copper_rail 16
give @p modernminecarts:weathered_copper_rail 16
give @p modernminecarts:oxidized_copper_rail 16
give @p minecraft:minecart 1
tellraw @p {"text":"Copper rail test prepared: survival mode, randomTickSpeed 300, axe, honeycomb, 16 of every unwaxed copper rail state, and a minecart.","color":"gold"}
