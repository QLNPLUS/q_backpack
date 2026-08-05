package com.qbackpack.client.render.model;

import com.qbackpack.QBackpack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public final class SmallBackpackModel extends AbstractBackpackModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(QBackpack.MOD_ID, "small_backpack"), "main");
    public static final ResourceLocation TEXTURE = new ResourceLocation(
            QBackpack.MOD_ID, "textures/entity/small_backpack.png");

    public SmallBackpackModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = createHumanoidMesh();
        PartDefinition body = mesh.getRoot().getChild("body");
        body.addOrReplaceChild("straps", CubeListBuilder.create().texOffs(24, 0)
                .addBox(-4.0F, 0.05F, -2.999F, 8.0F, 8.0F, 5.0F), PartPose.ZERO);
        body.addOrReplaceChild("fitting", CubeListBuilder.create().texOffs(50, 0)
                .addBox(-1.0F, 3.0F, 6.0F, 2.0F, 3.0F, 1.0F), PartPose.ZERO);
        body.addOrReplaceChild("backpack", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0F, 0.0F, 2.0F, 8.0F, 10.0F, 4.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }
}
