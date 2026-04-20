package net.tigereye.chestcavity.registration

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.tigereye.chestcavity.ChestCavity
import java.util.UUID

object CCDataComponents {
    val DATA_COMPONENTS: DeferredRegister.DataComponents =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ChestCavity.MODID)

    // Organ score data stored on organ items
    val ORGAN_DATA: DeferredHolder<DataComponentType<*>, DataComponentType<OrganItemData>> =
        DATA_COMPONENTS.registerComponentType("organ_data") { builder ->
            builder.persistent(OrganItemData.CODEC).networkSynchronized(OrganItemData.STREAM_CODEC)
        }

    // Compatibility/ownership tag on organs
    val ORGAN_COMPATIBILITY: DeferredHolder<DataComponentType<*>, DataComponentType<OrganCompatibility>> =
        DATA_COMPONENTS.registerComponentType("organ_compatibility") { builder ->
            builder.persistent(OrganCompatibility.CODEC).networkSynchronized(OrganCompatibility.STREAM_CODEC)
        }
}

data class OrganItemData(
    val pseudoOrgan: Boolean = false,
    val organScores: Map<String, Float> = emptyMap()
) {
    companion object {
        val CODEC: Codec<OrganItemData> = RecordCodecBuilder.create { builder ->
            builder.group(
                Codec.BOOL.optionalFieldOf("pseudoOrgan", false).forGetter { it.pseudoOrgan },
                Codec.unboundedMap(Codec.STRING, Codec.FLOAT).optionalFieldOf("organScores", emptyMap()).forGetter { it.organScores }
            ).apply(builder, ::OrganItemData)
        }

        val STREAM_CODEC: StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, OrganItemData> =
            StreamCodec.of(
                { buf, data ->
                    buf.writeBoolean(data.pseudoOrgan)
                    buf.writeInt(data.organScores.size)
                    data.organScores.forEach { (k, v) -> buf.writeUtf(k); buf.writeFloat(v) }
                },
                { buf ->
                    val pseudo = buf.readBoolean()
                    val count = buf.readInt()
                    val scores = (0 until count).associate { buf.readUtf() to buf.readFloat() }
                    OrganItemData(pseudo, scores)
                }
            )
    }

    fun toOrganData(): net.tigereye.chestcavity.chestcavities.organs.OrganData =
        net.tigereye.chestcavity.chestcavities.organs.OrganData(
            pseudoOrgan = pseudoOrgan,
            organScores = organScores.mapKeys { (k, _) -> ResourceLocation.parse(k) }
        )
}

data class OrganCompatibility(
    val ownerUuid: UUID,
    val ownerName: String = ""
) {
    companion object {
        val CODEC: Codec<OrganCompatibility> = RecordCodecBuilder.create { builder ->
            builder.group(
                net.minecraft.core.UUIDUtil.CODEC.fieldOf("owner").forGetter { it.ownerUuid },
                Codec.STRING.optionalFieldOf("name", "").forGetter { it.ownerName }
            ).apply(builder, ::OrganCompatibility)
        }

        val STREAM_CODEC: StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, OrganCompatibility> =
            StreamCodec.of(
                { buf, data -> buf.writeUUID(data.ownerUuid); buf.writeUtf(data.ownerName) },
                { buf -> OrganCompatibility(buf.readUUID(), buf.readUtf()) }
            )
    }
}
