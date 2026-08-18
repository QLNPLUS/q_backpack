package com.qbackpack.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qbackpack.QBackpack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public final class BackpackRenderer implements ICurioRenderer {
    public static final ResourceLocation SMALL_MODEL = model("small_backpack");
    public static final ResourceLocation MEDIUM_MODEL = model("medium_backpack");
    public static final ResourceLocation LARGE_MODEL = model("large_backpack");
    public static final ResourceLocation HUGE_MODEL = model("huge_backpack");
    public static final ResourceLocation NETHERITE_MODEL = model("netherite_backpack");

    private final ResourceLocation modelLocation;

    public BackpackRenderer(ResourceLocation modelLocation) {
        this.modelLocation = modelLocation;
    }

    private static ResourceLocation model(String name) {
        return new ResourceLocation(QBackpack.MOD_ID, "wearable/" + name);
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack, SlotContext slotContext, PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffers, int light,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch) {
        if (!(renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getModelManager().getModel(modelLocation);

        poseStack.pushPose();
        humanoidModel.body.translateAndRotate(poseStack);
        poseStack.translate(0.0F, 5.0F / 16.0F, 2.0F / 16.0F);
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        itemRenderer.render(stack, ItemDisplayContext.NONE, false, poseStack, buffers,
                light, OverlayTexture.NO_OVERLAY, model);
        poseStack.popPose();
    }
}
