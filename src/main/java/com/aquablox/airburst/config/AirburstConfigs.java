package com.aquablox.airburst.config;

import com.aquablox.airburst.content.AirburstWandItem;
import net.createmod.catnip.config.ConfigBase;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class AirburstConfigs {
    private static final Map<ModConfig.Type, ConfigBase> CONFIGS = new EnumMap<>(ModConfig.Type.class);

    private static CCommon common;
    private static CServer server;

    private AirburstConfigs() {
    }

    public static CCommon common() {
        return common;
    }

    public static CServer server() {
        return server;
    }

    public static int airburstCooldownTicks() {
        if (server != null) {
            return server.airburstCooldownTicks.get();
        }
        if (common != null) {
            return common.defaultAirburstCooldownTicks.get();
        }
        return AirburstWandItem.DEFAULT_AIRBURST_COOLDOWN_TICKS;
    }

    public static float airburstVelocity() {
        if (server != null) {
            return server.airburstVelocity.getF();
        }
        if (common != null) {
            return common.defaultAirburstVelocity.getF();
        }
        return (float) AirburstWandItem.DEFAULT_AIRBURST_VELOCITY;
    }

    public static float mountedAirburstVelocity() {
        if (server != null) {
            return server.mountedAirburstVelocity.getF();
        }
        if (common != null) {
            return common.defaultMountedAirburstVelocity.getF();
        }
        return (float) AirburstWandItem.MOUNTED_AIRBURST_VELOCITY;
    }

    public static float mountedAirburstChainPenalty() {
        if (server != null) {
            return server.mountedAirburstChainPenalty.getF();
        }
        if (common != null) {
            return common.defaultMountedAirburstChainPenalty.getF();
        }
        return (float) AirburstWandItem.MOUNTED_AIRBURST_CHAIN_PENALTY;
    }

    public static void register(ModContainer modContainer) {
        common = register(CCommon::new, ModConfig.Type.COMMON);
        server = register(CServer::new, ModConfig.Type.SERVER);

        for (Map.Entry<ModConfig.Type, ConfigBase> entry : CONFIGS.entrySet()) {
            modContainer.registerConfig(entry.getKey(), entry.getValue().specification);
        }
    }

    private static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type side) {
        Pair<T, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(builder -> {
            T config = factory.get();
            config.registerAll(builder);
            return config;
        });

        T config = pair.getLeft();
        config.specification = pair.getRight();
        CONFIGS.put(side, config);
        return config;
    }

    public static class CCommon extends ConfigBase {
        public final ConfigInt defaultAirburstCooldownTicks = i(AirburstWandItem.DEFAULT_AIRBURST_COOLDOWN_TICKS, 0, 200,
                "defaultAirburstCooldownTicks",
                "Default Airburst Wand cooldown, in ticks, used before a world-specific server config overrides it.");
        public final ConfigFloat defaultAirburstVelocity = f((float) AirburstWandItem.DEFAULT_AIRBURST_VELOCITY, 0.0F, 10.0F,
                "defaultAirburstVelocity",
                "Default Airburst Wand launch velocity, in blocks per tick, used before a world-specific server config overrides it.");
        public final ConfigFloat defaultMountedAirburstVelocity = f((float) AirburstWandItem.MOUNTED_AIRBURST_VELOCITY, 0.0F, 10.0F,
                "defaultMountedAirburstVelocity",
                "Default Airburst Wand launch velocity while riding, in blocks per tick, used before a world-specific server config overrides it.");
        public final ConfigFloat defaultMountedAirburstChainPenalty = f((float) AirburstWandItem.MOUNTED_AIRBURST_CHAIN_PENALTY, 0.0F, 10.0F,
                "defaultMountedAirburstChainPenalty",
                "Default velocity subtracted for each extra vehicle in a riding chain, used before a world-specific server config overrides it.");

        @Override
        public String getName() {
            return "common";
        }
    }

    public static class CServer extends ConfigBase {
        public final ConfigInt airburstCooldownTicks = i(AirburstWandItem.DEFAULT_AIRBURST_COOLDOWN_TICKS, 0, 200,
                "airburstCooldownTicks",
                "Cooldown applied after using the Airburst Wand, in ticks.");
        public final ConfigFloat airburstVelocity = f((float) AirburstWandItem.DEFAULT_AIRBURST_VELOCITY, 0.0F, 10.0F,
                "airburstVelocity",
                "Velocity added by an Airburst Wand use, in blocks per tick.");
        public final ConfigFloat mountedAirburstVelocity = f((float) AirburstWandItem.MOUNTED_AIRBURST_VELOCITY, 0.0F, 10.0F,
                "mountedAirburstVelocity",
                "Velocity added by an Airburst Wand use while riding, in blocks per tick.");
        public final ConfigFloat mountedAirburstChainPenalty = f((float) AirburstWandItem.MOUNTED_AIRBURST_CHAIN_PENALTY, 0.0F, 10.0F,
                "mountedAirburstChainPenalty",
                "Velocity subtracted for each extra vehicle in a riding chain.");

        @Override
        public String getName() {
            return "server";
        }
    }
}
