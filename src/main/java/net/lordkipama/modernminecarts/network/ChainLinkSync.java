package net.lordkipama.modernminecarts.network;

import net.lordkipama.modernminecarts.interfaces.ChainMinecartInterface;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public record ChainLinkSync(int parentId, int childId) {
    public static final int MISSING_ENTITY = -1;

    public static ChainLinkSync of(Entity parent, Entity child) {
        return new ChainLinkSync(parent == null ? MISSING_ENTITY : parent.getId(), child == null ? MISSING_ENTITY : child.getId());
    }

    public boolean hasParent() {
        return this.parentId != MISSING_ENTITY;
    }

    public boolean hasChild() {
        return this.childId != MISSING_ENTITY;
    }

    public void apply(Level level) {
        Entity parentEntity = this.hasParent() ? level.getEntity(this.parentId) : null;
        Entity childEntity = this.hasChild() ? level.getEntity(this.childId) : null;

        if (parentEntity instanceof ChainMinecartInterface parent) {
            parent.setLinkedChildClient(this.childId);
        }
        if (childEntity instanceof ChainMinecartInterface child) {
            child.setLinkedParentClient(this.parentId);
        }
    }
}
