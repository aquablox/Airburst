package com.aquablox.airburst;

import com.mojang.logging.LogUtils;
import com.aquablox.airburst.config.AirburstConfigs;
import com.aquablox.airburst.network.AirburstPackets;
import com.aquablox.airburst.registry.AirburstCreativeTabs;
import com.aquablox.airburst.registry.AirburstItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Airburst.MOD_ID)
public class Airburst {
    public static final String MOD_ID = "airburst";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Airburst(IEventBus modBus, ModContainer modContainer) {
        AirburstConfigs.register(modContainer);
        AirburstItems.register(modBus);
        AirburstCreativeTabs.register(modBus);
        AirburstPackets.register(modBus);
    }

    public static net.minecraft.resources.ResourceLocation asResource(String path) {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
