package net.momostudios.mediumcore.common.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand)
    {
        entity.getCapability(DeathCapability.DEATHS, null).ifPresent(deaths ->
        {
            if (deaths.getDeaths() > 0)
            {
                if (!world.isClientSide)
                {
                    int healAmount = Math.max(2, this.getHealAmount(deaths.getDeaths()));
                    entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(entity.getMaxHealth() + healAmount);
                    deaths.addDeaths(-healAmount / 2);
                    entity.getItemInHand(hand).shrink(1);
                    SoundEvent sound = new SoundEvent(new ResourceLocation("minecraft", "item.totem.use"));
                    entity.level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, entity.getSoundSource(), 1.0F, 1.0F);
                }
                else
                {
                    PlayerHandler.showAnimation = true;
                    PlayerHandler.animationStack = new ItemStack(this);
                }

                PlayerHandler.updateDeathState(entity);
            }
        });
        return super.use(world, entity, hand);
    }
}
