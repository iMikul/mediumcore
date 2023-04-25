package net.momostudios.mediumcore.core.init;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.momostudios.mediumcore.Mediumcore;
import net.momostudios.mediumcore.common.item.TotemItem;

public class ItemInit
{
    public static DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Mediumcore.MOD_ID);

    public static final RegistryObject<Item> REBIRTH_TOTEM = ITEMS.register("totem_of_rebirth", () -> new TotemItem(
    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).tab(Mediumcore.MEDIUMCORE_GROUP))
    {
        @Override
        public int getHealAmount(int deaths)
        {
            return deaths * 2;
        }
    });

    public static final RegistryObject<Item> VITALITY_TOTEM = ITEMS.register("totem_of_vitality", () -> new TotemItem(
    new Item.Properties().stacksTo(16).rarity(Rarity.RARE).tab(Mediumcore.MEDIUMCORE_GROUP))
    {
        @Override
        public int getHealAmount(int deaths)
        {
            return 2;
        }
    });
}
