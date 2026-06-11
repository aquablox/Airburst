package com.aquablox.airburst.network;

import com.aquablox.airburst.Airburst;
import com.aquablox.airburst.content.AirburstAbility;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class AirburstPackets {
    public static void register(IEventBus modBus) {
        modBus.addListener(AirburstPackets::registerPayloads);
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Airburst.MOD_ID).versioned("1");
        registrar.playToServer(AirburstUsePayload.TYPE, AirburstUsePayload.STREAM_CODEC, AirburstPackets::handleAirburstUse);
    }

    private static void handleAirburstUse(AirburstUsePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                AirburstAbility.tryAirburst(player, payload.reverse());
            }
        });
    }

    public record AirburstUsePayload(boolean reverse) implements CustomPacketPayload {
        public static final ResourceLocation ID = Airburst.asResource("airburst_use");
        public static final Type<AirburstUsePayload> TYPE = new Type<>(ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, AirburstUsePayload> STREAM_CODEC =
                ByteBufCodecs.BOOL.map(AirburstUsePayload::new, AirburstUsePayload::reverse).cast();

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
