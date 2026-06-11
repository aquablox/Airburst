package com.aquablox.airburst.content;

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

import java.util.List;

public class AirburstAbility {
    public static final String FALL_GRACE_TICKS_TAG = "AirburstFallGraceTicks";
    public static final String LAST_Y_VELOCITY_TAG = "AirburstLastYVelocity";
    public static final int FALL_GRACE_TICKS = 100;

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

        if (player.isPassenger()) {
            MountedTarget mountedTarget = findMountedTarget(player);
            target = mountedTarget.entity();
            velocity = Math.max(0.0D, AirburstConfigs.mountedAirburstVelocity()
                    - AirburstConfigs.mountedAirburstChainPenalty() * mountedTarget.extraVehicleCount());
        }

        Vec3 impulse = player.getLookAngle().normalize().scale(reverse ? -velocity : velocity);
        target.setDeltaMovement(target.getDeltaMovement().add(impulse));
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
}
