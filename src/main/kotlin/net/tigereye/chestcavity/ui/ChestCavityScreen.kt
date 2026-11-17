package net.tigereye.chestcavity.ui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import net.tigereye.chestcavity.ChestCavity

class ChestCavityScreen(
    menu: ChestCavityMenu,
    inventory: Inventory,
    title: Component
) : AbstractContainerScreen<ChestCavityMenu>(menu, inventory, title) {

    companion object {
        /**
         * The chest cavity's own background. The mod does not ship one, so a resource pack
         * or modpack can give the chest cavity screen its own look without touching the
         * vanilla shulker box texture.
         */
        private val TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ChestCavity.MODID, "textures/gui/container/chest_cavity.png"
        )

        /** Used when no pack provides [TEXTURE]: the vanilla shulker box background, as before. */
        private val FALLBACK_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/shulker_box.png")
    }

    private var background = FALLBACK_TEXTURE

    override fun renderBg(graphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val x = (width - imageWidth) / 2
        val y = (height - imageHeight) / 2
        graphics.blit(background, x, y, 0, 0, imageWidth, imageHeight)
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(graphics, mouseX, mouseY, partialTick)
        renderTooltip(graphics, mouseX, mouseY)
    }

    override fun init() {
        super.init()
        titleLabelX = (imageWidth - font.width(title)) / 2
        // Resolved on every open, so a resource pack reload takes effect without a restart.
        background = if (minecraft?.resourceManager?.getResource(TEXTURE)?.isPresent == true) {
            TEXTURE
        } else {
            FALLBACK_TEXTURE
        }
    }
}
