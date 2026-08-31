# Install once per world. The marker persists in level data.
execute unless data storage modernminecarts_qa:state installed run function modernminecarts_qa:install
execute unless data storage modernminecarts_qa:state installed run data modify storage modernminecarts_qa:state installed set value 1b
