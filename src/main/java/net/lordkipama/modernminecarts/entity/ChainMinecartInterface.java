package net.lordkipama.modernminecarts.entity;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface ChainMinecartInterface {
    default @Nullable CustomAbstractMinecartEntity getLinkedParent() {
        return null;
    }
    default void setLinkedParent(@Nullable CustomAbstractMinecartEntity parent) {}

    default @Nullable CustomAbstractMinecartEntity getLinkedChild() {
        return null;
    }
    default void setLinkedChild(@Nullable CustomAbstractMinecartEntity child) {}

    default void setLinkedParentClient(int id) {}
    default void setLinkedChildClient(int id) {}

    default CustomAbstractMinecartEntity asCustomAbstractMinecartEntity() { return (CustomAbstractMinecartEntity) this; }

    static void setParentChild(@NotNull ChainMinecartInterface parent, @NotNull ChainMinecartInterface child) {
        unsetParentChild(parent, parent.getLinkedChild());
        unsetParentChild(child, child.getLinkedParent());
        parent.setLinkedChild(child.asCustomAbstractMinecartEntity());
        child.setLinkedParent(parent.asCustomAbstractMinecartEntity());
    }

    static void unsetParentChild(@Nullable ChainMinecartInterface parent, @Nullable ChainMinecartInterface child) {
        if (parent != null) {
            parent.setLinkedChild(null);
        }
        if (child != null) {
            child.setLinkedParent(null);
        }
    }
}
