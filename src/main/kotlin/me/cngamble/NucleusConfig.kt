package me.cngamble

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path

/** Tiny JSON config at `.minecraft/config/crystalnucleusgamble.json`, edited with `/cngamble`. */
object NucleusConfig {

    enum class Mode(val label: String) {
        /** Lands on the drop worth the most coins (price x amount), like SkyOcean's "Most Expensive" Vanguard mode. */
        MOST_EXPENSIVE("Most expensive drop"),

        /** Lands on the drop that is rarest in the Crystal Loot Bundle table. */
        RAREST("Rarest drop"),
    }

    private class Data {
        var enabled: Boolean = true
        var hideChat: Boolean = true
        var mode: Mode = Mode.MOST_EXPENSIVE
        var spinSeconds: Int = 8
        var autoCloseSeconds: Int = 4
        var teaseChance: Int = 15
    }

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val file: Path get() = FabricLoader.getInstance().configDir.resolve("crystalnucleusgamble.json")
    private var data = Data()

    var enabled: Boolean
        get() = data.enabled
        set(value) { data.enabled = value; save() }

    var hideChat: Boolean
        get() = data.hideChat
        set(value) { data.hideChat = value; save() }

    var mode: Mode
        get() = data.mode
        set(value) { data.mode = value; save() }

    var spinSeconds: Int
        get() = data.spinSeconds.coerceIn(2, 30)
        set(value) { data.spinSeconds = value.coerceIn(2, 30); save() }

    var autoCloseSeconds: Int
        get() = data.autoCloseSeconds.coerceIn(0, 30)
        set(value) { data.autoCloseSeconds = value.coerceIn(0, 30); save() }

    /** Percent of spins that stop just short of a jackpot item (the "near miss"). */
    var teaseChance: Int
        get() = data.teaseChance.coerceIn(0, 100)
        set(value) { data.teaseChance = value.coerceIn(0, 100); save() }

    fun load() {
        try {
            if (Files.exists(file)) {
                data = gson.fromJson(Files.readString(file), Data::class.java) ?: Data()
            }
        } catch (e: Exception) {
            CrystalNucleusGamble.logger.error("Could not read config, using defaults", e)
            data = Data()
        }
        save()
    }

    private fun save() {
        try {
            Files.createDirectories(file.parent)
            Files.writeString(file, gson.toJson(data))
        } catch (e: Exception) {
            CrystalNucleusGamble.logger.error("Could not save config", e)
        }
    }
}
