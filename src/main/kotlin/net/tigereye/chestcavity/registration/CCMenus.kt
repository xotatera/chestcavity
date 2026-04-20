package net.tigereye.chestcavity.registration

import net.minecraft.core.registries.Registries
import net.minecraft.world.inventory.MenuType
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.ui.ChestCavityMenu

object CCMenus {
    val MENUS: DeferredRegister<MenuType<*>> =
        DeferredRegister.create(Registries.MENU, ChestCavity.MODID)

    val CHEST_CAVITY: DeferredHolder<MenuType<*>, MenuType<ChestCavityMenu>> =
        MENUS.register("chest_cavity") { ->
            IMenuTypeExtension.create { windowId, playerInv, _ ->
                ChestCavityMenu(windowId, playerInv)
            }
        }
}
