package com.aquablox.airburst.mixin;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripItemRenderer;
import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripRenderHandler;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.aquablox.airburst.client.AirburstWindChargeVisual;
import com.aquablox.airburst.registry.AirburstItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ExtendoGripItemRenderer.class, remap = false)
public class ExtendoGripItemRendererMixin {
    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/item/render/PartialItemModelRenderer;renderSolid(Lnet/minecraft/client/resources/model/BakedModel;I)V"
            )
    )
    private void airburst$replaceGripHand(
            PartialItemModelRenderer renderer,
            BakedModel model,
            int packedLight,
            ItemStack stack,
            CustomRenderedItemModel itemModel,
            PartialItemModelRenderer originalRenderer,
            net.minecraft.world.item.ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            int overlay
    ) {
        if (airburst$shouldRenderWindCharge(stack, displayContext) && airburst$isHandModel(model)) {
            AirburstWindChargeVisual.renderAtExtendoGripHand(poseStack, bufferSource, packedLight);
            return;
        }

        renderer.renderSolid(model, packedLight);
    }

    private static boolean airburst$isHandModel(BakedModel model) {
        return model == ExtendoGripRenderHandler.pose.get()
                || model == AllPartialModels.DEPLOYER_HAND_POINTING.get();
    }

    private static boolean airburst$shouldRenderWindCharge(ItemStack stack, ItemDisplayContext displayContext) {
        if (!AirburstItems.isAirburstWand(stack)) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return true;
        }

        boolean firstPerson = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        if (!firstPerson) {
            return true;
        }

        boolean mainHandWand = AirburstItems.isAirburstWand(minecraft.player.getMainHandItem());
        boolean offhandEmptyWand = AirburstItems.isAirburstWand(minecraft.player.getOffhandItem())
                && minecraft.player.getMainHandItem().isEmpty();
        return mainHandWand || offhandEmptyWand;
    }
}
