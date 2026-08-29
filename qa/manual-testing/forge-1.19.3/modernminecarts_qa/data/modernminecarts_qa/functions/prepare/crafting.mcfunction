# Craft every available mod recipe and the vanilla rail recipes.
clear @p
gamemode survival @p
tp @p 0.5 121 22.5
give @p minecraft:crafting_table 1
give @p minecraft:copper_ingot 64
give @p minecraft:iron_ingot 64
give @p minecraft:gold_ingot 64
give @p minecraft:stick 64
give @p minecraft:redstone 64
give @p minecraft:honeycomb 64
give @p minecraft:quartz 64
give @p minecraft:stone_pressure_plate 64
give @p minecraft:rail 64
give @p minecraft:powered_rail 64
give @p modernminecarts:exposed_copper_rail 1
give @p modernminecarts:weathered_copper_rail 1
give @p modernminecarts:oxidized_copper_rail 1
tellraw @p {"text":"Crafting recipe test prepared: survival mode, crafting table, and ingredients for every enabled Modern Minecarts recipe plus regular, powered, and directed powered rails.","color":"gold"}
