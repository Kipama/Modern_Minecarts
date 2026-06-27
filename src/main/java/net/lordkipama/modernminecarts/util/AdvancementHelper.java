package net.lordkipama.modernminecarts.util;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class AdvancementHelper {
    private static final ResourceLocation WAX_ON = ResourceLocation.fromNamespaceAndPath("minecraft", "husbandry/wax_on");
    private static final ResourceLocation WAX_OFF = ResourceLocation.fromNamespaceAndPath("minecraft", "husbandry/wax_off");

    private AdvancementHelper() {
    }

    public static void awardWaxOn(ServerPlayer player) {
        awardCriterion(player, WAX_ON, "wax_on");
    }

    public static void awardWaxOff(ServerPlayer player) {
        awardCriterion(player, WAX_OFF, "wax_off");
    }

    private static void awardCriterion(ServerPlayer player, ResourceLocation advancementId, String criterion) {
        if (player == null) {
            return;
        }

        var server = player.getServer();
        if (server == null) {
            return;
        }

        AdvancementHolder advancement = server.getAdvancements().get(advancementId);
        if (advancement != null) {
            // Award the vanilla criterion directly so our rails are not dependent on data-pack override ordering.
            player.getAdvancements().award(advancement, criterion);
        }
    }
}
