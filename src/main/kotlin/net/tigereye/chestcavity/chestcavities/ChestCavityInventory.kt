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
        val list = ListTag()
        for (i in 0 until containerSize) {
            val stack = getItem(i)
            if (stack.isEmpty) continue
            val compound = stack.save(registryAccess) as? CompoundTag ?: continue
            compound.putByte("Slot", i.toByte())
            list.add(compound)
        }
        return list
    }

    override fun stillValid(player: Player): Boolean {
        val inst = instance ?: return true
        if (inst.owner.isDeadOrDying) return false
        return player.distanceTo(inst.owner) < 8
    }
}
