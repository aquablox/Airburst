package com.aquablox.airburst.content;

import com.aquablox.airburst.Airburst;
import com.aquablox.airburst.config.AirburstConfigs;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.aquablox.airburst.registry.AirburstItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Airburst.MOD_ID)
public class AirburstAbility {
    public static final String FALL_GRACE_TICKS_TAG = "AirburstFallGraceTicks";
    public static final String LAST_Y_VELOCITY_TAG = "AirburstLastYVelocity";
    public static final int FALL_GRACE_TICKS = 100;
    private static final int MOUNTED_IMPULSE_RETRY_TICKS = 2;
    private static final double MOUNTED_IMPULSE_RETRY_THRESHOLD = 0.5D;
    private static final Map<UUID, PendingMountedImpulse> PENDING_MOUNTED_IMPULSES = new HashMap<>();

    public static void tryAirburst(ServerPlayer player) {
        tryAirburst(player, false);
    }

    public static void tryAirburst(ServerPlayer player, boolean reverse) {
        if (!isHoldingAirburstWand(player)) {
            player.displayClientMessage(Component.translatable("airburst.no_wand"), true);
            return;
        }

        if (player.getCooldowns().isOnCooldown(AirburstItems.AIRBURST_WAND.get())) {
            return;
        }

        if (!player.isCreative()) {
            ItemStack backtank = findBacktankWithEnoughAir(player);
            if (backtank.isEmpty()) {
                player.displayClientMessage(Component.translatable("airburst.no_air"), true);
                return;
            }

            BacktankUtil.consumeAir(player, backtank, AirburstWandItem.AIRBURST_PRESSURE_COST);
        }

        player.getCooldowns().addCooldown(AirburstItems.AIRBURST_WAND.get(), AirburstConfigs.airburstCooldownTicks());

        applyAirburstImpulse(player, reverse);
        player.getPersistentData().putInt(FALL_GRACE_TICKS_TAG, FALL_GRACE_TICKS);
        player.getPersistentData().putDouble(LAST_Y_VELOCITY_TAG, player.getDeltaMovement().y);

        player.level().playSound(null, player.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static boolean isHoldingAirburstWand(Player player) {
        return AirburstItems.isAirburstWand(player.getMainHandItem()) || AirburstItems.isAirburstWand(player.getOffhandItem());
    }

    private static void applyAirburstImpulse(ServerPlayer player, boolean reverse) {
        Entity target = player;
        double velocity = AirburstConfigs.airburstVelocity();
        Vec3 direction = player.getLookAngle().normalize();
        if (reverse) {
            direction = direction.scale(-1.0D);
        }

        if (player.isPassenger()) {
            MountedTarget mountedTarget = findMountedTarget(player);
            target = mountedTarget.entity();
            velocity = Math.max(0.0D, AirburstConfigs.mountedAirburstVelocity()
                    - AirburstConfigs.mountedAirburstChainPenalty() * mountedTarget.extraVehicleCount());
            queueMountedImpulseRetry(target, direction, target.getDeltaMovement().dot(direction) + velocity);
        }

        applyImpulse(target, direction.scale(velocity));
    }

    private static void applyImpulse(Entity target, Vec3 impulse) {
        target.push(impulse);
        target.hurtMarked = true;
    }

    private static MountedTarget findMountedTarget(ServerPlayer player) {
        Entity target = player.getVehicle();
        int extraVehicleCount = 0;

        while (target != null && target.isPassenger() && target.getVehicle() != null) {
            target = target.getVehicle();
            extraVehicleCount++;
        }

        return new MountedTarget(target, extraVehicleCount);
    }

    private static void queueMountedImpulseRetry(Entity target, Vec3 direction, double desiredProjection) {
        if (desiredProjection > 0.0D) {
            PENDING_MOUNTED_IMPULSES.put(target.getUUID(), new PendingMountedImpulse(direction, desiredProjection, MOUNTED_IMPULSE_RETRY_TICKS));
        }
    }

    @SubscribeEvent
    public static void restoreMountedImpulse(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }

        PendingMountedImpulse pending = PENDING_MOUNTED_IMPULSES.get(entity.getUUID());
        if (pending == null) {
            return;
        }

        double projection = entity.getDeltaMovement().dot(pending.direction());
        if (projection >= pending.desiredProjection() * MOUNTED_IMPULSE_RETRY_THRESHOLD) {
            PENDING_MOUNTED_IMPULSES.remove(entity.getUUID());
            return;
        }

        applyImpulse(entity, pending.direction().scale(pending.desiredProjection() - projection));
        if (pending.attemptsRemaining() <= 1) {
            PENDING_MOUNTED_IMPULSES.remove(entity.getUUID());
        } else {
            PENDING_MOUNTED_IMPULSES.put(entity.getUUID(), new PendingMountedImpulse(
                    pending.direction(),
                    pending.desiredProjection(),
                    pending.attemptsRemaining() - 1
            ));
        }
    }

    private static ItemStack findBacktankWithEnoughAir(Player player) {
        List<ItemStack> tanks = BacktankUtil.getAllWithAir(player);
        for (ItemStack tank : tanks) {
            if (BacktankUtil.getAir(tank) >= AirburstWandItem.AIRBURST_PRESSURE_COST) {
                return tank;
            }
        }
        return ItemStack.EMPTY;
    }

    private record MountedTarget(Entity entity, int extraVehicleCount) {
    }

    private record PendingMountedImpulse(Vec3 direction, double desiredProjection, int attemptsRemaining) {
    }
}
