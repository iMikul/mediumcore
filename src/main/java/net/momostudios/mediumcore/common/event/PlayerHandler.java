package net.momostudios.mediumcore.common.event;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.momostudios.mediumcore.common.capability.DeathCapability;
import net.momostudios.mediumcore.core.network.MediumcorePacketHandler;
import net.momostudios.mediumcore.core.network.message.DeathsSyncMessage;
import net.momostudios.mediumcore.core.util.MediumcoreDamageSources;

@Mod.EventBusSubscriber
public class PlayerHandler
{
    public static boolean showAnimation = false;
    public static ItemStack animationStack = ItemStack.EMPTY;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (showAnimation)
        {
            Minecraft.getInstance().gameRenderer.displayItemActivation(animationStack);
            showAnimation = false;
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            if (event.player.getPersistentData().getInt("resistanceTime") > 0)
            {
                event.player.getPersistentData().putInt("resistanceTime", event.player.getPersistentData().getInt("resistanceTime") - 1);
            }
            Player player = event.player;
            if (!player.level.isClientSide && player.tickCount % 20 == 0)
            {
                updateDeathState(player);
            }
            player.getCapability(DeathCapability.DEATHS).ifPresent(cap ->
            {
                cap.setSpectator(player.getMaxHealth() < 2 && cap.getDeaths() >= 10);

                if (cap.isDown())
                {
                    player.setPose(Pose.STANDING);
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();

                    if (player.isShiftKeyDown() && player.tickCount % 3 == 0)
                    {
                        cap.addDownTimeLeft(-1);
                    }

                    if (cap.getDownPos() != null)
                    {
                        if (player.isInWaterOrBubble())
                        {
                            player.setPos(cap.getDownPos().getX() + 0.5, player.getY(), cap.getDownPos().getZ() + 0.5);
                            double waterLevel = (player.level.getBlockState(player.blockPosition().above()).getBlock() != Blocks.WATER) ? (player.blockPosition().getY() + 1.4D - player.getY()) : ((player.blockPosition().getY() + 2) - player.getY());
                            player.push(0.0D, 0.01D * waterLevel, 0.0D);
                        }
                        else
                        {
                            player.setPos(cap.getDownPos().getX() + 0.5, player.getY(), cap.getDownPos().getZ() + 0.5);
                        }
                    }
                    player.setGlowingTag(true);
                    if (player.tickCount % 20 == 0)
                    {
                        boolean beingRevived = false;
                        AABB bb = new AABB(player.position().add(-1.5, -1.5, -1.5), player.position().add(1.5, 1.5, 1.5));
                        for (Entity entity : player.level.getEntities(player, bb))
                        {
                            if (entity instanceof Player && entity.isShiftKeyDown())
                            {
                                beingRevived = true;
                                break;
                            }
                        }
                        cap.setBeingRevived(beingRevived);

                        if (cap.getDownTimeLeft() <= 0)
                        {
                            if (!player.getAbilities().instabuild)
                            {
                                player.getAbilities().invulnerable = false;
                            }
                            player.onUpdateAbilities();
                            player.hurt(MediumcoreDamageSources.BLEED_OUT, Float.MAX_VALUE);
                        }
                        if (!cap.isBeingRevived() && !player.isShiftKeyDown())
                        {
                            cap.addDownTimeLeft(-1);
                        }
                    }
                }
                else
                {
                    boolean spec = cap.isSpectator() && !player.getAbilities().instabuild;
                    if (spec) player.getAbilities().flying = true;

                    if (player.tickCount % 20 == 0)
                    {
                        player.getAbilities().invulnerable = player.getAbilities().instabuild;
                        player.setGlowingTag(false);
                        //player.getAbilities().setFlySpeed(spec ? 0.03f : 0.045f);
                        player.getAbilities().mayBuild = !spec;
                        player.onUpdateAbilities();
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event)
    {
        if (event.getEntityLiving().getCapability(DeathCapability.DEATHS).map(DeathCapability::isDown).orElse(false))
        {
            event.setCanceled(true);
        }
    }

    // Prevent mining if down
    @SubscribeEvent
    public static void onPlayerMine(PlayerEvent.BreakSpeed event)
    {
        event.getPlayer().getCapability(DeathCapability.DEATHS).ifPresent(cap ->
        {
            if (cap.isDown() || cap.isSpectator())
            {
                event.setNewSpeed(0f);
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event)
    {
        if (event.getEntityLiving() instanceof Player && event.getEntityLiving().getPersistentData().getInt("resistanceTime") > 0
        && event.getSource() != DamageSource.OUT_OF_WORLD)
        {
            event.setCanceled(true);
        }
    }

    // Prevent attacking if down
    @SubscribeEvent
    public static void onPlayerAttack(LivingAttackEvent event)
    {
        if (event.getSource().getEntity() instanceof Player &&
        event.getSource().getEntity().getCapability(DeathCapability.DEATHS).map(DeathCapability::isDown).orElse(false))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.Clone event)
    {
        Player player = event.getPlayer();
        Player oldPlayer = event.getOriginal();
        oldPlayer.getCapability(DeathCapability.DEATHS).ifPresent(cap ->
        {
            player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(player.getMaxHealth() - cap.getDeaths() * 2);
            player.getCapability(DeathCapability.DEATHS).ifPresent(cap2 ->
            {
                cap2.setDeaths(cap.getDeaths());
            });
        });
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event)
    {
        if (event.getEntityLiving() instanceof Player player && !event.getEntityLiving().level.isClientSide)
        {
            player.getCapability(DeathCapability.DEATHS).ifPresent(cap ->
            {
                if (!cap.isDown() && !player.isCreative())
                {   event.setCanceled(true);
                    player.setHealth(1);
                    cap.setDown(true);
                    cap.setDownTimeLeft(60);
                    cap.setDownPos(player.blockPosition());
                    player.getAbilities().invulnerable = true;
                }
                updateDeathState(player);
            });
        }
    }

    @SubscribeEvent
    public static void revivePlayer(TickEvent.PlayerTickEvent event)
    {
        if (!event.player.level.isClientSide && event.phase == TickEvent.Phase.START
        && !event.player.getCapability(DeathCapability.DEATHS).map(DeathCapability::isDown).orElse(false))
        {
            AABB bb = new AABB(event.player.position().add(-1.5, -1.5, -1.5), event.player.position().add(1.5, 1.5, 1.5));
            for (Entity entity : event.player.level.getEntities(event.player, bb))
            {
                if (entity instanceof Player)
                {
                    Player downPlayer = (Player) entity;
                    if (event.player.isShiftKeyDown())
                    {
                        downPlayer.getCapability(DeathCapability.DEATHS).ifPresent(cap ->
                        {
                            if (cap.isDown() && (cap.getRevivingPlayer() == null || !cap.isBeingRevived() || cap.getRevivingPlayer() == event.player))
                            {
                                cap.setBeingRevived(true);
                                cap.setRevivingPlayer(event.player);
                                if (event.player.tickCount % 20 == 0)
                                {
                                    cap.setDownTimeLeft(Math.min(Math.max(50, cap.getDownTimeLeft() + 1), 60));
                                    updateDeathState(downPlayer);
                                }
                                if (cap.getDownTimeLeft() >= 60)
                                {
                                    downPlayer.getPersistentData().putInt("resistanceTime", 200);
                                    cap.setBeingRevived(false);
                                    cap.setDown(false);
                                    downPlayer.setGlowingTag(false);
                                    updateDeathState(downPlayer);
                                    downPlayer.getAbilities().invulnerable = downPlayer.getAbilities().instabuild;
                                }
                                TextComponent message = new TextComponent("Reviving... (§e" + Math.min(10, 60 - cap.getDownTimeLeft()) + " seconds left§r)");
                                downPlayer.displayClientMessage(message, true);
                                event.player.displayClientMessage(message, true);
                            }
                        });
                    }
                    else
                    {
                        downPlayer.getCapability(DeathCapability.DEATHS).ifPresent(cap -> cap.setBeingRevived(false));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickPlayer(PlayerInteractEvent.EntityInteractSpecific event)
    {
        if (event.getPlayer().getCapability(DeathCapability.DEATHS).map(DeathCapability::isDown).orElse(false))
        {
            event.setCanceled(true);
            return;
        }
        if (event.getTarget() instanceof Player && !event.getTarget().level.isClientSide)
        {
            Player target = (Player) event.getTarget();
            target.getCapability(DeathCapability.DEATHS).ifPresent(cap ->
            {
                if (cap.isDown())
                {
                    target.getAbilities().invulnerable = false;
                    target.onUpdateAbilities();
                    target.hurt(MediumcoreDamageSources.gimpPlayerFrom(event.getPlayer()), Float.MAX_VALUE);
                    target.level.playSound(null, target.blockPosition(), SoundEvents.GENERIC_BIG_FALL, SoundSource.PLAYERS, 1, 1);
                }
            });
        }
    }

    public static void updateDeathState(Player player)
    {
        if (!player.level.isClientSide)
        {
            player.getCapability(DeathCapability.DEATHS).ifPresent(cap ->
            {
                MediumcorePacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new DeathsSyncMessage(cap.getDeaths(), cap.isDown(), cap.getDownTimeLeft(), player.getUUID(), cap.isBeingRevived(), player.blockPosition()));
            });
        }
    }
}
