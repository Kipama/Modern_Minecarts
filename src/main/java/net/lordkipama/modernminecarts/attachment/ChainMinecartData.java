package net.lordkipama.modernminecarts.attachment;

import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ChainMinecartData implements ValueIOSerializable {
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
    public void serialize(ValueOutput output) {
        output.storeNullable(PARENT_UUID_TAG, UUIDUtil.CODEC, parentUuid);
        output.storeNullable(CHILD_UUID_TAG, UUIDUtil.CODEC, childUuid);
        output.putInt(PARENT_ID_CLIENT_TAG, parentIdClient);
        output.putInt(CHILD_ID_CLIENT_TAG, childIdClient);
    }

    @Override
    public void deserialize(ValueInput input) {
        parentUuid = input.read(PARENT_UUID_TAG, UUIDUtil.CODEC).orElse(null);
        childUuid = input.read(CHILD_UUID_TAG, UUIDUtil.CODEC).orElse(null);
        parentIdClient = input.getIntOr(PARENT_ID_CLIENT_TAG, -1);
        childIdClient = input.getIntOr(CHILD_ID_CLIENT_TAG, -1);
    }
}
