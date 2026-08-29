# Builds the permanent control station and fixed rail tests at ground level.
# Redstone Ready's exposed sandstone is Y=55; test structures begin at Y=56.
fill -7 56 -6 7 56 6 minecraft:smooth_stone
fill -8 56 17 8 56 23 minecraft:smooth_stone
fill -12 56 34 12 56 46 minecraft:smooth_stone
fill 15 56 -15 37 56 11 minecraft:smooth_stone
fill -12 56 -25 12 56 -15 minecraft:smooth_stone
fill 15 56 15 37 56 27 minecraft:smooth_stone

# One-time global setup, triggered by the redstone block below the first block.
setblock -6 55 0 minecraft:chain_command_block[facing=east]{Command:"gamerule keepInventory true",auto:0b,TrackOutput:0b} replace
setblock -5 55 0 minecraft:chain_command_block[facing=east]{Command:"gamerule doDaylightCycle false",auto:0b,TrackOutput:0b} replace
setblock -4 55 0 minecraft:chain_command_block[facing=east]{Command:"gamerule doWeatherCycle false",auto:0b,TrackOutput:0b} replace
setblock -3 55 0 minecraft:chain_command_block[facing=east]{Command:"gamerule doMobSpawning false",auto:0b,TrackOutput:0b} replace
setblock -2 55 0 minecraft:chain_command_block[facing=east]{Command:"time set day",auto:0b,TrackOutput:0b} replace
setblock -1 55 0 minecraft:chain_command_block[facing=east]{Command:"gamerule randomTickSpeed 3",auto:0b,TrackOutput:0b} replace
setblock -7 55 0 minecraft:command_block[facing=east]{Command:"setworldspawn 0 57 -5",auto:0b,TrackOutput:0b} replace
setblock -7 54 0 minecraft:redstone_block replace

# Test-selection buttons: crafting, copper, furnace/linking, rail compatibility.
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
setblock -6 57 2 minecraft:oak_sign[rotation=8]{Text1:'{"text":"CRAFT RECIPES"}',Text2:'{"text":"Survival + items"}',Text3:'{"text":"Press button"}'} replace
setblock -2 57 2 minecraft:oak_sign[rotation=8]{Text1:'{"text":"COPPER RAILS"}',Text2:'{"text":"Survival + aging"}',Text3:'{"text":"Press button"}'} replace
setblock 2 57 2 minecraft:oak_sign[rotation=8]{Text1:'{"text":"FURNACE + LINKS"}',Text2:'{"text":"Creative + loop"}',Text3:'{"text":"Press button"}'} replace
setblock 6 57 2 minecraft:oak_sign[rotation=8]{Text1:'{"text":"RAIL COMPAT"}',Text2:'{"text":"Creative + parts"}',Text3:'{"text":"Press button"}'} replace

setblock 0 57 20 minecraft:crafting_table replace

# Sixteen waxed copper rails physically powered from below, ending in an east-facing slope.
fill -10 56 40 5 56 40 minecraft:redstone_block
fill -10 57 40 5 57 40 modernminecarts:waxed_copper_rail[shape=east_west]
setblock 6 57 40 modernminecarts:sloped_rail[shape=ascending_east,const_shape=ascending_east] replace
setblock -10 57 42 minecraft:oak_sign[rotation=8]{Text1:'{"text":"POWERED COPPER RAMP"}',Text2:'{"text":"16 waxed rails east"}',Text3:'{"text":"Sloped launch rail"}'} replace

# Furnace and linked-minecart test: three regular sides, one powered side.
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
setblock 20 57 22 minecraft:oak_sign[rotation=8]{Text1:'{"text":"DIRECTED POWERED RAIL"}',Text2:'{"text":"East line / west line"}',Text3:'{"text":"Stationary + reversal"}'} replace

tellraw @a [{"text":"Modern Minecarts manual QA world is ready. ","color":"gold"},{"text":"Use the four labeled buttons at spawn; pressing any button clears and reprovisions your inventory and gamemode for that scenario.","color":"white"}]
data modify storage modernminecarts_qa:state installed set value 1b
