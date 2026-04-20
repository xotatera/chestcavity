package net.tigereye.chestcavity.chestcavities

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstance

class ChestCavityInventory(
    size: Int = 27,
    var instance: ChestCavityInstance? = null
) : SimpleContainer(size) {

    fun readTags(tags: ListTag) {
        val registryAccess = instance?.owner?.registryAccess() ?: return
        clearContent()
        for (j in 0 until tags.size) {
            val compound = tags.getCompound(j)
            val slot = compound.getByte("Slot").toInt() and 255
            if (slot !in 0 until containerSize) continue
            setItem(slot, ItemStack.parseOptional(registryAccess, compound))
        }
    }

    fun getTags(): ListTag {
        val registryAccess = instance?.owner?.registryAccess() ?: return ListTag()
        return (0 until containerSize)
            .map { i -> i to getItem(i) }
            .filter { (_, stack) -> !stack.isEmpty }
            .fold(ListTag()) { list, (i, stack) ->
                val compound = CompoundTag().apply { putByte("Slot", i.toByte()) }
                stack.save(registryAccess, compound)
                list.apply { add(compound) }
            }
    }

    override fun stillValid(player: Player): Boolean {
        val inst = instance ?: return true
        if (inst.owner.isDeadOrDying) return false
        return player.distanceTo(inst.owner) < 8
    }
}
