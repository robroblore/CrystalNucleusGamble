package me.cngamble

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.ARGB
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.render.HudElement
import tech.thatgravyboat.skyblockapi.api.events.render.RenderHudElementEvent
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.platform.drawGradient
import tech.thatgravyboat.skyblockapi.platform.drawString
import tech.thatgravyboat.skyblockapi.utils.extentions.get
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * The CS:GO style case opening. The reel, cards, easing, sounds and post effect are the
 * same as SkyOcean's Dungeon Chest gambling (`DungeonGamblingRenderer` + `DungeonCard`),
 * moved into a stand-alone screen the way SkyOcean's Vanguard `SlotMachineSpinner` works.
 *
 * @param winner the item the reel stops on, or null for a random stop.
 */
class NucleusCaseScreen(winner: SkyBlockId?) : Screen(Component.literal("Crystal Nucleus Case")) {

    private companion object {
        const val CARDS = 50
        const val WIN_INDEX = 40
        const val CARD_SPACING = 101 // 24 * 4 px card + 5 px gap
        const val CARD_HEIGHT = 72 // 18 * 4
        const val CARD_BACKGROUND = -2144325584 // same colour as SkyOcean's DungeonCard
        const val MARKER = -43691 // same red marker as SkyOcean
    }

    private val random = ThreadLocalRandom.current()
    private val cache = HashMap<SkyBlockId, ItemStack>()
    private val items = ArrayList<ItemStack>(CARDS)
    /**
     * Where inside the winning card the marker stops. The marker sits at `32 + randomOffset`
     * px into the 96 px card: SkyOcean's range keeps it well inside the card, the near-miss
     * values push it right up against the edge next to the tease item.
     */
    private var randomOffset = (16 + random.nextInt(16)) * if (random.nextBoolean()) 1 else -1
    private val durationMs = NucleusConfig.spinSeconds * 1000L
    private val startNanos = System.nanoTime() + 200_000_000L // small pause before it starts, like SkyOcean
    private var lastSound = 0
    private var finishedAtMs = -1L
    private var playedWinSound = false
    private var closing = false

    init {
        repeat(CARDS) { items.add(randomItem()) }
        sprinkleJackpots()
        if (winner != null) items[WIN_INDEX] = itemOf(winner)
        maybeTease(winner)
        SkyBlockAPI.eventBus.register(this)
    }

    private fun pickTease(exclude: SkyBlockId? = null): SkyBlockId? {
        val options = NucleusLoot.teaseItems.filterKeys { it != exclude }
        if (options.isEmpty()) return null
        var roll = random.nextInt(options.values.sum())
        return options.entries.first { roll -= it.value; roll < 0 }.key
    }

    /**
     * Puts 2-4 jackpot items (Alloy, Jade Dye, Quick Claw, Flawless gems) into the part of
     * the reel that flies past at full speed, so you see them whizz by like in a CS:GO case.
     * They never go near the stopping point (cards 36-44), which only the near miss uses.
     */
    private fun sprinkleJackpots() {
        val spots = (6..34).shuffled(random).take(2 + random.nextInt(3))
        for (spot in spots) pickTease()?.let { items[spot] = itemOf(it) }
    }

    /**
     * Near miss: sometimes put a jackpot item right next to the real result and stop the
     * reel at the very edge of the real card, so it looks like it *almost* got there.
     * Purely visual: the card you land on is always the item you actually got.
     */
    private fun maybeTease(winner: SkyBlockId?) {
        if (random.nextInt(100) >= NucleusConfig.teaseChance) return
        val tease = pickTease(exclude = winner) ?: return

        // Cards are 96 px wide; the marker stops at (32 + randomOffset) px into the real card.
        if (random.nextInt(4) != 0) {
            // "Almost": the jackpot is the next card and the reel dies 1-5 px before reaching it.
            items[WIN_INDEX + 1] = itemOf(tease)
            randomOffset = 59 + random.nextInt(5) // marker 91-95 px into the card
        } else {
            // "Overshot": it sat on the jackpot, crept over the edge and dropped onto the next card.
            items[WIN_INDEX - 1] = itemOf(tease)
            randomOffset = -31 + random.nextInt(5) // marker 1-5 px into the card
        }
    }

    private fun itemOf(id: SkyBlockId): ItemStack = cache.getOrPut(id) { id.toItem() }

    private fun randomItem(): ItemStack {
        var roll = random.nextInt(NucleusLoot.reelTotal)
        for ((id, weight) in NucleusLoot.reelPool) {
            roll -= weight
            if (roll < 0) return itemOf(id)
        }
        return itemOf(NucleusLoot.pool.keys.first())
    }

    /** SkyOcean's ease (circular in-out). */
    private fun ease(t: Float): Float =
        if (t < 0.5f) (1f - sqrt(1f - (2f * t).pow(2))) / 2f
        else (sqrt(1f - (-2f * t + 2f).pow(2)) + 1f) / 2f

    private fun nowMs(): Long = System.currentTimeMillis()

    override fun isPauseScreen(): Boolean = false

    override fun removed() {
        super.removed()
        SkyBlockAPI.eventBus.unregister(this)
    }

    @Subscription(inherited = true)
    private fun onHudRender(event: RenderHudElementEvent) {
        // Keep the chat hidden while rolling so the loot message does not spoil the result.
        if (event.element == HudElement.CHAT && NucleusConfig.hideChat) event.cancel()
    }

    // No vanilla blur/dirt background, we draw our own.
    override fun extractBackground(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {}

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        val width = graphics.guiWidth()
        val height = graphics.guiHeight()
        val elapsedMs = ((System.nanoTime() - startNanos) / 1_000_000L).coerceAtLeast(0L)
        val rawProgress = elapsedMs.toFloat() / durationMs
        val progress = (rawProgress + 0.25f).coerceIn(0f, 1f)
        val endOffset = (CARD_SPACING * WIN_INDEX) * ease(progress)

        if (rawProgress >= 1f && finishedAtMs < 0) finishedAtMs = nowMs()

        graphics.fill(0, 0, width, height, Int.MIN_VALUE)

        // Tick sound every time a card passes the marker.
        val soundIndex = endOffset.toInt() / CARD_SPACING
        if (soundIndex > lastSound) {
            McClient.playSound(SoundEvents.ITEM_PICKUP, 1f, 2f)
            lastSound = soundIndex
        }

        val reelTop = (height - CARD_HEIGHT) / 2
        val pose = graphics.pose()
        pose.pushMatrix()
        pose.translate(width / 2f - endOffset - (32 + randomOffset), reelTop.toFloat())
        items.forEachIndexed { index, item ->
            val cardX = width / 2f - endOffset - (32 + randomOffset) + index * CARD_SPACING
            if (cardX > -CARD_SPACING && cardX < width) {
                pose.pushMatrix()
                pose.translate((index * CARD_SPACING).toFloat(), 0f)
                pose.scale(4f, 4f)
                renderCard(graphics, item)
                pose.popMatrix()
            }
        }
        pose.popMatrix()

        // Red marker above and below the reel.
        graphics.drawGradient(width / 2 - 1, reelTop - 18, 2, 18, MARKER, MARKER, MARKER, MARKER)
        graphics.drawGradient(width / 2 - 1, reelTop + CARD_HEIGHT, 2, 18, MARKER, MARKER, MARKER, MARKER)

        SkyOceanBridge.applyCaseEffect(graphics)

        val title = Component.literal("CRYSTAL NUCLEUS").withColor(0xAA00AA)
        graphics.drawString(title, width / 2 - McFont.width(title) / 2, reelTop - 34, -1, true)

        if (progress >= 0.96f) renderWinner(graphics, width, height, progress)

        if (finishedAtMs > 0) {
            if (!playedWinSound) {
                McClient.playSound(SoundEvents.PLAYER_LEVELUP, 1f, 1f)
                playedWinSound = true
            }
            val hint = Component.literal("Press ESC to close").withColor(0xAAAAAA)
            graphics.drawString(hint, width / 2 - McFont.width(hint) / 2, reelTop + CARD_HEIGHT + 26, -1, true)

            val autoClose = NucleusConfig.autoCloseSeconds
            if (!closing && autoClose > 0 && nowMs() - finishedAtMs > autoClose * 1000L) {
                closing = true
                // Close on the next client tick, not in the middle of rendering.
                McClient.runNextTick { if (McScreen.self === this) onClose() }
            }
        }

        super.extractRenderState(graphics, mouseX, mouseY, partialTick)
    }

    private fun renderWinner(graphics: GuiGraphicsExtractor, width: Int, height: Int, progress: Float) {
        val name = items[WIN_INDEX].hoverName
        val textWidth = McFont.width(name)
        val scale = Mth.lerp(((progress - 0.96f) / 0.04f).coerceIn(0f, 1f), 1f, 3f)
        val pose = graphics.pose()
        pose.pushMatrix()
        pose.translate(width / 2f, height * 0.2f)
        pose.scale(scale, scale)
        pose.translate(-textWidth / 2f, 0f)
        graphics.drawString(name, 0, 0, -1, true)
        pose.popMatrix()
    }

    /** SkyOcean's DungeonCard: 24x18 card, rarity tinted, coloured bar at the bottom. */
    private fun renderCard(graphics: GuiGraphicsExtractor, item: ItemStack) {
        val color = ARGB.opaque(item.get(DataTypes.RARITY)?.color ?: -1)
        val tint = ARGB.color(96, color)
        graphics.drawGradient(0, 0, 24, 17, CARD_BACKGROUND, tint, tint, CARD_BACKGROUND)
        graphics.drawGradient(0, 17, 24, 1, color, color, color, color)
        graphics.item(item, 4, 1)
    }
}
