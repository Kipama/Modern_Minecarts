package net.lordkipama.modernminecarts.attachment;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ChainMinecartData implements INBTSerializable<CompoundTag> {
    private static final String PARENT_UUID_TAG = "ParentUuid";
    private static final String CHILD_UUID_TAG = "ChildUuid";
    private static final String PARENT_ID_CLIENT_TAG = "ParentIdClient";
    private static final String CHILD_ID_CLIENT_TAG = "ChildIdClient";

    @Nullable
    private UUID parentUuid;
    @Nullable
    private UUID childUuid;
    private int parentIdClient = -1;
    private int childIdClient = -1;

    @Nullable
    public UUID getParentUuid() {
        return parentUuid;
    }

    public void setParent(@Nullable UUID parentUuid, int parentIdClient) {
        this.parentUuid = parentUuid;
        this.parentIdClient = parentIdClient;
    }

    @Nullable
    public UUID getChildUuid() {
        return childUuid;
    }

    public void setChild(@Nullable UUID childUuid, int childIdClient) {
        this.childUuid = childUuid;
        this.childIdClient = childIdClient;
    }

    public int getParentIdClient() {
        return parentIdClient;
    }

    public void setParentIdClient(int parentIdClient) {
        this.parentIdClient = parentIdClient;
    }

    public int getChildIdClient() {
        return childIdClient;
    }

    public void setChildIdClient(int childIdClient) {
        this.childIdClient = childIdClient;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (parentUuid != null) {
            tag.putUUID(PARENT_UUID_TAG, parentUuid);
        }
        if (childUuid != null) {
            tag.putUUID(CHILD_UUID_TAG, childUuid);
        }
        tag.putInt(PARENT_ID_CLIENT_TAG, parentIdClient);
        tag.putInt(CHILD_ID_CLIENT_TAG, childIdClient);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        parentUuid = tag.hasUUID(PARENT_UUID_TAG) ? tag.getUUID(PARENT_UUID_TAG) : null;
        childUuid = tag.hasUUID(CHILD_UUID_TAG) ? tag.getUUID(CHILD_UUID_TAG) : null;
        parentIdClient = tag.contains(PARENT_ID_CLIENT_TAG) ? tag.getInt(PARENT_ID_CLIENT_TAG) : -1;
        childIdClient = tag.contains(CHILD_ID_CLIENT_TAG) ? tag.getInt(CHILD_ID_CLIENT_TAG) : -1;
    }
}
