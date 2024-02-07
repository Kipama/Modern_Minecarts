package net.lordkipama.modernminecarts.Proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ClientProxy implements IProxy {

    @Override
    public Level getWorld() {
        return Minecraft.getInstance().level;
    }
}