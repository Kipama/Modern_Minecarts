package net.lordkipama.modernminecarts.util;

import net.lordkipama.modernminecarts.Proxy.ModernMinecartsPacketHandler;
import net.lordkipama.modernminecarts.attachment.ChainMinecartData;
import net.lordkipama.modernminecarts.attachment.MinecartAttachmentTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public final class MinecartLinkHelper {
    private MinecartLinkHelper() {
    }

    public static ChainMinecartData getData(AbstractMinecart minecart) {
        return minecart.getData(MinecartAttachmentTypes.CHAIN_DATA.get());
    }

    @Nullable
    public static AbstractMinecart getLinkedParent(AbstractMinecart minecart) {
        ChainMinecartData data = getData(minecart);
        Entity entity = null;
        if (minecart.level().isClientSide()) {
            entity = minecart.level().getEntity(data.getParentIdClient());
        } else if (minecart.level() instanceof ServerLevel server && data.getParentUuid() != null) {
            entity = server.getEntity(data.getParentUuid());
        }
        return entity instanceof AbstractMinecart linked ? linked : null;
    }

    @Nullable
    public static AbstractMinecart getLinkedChild(AbstractMinecart minecart) {
        ChainMinecartData data = getData(minecart);
        Entity entity = null;
        if (minecart.level().isClientSide()) {
            entity = minecart.level().getEntity(data.getChildIdClient());
        } else if (minecart.level() instanceof ServerLevel server && data.getChildUuid() != null) {
            entity = server.getEntity(data.getChildUuid());
        }
        return entity instanceof AbstractMinecart linked ? linked : null;
    }

    public static void setLinkedParent(AbstractMinecart minecart, @Nullable AbstractMinecart parent) {
        ChainMinecartData data = getData(minecart);
        if (parent != null) {
            data.setParent(parent.getUUID(), parent.getId());
        } else {
            data.setParent(null, -1);
        }

        if (!minecart.level().isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntity(minecart, new ModernMinecartsPacketHandler.CouplePacket(data.getParentIdClient(), minecart.getId()));
        }
    }

    public static void setLinkedChild(AbstractMinecart minecart, @Nullable AbstractMinecart child) {
        ChainMinecartData data = getData(minecart);
        if (child != null) {
            data.setChild(child.getUUID(), child.getId());
        } else {
            data.setChild(null, -1);
        }
    }

    public static void setParentChild(AbstractMinecart parent, AbstractMinecart child) {
        AbstractMinecart previousChild = getLinkedChild(parent);
        if (previousChild != null) {
            unsetParentChild(parent, previousChild);
        }
        setLinkedChild(parent, child);
        setLinkedParent(child, parent);
    }

    public static void unsetParentChild(@Nullable AbstractMinecart parent, @Nullable AbstractMinecart child) {
        if (parent != null) {
            setLinkedChild(parent, null);
        }
        if (child != null) {
            setLinkedParent(child, null);
        }
    }

    public static void setLinkedParentClient(AbstractMinecart child, int parentId) {
        getData(child).setParentIdClient(parentId);
    }

    public static void setLinkedChildClient(AbstractMinecart parent, int childId) {
        getData(parent).setChildIdClient(childId);
    }

    public static boolean isTrainCircular(AbstractMinecart candidateParent, AbstractMinecart child) {
        AbstractMinecart cursor = candidateParent;
        while (cursor != null) {
            if (cursor == child) {
                return true;
            }
            cursor = getLinkedParent(cursor);
        }
        return false;
    }

    public static void tickFollower(AbstractMinecart minecart) {
        if (minecart.level().isClientSide()) {
            return;
        }

        AbstractMinecart parent = getLinkedParent(minecart);
        if (parent == null) {
            return;
        }

        if (parent.isRemoved()) {
            dropChainItem(minecart);
            unsetParentChild(parent, minecart);
            return;
        }

        double distance = parent.distanceTo(minecart) - 1.0D;
        if (distance > 4.0D) {
            unsetParentChild(parent, minecart);
            dropChainItem(minecart);
            return;
        }

        PacketDistributor.sendToPlayersTrackingEntity(minecart, new ModernMinecartsPacketHandler.CouplePacket(parent.getId(), minecart.getId()));
        Vec3 direction = parent.position().subtract(minecart.position()).normalize();
        Vec3 parentVelocity = parent.getDeltaMovement();

        if (parentVelocity.length() < 0.06D) {
            Vec3 newVelocity = direction;
            if (distance > 1.1D) {
                newVelocity = newVelocity.multiply(0.06D * distance, distance, 0.06D * distance);
            } else if (distance > 1.02D) {
                newVelocity = newVelocity.multiply(0.04D, distance, 0.04D);
            } else if (distance < 0.9D) {
                newVelocity = newVelocity.multiply(-0.3D, distance, -0.15D);
            } else if (distance < 0.98D) {
                newVelocity = newVelocity.multiply(-0.04D, distance, -0.04D);
            } else {
                newVelocity = newVelocity.multiply(0.0D, distance, 0.0D);
            }
            minecart.setDeltaMovement(newVelocity);
        } else {
            double newDistance = distance - 0.33D;
            minecart.setDeltaMovement(direction.multiply(parentVelocity.length(), parentVelocity.length(), parentVelocity.length())
                    .multiply(newDistance * newDistance, newDistance, newDistance * newDistance));
        }
    }

    public static void unlinkRemovedNeighbor(AbstractMinecart minecart) {
        AbstractMinecart parent = getLinkedParent(minecart);
        if (parent != null && parent.isRemoved()) {
            dropChainItem(minecart);
            unsetParentChild(parent, minecart);
        }

        AbstractMinecart child = getLinkedChild(minecart);
        if (child != null && child.isRemoved()) {
            dropChainItem(minecart);
            unsetParentChild(minecart, child);
        }
    }

    public static void dropChainItem(AbstractMinecart minecart) {
        minecart.level().addFreshEntity(new ItemEntity(minecart.level(), minecart.getX(), minecart.getY(), minecart.getZ(), new ItemStack(Items.IRON_CHAIN)));
    }
}
