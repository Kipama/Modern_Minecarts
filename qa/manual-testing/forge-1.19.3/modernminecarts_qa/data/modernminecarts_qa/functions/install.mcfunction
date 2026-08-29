# Builds the permanent control station and fixed rail tests at ground level.
# Redstone Ready's exposed sandstone is Y=119; test structures begin at Y=120.
fill -7 120 -6 7 120 6 minecraft:smooth_stone
fill -8 120 17 8 120 23 minecraft:smooth_stone
fill -12 120 34 12 120 46 minecraft:smooth_stone
fill 15 120 -15 37 120 11 minecraft:smooth_stone
fill -12 120 -25 12 120 -15 minecraft:smooth_stone
fill 15 120 15 37 120 27 minecraft:smooth_stone

# One-time global setup, triggered by the redstone block below the first block.
setblock -6 119 0 minecraft:chain_command_block[facing=east]{Command:"gamerule keepInventory true",auto:0b,TrackOutput:0b} replace
setblock -5 119 0 minecraft:chain_command_block[facing=east]{Command:"gamerule doDaylightCycle false",auto:0b,TrackOutput:0b} replace
setblock -4 119 0 minecraft:chain_command_block[facing=east]{Command:"gamerule doWeatherCycle false",auto:0b,TrackOutput:0b} replace
setblock -3 119 0 minecraft:chain_command_block[facing=east]{Command:"gamerule doMobSpawning false",auto:0b,TrackOutput:0b} replace
setblock -2 119 0 minecraft:chain_command_block[facing=east]{Command:"time set day",auto:0b,TrackOutput:0b} replace
setblock -1 119 0 minecraft:chain_command_block[facing=east]{Command:"gamerule randomTickSpeed 3",auto:0b,TrackOutput:0b} replace
setblock -7 119 0 minecraft:command_block[facing=east]{Command:"setworldspawn 0 121 -5",auto:0b,TrackOutput:0b} replace
setblock -7 118 0 minecraft:redstone_block replace

# Test-selection buttons: crafting, copper, furnace/linking, rail compatibility.
setblock -6 120 0 minecraft:command_block[facing=east]{Command:"gamerule randomTickSpeed 3",auto:0b,TrackOutput:0b} replace
setblock -5 120 0 minecraft:chain_command_block[facing=east]{Command:"function modernminecarts_qa:prepare/crafting",auto:0b,TrackOutput:0b} replace
setblock -2 120 0 minecraft:command_block[facing=east]{Command:"gamerule randomTickSpeed 2000",auto:0b,TrackOutput:0b} replace
setblock -1 120 0 minecraft:chain_command_block[facing=east]{Command:"function modernminecarts_qa:prepare/copper_rails",auto:0b,TrackOutput:0b} replace
setblock 2 120 0 minecraft:command_block[facing=east]{Command:"gamerule randomTickSpeed 3",auto:0b,TrackOutput:0b} replace
setblock 3 120 0 minecraft:chain_command_block[facing=east]{Command:"function modernminecarts_qa:prepare/furnace_and_links",auto:0b,TrackOutput:0b} replace
setblock 6 120 0 minecraft:command_block[facing=east]{Command:"gamerule randomTickSpeed 3",auto:0b,TrackOutput:0b} replace
setblock 7 120 0 minecraft:chain_command_block[facing=east]{Command:"function modernminecarts_qa:prepare/rail_compatibility",auto:0b,TrackOutput:0b} replace
setblock -6 121 0 minecraft:stone_button[face=floor,facing=north] replace
setblock -2 121 0 minecraft:stone_button[face=floor,facing=north] replace
setblock 2 121 0 minecraft:stone_button[face=floor,facing=north] replace
setblock 6 121 0 minecraft:stone_button[face=floor,facing=north] replace
setblock -6 121 2 minecraft:oak_sign[rotation=8]{Text1:'{"text":"CRAFT RECIPES"}',Text2:'{"text":"Survival + items"}',Text3:'{"text":"Press button"}'} replace
setblock -2 121 2 minecraft:oak_sign[rotation=8]{Text1:'{"text":"COPPER RAILS"}',Text2:'{"text":"Survival + aging"}',Text3:'{"text":"Press button"}'} replace
setblock 2 121 2 minecraft:oak_sign[rotation=8]{Text1:'{"text":"FURNACE + LINKS"}',Text2:'{"text":"Creative + loop"}',Text3:'{"text":"Press button"}'} replace
setblock 6 121 2 minecraft:oak_sign[rotation=8]{Text1:'{"text":"RAIL COMPAT"}',Text2:'{"text":"Creative + parts"}',Text3:'{"text":"Press button"}'} replace

setblock 0 121 20 minecraft:crafting_table replace

# Sixteen waxed copper rails physically powered from below, ending in an east-facing slope.
fill -10 120 40 5 120 40 minecraft:redstone_block
fill -10 121 40 5 121 40 modernminecarts:waxed_copper_rail[shape=east_west]
setblock 6 121 40 modernminecarts:sloped_rail[shape=ascending_east,const_shape=ascending_east] replace
setblock -10 121 42 minecraft:oak_sign[rotation=8]{Text1:'{"text":"POWERED COPPER RAMP"}',Text2:'{"text":"16 waxed rails east"}',Text3:'{"text":"Sloped launch rail"}'} replace

# Furnace and linked-minecart test: three regular sides, one powered side.
fill 21 121 -10 31 121 -10 minecraft:rail[shape=east_west]
fill 21 121 2 31 121 2 minecraft:rail[shape=east_west]
fill 20 121 -9 20 121 1 minecraft:rail[shape=north_south]
fill 32 121 -9 32 121 1 minecraft:powered_rail[shape=north_south]
setblock 20 121 -10 minecraft:rail[shape=south_east] replace
setblock 32 121 -10 minecraft:rail[shape=south_west] replace
setblock 20 121 2 minecraft:rail[shape=north_east] replace
setblock 32 121 2 minecraft:rail[shape=north_west] replace
setblock 31 121 -9 minecraft:redstone_torch replace
setblock 31 121 1 minecraft:redstone_torch replace
fill 20 121 5 35 121 5 minecraft:rail[shape=east_west]

# Directed powered rail test area: opposing direction lines with external power.
fill 20 120 20 35 120 20 minecraft:redstone_block
fill 20 121 20 35 121 20 modernminecarts:directed_powered_rail[shape=east_west,inverted=false,powered=true]
fill 20 120 24 35 120 24 minecraft:redstone_block
fill 20 121 24 35 121 24 modernminecarts:directed_powered_rail[shape=east_west,inverted=true,powered=true]
setblock 20 121 22 minecraft:oak_sign[rotation=8]{Text1:'{"text":"DIRECTED POWERED RAIL"}',Text2:'{"text":"East line / west line"}',Text3:'{"text":"Stationary + reversal"}'} replace

tellraw @a [{"text":"Modern Minecarts manual QA world is ready. ","color":"gold"},{"text":"Use the four labeled buttons at spawn; pressing any button clears and reprovisions your inventory and gamemode for that scenario.","color":"white"}]
data modify storage modernminecarts_qa:state installed set value 1b
