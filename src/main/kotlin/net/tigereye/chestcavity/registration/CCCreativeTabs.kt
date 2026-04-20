package net.tigereye.chestcavity.registration

import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.tigereye.chestcavity.ChestCavity

object CCCreativeTabs {
    val CREATIVE_MODE_TABS: DeferredRegister<CreativeModeTab> =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ChestCavity.MODID)

    val ORGANS_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> =
        CREATIVE_MODE_TABS.register("organs") { ->
            CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.${ChestCavity.MODID}.organs"))
                .icon { CCItems.HUMAN_STOMACH.get().defaultInstance }
                .displayItems { _, output ->
                    CCItems.ITEMS.entries.forEach { output.accept(it.get()) }
                }
                .build()
        }
}
