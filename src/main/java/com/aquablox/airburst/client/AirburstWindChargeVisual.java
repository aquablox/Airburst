package com.aquablox.airburst.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.WindChargeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class AirburstWindChargeVisual {
    private static final ResourceLocation WIND_CHARGE_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/projectiles/wind_charge.png");

    private static WindChargeModel model;

    private AirburstWindChargeVisual() {
    }

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        render(poseStack, bufferSource, packedLight, 0.16F, 0.0F, 0.0F, 0.08F);
    }

    public static void renderAtExtendoGripHand(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        render(poseStack, bufferSource, packedLight, 1.0F, 0.0F, 0.28F, 0.82F);
    }

    private static void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
            float scale, float x, float y, float z) {
        Minecraft minecraft = Minecraft.getInstance();
        if (model == null) {
            model = new WindChargeModel(minecraft.getEntityModels().bakeLayer(ModelLayers.WIND_CHARGE));
        }

        float age = minecraft.player != null
                ? minecraft.player.tickCount + minecraft.getTimer().getGameTimeDeltaPartialTick(false)
                : (System.currentTimeMillis() % 20000L) / 50.0F;
        model.setupAnim(null, 0.0F, 0.0F, age, 0.0F, 0.0F);

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(scale, scale, scale);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.breezeWind(WIND_CHARGE_TEXTURE, age * 0.03F % 1.0F, 0.0F));
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
