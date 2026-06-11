package com.aquablox.airburst.mixin;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.aquablox.airburst.registry.AirburstItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ExtendoGripItem.class, remap = false)
public class ExtendoGripItemMixin {
    @Redirect(
            method = {
                    "holdingExtendoGripIncreasesRange",
                    "findAndDamageExtendoGrip",
                    "isHoldingExtendoGrip"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tterrag/registrate/util/entry/ItemEntry;isIn(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private static boolean airburst$acceptAirburstWand(ItemEntry<?> entry, ItemStack stack) {
        return entry.isIn(stack) || entry == AllItems.EXTENDO_GRIP && AirburstItems.isAirburstWand(stack);
    }
}
