package me.cngamble

import com.mojang.brigadier.arguments.IntegerArgumentType
import net.minecraft.ChatFormatting
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent

/**
 * `/cngamble` client command (registered through SkyBlockAPI like SkyOcean's commands).
 * Client commands run on your own game and are never sent to Hypixel.
 */
object NucleusCommands {

    @Subscription
    private fun onRegisterCommands(event: RegisterCommandsEvent) {
        event.register("cngamble") {
            callback { status() }

            then("test") {
                callback { NucleusGambling.simulate() }
            }

            then("toggle") {
                callback {
                    NucleusConfig.enabled = !NucleusConfig.enabled
                    NucleusChat.send("Case opening is now ${onOff(NucleusConfig.enabled)}.")
                }
            }

            then("hidechat") {
                callback {
                    NucleusConfig.hideChat = !NucleusConfig.hideChat
                    NucleusChat.send("Hide chat while rolling is now ${onOff(NucleusConfig.hideChat)}.")
                }
            }

            then("mode") {
                callback {
                    val modes = NucleusConfig.Mode.entries
                    NucleusConfig.mode = modes[(NucleusConfig.mode.ordinal + 1) % modes.size]
                    NucleusChat.send("The reel now lands on: ${NucleusConfig.mode.label}.")
                }
            }

            then("time") {
                then("seconds", IntegerArgumentType.integer(2, 30)) {
                    callback {
                        NucleusConfig.spinSeconds = IntegerArgumentType.getInteger(this, "seconds")
                        NucleusChat.send("Spin time set to ${NucleusConfig.spinSeconds}s.")
                    }
                }
            }

            then("tease") {
                then("percent", IntegerArgumentType.integer(0, 100)) {
                    callback {
                        NucleusConfig.teaseChance = IntegerArgumentType.getInteger(this, "percent")
                        NucleusChat.send("Near misses now happen on ${NucleusConfig.teaseChance}% of spins.")
                    }
                }
            }

            then("autoclose") {
                then("seconds", IntegerArgumentType.integer(0, 30)) {
                    callback {
                        NucleusConfig.autoCloseSeconds = IntegerArgumentType.getInteger(this, "seconds")
                        NucleusChat.send("Auto close set to ${NucleusConfig.autoCloseSeconds}s (0 = never).")
                    }
                }
            }
        }
    }

    private fun onOff(value: Boolean) = if (value) "ON" else "OFF"

    private fun status() {
        NucleusChat.send("Crystal Nucleus Gamble", ChatFormatting.LIGHT_PURPLE)
        NucleusChat.send(" Enabled: ${onOff(NucleusConfig.enabled)}  (/cngamble toggle)")
        NucleusChat.send(" Lands on: ${NucleusConfig.mode.label}  (/cngamble mode)")
        NucleusChat.send(" Hide chat: ${onOff(NucleusConfig.hideChat)}  (/cngamble hidechat)")
        NucleusChat.send(" Spin time: ${NucleusConfig.spinSeconds}s  (/cngamble time <2-30>)")
        NucleusChat.send(" Near misses: ${NucleusConfig.teaseChance}% of spins  (/cngamble tease <0-100>)")
        NucleusChat.send(" Auto close:${NucleusConfig.autoCloseSeconds}s  (/cngamble autoclose <0-30>)")
        NucleusChat.send(" Try it: /cngamble test", ChatFormatting.YELLOW)
    }
}
