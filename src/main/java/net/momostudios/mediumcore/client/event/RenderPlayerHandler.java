package net.momostudios.mediumcore.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.mediumcore.common.capability.DeathCapability;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RenderPlayerHandler
{
    static boolean down = false;
    static boolean forcedPerspective = false;


    @SubscribeEvent
    public static void renderPlayerDown(RenderPlayerEvent.Pre event) {
        DeathCapability cap = event.getPlayer().getCapability(DeathCapability.DEATHS).orElse(new DeathCapability());
        if (cap.isDown()) {
            PoseStack ms = event.getPoseStack();
            ms.pushPose();
            ms.translate(0.0D, 1.5D, 0.0D);
            ms.scale(0.05F, 0.05F, 0.05F);
            ms.mulPose(Vector3f.YP.rotationDegrees(-Minecraft.getInstance().gameRenderer.getMainCamera().getYRot()));
            ms.mulPose(Vector3f.XP.rotationDegrees(Minecraft.getInstance().gameRenderer.getMainCamera().getXRot()));
            ms.mulPose(Vector3f.ZP.rotationDegrees(180));
            Font fontRenderer = event.getRenderer().getFont();
            fontRenderer.drawShadow(event.getPoseStack(), "DOWN", -fontRenderer.width("DOWN") / 2.0F, 0.0F, 14762306);
            String downSubtitle = cap.isBeingRevived() ? "Reviving" : ("" + cap.getDownTimeLeft());
            ms.scale(0.5F, 0.5F, 0.5F);
            ms.translate(0.0D, 20.0D, 0.0D);
            fontRenderer.drawShadow(event.getPoseStack(), downSubtitle, -fontRenderer.width(downSubtitle) / 2.0F, 0.0F, 16761927);
            ms.popPose();
            ms.pushPose();
            ms.mulPose(Vector3f.XP.rotationDegrees(90));
            (event.getPlayer()).yBodyRot = 180.0F;
            ms.translate(0.0D, -1.0D, -0.1D);
            down = true;
        }
    }

    @SubscribeEvent
    public static void renderPlayerDown(RenderPlayerEvent.Post event) {
        if (down) {
            event.getPoseStack().popPose();
            down = false;
        }
    }

    @SubscribeEvent
    public static void onChangeView(EntityViewRenderEvent.CameraSetup event) {
        DeathCapability cap = (Minecraft.getInstance()).player.getCapability(DeathCapability.DEATHS).orElse(new DeathCapability());
        if (cap.isDown()) {
            if ((Minecraft.getInstance()).options.getCameraType() != CameraType.THIRD_PERSON_BACK) {
                forcedPerspective = true;
                (Minecraft.getInstance()).options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }
            (Minecraft.getInstance()).player.yHeadRot = Math.max(120.0F, Math.min(Math.abs((Minecraft.getInstance()).gameRenderer.getMainCamera().getYRot() % 360.0F), 240.0F));
            if ((Minecraft.getInstance()).gameRenderer.getMainCamera().getXRot() < -45.0F) {
                (Minecraft.getInstance()).player.xRotO = -45.0F;
            } else if ((Minecraft.getInstance()).gameRenderer.getMainCamera().getXRot() > 45.0F) {
                (Minecraft.getInstance()).player.xRotO = 45.0F;
            }
            if (event.getPitch() < -45.0F) {
                event.setPitch(-45.0F);
            } else if (event.getPitch() > 45.0F) {
                event.setPitch(45.0F);
            }
        } else if (forcedPerspective) {
            (Minecraft.getInstance()).options.setCameraType(CameraType.FIRST_PERSON);
        }
    }
}
