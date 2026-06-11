package com.aquablox.airburst.registry;

import com.aquablox.airburst.Airburst;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AirburstCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Airburst.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> AIRBURST = TABS.register(
            "airburst",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.airburst"))
                    .icon(() -> new ItemStack(AirburstItems.AIRBURST_WAND.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(AirburstItems.AIRBURST_WAND.get());
                    })
                    .build()
    );

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
