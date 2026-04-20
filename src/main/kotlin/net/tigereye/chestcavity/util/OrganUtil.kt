package net.tigereye.chestcavity.util

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.AreaEffectCloud
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.targeting.TargetingConditions
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.*
import net.minecraft.world.item.Items
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.tigereye.chestcavity.CCConfig
import net.tigereye.chestcavity.chestcavities.ChestCavityEntity
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstance
import net.tigereye.chestcavity.registration.CCOrganScores
import net.tigereye.chestcavity.registration.CCStatusEffects
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object OrganUtil {

    fun explode(entity: LivingEntity, explosionYield: Float) {
        if (entity.level().isClientSide) return
        val destructionType = if (entity.level().gameRules.getBoolean(GameRules.RULE_MOBGRIEFING))
            Level.ExplosionInteraction.MOB
        else
            Level.ExplosionInteraction.NONE
        entity.level().explode(null, entity.x, entity.y, entity.z, sqrt(explosionYield.toDouble()).toFloat(), destructionType)
        spawnEffectsCloud(entity)
    }

    fun teleportRandomly(entity: LivingEntity, range: Float): Boolean {
        if (entity.level().isClientSide || !entity.isAlive) return false
        repeat(CCConfig.MAX_TELEPORT_ATTEMPTS.get()) {
            val x = entity.x + (entity.random.nextDouble() - 0.5) * range
            val y = max(1.0, entity.y + (entity.random.nextDouble() - 0.5) * range)
            val z = entity.z + (entity.random.nextDouble() - 0.5) * range
            if (teleportTo(entity, x, y, z)) return true
        }
        return false
    }

    fun teleportTo(entity: LivingEntity, x: Double, y: Double, z: Double): Boolean {
        if (entity.isPassenger) entity.stopRiding()
        val level = entity.level()
        val pos = BlockPos.MutableBlockPos(x.toInt(), y.toInt(), z.toInt())

        // Find ground
        while (pos.y > level.minBuildHeight && level.getBlockState(pos).isAir) {
            pos.move(Direction.DOWN)
        }
        if (pos.y <= level.minBuildHeight) return false

        // Find open space above ground
        pos.move(Direction.UP)
        while (!level.getBlockState(pos).isAir || !level.getBlockState(pos.above()).isAir) {
            pos.move(Direction.UP)
            if (pos.y >= level.maxBuildHeight) return false
        }

        entity.teleportTo(x, pos.y + 0.1, z)
        if (!entity.isSilent) {
            entity.level().playSound(null, entity.xOld, entity.yOld, entity.zOld,
                SoundEvents.ENDERMAN_TELEPORT, entity.soundSource, 1f, 1f)
            entity.playSound(SoundEvents.ENDERMAN_TELEPORT, 1f, 1f)
        }
        return true
    }

    // --- Projectile spawning ---

    fun spawnDragonBreath(entity: LivingEntity) {
        val cce = ChestCavityEntity.of(entity) ?: return
        val cc = cce.chestCavityInstance
        val breath = cc.organScore(CCOrganScores.DRAGON_BREATH)
        val range = sqrt((breath / 2).toDouble()) * 5
        val result = entity.pick(range, 0f, false)
        val pos = result.location
        var y = pos.y
        val mutable = BlockPos.MutableBlockPos(pos.x.toInt(), y.toInt(), pos.z.toInt())
        while (entity.level().getBlockState(mutable).isAir) {
            y--
            if (y < entity.level().minBuildHeight.toDouble()) return
            mutable.set(pos.x.toInt(), y.toInt(), pos.z.toInt())
        }
        y = (mutable.y + 1).toDouble()

        val cloud = AreaEffectCloud(entity.level(), pos.x, y, pos.z)
        cloud.owner = entity
        cloud.radius = min(range, entity.distanceTo(cloud).toDouble()).coerceAtLeast(range / 2).toFloat()
        cloud.duration = 200
        cloud.setParticle(ParticleTypes.DRAGON_BREATH)
        cloud.addEffect(MobEffectInstance(MobEffects.HARM))
        entity.level().addFreshEntity(cloud)
    }

    fun spawnDragonBomb(entity: LivingEntity) {
        val facing = entity.lookAngle.normalize()
        val fireball = DragonFireball(entity.level(), entity, facing)
        fireball.setPos(fireball.x, entity.getY(0.5) + 0.3, fireball.z)
        entity.level().addFreshEntity(fireball)
        entity.deltaMovement = entity.deltaMovement.add(facing.scale(-0.2))
    }

    fun spawnGhastlyFireball(entity: LivingEntity) {
        val facing = entity.lookAngle.normalize()
        val fireball = LargeFireball(entity.level(), entity, facing, 1)
        fireball.setPos(fireball.x, entity.getY(0.5) + 0.3, fireball.z)
        entity.level().addFreshEntity(fireball)
        entity.deltaMovement = entity.deltaMovement.add(facing.scale(-0.8))
    }

    fun spawnPyromancyFireball(entity: LivingEntity) {
        val facing = entity.lookAngle.normalize()
        val spread = Vec3(
            facing.x + entity.random.nextGaussian() * 0.1,
            facing.y,
            facing.z + entity.random.nextGaussian() * 0.1
        )
        val fireball = SmallFireball(entity.level(), entity, spread)
        fireball.setPos(fireball.x, entity.getY(0.5) + 0.3, fireball.z)
        entity.level().addFreshEntity(fireball)
        entity.deltaMovement = entity.deltaMovement.add(facing.scale(-0.2))
    }

    fun spawnShulkerBullet(entity: LivingEntity) {
        val range = CCConfig.SHULKER_BULLET_TARGETING_RANGE.get().toDouble()
        val target = entity.level().getNearestEntity(
            LivingEntity::class.java,
            TargetingConditions.forCombat().range(range * 2),
            entity, entity.x, entity.y, entity.z,
            AABB(
                entity.x - range, entity.y - range, entity.z - range,
                entity.x + range, entity.y + range, entity.z + range
            )
        ) ?: return
        val bullet = ShulkerBullet(entity.level(), entity, target, Direction.Axis.Y)
        bullet.setPos(bullet.x, entity.getY(0.5) + 0.3, bullet.z)
        entity.level().addFreshEntity(bullet)
    }

    fun spawnSpit(entity: LivingEntity) {
        val facing = entity.lookAngle.normalize()
        val llama = EntityType.LLAMA.create(entity.level()) ?: return
        llama.setPos(entity.x, entity.y, entity.z)
        llama.xRot = entity.xRot
        llama.yRot = entity.yRot
        llama.yBodyRot = entity.yBodyRot
        val spit = LlamaSpit(entity.level(), llama)
        spit.owner = entity
        spit.deltaMovement = facing.scale(2.0)
        entity.level().addFreshEntity(spit)
        entity.deltaMovement = entity.deltaMovement.add(facing.scale(-0.1))
    }

    // --- Silk ---

    fun spinWeb(entity: LivingEntity, cc: ChestCavityInstance, silkScore: Float): Boolean {
        val player = entity as? Player
        if (player != null && player.foodData.foodLevel < 6) return false

        var remaining = silkScore
        var hungerCost = 0

        if (remaining >= 2) {
            val pos = entity.blockPosition().relative(entity.direction.opposite)
            if (entity.level().getBlockState(pos).isAir) {
                val block = if (remaining >= 3) { remaining -= 3; hungerCost += 16; Blocks.WHITE_WOOL }
                else { remaining -= 2; hungerCost += 8; Blocks.COBWEB }
                entity.level().setBlock(pos, block.defaultBlockState(), 2)
            }
        }

        while (remaining >= 1) {
            remaining--
            hungerCost += 4
            cc.projectileQueue.add { e -> e.spawnAtLocation(Items.STRING) }
        }

        if (player != null) player.causeFoodExhaustion(hungerCost.toFloat())
        return hungerCost > 0
    }

    // --- Effect cloud ---

    private fun spawnEffectsCloud(entity: LivingEntity) {
        val effects = entity.activeEffects
        if (effects.isEmpty()) return
        val cloud = AreaEffectCloud(entity.level(), entity.x, entity.y, entity.z)
        cloud.radius = 2.5f
        cloud.radiusOnUse = -0.5f
        cloud.waitTime = 10
        cloud.duration = cloud.duration / 2
        cloud.radiusPerTick = -cloud.radius / cloud.duration.toFloat()
        effects.forEach { cloud.addEffect(MobEffectInstance(it)) }
        entity.level().addFreshEntity(cloud)
    }

    // --- Queuing helpers (used by activation listeners) ---

    fun queueDragonBombs(entity: LivingEntity, cc: ChestCavityInstance, count: Int) {
        if (entity is Player) entity.causeFoodExhaustion(count * 0.6f)
        repeat(count) { cc.projectileQueue.add(::spawnDragonBomb) }
        entity.addEffect(MobEffectInstance(
            CCStatusEffects.DRAGON_BOMB_COOLDOWN, CCConfig.DRAGON_BOMB_COOLDOWN.get(), 0, false, false, true
        ))
    }

    fun queueForcefulSpit(entity: LivingEntity, cc: ChestCavityInstance, count: Int) {
        if (entity is Player) entity.causeFoodExhaustion(count * 0.1f)
        repeat(count) { cc.projectileQueue.add(::spawnSpit) }
        entity.addEffect(MobEffectInstance(
            CCStatusEffects.FORCEFUL_SPIT_COOLDOWN, CCConfig.FORCEFUL_SPIT_COOLDOWN.get(), 0, false, false, true
        ))
    }

    fun queueGhastlyFireballs(entity: LivingEntity, cc: ChestCavityInstance, count: Int) {
        if (entity is Player) entity.causeFoodExhaustion(count * 0.3f)
        repeat(count) { cc.projectileQueue.add(::spawnGhastlyFireball) }
        entity.addEffect(MobEffectInstance(
            CCStatusEffects.GHASTLY_COOLDOWN, CCConfig.GHASTLY_COOLDOWN.get(), 0, false, false, true
        ))
    }

    fun queuePyromancyFireballs(entity: LivingEntity, cc: ChestCavityInstance, count: Int) {
        if (entity is Player) entity.causeFoodExhaustion(count * 0.1f)
        repeat(count) { cc.projectileQueue.add(::spawnPyromancyFireball) }
        entity.addEffect(MobEffectInstance(
            CCStatusEffects.PYROMANCY_COOLDOWN, CCConfig.PYROMANCY_COOLDOWN.get(), 0, false, false, true
        ))
    }

    fun queueShulkerBullets(entity: LivingEntity, cc: ChestCavityInstance, count: Int) {
        if (entity is Player) entity.causeFoodExhaustion(count * 0.3f)
        repeat(count) { cc.projectileQueue.add(::spawnShulkerBullet) }
        entity.addEffect(MobEffectInstance(
            CCStatusEffects.SHULKER_BULLET_COOLDOWN, CCConfig.SHULKER_BULLET_COOLDOWN.get(), 0, false, false, true
        ))
    }
}
