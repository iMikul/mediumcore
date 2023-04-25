package net.momostudios.mediumcore.core.util;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.player.Player;

public class MediumcoreDamageSources
{
    public static final DamageSource BLEED_OUT = new DamageSource("bleed_out")
            .bypassArmor().bypassInvul().bypassMagic();

    public static DamageSource gimpPlayerFrom(Player player)
    {
        return new EntityDamageSource("gimp", player);
    }
}
