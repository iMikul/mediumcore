package net.momostudios.mediumcore.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Pose;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.mediumcore.common.capability.DeathCapability;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RenderPlayerHandler
{
    static boolean down = false;
    static boolean forcedPerspective = false;

    @SubscribeEvent
    public static void renderPlayerDown(RenderPlayerEvent.Pre event)
    {
        DeathCapability cap = event.getPlayer().getCapability(DeathCapability.DEATHS).orElse(new DeathCapability());
        if (cap.isDown())
        {
            MatrixStack ms = event.getMatrixStack();
            ms.push();
            ms.translate(0, 1.5, 0);
            ms.scale(0.05F, 0.05F, 0.05F);
            ms.rotate(Vector3f.YP.rotationDegrees(-Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getYaw()));
            ms.rotate(Vector3f.XP.rotationDegrees(Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getPitch()));
            ms.rotate(Vector3f.ZP.rotationDegrees(180));
            FontRenderer fontRenderer = event.getRenderer().getRenderManager().getFontRenderer();
            fontRenderer.drawStringWithShadow(event.getMatrixStack(), "DOWN", -fontRenderer.getStringWidth("DOWN") / 2f, 0, 14762306);

            String downSubtitle = cap.isBeingRevived() ? "Reviving" : "" + cap.getDownTimeLeft();
            ms.scale(0.5f, 0.5f, 0.5f);
            ms.translate(0, 20f, 0);
            fontRenderer.drawStringWithShadow(event.getMatrixStack(), downSubtitle, -fontRenderer.getStringWidth(downSubtitle) / 2f, 0, 16761927);

            ms.pop();

            ms.push();
            ms.rotate(Vector3f.XP.rotationDegrees(90));
            event.getPlayer().renderYawOffset = 180;
            ms.translate(0, -1, -0.1);
            down = true;
        }
    }

    @SubscribeEvent
    public static void renderPlayerDown(RenderPlayerEvent.Post event)
    {
        if (down)
        {
            event.getMatrixStack().pop();
            down = false;
        }
    }

    @SubscribeEvent
    public static void onChangeView(EntityViewRenderEvent.CameraSetup event)
    {
        DeathCapability cap = Minecraft.getInstance().player.getCapability(DeathCapability.DEATHS).orElse(new DeathCapability());

        if (cap.isDown())
        {
            if (Minecraft.getInstance().gameSettings.getPointOfView() != PointOfView.THIRD_PERSON_BACK)
            {
                forcedPerspective = true;
                Minecraft.getInstance().gameSettings.setPointOfView(PointOfView.THIRD_PERSON_BACK);
            }

            Minecraft.getInstance().player.rotationYawHead = Math.max(120, Math.min(Math.abs(Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getYaw() % 360), 240));
            if (Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getPitch() < -45)
            {
                Minecraft.getInstance().player.rotationPitch = -45;
            }
            else if (Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getPitch() > 45)
            {
                Minecraft.getInstance().player.rotationPitch = 45;
            }

            if (event.getPitch() < -45)
            {
                event.setPitch(-45);
            }
            else if (event.getPitch() > 45)
            {
                event.setPitch(45);
            }
        }
        else if (forcedPerspective)
        {
            Minecraft.getInstance().gameSettings.setPointOfView(PointOfView.FIRST_PERSON);
        }
    }
}
