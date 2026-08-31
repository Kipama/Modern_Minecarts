# Builds the permanent control station and the fixed rail test layout.
# Redstone Ready's surface is Y=55; all exposed test structures start at Y=56.

# The dedicated generator starts at a random spawn; keep every test chunk loaded
# until the installer has created the ground-level platforms.
forceload add -16 -32 47 47

fill -7 56 -6 7 56 6 minecraft:smooth_stone
fill -8 56 17 8 56 23 minecraft:smooth_stone
fill -12 56 34 12 56 46 minecraft:smooth_stone
fill 15 56 -15 37 56 11 minecraft:smooth_stone
fill -12 56 -25 12 56 -15 minecraft:smooth_stone
fill 15 56 15 37 56 27 minecraft:smooth_stone

# One-time global setup. Hidden command blocks receive power from the redstone
# block beneath the first block, which is placed last.
setblock -6 55 0 minecraft:chain_command_block[facing=east]{Command:"gamerule keepInventory true",auto:0b,TrackOutput:0b} replace
setblock -5 55 0 minecraft:chain_command_block[facing=east]{Command:"gamerule doDaylightCycle false",auto:0b,TrackOutput:0b} replace
setblock -4 55 0 minecraft:chain_command_block[facing=east]{Command:"gamerule doWeatherCycle false",auto:0b,TrackOutput:0b} replace
setblock -3 55 0 minecraft:chain_command_block[facing=east]{Command:"gamerule doMobSpawning false",auto:0b,TrackOutput:0b} replace
setblock -2 55 0 minecraft:chain_command_block[facing=east]{Command:"time set day",auto:0b,TrackOutput:0b} replace
setblock -1 55 0 minecraft:chain_command_block[facing=east]{Command:"gamerule randomTickSpeed 3",auto:0b,TrackOutput:0b} replace
setblock -7 55 0 minecraft:command_block[facing=east]{Command:"setworldspawn 0 57 -5",auto:0b,TrackOutput:0b} replace
setblock -7 54 0 minecraft:redstone_block replace

# Test selection buttons: crafting, copper, furnace/linking, rail compatibility.
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

setblock -6 57 2 minecraft:oak_sign[rotation=8]{front_text:{messages:['{"text":"CRAFT RECIPES"}','{"text":"Survival + items"}','{"text":"Press button"}','{"text":""}']}} replace
setblock -2 57 2 minecraft:oak_sign[rotation=8]{front_text:{messages:['{"text":"COPPER RAILS"}','{"text":"Survival + aging"}','{"text":"Press button"}','{"text":""}']}} replace
setblock 2 57 2 minecraft:oak_sign[rotation=8]{front_text:{messages:['{"text":"FURNACE + LINKS"}','{"text":"Creative + loop"}','{"text":"Press button"}','{"text":""}']}} replace
setblock 6 57 2 minecraft:oak_sign[rotation=8]{front_text:{messages:['{"text":"RAIL COMPAT"}','{"text":"Creative + parts"}','{"text":"Press button"}','{"text":""}']}} replace

setblock 0 57 20 minecraft:crafting_table replace

# Sixteen waxed copper rails physically powered from below, ending in an
# east-facing sloped launch rail.
fill -10 56 40 5 56 40 minecraft:redstone_block
fill -10 57 40 5 57 40 modernminecarts:waxed_copper_rail[shape=east_west]
setblock 6 57 40 modernminecarts:rail_jump[shape=ascending_east,const_shape=ascending_east] replace
setblock -10 57 42 minecraft:oak_sign[rotation=8]{front_text:{messages:['{"text":"POWERED COPPER RAMP"}','{"text":"16 waxed rails east"}','{"text":"Sloped launch rail"}','{"text":""}']}} replace

# Furnace and linked-minecart test: regular rails on three sides, powered
# rails on the fourth, with a redstone torch at each end.
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
fill 20 57 5 35 57 5 minecraft:rail[shape=east_west]

# Directed powered rail test area: opposing direction lines with external power.
fill 20 56 20 35 56 20 minecraft:redstone_block
fill 20 57 20 35 57 20 modernminecarts:directed_powered_rail[shape=east_west,inverted=false,powered=true]
fill 20 56 24 35 56 24 minecraft:redstone_block
fill 20 57 24 35 57 24 modernminecarts:directed_powered_rail[shape=east_west,inverted=true,powered=true]
setblock 20 57 22 minecraft:oak_sign[rotation=8]{front_text:{messages:['{"text":"DIRECTED POWERED RAIL"}','{"text":"East line / west line"}','{"text":"Stationary + reversal"}','{"text":""}']}} replace

tellraw @a [{"text":"Modern Minecarts manual QA world is ready. ","color":"gold"},{"text":"Use the four labeled buttons at spawn; pressing any button clears and reprovisions your inventory and gamemode for that scenario.","color":"white"}]
