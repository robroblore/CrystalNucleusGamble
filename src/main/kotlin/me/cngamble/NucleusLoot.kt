package me.cngamble

import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId

/**
 * What the reel can show, weighted by the Crystal Loot Bundle drop table (wiki chances x 10,000).
 * Amounts are ignored for the reel, so all amount rows of one item are summed, and the
 * "Ruby/Amethyst/Jade/Amber/Sapphire/Topaz" rows are split evenly over the six gem types.
 *
 * This plays the same role as SkyOcean's `repo/vanguard.json` for its Vanguard gambling.
 */
object NucleusLoot {

    private val gemTypes = listOf("ruby", "amethyst", "jade", "amber", "sapphire", "topaz")

    val pool: Map<SkyBlockId, Int> = buildMap {
        for (gem in gemTypes) {
            put(SkyBlockId.item("flawed_${gem}_gem"), 20202)
            put(SkyBlockId.item("fine_${gem}_gem"), 491)
            put(SkyBlockId.item("flawless_${gem}_gem"), 8)
        }
        put(SkyBlockId.item("flawed_jasper_gem"), 57986)
        put(SkyBlockId.item("fine_jasper_gem"), 655)
        put(SkyBlockId.item("flawless_jasper_gem"), 16)

        put(SkyBlockId.item("bob_omb"), 39313)
        put(SkyBlockId.item("oil_barrel"), 32761)
        put(SkyBlockId.item("pickonimbus"), 32761)
        put(SkyBlockId.item("divan_fragment"), 19656)
        put(SkyBlockId.item("prehistoric_egg"), 16380)
        put(SkyBlockId.item("helix"), 13104)
        put(SkyBlockId.item("ruby_crystal"), 8190)
        put(SkyBlockId.item("jasper_crystal"), 8190)
        put(SkyBlockId.item("treasurite"), 7207)
        put(SkyBlockId.item("jaderald"), 4750)
        put(SkyBlockId.item("essence_gold"), 2457)
        put(SkyBlockId.item("essence_diamond"), 2457)
        put(SkyBlockId.item("gemstone_mixture"), 2293)
        put(SkyBlockId.enchantment("fortune", 4), 2293)
        put(SkyBlockId.item("claw_fossil"), 1630)
        put(SkyBlockId.item("wishing_compass"), 1638)
        put(SkyBlockId.item("precious_pearl"), 1638)
        put(SkyBlockId.item("recall_potion"), 819)
        put(SkyBlockId.item("pet_item_quick_claw"), 164)
        put(SkyBlockId.item("divan_alloy"), 59)
        put(SkyBlockId.item("dye_jade"), 2)
    }

    val totalWeight: Int = pool.values.sum()

    /**
     * Weights used only to *fill the reel*. Square-rooting flattens the table so rare items
     * still look rare but actually show up while the reel spins (Alloy goes from ~1 in 6,500
     * cards to roughly 1 in 400). Winners are never picked from this, only drawn.
     */
    val reelPool: Map<SkyBlockId, Int> = pool.mapValues { (_, weight) -> kotlin.math.sqrt(weight.toDouble()).toInt().coerceAtLeast(1) }
    val reelTotal: Int = reelPool.values.sum()

    /** Items used for the near miss, weighted so Divan's Alloy is by far the most common tease. */
    val teaseItems: Map<SkyBlockId, Int> = buildMap {
        put(SkyBlockId.item("divan_alloy"), 60)
        put(SkyBlockId.item("dye_jade"), 15)
        put(SkyBlockId.item("pet_item_quick_claw"), 10)
        put(SkyBlockId.item("flawless_jasper_gem"), 5)
        for (gem in gemTypes) put(SkyBlockId.item("flawless_${gem}_gem"), 2)
    }

    /** Lower = rarer. Items that are not in the table (e.g. the guaranteed Fine gems) count as common. */
    fun weightOf(id: SkyBlockId): Int = pool[id] ?: Int.MAX_VALUE
}
