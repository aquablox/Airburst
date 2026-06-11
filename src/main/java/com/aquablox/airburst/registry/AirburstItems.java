package com.aquablox.airburst.registry;

import com.aquablox.airburst.Airburst;
import com.aquablox.airburst.content.AirburstWandItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AirburstItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Airburst.MOD_ID);

    public static final DeferredHolder<Item, AirburstWandItem> AIRBURST_WAND = ITEMS.register(
            "airburst_wand",
            () -> new AirburstWandItem(new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> INCOMPLETE_AIRBURST_WAND = ITEMS.register(
            "incomplete_airburst_wand",
            () -> new Item(new Item.Properties())
    );

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static boolean isAirburstWand(net.minecraft.world.item.ItemStack stack) {
        return stack.is(AIRBURST_WAND.get());
    }
}
