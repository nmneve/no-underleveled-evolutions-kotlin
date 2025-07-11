package com.justicus.nounderleveledevolutions

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.pokemon.evolution.requirements.LevelRequirement
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.server.world.ServerWorld
import org.slf4j.LoggerFactory
import java.util.UUID


object NoUnderleveledEvolutions : ModInitializer {
	const val MOD_ID = "no-underleveled-evolutions"
	private val logger = LoggerFactory.getLogger(MOD_ID)

	val toRemove = mutableListOf<UUID>()

	override fun onInitialize() {
		CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe { evt ->
			logger.info("Spawn detected")
//			evt.entity.discard()
//			evt.entity.remove(Entity.RemovalReason.DISCARDED)
//			toRemove.add(evt.entity.uuid)
			//evt.cancel()
			val pokemon = evt.entity.pokemon
			val species = pokemon.species
			val level = pokemon.level
			val preEvo = pokemon.preEvolution

			logger.info("SERVER: $species's pre-evolution is ${preEvo?.species}")
			//logger.info("They evolve at ${(preEvo?.form?.evolutions?.first()?.requirements?.first() as LevelRequirement).minLevel}")

			val evolutions = preEvo?.form?.evolutions
			val levelRequirement = evolutions
				?.flatMap { it.requirements }
				?.filterIsInstance<LevelRequirement>()
				?.firstOrNull()

			if (levelRequirement != null && level > levelRequirement.minLevel) {
				logger.info("SERVER: Invalid level. Deleting entity.")
				evt.cancel()
				logger.info("${evt.entity.pokemon.species} has been deleted")
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

//		ServerEntityEvents.ENTITY_LOAD.register { ent, world ->
//			logger.info("SERVER: Spawn Detected")
//
//			if (ent is PokemonEntity) {
//				val pokemon = ent.pokemon
//				invalidEvolutionCheck(pokemon)
//			}
//		}
	}

//	fun invalidEvolutionCheck(pokemon: Pokemon) {
//		val species = pokemon.species
//		val level = pokemon.level
//		val preEvo = pokemon.preEvolution
//
//		logger.info("SERVER: $species's pre-evolution is ${preEvo?.species}")
//		//logger.info("They evolve at ${(preEvo?.form?.evolutions?.first()?.requirements?.first() as LevelRequirement).minLevel}")
//
//		val evolutions = preEvo?.form?.evolutions
//		val levelRequirement = evolutions
//			?.flatMap { it.requirements }
//			?.filterIsInstance<LevelRequirement>()
//			?.firstOrNull()
//
//		if (levelRequirement != null && level > levelRequirement.minLevel) {
//			logger.info("SERVER: Invalid level. Deleting entity.")
//			pokemon.entity?.discard()
//		}
//	}
}