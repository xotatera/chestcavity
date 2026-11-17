package net.tigereye.chestcavity.events

import net.minecraft.tags.DamageTypeTags
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.LivingEntity
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent
import net.neoforged.neoforge.event.entity.living.MobEffectEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent
import net.tigereye.chestcavity.CCConfig
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.chestcavities.ChestCavityEntity
import net.tigereye.chestcavity.registration.CCOrganScores
import net.tigereye.chestcavity.util.ChestCavityUtil
import net.tigereye.chestcavity.util.OrganUtil
import kotlin.math.max
import kotlin.math.min

@EventBusSubscriber(modid = ChestCavity.MODID)
object CCEvents {

    /**
     * Deterministic initialisation point for a fresh body.
     *
     * The instance is created in the [net.minecraft.world.entity.LivingEntity] constructor, far too
     * early to resolve datapack types or to touch attributes. This event fires once the entity is
     * fully constructed and is being added to a level, which is the first moment the body exists as a
     * real thing — and unlike the tick path it does not depend on a player keeping the chunk alive,
     * so a summoned or scripted body is never left with an empty score map (L-01).
     */
    @SubscribeEvent
    fun onEntityJoinLevel(event: net.neoforged.neoforge.event.entity.EntityJoinLevelEvent) {
        if (event.level.isClientSide) return
        val cce = ChestCavityEntity.of(event.entity) ?: return
        val cc = cce.chestCavityInstance
        if (!cc.initialized) {
            ChestCavityUtil.initializeChestCavity(cc)
        }
    }

    @SubscribeEvent
    fun onEntityTick(event: EntityTickEvent.Post) {
        val entity = event.entity
        val cce = ChestCavityEntity.of(entity) ?: return
        val cc = cce.chestCavityInstance
        // Fallback for a body that reached the level before the datapack types were available.
        if (!cc.initialized) {
            ChestCavityUtil.initializeChestCavity(cc)
        }
        // A body that is plainly alive again was resurrected after a committed death (totem-like
        // effects, other mods): the next death is a new transaction and must commit on its own.
        if (cc.deathCommitted && !cc.owner.isDeadOrDying) {
            cc.deathCommitted = false
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

    /**
     * Death transaction entry point.
     *
     * Deliberately NOT on [LivingDeathEvent]: that event is cancellable and every listener may still
     * resurrect the entity, in which case nothing about the body may change (L-03 / A13). The drops
     * phase only runs after the death was approved, and it hands us the platform's own drop
     * collection, so the organs we shed are still subject to every later drop listener.
     */
    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST)
    fun onLivingDrops(event: net.neoforged.neoforge.event.entity.living.LivingDropsEvent) {
        val cce = ChestCavityEntity.of(event.entity) ?: return
        ChestCavityUtil.commitDeath(cce.chestCavityInstance, event.drops)
    }

    /**
     * Body hand-over on clone.
     *
     * The original always copied the instance here, for every clone path: the death transaction has
     * already decided what the new body looks like (rebuild under a fresh id, or keep the old one), and
     * the clone is what carries that decided body to the final respawn instance. Splitting this on
     * KEEP_CHEST_CAVITY here (as the fork did) drops the whole body — inventory, opened state, body id
     * and scores — on the default death path, which no later tick can repair (A-08 / L-01 / L-02).
     */
    @SubscribeEvent
    fun onPlayerClone(event: PlayerEvent.Clone) {
        val oldCce = ChestCavityEntity.of(event.original) ?: return
        val newCce = ChestCavityEntity.of(event.entity) ?: return
        // Copies the decided body (incl. its body id) and re-evaluates for the new owner (A-08).
        newCce.chestCavityInstance.copyFrom(oldCce.chestCavityInstance)
    }

    /**
     * After an actual respawn the client has just built a brand new player object, so the state it
     * holds is whatever the server sends now — not the packet that was sent during the clone (L-04).
     */
    @SubscribeEvent
    fun onPlayerRespawn(event: net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent) {
        val cce = ChestCavityEntity.of(event.entity) ?: return
        ChestCavityUtil.sendStateToClient(cce.chestCavityInstance)
    }

    @SubscribeEvent
    fun onPlayerBreakSpeed(event: PlayerEvent.BreakSpeed) {
        val cce = ChestCavityEntity.of(event.entity) ?: return
        event.newSpeed = ChestCavityUtil.applyNervesToMining(cce.chestCavityInstance, event.newSpeed)
    }

    // --- Status effect modification (rotgut/kidneys/liver/buff purging) ---
    // Rotgut suppression runs on MobEffectEvent.Applicable, which fires inside
    // addEffect BEFORE the instance is entered into activeEffects (L-10). A
    // cancelled application never reaches the effect map and addEffect then
    // correctly reports failure to the caller.
    //
    // Duration shortening no longer goes through Applicable + re-entry (R2): it
    // rewrites the instance via a mixin that swaps the addEffect parameter at
    // HEAD (the original mod did the same with ModifyVariable on
    // addStatusEffect). That keeps addEffect's return value true so /effect
    // still reports success, preserves the source entity, and lets vanilla's
    // own merge path handle the shortened instance. No re-entry flag exists
    // anymore, so there is nothing to go stale (R3).

    @SubscribeEvent
    fun onMobEffectApplicable(event: MobEffectEvent.Applicable) {
        val entity = event.entity as? LivingEntity ?: return
        val cce = ChestCavityEntity.of(entity) ?: return
        val cc = cce.chestCavityInstance
        if (!cc.opened) return

        val effect = event.effectInstance ?: return

        // Rotgut: suppress HUNGER effect if entity has rot digestion organs
        if (effect.effect == net.minecraft.world.effect.MobEffects.HUNGER) {
            val rotgut = cc.organScore(CCOrganScores.ROTGUT) + cc.organScore(CCOrganScores.ROT_DIGESTION)
            if (rotgut > 0) {
                event.result = MobEffectEvent.Applicable.Result.DO_NOT_APPLY
            }
        }
    }

    /**
     * Returns a shortened copy of [effect] for entities whose organs shorten
     * durations, or null when no shortening applies. Called from the
     * addEffect-parameter mixin; infinite durations are left untouched instead
     * of being cut to 1 tick.
     */
    fun shortenAppliedEffect(entity: LivingEntity, effect: net.minecraft.world.effect.MobEffectInstance): net.minecraft.world.effect.MobEffectInstance? {
        val cce = ChestCavityEntity.of(entity) ?: return null
        val cc = cce.chestCavityInstance
        if (!cc.opened) return null

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

        if (factor >= 1f || effect.isInfiniteDuration) return null
        val newDuration = (effect.duration * factor).toInt().coerceAtLeast(1)

        // MobEffectInstance has no public duration setter, so rebuild it from
        // its serialized form (same shape collectFurnaceDurations reads) to
        // keep amplifier, visibility flags and the hidden-effect chain intact.
        val tag = effect.save() as? net.minecraft.nbt.CompoundTag
        if (tag != null) {
            tag.putInt("duration", newDuration)
            val parsed = net.minecraft.world.effect.MobEffectInstance.CODEC
                .parse(net.minecraft.nbt.NbtOps.INSTANCE, tag).result().orElse(null)
            if (parsed != null) return parsed
        }
        return net.minecraft.world.effect.MobEffectInstance(
            effect.effect, newDuration, effect.amplifier,
            effect.isAmbient, effect.isVisible, effect.showIcon()
        )
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
