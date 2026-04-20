package net.tigereye.chestcavity.chestcavities.organs

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.item.ItemStack
import net.tigereye.chestcavity.ChestCavity
import java.io.InputStreamReader
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

object OrganManager : PreparableReloadListener {

    var organData: Map<ResourceLocation, OrganData> = emptyMap()
        private set

    override fun reload(
        preparationBarrier: PreparableReloadListener.PreparationBarrier,
        resourceManager: ResourceManager,
        preparationsProfiler: ProfilerFiller,
        reloadProfiler: ProfilerFiller,
        backgroundExecutor: Executor,
        gameExecutor: Executor
    ): CompletableFuture<Void> {
        return CompletableFuture.supplyAsync({ loadOrgans(resourceManager) }, backgroundExecutor)
            .thenCompose(preparationBarrier::wait)
            .thenAcceptAsync({ data ->
                organData = data
                ChestCavity.LOGGER.info("Loaded ${data.size} organs.")
            }, gameExecutor)
    }

    private fun loadOrgans(manager: ResourceManager): Map<ResourceLocation, OrganData> {
        ChestCavity.LOGGER.info("Loading organs.")
        val gson = Gson()
        return manager.listResources("organs") { it.path.endsWith(".json") }
            .mapNotNull { (id, resource) -> parseOrgan(id, resource, gson) }
            .toMap()
    }

    private fun parseOrgan(
        id: ResourceLocation,
        resource: net.minecraft.server.packs.resources.Resource,
        gson: Gson
    ): Pair<ResourceLocation, OrganData>? = runCatching {
        resource.open().use { stream ->
            val json = gson.fromJson(InputStreamReader(stream), JsonObject::class.java)
            val itemId = ResourceLocation.parse(json.get("itemID").asString)
            val pseudoOrgan = json.get("pseudoOrgan")?.asBoolean ?: false
            val scores = parseOrganScores(id, json.getAsJsonArray("organScores"))
            itemId to OrganData(pseudoOrgan, scores)
        }
    }.onFailure {
        ChestCavity.LOGGER.error("Error loading organ $id", it)
    }.getOrNull()

    private fun parseOrganScores(id: ResourceLocation, json: JsonArray?): Map<ResourceLocation, Float> {
        if (json == null) return emptyMap()
        return json.mapNotNull { element ->
            val obj = element.asJsonObject
            val scoreId = obj.get("id")?.asString ?: run {
                ChestCavity.LOGGER.error("Missing id in $id organ scores")
                return@mapNotNull null
            }
            val value = obj.get("value")?.asFloat ?: run {
                ChestCavity.LOGGER.error("Missing value in $id organ scores")
                return@mapNotNull null
            }
            ResourceLocation.parse(scoreId) to value
        }.toMap()
    }

    // --- Lookup ---

    fun hasEntry(stack: ItemStack): Boolean =
        organData.containsKey(itemId(stack))

    fun getEntry(stack: ItemStack): OrganData? =
        organData[itemId(stack)]

    fun readNbtOrganData(stack: ItemStack): OrganData? {
        // In 1.21.1, item NBT is replaced by DataComponents.
        // Custom organ data will use a custom DataComponent when fully implemented.
        // For now, return null (organs are looked up from the data-driven registry instead).
        return null
    }

    private fun itemId(stack: ItemStack): ResourceLocation =
        stack.item.builtInRegistryHolder().key().location()
}
