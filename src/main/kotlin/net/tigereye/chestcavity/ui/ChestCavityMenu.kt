package net.tigereye.chestcavity.ui

import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.tigereye.chestcavity.chestcavities.ChestCavityInventory
import net.tigereye.chestcavity.registration.CCMenus

class ChestCavityMenu(
    containerId: Int,
    playerInventory: Inventory,
    val chestInventory: ChestCavityInventory = ChestCavityInventory()
) : AbstractContainerMenu(CCMenus.CHEST_CAVITY.get(), containerId) {

    private val rows = (chestInventory.containerSize - 1) / 9 + 1

    init {
        chestInventory.startOpen(playerInventory.player)
        val yOffset = (rows - 4) * 18

        // Chest cavity slots
        for (row in 0 until rows) {
            for (col in 0 until 9) {
                val index = col + row * 9
                if (index >= chestInventory.containerSize) break
                addSlot(Slot(chestInventory, index, 8 + col * 18, 18 + row * 18))
            }
        }

        // Player inventory
        for (row in 0 until 3) {
            for (col in 0 until 9) {
                addSlot(Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 102 + row * 18 + yOffset))
            }
        }

        // Player hotbar
        for (col in 0 until 9) {
            addSlot(Slot(playerInventory, col, 8 + col * 18, 160 + yOffset))
        }
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        val slot = slots.getOrNull(index) ?: return ItemStack.EMPTY
        if (!slot.hasItem()) return ItemStack.EMPTY

        val stack = slot.item
        val result = stack.copy()

        val moved = if (index < chestInventory.containerSize)
            moveItemStackTo(stack, chestInventory.containerSize, slots.size, true)
        else
            moveItemStackTo(stack, 0, chestInventory.containerSize, false)

        if (!moved) return ItemStack.EMPTY

        if (stack.isEmpty) slot.setByPlayer(ItemStack.EMPTY) else slot.setChanged()
        return result
    }

    override fun stillValid(player: Player): Boolean =
        chestInventory.stillValid(player)

    override fun removed(player: Player) {
        super.removed(player)
        chestInventory.stopOpen(player)
    }
}
