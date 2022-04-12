package net.momostudios.mediumcore.common.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.momostudios.mediumcore.common.capability.DeathCapability;
import net.momostudios.mediumcore.common.event.PlayerHandler;

public abstract class TotemItem extends Item
{
    public TotemItem(Properties properties)
    {
        super(properties);
    }

    public abstract int getHealAmount(int deaths);

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity entity, Hand hand)
    {
        entity.getCapability(DeathCapability.DEATHS, null).ifPresent(deaths ->
        {
            if (deaths.getDeaths() > 0)
            {
                if (!world.isRemote)
                {
                    int healAmount = Math.max(2, this.getHealAmount(deaths.getDeaths()));
                    entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(entity.getMaxHealth() + healAmount);
                    deaths.addDeaths(-healAmount / 2);
                    entity.getHeldItem(hand).shrink(1);
                    SoundEvent sound = new SoundEvent(new ResourceLocation("minecraft", "item.totem.use"));
                    entity.world.playSound(null, entity.getPosX(), entity.getPosY(), entity.getPosZ(), sound, entity.getSoundCategory(), 1.0F, 1.0F);
                }
                else
                {
                    PlayerHandler.showAnimation = true;
                    PlayerHandler.animationStack = new ItemStack(this);
                }

                PlayerHandler.updateDeathState(entity);
            }
        });
        return super.onItemRightClick(world, entity, hand);
    }
}
