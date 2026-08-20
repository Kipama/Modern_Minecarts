# Builds the permanent control station and the fixed rail test layout.
# Gamerule commands are deliberately in the command blocks below: dedicated
# servers compile datapack functions before they load function-permission-level.

# Redstone Ready's surface is Y=55; all exposed test structures start at Y=56.
# Control station and the four test areas.
fill -7 56 -6 7 56 6 minecraft:smooth_stone
fill -8 56 17 8 56 23 minecraft:smooth_stone
fill -12 56 34 12 56 46 minecraft:smooth_stone
fill 15 56 -15 37 56 11 minecraft:smooth_stone
fill -12 56 -25 12 56 -15 minecraft:smooth_stone

# One-time global setup. These hidden command blocks run with command-block
# permissions when the redstone block below the first block is placed last.
setblock -6 55 0 minecraft:chain_command_block[facing=east]{Command:"gamerule keepInventory true",auto:0b,TrackOutput:0b} replace
setblock -5 55 0 minecraft:chain_command_block[facing=east]{Command:"gamerule doDaylightCycle false",auto:0b,TrackOutput:0b} replace
setblock -4 55 0 minecraft:chain_command_block[facing=east]{Command:"gamerule doWeatherCycle false",auto:0b,TrackOutput:0b} replace
setblock -3 55 0 minecraft:chain_command_block[facing=east]{Command:"gamerule doMobSpawning false",auto:0b,TrackOutput:0b} replace
setblock -2 55 0 minecraft:chain_command_block[facing=east]{Command:"time set day",auto:0b,TrackOutput:0b} replace
setblock -1 55 0 minecraft:chain_command_block[facing=east]{Command:"gamerule randomTickSpeed 3",auto:0b,TrackOutput:0b} replace
setblock -7 55 0 minecraft:command_block[facing=east]{Command:"setworldspawn 0 57 -5",auto:0b,TrackOutput:0b} replace
setblock -7 54 0 minecraft:redstone_block replace

# Test selection buttons, from left to right: crafting, copper, furnace/linking, rail compatibility.
# Every impulse block sets the scenario tick rate and invokes its function through
# the chain block immediately to its east.
setblock -6 56 0 minecraft:command_block[facing=east]{Command:"gamerule randomTickSpeed 3",auto:0b,TrackOutput:0b} replace
setblock -5 56 0 minecraft:chain_command_block[facing=east]{Command:"function modernminecarts_qa:prepare/crafting",auto:0b,TrackOutput:0b} replace
setblock -2 56 0 minecraft:command_block[facing=east]{Command:"gamerule randomTickSpeed 2000",auto:0b,TrackOutput:0b} replace
setblock -1 56 0 minecraft:chain_command_block[facing=east]{Command:"function modernminecarts_qa:prepare/copper_rails",auto:0b,TrackOutput:0b} replace
setblock 2 56 0 minecraft:command_block[facing=east]{Command:"gamerule randomTickSpeed 3",auto:0b,TrackOutput:0b} replace
setblock 3 56 0 minecraft:chain_command_block[facing=east]{Command:"function modernminecarts_qa:prepare/furnace_and_links",auto:0b,TrackOutput:0b} replace
setblock 6 56 0 minecraft:command_block[facing=east]{Command:"gamerule randomTickSpeed 3",auto:0b,TrackOutput:0b} replace
setblock 7 56 0 minecraft:chain_command_block[facing=east]{Command:"function modernminecarts_qa:prepare/rail_compatibility",auto:0b,TrackOutput:0b} replace
setblock -6 57 0 minecraft:stone_button[face=floor,facing=north] replace
setblock -2 57 0 minecraft:stone_button[face=floor,facing=north] replace
setblock 2 57 0 minecraft:stone_button[face=floor,facing=north] replace
setblock 6 57 0 minecraft:stone_button[face=floor,facing=north] replace

# Labels face the world spawn point.
setblock -6 57 2 minecraft:oak_sign[rotation=8]{front_text:{messages:['{"text":"CRAFT RECIPES"}','{"text":"Survival + items"}','{"text":"Press button"}','{"text":""}']}} replace
setblock -2 57 2 minecraft:oak_sign[rotation=8]{front_text:{messages:['{"text":"COPPER RAILS"}','{"text":"Survival + aging"}','{"text":"Press button"}','{"text":""}']}} replace
setblock 2 57 2 minecraft:oak_sign[rotation=8]{front_text:{messages:['{"text":"FURNACE + LINKS"}','{"text":"Creative + loop"}','{"text":"Press button"}','{"text":""}']}} replace
setblock 6 57 2 minecraft:oak_sign[rotation=8]{front_text:{messages:['{"text":"RAIL COMPAT"}','{"text":"Creative + parts"}','{"text":"Press button"}','{"text":""}']}} replace

# Crafting test station.
setblock 0 57 20 minecraft:crafting_table replace

# Copper rail launch test: sixteen physically powered waxed copper rails lead
# east into a sloped launch rail. Redstone blocks below the rails keep the
# entire run powered without adding a separate setup step to the test.
fill -10 56 40 5 56 40 minecraft:redstone_block
fill -10 57 40 5 57 40 modernminecarts:waxed_copper_rail[shape=east_west]
setblock 6 57 40 modernminecarts:sloped_rail[shape=ascending_east,const_shape=ascending_east] replace
setblock -10 57 42 minecraft:oak_sign[rotation=8]{front_text:{messages:['{"text":"POWERED COPPER RAMP"}','{"text":"16 waxed rails east"}','{"text":"Sloped launch rail"}','{"text":""}']}} replace

# Furnace and linked-minecart test: a 13-by-13 loop with regular-rail corners,
# regular rails on three sides, and powered rails with a torch at each end on the fourth.
fill 21 57 -10 31 57 -10 minecraft:rail[shape=east_west]
fill 21 57 2 31 57 2 minecraft:rail[shape=east_west]
fill 20 57 -9 20 57 1 minecraft:rail[shape=north_south]
fill 32 57 -9 32 57 1 minecraft:powered_rail[shape=north_south]
setblock 20 57 -10 minecraft:rail[shape=south_east] replace
setblock 32 57 -10 minecraft:rail[shape=south_west] replace
setblock 20 57 2 minecraft:rail[shape=north_east] replace
setblock 32 57 2 minecraft:rail[shape=north_west] replace
setblock 31 57 -9 minecraft:redstone_torch replace
setblock 31 57 1 minecraft:redstone_torch replace

# Sixteen regular rails beside the loop's lower edge.
fill 20 57 5 35 57 5 minecraft:rail[shape=east_west]

tellraw @a [{"text":"Modern Minecarts manual QA world is ready. ","color":"gold"},{"text":"Use the four labeled buttons at spawn; pressing any button clears and reprovisions your inventory and gamemode for that scenario.","color":"white"}]
