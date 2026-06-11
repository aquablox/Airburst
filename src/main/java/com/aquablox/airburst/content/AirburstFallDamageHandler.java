package com.aquablox.airburst.content;

import com.aquablox.airburst.Airburst;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Airburst.MOD_ID)
public class AirburstFallDamageHandler {
    private static final double SAFE_LANDING_Y_VELOCITY = -0.85D;

    private AirburstFallDamageHandler() {
    }

    @SubscribeEvent
    public static void trackAirburstLandingVelocity(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        int graceTicks = player.getPersistentData().getInt(AirburstAbility.FALL_GRACE_TICKS_TAG);
        if (graceTicks <= 0) {
            return;
        }

        player.getPersistentData().putDouble(AirburstAbility.LAST_Y_VELOCITY_TAG, player.getDeltaMovement().y);
        player.getPersistentData().putInt(AirburstAbility.FALL_GRACE_TICKS_TAG, graceTicks - 1);
    }

    @SubscribeEvent
    public static void preventGentleAirburstFallDamage(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        int graceTicks = player.getPersistentData().getInt(AirburstAbility.FALL_GRACE_TICKS_TAG);
        if (graceTicks <= 0) {
            return;
        }

        double yVelocity = player.getPersistentData().getDouble(AirburstAbility.LAST_Y_VELOCITY_TAG);
        if (yVelocity >= SAFE_LANDING_Y_VELOCITY) {
            event.setCanceled(true);
            player.fallDistance = 0;
            player.getPersistentData().remove(AirburstAbility.FALL_GRACE_TICKS_TAG);
            player.getPersistentData().remove(AirburstAbility.LAST_Y_VELOCITY_TAG);
        }
    }
}
