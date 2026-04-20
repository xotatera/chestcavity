package net.tigereye.chestcavity.ui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory

class ChestCavityScreen(
    menu: ChestCavityMenu,
    inventory: Inventory,
    title: Component
) : AbstractContainerScreen<ChestCavityMenu>(menu, inventory, title) {

    companion object {
        private val TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/shulker_box.png")
    }

    override fun renderBg(graphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val x = (width - imageWidth) / 2
        val y = (height - imageHeight) / 2
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight)
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(graphics, mouseX, mouseY, partialTick)
        renderTooltip(graphics, mouseX, mouseY)
    }

    override fun init() {
        super.init()
        titleLabelX = (imageWidth - font.width(title)) / 2
    }
}
