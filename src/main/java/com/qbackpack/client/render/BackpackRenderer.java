package com.qbackpack.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.function.Function;

public final class BackpackRenderer implements ICurioRenderer {
    private final HumanoidModel<LivingEntity> model;
    private final ResourceLocation texture;

    public BackpackRenderer(ModelLayerLocation layer, ResourceLocation texture,
                            Function<ModelPart, HumanoidModel<LivingEntity>> modelFactory) {
        this.model = modelFactory.apply(Minecraft.getInstance().getEntityModels().bakeLayer(layer));
        this.texture = texture;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack, SlotContext slotContext, PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffers, int light,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch) {
        renderLayerParent.getModel().copyPropertiesTo((EntityModel) model);
        model.setAllVisible(false);
        model.body.visible = true;

        VertexConsumer consumer = ItemRenderer.getArmorFoilBuffer(
                buffers, RenderType.armorCutoutNoCull(texture), false, stack.hasFoil());
        model.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
    }
}
