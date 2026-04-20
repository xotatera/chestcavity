package net.tigereye.chestcavity.registration

import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstance
import java.util.function.Supplier

object CCAttachments {
    val ATTACHMENT_TYPES: DeferredRegister<AttachmentType<*>> =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ChestCavity.MODID)

    val CHEST_CAVITY: Supplier<AttachmentType<ChestCavityInstance>> =
        ATTACHMENT_TYPES.register("chest_cavity") { ->
            AttachmentType.builder { _ -> null as ChestCavityInstance? }
                .build() as AttachmentType<ChestCavityInstance>
        }
}
