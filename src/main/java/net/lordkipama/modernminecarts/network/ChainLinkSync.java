package net.lordkipama.modernminecarts.network;

import net.lordkipama.modernminecarts.interfaces.ChainMinecartInterface;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

public record ChainLinkSync(int parentId, int childId) {
    public static final int MISSING_ENTITY = -1;

    public static ChainLinkSync of(Entity parent, Entity child) {
        return new ChainLinkSync(
                parent == null ? MISSING_ENTITY : parent.getId(),
                child == null ? MISSING_ENTITY : child.getId()
        );
    }

    public boolean hasParent() {
        return parentId != MISSING_ENTITY;
    }

    public boolean hasChild() {
        return childId != MISSING_ENTITY;
    }

    public void apply(ClientWorld world) {
        Entity parentEntity = hasParent() ? world.getEntityById(parentId) : null;
        Entity childEntity = hasChild() ? world.getEntityById(childId) : null;

        if (parentEntity instanceof ChainMinecartInterface parent) {
            parent.setLinkedChildClient(childId);
        }
        if (childEntity instanceof ChainMinecartInterface child) {
            child.setLinkedParentClient(parentId);
        }
    }
}
