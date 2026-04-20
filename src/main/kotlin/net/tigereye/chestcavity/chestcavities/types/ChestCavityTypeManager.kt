package net.tigereye.chestcavity.chestcavities.types

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.Resource
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.chestcavities.ChestCavityInventory
import java.io.InputStreamReader
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

object ChestCavityTypeManager : PreparableReloadListener {

    var types: Map<ResourceLocation, GeneratedChestCavityType> = emptyMap()
        private set
    var assignments: Map<ResourceLocation, ResourceLocation> = emptyMap()
        private set

    override fun reload(
        barrier: PreparableReloadListener.PreparationBarrier,
        resourceManager: ResourceManager,
        preparationsProfiler: ProfilerFiller,
        reloadProfiler: ProfilerFiller,
        backgroundExecutor: Executor,
        gameExecutor: Executor
    ): CompletableFuture<Void> {
        return CompletableFuture.supplyAsync({ load(resourceManager) }, backgroundExecutor)
            .thenCompose(barrier::wait)
            .thenAcceptAsync({ (loadedTypes, loadedAssignments) ->
                types = loadedTypes
                assignments = loadedAssignments
                // Invalidate cached default scores so they recompute with fresh OrganManager data
                types.values.forEach { it.invalidateCache() }
                ChestCavity.LOGGER.info("Loaded ${types.size} chest cavity types, ${assignments.size} assignments.")
            }, gameExecutor)
    }

    private fun load(manager: ResourceManager): Pair<Map<ResourceLocation, GeneratedChestCavityType>, Map<ResourceLocation, ResourceLocation>> {
        val gson = Gson()
        val loadedTypes = manager.listResources("types") { it.path.endsWith(".json") }
            .mapNotNull { (id, resource) -> parseType(id, resource, gson) }
            .toMap()

        val loadedAssignments = manager.listResources("entity_assignment") { it.path.endsWith(".json") }
            .flatMap { (id, resource) -> parseAssignment(id, resource, gson) }
            .toMap()

        return loadedTypes to loadedAssignments
    }

    private fun parseType(id: ResourceLocation, resource: Resource, gson: Gson): Pair<ResourceLocation, GeneratedChestCavityType>? =
        runCatching {
            resource.open().use { stream ->
                val json = gson.fromJson(InputStreamReader(stream), JsonObject::class.java)
                id to deserializeType(id, json)
            }
        }.onFailure { ChestCavity.LOGGER.error("Error loading chest cavity type $id", it) }.getOrNull()

    private fun parseAssignment(id: ResourceLocation, resource: Resource, gson: Gson): List<Pair<ResourceLocation, ResourceLocation>> =
        runCatching {
            resource.open().use { stream ->
                val json = gson.fromJson(InputStreamReader(stream), JsonObject::class.java)
                val typeId = ResourceLocation.parse(json.get("chestcavity").asString)
                json.getAsJsonArray("entities").map { entry ->
                    ResourceLocation.parse(entry.asString) to typeId
                }
            }
        }.onFailure { ChestCavity.LOGGER.error("Error loading assignment $id", it) }.getOrDefault(emptyList())

    private fun deserializeType(id: ResourceLocation, json: JsonObject): GeneratedChestCavityType {
        val cct = GeneratedChestCavityType()
        val forbiddenSlots = json.getAsJsonArray("forbiddenSlots")
            ?.map { it.asInt } ?: emptyList()
        cct.setForbiddenSlots(forbiddenSlots)
        cct.setDefaultInventory(readInventory(id, json.getAsJsonArray("defaultChestCavity"), forbiddenSlots))
        cct.setBaseOrganScores(readOrganScores(json.getAsJsonArray("baseOrganScores")))
        cct.setExceptionalOrgans(readExceptionalOrgans(json.getAsJsonArray("exceptionalOrgans")))
        cct.dropRateMultiplier = json.get("dropRateMultiplier")?.asFloat ?: 1f
        cct.playerChestCavity = json.get("playerChestCavity")?.asBoolean ?: false
        cct.bossChestCavity = json.get("bossChestCavity")?.asBoolean ?: false
        return cct
    }

    private fun readInventory(id: ResourceLocation, json: JsonArray?, forbidden: List<Int>): ChestCavityInventory {
        val inv = ChestCavityInventory()
        json ?: return inv
        for (element in json) {
            val obj = element.asJsonObject
            val itemId = ResourceLocation.parse(obj.get("item")?.asString ?: continue)
            val item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null) ?: continue
            val pos = obj.get("position")?.asInt ?: continue
            if (pos !in 0 until inv.containerSize) continue
            if (pos in forbidden) continue
            val count = obj.get("count")?.asInt ?: item.defaultMaxStackSize
            inv.setItem(pos, ItemStack(item, count))
        }
        return inv
    }

    private fun readOrganScores(json: JsonArray?): Map<ResourceLocation, Float> {
        json ?: return emptyMap()
        return json.mapNotNull { element ->
            val obj = element.asJsonObject
            val scoreId = obj.get("id")?.asString ?: return@mapNotNull null
            val value = obj.get("value")?.asFloat ?: return@mapNotNull null
            ResourceLocation.parse(scoreId) to value
        }.toMap()
    }

    private fun readExceptionalOrgans(json: JsonArray?): Map<Ingredient, Map<ResourceLocation, Float>> {
        json ?: return emptyMap()
        return json.mapNotNull { element ->
            val obj = element.asJsonObject
            val ingredientJson = obj.get("ingredient") ?: return@mapNotNull null
            val itemId = ingredientJson.asJsonObject?.get("item")?.asString ?: return@mapNotNull null
            val item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(itemId)).orElse(null) ?: return@mapNotNull null
            val ingredient = Ingredient.of(item)
            val scores = readOrganScores(obj.getAsJsonArray("value"))
            ingredient to scores
        }.toMap()
    }
}
