package me.cngamble

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.item.calculator.getItemValue
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McClient
import java.util.concurrent.ThreadLocalRandom

/**
 * Port of SkyOcean's `VanguardGambling` to the Crystal Nucleus.
 *
 * Hypixel prints this when you finish a nucleus run (stripped of colours):
 * ```
 * ▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
 *   CRYSTAL NUCLEUS LOOT BUNDLE
 *
 *   REWARDS
 *     Pickonimbus 2000
 *     Flawed Ruby Gemstone x24
 *     ...
 * ▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
 * ```
 * We collect the item lines and, at the closing line, open the case animation
 * that lands on your best drop. The chat event is only read, never cancelled.
 */
object NucleusGambling {

    private val startRegex = Regex(" +CRYSTAL NUCLEUS LOOT BUNDLE.*")
    private val itemRegex = Regex(" {4}(?<item>.+?)(?: x(?<amount>[\\d,]+))?")
    private val endRegex = Regex("▬{64}")

    /** Text that shows up in the rewards block but is not an item. */
    private val ignoredWords = listOf("Powder", "Experience", "Coins", "REWARDS")

    /** Every run gives one of each Fine gemstone, so those never count as "your pull". */
    private val guaranteed: Set<SkyBlockId> =
        listOf("ruby", "amethyst", "jade", "amber", "sapphire", "topaz", "jasper", "opal")
            .map { SkyBlockId.item("fine_${it}_gem") }
            .toSet()

    private var parsing = false
    private val loot = mutableListOf<Pair<SkyBlockId, Int>>()

    @Subscription
    private fun onMessage(event: ChatReceivedEvent.Pre) {
        handleLine(event.text)
    }

    private fun handleLine(text: String, force: Boolean = false) {
        if (!force && !NucleusConfig.enabled) return
        when {
            startRegex.matches(text) -> {
                parsing = true
                loot.clear()
            }

            parsing && endRegex.matches(text.trim()) -> {
                parsing = false
                gamblingTime()
                loot.clear()
            }

            parsing -> parseItem(text)
        }
    }

    private fun parseItem(text: String) {
        val match = itemRegex.matchEntire(text) ?: return
        val name = match.groups["item"]?.value?.trim() ?: return
        if (ignoredWords.any { name.contains(it) }) return
        val id = SkyBlockId.fromName(name) ?: return
        val amount = match.groups["amount"]?.value?.replace(",", "")?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        loot.add(id to amount)
    }

    private fun gamblingTime() {
        if (loot.isEmpty()) return

        // Take away the guaranteed Fine gem from each stack; if nothing is left, use everything.
        val rolled = loot.mapNotNull { (id, amount) ->
            val left = if (id in guaranteed) amount - 1 else amount
            if (left > 0) id to left else null
        }.ifEmpty { loot.toList() }

        val winner = when (NucleusConfig.mode) {
            NucleusConfig.Mode.MOST_EXPENSIVE -> rolled.maxByOrNull { (id, amount) -> id.toItem().getItemValue().price * amount }
            NucleusConfig.Mode.RAREST -> rolled.minByOrNull { (id, _) -> NucleusLoot.weightOf(id) }
        }?.first

        if (debugNext) {
            debugNext = false
            val parsed = loot.joinToString(", ") { (id, amount) -> "${id.toItem().hoverName.string} x$amount" }
            NucleusChat.send("Test loot read from chat: $parsed")
            NucleusChat.send("Reel lands on: ${winner?.toItem()?.hoverName?.string ?: "nothing (random)"}", ChatFormatting.YELLOW)
        }

        McClient.setScreenAsync { NucleusCaseScreen(winner) }
    }

    /** Set by [simulate] so the test prints what it parsed and what it picked. */
    private var debugNext = false

    /** Weighted pick from the real drop table, so the test looks like a real run. */
    private fun rollFromTable(random: ThreadLocalRandom): SkyBlockId {
        var roll = random.nextInt(NucleusLoot.totalWeight)
        for ((id, weight) in NucleusLoot.pool) {
            roll -= weight
            if (roll < 0) return id
        }
        return NucleusLoot.pool.keys.first()
    }

    /** `/cngamble test`: feeds a fake loot message through the same parser. Nothing is sent anywhere. */
    fun simulate() {
        val random = ThreadLocalRandom.current()

        // Like a real bundle: the guaranteed Fine gems + 14-20 rolls on the real drop table.
        val rolls = HashMap<SkyBlockId, Int>()
        for (id in guaranteed) rolls.merge(id, 1) { a, b -> a + b }
        repeat(14 + random.nextInt(7)) { rolls.merge(rollFromTable(random), 1) { a, b -> a + b } }

        val lines = buildList {
            add("▬".repeat(64))
            add("  CRYSTAL NUCLEUS LOOT BUNDLE")
            add(" ")
            add("  REWARDS")
            add("    Mithril Powder x1,500")
            for ((id, amount) in rolls) {
                val name = id.toItem().hoverName.string
                add(if (amount > 1) "    $name x$amount" else "    $name")
            }
            add("▬".repeat(64))
        }
        NucleusChat.send(Component.literal("Simulating a Crystal Nucleus loot message (only on your screen)."))
        debugNext = true
        lines.forEach { handleLine(it, force = true) }
    }
}
