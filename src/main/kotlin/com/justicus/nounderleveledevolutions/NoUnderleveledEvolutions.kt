package com.justicus.nounderleveledevolutions

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.pokemon.evolution.requirements.LevelRequirement
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import org.slf4j.LoggerFactory
import java.util.UUID


object NoUnderleveledEvolutions : ModInitializer {
	const val MOD_ID = "no-underleveled-evolutions"
	private val logger = LoggerFactory.getLogger(MOD_ID)

	val toRemove = mutableListOf<UUID>()

	override fun onInitialize() {
		CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe { evt ->
			logger.info("Spawn detected")
			val pokemon = evt.entity.pokemon
			val species = pokemon.species
			val level = pokemon.level
			val preEvo = pokemon.preEvolution

			val evolutions = preEvo?.form?.evolutions
			val levelRequirement = evolutions
				?.flatMap { it.requirements }
				?.filterIsInstance<LevelRequirement>()
				?.firstOrNull()

			if (levelRequirement != null && level < levelRequirement.minLevel) {
				logger.info("$level is below $species's threshold of ${levelRequirement.minLevel}. Deleting entity.")
				evt.cancel()
			}
		}

		ServerTickEvents.END_SERVER_TICK.register { server ->
			server.worlds.forEach { level ->
				level.iterateEntities().forEach { entity ->
					if (entity.uuid != null && toRemove.contains(entity.uuid)) {
						entity.discard()
						logger.info("Removed entity: ${entity.name.string}")
						toRemove.remove(entity.uuid)
					}
				}
			}
		}
	}
}