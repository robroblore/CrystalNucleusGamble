package me.cngamble

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.utils.text.Text

/** Local-only chat lines (added to your own chat box, never sent to the server). */
object NucleusChat {

    private val prefix: Component
        get() = Component.literal("[Nucleus Gamble] ").withStyle(ChatFormatting.LIGHT_PURPLE)

    fun send(message: Component) {
        val line = Component.empty().append(prefix).append(message)
        with(Text) { line.send() }
    }

    fun send(message: String, color: ChatFormatting = ChatFormatting.GRAY) {
        send(Component.literal(message).withStyle(color))
    }
}
