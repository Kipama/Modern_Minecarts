# Install once per world. The storage marker persists in level.dat, while this
# function stays harmless on every later tick and keeps the world repairable.
execute unless data storage modernminecarts_qa:state installed run function modernminecarts_qa:install
execute unless data storage modernminecarts_qa:state installed run data modify storage modernminecarts_qa:state installed set value 1b
