# Test furnace minecart behavior and chained minecarts on the prebuilt loop and straight track.
clear @p
gamemode creative @p
tp @p 26.5 57 -4.5
give @p minecraft:furnace_minecart 1
give @p minecraft:minecart 1
give @p minecraft:hopper_minecart 1
give @p minecraft:chest_minecart 1
give @p minecraft:bamboo 64
give @p minecraft:iron_ingot 64
give @p minecraft:iron_ingot 64
tellraw @p {"text":"Furnace and linked-minecart test prepared: creative mode, test carts, bamboo, two stacks of iron, a powered loop, and a 16-rail straight track.","color":"gold"}
