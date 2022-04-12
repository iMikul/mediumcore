package net.momostudios.mediumcore.core.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;

public class MediumcoreDamageSources
{
    public static final DamageSource BLEED_OUT = new DamageSource("bleed_out")
            .setDamageBypassesArmor().setDamageAllowedInCreativeMode().setDamageIsAbsolute();

    public static EntityDamageSource gimpPlayerFrom(PlayerEntity player)
    {
        return new EntityDamageSource("gimp", player);
    }
}
