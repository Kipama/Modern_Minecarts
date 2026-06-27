package net.lordkipama.modernminecarts.entity;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
//The following code is based on the work of Cammie. The only changes have been porting it from fabric to forge.
/*Copyright (C) 2022 Cammie

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to use, copy, modify, and/or merge copies of the
Software, and to permit persons to whom the Software is furnished to do so, subject to the following
restrictions:

 1) The above copyright notice and this permission notice shall be included in all copies or substantial
    portions of the Software.
 2) You include attribution to the copyright holder(s) in public display of any project that uses any
    portion of the Software.
 3) You may not publish or distribute substantial portions of the Software in its compiled or uncompiled
    forms without prior permission from the copyright holder.
 4) The Software does not make up a substantial portion of your own projects.
*
* */
public interface ChainMinecartInterface {
    default @Nullable AbstractMinecart getLinkedParent() {
        return null;
    }
    default void setLinkedParent(@Nullable AbstractMinecart parent) {}

    default @Nullable AbstractMinecart getLinkedChild() {
        return null;
    }
    default void setLinkedChild(@Nullable AbstractMinecart child) {}

    default void setLinkedParentClient(int id) {}
    default void setLinkedChildClient(int id) {}

    default AbstractMinecart asAbstractMinecart() { return (AbstractMinecart) this; }

    static void setParentChild(@NotNull ChainMinecartInterface parent, @NotNull ChainMinecartInterface child) {
        unsetParentChild(parent, parent.getLinkedChild() instanceof ChainMinecartInterface linkedChild ? linkedChild : null);
        //unsetParentChild(child, child.getLinkedParent()); This line leads to bugs when connecting to the front of trains
        parent.setLinkedChild(child.asAbstractMinecart());
        child.setLinkedParent(parent.asAbstractMinecart());
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
