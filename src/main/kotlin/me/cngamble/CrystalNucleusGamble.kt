package me.cngamble

import net.fabricmc.api.ClientModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI

/**
 * Entry point. Same idea as SkyOcean: every feature is a Kotlin `object` whose
 * `@Subscription` methods are registered on SkyBlockAPI's event bus.
 *
 * Nothing in this mod sends, reads, cancels or modifies packets. It only listens to
 * the chat messages the game already shows you and draws a screen on top.
 */
object CrystalNucleusGamble : ClientModInitializer {

    const val MOD_ID = "crystalnucleusgamble"
    val logger: Logger = LoggerFactory.getLogger("CrystalNucleusGamble")

    override fun onInitializeClient() {
        NucleusConfig.load()
        SkyBlockAPI.eventBus.register(NucleusGambling)
        SkyBlockAPI.eventBus.register(NucleusCommands)
        logger.info("Crystal Nucleus Gamble loaded. Good luck.")
    }
}
