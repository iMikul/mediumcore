package net.momostudios.mediumcore.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.mediumcore.common.capability.DeathCapability;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RenderPlayerHandler
{
    static boolean IS_DOWNED = false;
    static boolean PERSPECTIVE_CHANGED = false;


    @SubscribeEvent
    public static void renderPlayerDown(RenderPlayerEvent.Pre event)
    {
        DeathCapability cap = event.getEntity().getCapability(DeathCapability.DEATHS).orElse(new DeathCapability());

        if (cap.isDown())
        {
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

            // Render the player lying down
            ms.pushPose();
            ms.mulPose(Vector3f.XP.rotationDegrees(90));
            (event.getEntity()).yBodyRot = 180.0F;
            ms.translate(0.0D, -1.0D, -0.1D);
            IS_DOWNED = true;
            // Leave the matrix stack pushed to apply the transformation to the player
        }
    }

    @SubscribeEvent
    public static void renderPlayerDown(RenderPlayerEvent.Post event)
    {
        // Pop the matrix stack after the player is rendered
        if (IS_DOWNED)
        {   event.getPoseStack().popPose();
            IS_DOWNED = false;
        }
    }

    @SubscribeEvent
    public static void onChangeView(EntityViewRenderEvent.CameraSetup event) {
        DeathCapability cap = (Minecraft.getInstance()).player.getCapability(DeathCapability.DEATHS).orElse(new DeathCapability());
        if (cap.isDown())
        {
            if ((Minecraft.getInstance()).options.getCameraType() != CameraType.THIRD_PERSON_BACK)
            {   PERSPECTIVE_CHANGED = true;
                Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }

            // Limit head rotation so the player can't look behind themselves when downed
            Minecraft.getInstance().player.yHeadRot = Math.max(120.0F, Math.min(Math.abs((Minecraft.getInstance()).gameRenderer.getMainCamera().getYRot() % 360.0F), 240.0F));

            // Limit pitch so the player can't look up or down too far
            if (Minecraft.getInstance().gameRenderer.getMainCamera().getXRot() < -45.0F)
            {   Minecraft.getInstance().player.setXRot(-45.0F);
            }
            else if ((Minecraft.getInstance()).gameRenderer.getMainCamera().getXRot() > 45.0F)
            {   Minecraft.getInstance().player.setXRot(45.0F);
            }
        }
        else if (PERSPECTIVE_CHANGED)
        {   Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
        }
    }
}
