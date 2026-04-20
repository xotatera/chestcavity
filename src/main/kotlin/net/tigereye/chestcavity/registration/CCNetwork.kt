package net.tigereye.chestcavity.registration

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.IPayloadContext
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.chestcavities.ChestCavityEntity

// --- Payloads ---

data class ChestCavityUpdatePayload(
    val opened: Boolean,
    val organScores: Map<ResourceLocation, Float>
) : CustomPacketPayload {
    companion object {
        val TYPE = CustomPacketPayload.Type<ChestCavityUpdatePayload>(
            ResourceLocation.fromNamespaceAndPath(ChestCavity.MODID, "update")
        )
        val CODEC: StreamCodec<FriendlyByteBuf, ChestCavityUpdatePayload> =
            StreamCodec.of(::encode, ::decode)

        private fun encode(buf: FriendlyByteBuf, payload: ChestCavityUpdatePayload) {
            buf.writeBoolean(payload.opened)
            buf.writeInt(payload.organScores.size)
            payload.organScores.forEach { (id, value) ->
                buf.writeResourceLocation(id)
                buf.writeFloat(value)
            }
        }

        private fun decode(buf: FriendlyByteBuf): ChestCavityUpdatePayload {
            val opened = buf.readBoolean()
            val count = buf.readInt()
            val scores = (0 until count).associate {
                buf.readResourceLocation() to buf.readFloat()
            }
            return ChestCavityUpdatePayload(opened, scores)
        }
    }

    override fun type() = TYPE
}

data class HotkeyPayload(
    val organScore: ResourceLocation
) : CustomPacketPayload {
    companion object {
        val TYPE = CustomPacketPayload.Type<HotkeyPayload>(
            ResourceLocation.fromNamespaceAndPath(ChestCavity.MODID, "hotkey")
        )
        val CODEC: StreamCodec<FriendlyByteBuf, HotkeyPayload> =
            StreamCodec.of(::encode, ::decode)

        private fun encode(buf: FriendlyByteBuf, payload: HotkeyPayload) {
            buf.writeResourceLocation(payload.organScore)
        }

        private fun decode(buf: FriendlyByteBuf): HotkeyPayload {
            return HotkeyPayload(buf.readResourceLocation())
        }
    }

    override fun type() = TYPE
}

// --- Registration ---

@EventBusSubscriber(modid = ChestCavity.MODID, bus = EventBusSubscriber.Bus.MOD)
object CCNetwork {

    @SubscribeEvent
    fun onRegisterPayloads(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar(ChestCavity.MODID)

        registrar.playToClient(
            ChestCavityUpdatePayload.TYPE,
            ChestCavityUpdatePayload.CODEC,
            ::handleUpdateOnClient
        )

        registrar.playToServer(
            HotkeyPayload.TYPE,
            HotkeyPayload.CODEC,
            ::handleHotkeyOnServer
        )
    }

    private fun handleUpdateOnClient(payload: ChestCavityUpdatePayload, ctx: IPayloadContext) {
        ctx.enqueueWork {
            val player = ctx.player()
            val cce = ChestCavityEntity.of(player) ?: return@enqueueWork
            val cc = cce.chestCavityInstance
            cc.opened = payload.opened
            cc.organScores = payload.organScores
        }
    }

    private fun handleHotkeyOnServer(payload: HotkeyPayload, ctx: IPayloadContext) {
        ctx.enqueueWork {
            val player = ctx.player()
            val cce = ChestCavityEntity.of(player) ?: return@enqueueWork
            net.tigereye.chestcavity.listeners.OrganActivationListeners.activate(
                payload.organScore, cce.chestCavityInstance
            )
        }
    }
}
