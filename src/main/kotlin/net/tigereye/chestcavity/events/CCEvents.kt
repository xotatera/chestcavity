package net.tigereye.chestcavity.events

import net.minecraft.tags.DamageTypeTags
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.LivingEntity
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent
import net.neoforged.neoforge.event.entity.living.MobEffectEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent
import net.tigereye.chestcavity.CCConfig
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.chestcavities.ChestCavityEntity
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstanceFactory
import net.tigereye.chestcavity.registration.CCOrganScores
import net.tigereye.chestcavity.util.ChestCavityUtil
import net.tigereye.chestcavity.util.OrganUtil
import kotlin.math.max
import kotlin.math.min

@EventBusSubscriber(modid = ChestCavity.MODID)
object CCEvents {

    @SubscribeEvent
    fun onEntityTick(event: EntityTickEvent.Post) {
        val entity = event.entity
        val cce = ChestCavityEntity.of(entity) ?: return
        val cc = cce.chestCavityInstance
        // Lazily resolve type after datapacks have loaded
        if (cc.type.defaultOrganScores.isEmpty() && entity is LivingEntity) {
            val resolved = ChestCavityInstanceFactory.resolveType(entity.type)
            if (resolved.defaultOrganScores.isNotEmpty()) {
                cc.type = resolved
                ChestCavityUtil.evaluateChestCavity(cc)
            }
        }
        ChestCavityUtil.onTick(cc)
    }

    // --- Damage: apply defenses to TARGET ---
    @SubscribeEvent
    fun onLivingDamage(event: LivingIncomingDamageEvent) {
        val target = event.entity
        val cce = ChestCavityEntity.of(target) ?: return
        val cc = cce.chestCavityInstance
        if (!cc.opened) return

        var damage = event.amount

        // Arrow dodging
        if (event.source.`is`(DamageTypeTags.IS_PROJECTILE)) {
            val dodge = cc.organScore(CCOrganScores.ARROW_DODGING)
            if (dodge > 0 && !target.hasEffect(net.tigereye.chestcavity.registration.CCStatusEffects.ARROW_DODGE_COOLDOWN)) {
                if (OrganUtil.teleportRandomly(target, CCConfig.ARROW_DODGE_DISTANCE.get() / dodge)) {
                    target.addEffect(net.minecraft.world.effect.MobEffectInstance(
                        net.tigereye.chestcavity.registration.CCStatusEffects.ARROW_DODGE_COOLDOWN,
                        (CCConfig.ARROW_DODGE_COOLDOWN.get() / dodge).toInt(), 0, false, false, true
                    ))
                    event.amount = 0f
                    return
                }
            }
        }

        // Bone defense
        if (!event.source.`is`(DamageTypeTags.BYPASSES_ARMOR)) {
            damage = ChestCavityUtil.applyBoneDefense(cc, damage)
        }

        // Fall damage reduction from leaping
        if (event.source.`is`(DamageTypes.FALL)) {
            damage = ChestCavityUtil.applyLeapingToFallDamage(cc, damage)
            damage = ChestCavityUtil.applyImpactResistant(cc, damage)
        }

        // Fly into wall
        if (event.source.`is`(DamageTypes.FLY_INTO_WALL)) {
            damage = ChestCavityUtil.applyImpactResistant(cc, damage)
        }

        // Fire resistance
        if (event.source.`is`(DamageTypeTags.IS_FIRE)) {
            damage = ChestCavityUtil.applyFireResistant(cc, damage)
        }

        event.amount = damage
    }

    // --- Damage: apply on-hit from ATTACKER ---
    @SubscribeEvent
    fun onLivingDamageAttacker(event: LivingDamageEvent.Post) {
        val source = event.source
        val attacker = source.entity as? LivingEntity ?: return
        val cce = ChestCavityEntity.of(attacker) ?: return
        val cc = cce.chestCavityInstance
        if (!cc.opened) return
        ChestCavityUtil.onHit(cc, source, event.entity, event.newDamage)
        applyLaunching(attacker, event.entity, cc)
    }

    private fun applyLaunching(attacker: LivingEntity, target: LivingEntity, cc: net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstance) {
        val diff = cc.organScore(CCOrganScores.LAUNCHING) - cc.type.getDefaultOrganScore(CCOrganScores.LAUNCHING)
        if (diff == 0f) return
        if (!attacker.closerThan(target, 4.0)) return
        val kbResist = target.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE)
        val upward = (CCConfig.LAUNCHING_POWER.get() * diff * (1.0 - kbResist)).coerceAtLeast(0.0)
        target.deltaMovement = target.deltaMovement.add(0.0, upward, 0.0)
    }

    @SubscribeEvent
    fun onLivingDeath(event: LivingDeathEvent) {
        val cce = ChestCavityEntity.of(event.entity) ?: return
        ChestCavityUtil.onDeath(cce)
    }

    @SubscribeEvent
    fun onPlayerClone(event: PlayerEvent.Clone) {
        val oldCce = ChestCavityEntity.of(event.original) ?: return
        val newCce = ChestCavityEntity.of(event.entity) ?: return
        if (CCConfig.KEEP_CHEST_CAVITY.get() || event.isWasDeath.not()) {
            newCce.chestCavityInstance.copyFrom(oldCce.chestCavityInstance)
        }
    }

    @SubscribeEvent
    fun onPlayerBreakSpeed(event: PlayerEvent.BreakSpeed) {
        val cce = ChestCavityEntity.of(event.entity) ?: return
        event.newSpeed = ChestCavityUtil.applyNervesToMining(cce.chestCavityInstance, event.newSpeed)
    }

    // --- Status effect modification (kidneys/liver/buff purging) ---
    private var modifyingEffect = false

    @SubscribeEvent
    fun onMobEffectAdded(event: MobEffectEvent.Added) {
        if (modifyingEffect) return
        val entity = event.entity as? LivingEntity ?: return
        val cce = ChestCavityEntity.of(entity) ?: return
        val cc = cce.chestCavityInstance
        if (!cc.opened) return

        val effect = event.effectInstance ?: return

        // Rotgut: suppress HUNGER effect if entity has rot digestion organs
        if (effect.effect == net.minecraft.world.effect.MobEffects.HUNGER) {
            val rotgut = cc.organScore(CCOrganScores.ROTGUT) + cc.organScore(CCOrganScores.ROT_DIGESTION)
            if (rotgut > 0) {
                modifyingEffect = true
                entity.removeEffect(effect.effect)
                modifyingEffect = false
                return
            }
        }

        val category = effect.effect.value().category
        var factor = 1f

        // Filtration (kidneys shorten harmful)
        if (category == net.minecraft.world.effect.MobEffectCategory.HARMFUL) {
            val filtration = cc.organScore(CCOrganScores.FILTRATION)
            val defaultFiltration = cc.type.getDefaultOrganScore(CCOrganScores.FILTRATION)
            if (filtration > defaultFiltration && defaultFiltration > 0) {
                factor *= (2f / (1f + filtration / defaultFiltration)).coerceIn(0.01f, 1f)
            }
            // Detoxification (liver)
            val detox = cc.organScore(CCOrganScores.DETOXIFICATION)
            val defaultDetox = cc.type.getDefaultOrganScore(CCOrganScores.DETOXIFICATION)
            if (detox > defaultDetox && detox > 0) {
                factor *= (2f / (1f + detox / defaultDetox)).coerceIn(0.01f, 1f)
            }
        }

        // Buff purging (withered bones shorten beneficial)
        if (category == net.minecraft.world.effect.MobEffectCategory.BENEFICIAL) {
            val buffPurge = cc.organScore(CCOrganScores.BUFF_PURGING)
            if (buffPurge > 0) {
                factor *= (1f / (1f + buffPurge * CCConfig.BUFF_PURGING_DURATION_FACTOR.get().toFloat())).coerceIn(0.01f, 1f)
            }
        }

        // Withered (shorten wither)
        if (effect.effect == net.minecraft.world.effect.MobEffects.WITHER) {
            val withered = cc.organScore(CCOrganScores.WITHERED)
            if (withered > 0) {
                factor *= (1f / (1f + withered * CCConfig.WITHERED_DURATION_FACTOR.get().toFloat())).coerceIn(0.01f, 1f)
            }
        }

        // Replace effect with shorter duration if modified
        if (factor < 1f) {
            val newDuration = (effect.duration * factor).toInt().coerceAtLeast(1)
            modifyingEffect = true
            entity.removeEffect(effect.effect)
            entity.addEffect(net.minecraft.world.effect.MobEffectInstance(
                effect.effect, newDuration, effect.amplifier,
                effect.isAmbient, effect.isVisible, effect.showIcon()
            ))
            modifyingEffect = false
        }
    }

    // --- Entity interaction (chest opener on mobs, milking silk, shearing silk) ---
    @SubscribeEvent
    fun onEntityInteract(event: PlayerInteractEvent.EntityInteract) {
        val player = event.entity
        val stack = player.getItemInHand(event.hand)
        val target = event.target as? LivingEntity ?: return

        // Chest opener
        val opener = stack.item as? net.tigereye.chestcavity.items.ChestOpener
        if (opener != null) {
            if (target is net.minecraft.world.entity.player.Player && !CCConfig.CAN_OPEN_OTHER_PLAYERS.get()) return
            opener.openChestCavity(player, target, shouldKnockback = true)
            event.cancellationResult = net.minecraft.world.InteractionResult.SUCCESS
            event.isCanceled = true
            return
        }

        // Shearing: if using shears on an entity with silk organs, produce silk
        if (stack.`is`(net.minecraft.world.item.Items.SHEARS)) {
            val cce = ChestCavityEntity.of(target) ?: return
            val cc = cce.chestCavityInstance
            if (!cc.opened) return
            val silk = cc.organScore(CCOrganScores.SILK)
            if (silk <= 0) return
            if (silk >= 2) {
                val cobweb = net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COBWEB, silk.toInt() / 2)
                target.spawnAtLocation(cobweb)
            }
            if (silk.toInt() % 2 >= 1) {
                target.spawnAtLocation(net.minecraft.world.item.Items.STRING)
            }
            return
        }

        // Milking: if using bucket on a cow-like entity, produce silk
        if (stack.`is`(net.minecraft.world.item.Items.BUCKET) && target is net.minecraft.world.entity.animal.Cow) {
            val cce = ChestCavityEntity.of(target) ?: return
            val cc = cce.chestCavityInstance
            if (!cc.opened) return
            val silk = cc.organScore(CCOrganScores.SILK)
            if (silk <= 0) return
            if (target.hasEffect(net.tigereye.chestcavity.registration.CCStatusEffects.SILK_COOLDOWN)) return
            net.tigereye.chestcavity.util.OrganUtil.spinWeb(target, cc, silk)
            target.addEffect(net.minecraft.world.effect.MobEffectInstance(
                net.tigereye.chestcavity.registration.CCStatusEffects.SILK_COOLDOWN,
                CCConfig.SILK_COOLDOWN.get(), 0, false, false, true
            ))
        }
    }

    // --- Jump height modification ---
    @SubscribeEvent
    fun onLivingJump(event: net.neoforged.neoforge.event.entity.living.LivingEvent.LivingJumpEvent) {
        val cce = ChestCavityEntity.of(event.entity) ?: return
        val cc = cce.chestCavityInstance
        if (!cc.opened) return
        val leaping = cc.organScore(CCOrganScores.LEAPING)
        val defaultLeaping = cc.type.getDefaultOrganScore(CCOrganScores.LEAPING)
        val diff = leaping - defaultLeaping
        if (diff == 0f) return
        val multiplier = max(0f, 1f + diff * 0.25f)
        val vel = event.entity.deltaMovement
        event.entity.deltaMovement = vel.add(0.0, vel.y * (multiplier - 1.0).toDouble(), 0.0)
    }

    // --- Breath/air modification ---
    @SubscribeEvent
    fun onLivingBreathe(event: net.neoforged.neoforge.event.entity.living.LivingBreatheEvent) {
        val entity = event.entity
        val cce = ChestCavityEntity.of(entity) ?: return
        val cc = cce.chestCavityInstance
        if (!cc.opened) return

        val defaultCapacity = cc.type.getDefaultOrganScore(CCOrganScores.BREATH_CAPACITY)
        val defaultWaterbreath = cc.type.getDefaultOrganScore(CCOrganScores.WATERBREATH)
        val defaultRecovery = cc.type.getDefaultOrganScore(CCOrganScores.BREATH_RECOVERY)
        val capacity = cc.organScore(CCOrganScores.BREATH_CAPACITY)
        val waterbreath = cc.organScore(CCOrganScores.WATERBREATH)
        val recovery = cc.organScore(CCOrganScores.BREATH_RECOVERY)

        // If nothing changed from defaults, don't interfere
        if (capacity == defaultCapacity && waterbreath == defaultWaterbreath && recovery == defaultRecovery) return

        // Underwater: waterbreath allows breathing, capacity slows air loss
        if (entity.isUnderWater) {
            var wb = waterbreath
            if (entity.isSprinting) wb /= 4f
            if (wb > 0) {
                // Can breathe underwater — prevent air loss and possibly recover
                event.setCanBreathe(true)
                if (wb >= 1f) event.refillAirAmount = event.refillAirAmount.coerceAtLeast(4)
            } else if (capacity != defaultCapacity) {
                // Modified lung capacity changes air loss rate
                val ratio = if (capacity > 0) min(2f / capacity, 20f) else 20f
                if (ratio > 1f) {
                    // Lose air faster
                    event.setConsumeAirAmount((event.consumeAirAmount * ratio).toInt().coerceAtLeast(1))
                } else if (ratio < 1f) {
                    // Lose air slower (better lungs)
                    event.setConsumeAirAmount((event.consumeAirAmount * ratio).toInt().coerceAtLeast(0))
                }
            }
        } else {
            // On land: breath recovery and possible suffocation for gill-only creatures
            var breath = recovery
            if (entity.isSprinting) breath /= 4f
            if (entity.isInWaterRainOrBubble) breath += waterbreath / 4f

            if (breath <= 0 && recovery < defaultRecovery) {
                // Can't breathe on land (gills only) — suffocate
                event.setCanBreathe(false)
                event.refillAirAmount = 0
            } else if (breath > defaultRecovery && defaultRecovery > 0) {
                // Extra breath recovery — refill faster
                event.setRefillAirAmount((event.refillAirAmount * breath / defaultRecovery).toInt().coerceAtLeast(1))
            }
        }
    }

    // --- Dimension change sync ---
    @SubscribeEvent
    fun onPlayerChangedDimension(event: PlayerEvent.PlayerChangedDimensionEvent) {
        val cce = ChestCavityEntity.of(event.entity) ?: return
        val cc = cce.chestCavityInstance
        if (event.entity is net.minecraft.server.level.ServerPlayer) {
            val payload = net.tigereye.chestcavity.registration.ChestCavityUpdatePayload(cc.opened, cc.organScores)
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                event.entity as net.minecraft.server.level.ServerPlayer, payload
            )
        }
    }

    // --- Commands ---
    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        CCCommands.register(event.dispatcher)
    }
}
