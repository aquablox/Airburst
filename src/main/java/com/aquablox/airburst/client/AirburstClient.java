package com.aquablox.airburst.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.aquablox.airburst.config.AirburstConfigs;
import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripRenderHandler;
import com.aquablox.airburst.network.AirburstPackets;
import com.aquablox.airburst.registry.AirburstItems;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class AirburstClient {
    public static final KeyMapping AIRBURST_KEY = new KeyMapping(
            "key.airburst.airburst",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.airburst"
    );
    public static final KeyMapping REVERSE_AIRBURST_KEY = new KeyMapping(
            "key.airburst.reverse_airburst",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.airburst"
    );

    @EventBusSubscriber(modid = com.aquablox.airburst.Airburst.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(AIRBURST_KEY);
            event.register(REVERSE_AIRBURST_KEY);
        }
    }

    @EventBusSubscriber(modid = com.aquablox.airburst.Airburst.MOD_ID, value = Dist.CLIENT)
    public static class ForgeBusEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                return;
            }

            while (AIRBURST_KEY.consumeClick()) {
                tryAirburst(minecraft, false);
            }

            while (REVERSE_AIRBURST_KEY.consumeClick()) {
                tryAirburst(minecraft, true);
            }
        }

        private static void tryAirburst(Minecraft minecraft, boolean reverse) {
            if (minecraft.player.getCooldowns().isOnCooldown(AirburstItems.AIRBURST_WAND.get())) {
                return;
            }

            if (AirburstItems.isAirburstWand(minecraft.player.getMainHandItem())
                || AirburstItems.isAirburstWand(minecraft.player.getOffhandItem())) {
                ExtendoGripRenderHandler.mainHandAnimation = 0.95F;
                applyControlledVehicleImpulse(minecraft.player, reverse);
                minecraft.player.getCooldowns().addCooldown(AirburstItems.AIRBURST_WAND.get(), AirburstConfigs.airburstCooldownTicks());
                PacketDistributor.sendToServer(new AirburstPackets.AirburstUsePayload(reverse));
            }
        }

        private static void applyControlledVehicleImpulse(Player player, boolean reverse) {
            if (!player.isPassenger()) {
                return;
            }

            MountedTarget mountedTarget = findMountedTarget(player);
            Entity target = mountedTarget.entity();
            if (target.getControllingPassenger() != player) {
                return;
            }

            double velocity = Math.max(0.0D, AirburstConfigs.mountedAirburstVelocity()
                    - AirburstConfigs.mountedAirburstChainPenalty() * mountedTarget.extraVehicleCount());
            Vec3 direction = player.getLookAngle().normalize();
            if (reverse) {
                direction = direction.scale(-1.0D);
            }

            target.push(direction.scale(velocity));
            target.hurtMarked = true;
        }

        private static MountedTarget findMountedTarget(Player player) {
            Entity target = player.getVehicle();
            int extraVehicleCount = 0;

            while (target != null && target.isPassenger() && target.getVehicle() != null) {
                target = target.getVehicle();
                extraVehicleCount++;
            }

            return new MountedTarget(target, extraVehicleCount);
        }

        private record MountedTarget(Entity entity, int extraVehicleCount) {
        }
    }
}
