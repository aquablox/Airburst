package com.aquablox.airburst.content;

import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripItem;
import net.minecraft.world.item.Item;

import java.util.function.Consumer;

public class AirburstWandItem extends ExtendoGripItem {
    public static final int AIRBURST_PRESSURE_COST = 10;
    public static final int DEFAULT_AIRBURST_COOLDOWN_TICKS = 10;
    public static final double DEFAULT_AIRBURST_VELOCITY = 1.2D;
    public static final double MOUNTED_AIRBURST_VELOCITY = 1.0D;
    public static final double MOUNTED_AIRBURST_CHAIN_PENALTY = 0.2D;

    public AirburstWandItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<net.neoforged.neoforge.client.extensions.common.IClientItemExtensions> consumer) {
        super.initializeClient(consumer);
    }
}
