# Crystal Nucleus Gamble

A CS:GO-style case opening for your Crystal Nucleus runs on Hypixel SkyBlock.

**This project is in no way affiliated with, endorsed by, or connected to SkyOcean or its developers. It only uses SkyOcean as a dependency.**

---

## What it does

When you finish a Crystal Nucleus run, Hypixel posts your **Crystal Nucleus Loot Bundle** rewards in chat. Instead of just reading a list, you get a case opening:

- A reel of rarity-coloured item cards spins across your screen. It ticks as each card passes the marker, slows down, and stops on your best drop. The item's name zooms in at the end.
- **The result is always your real loot.** The reel only changes what flies past. The card it stops on is always the item Hypixel actually gave you.
- **Chat is hidden while it spins**, so the loot message doesn't spoil the result.
- **Jackpots fly past.** Divan's Alloy, Jade Dye, Quick Claw and Flawless gemstones show up while the reel is going fast, and good drops like Jaderald and Wishing Compass show up regularly.
- **Near misses.** Sometimes the reel stops a few pixels short of a jackpot, or creeps over the edge of one onto your real item. How often this happens is configurable, and you can turn it off.
- **Two modes.** The reel can land on your **most expensive** drop (by item value × amount) or your **rarest** drop (by the loot table).
- **Test mode.** `/cngamble test` plays the animation with realistic fake loot, so you can try it without doing a run.

## Is it safe?

The mod is built to be as harmless as possible:

- **It doesn't touch packets.** Nothing is sent, read, cancelled or modified at the network level.
- **It has no mixins**, so it doesn't change any of Minecraft's code.
- **Its only input** is chat lines your game already shows you, read through SkyBlockAPI's chat event and never cancelled or changed.
- **Its only outputs** are a screen and sounds on your own client, plus lines added to your own chat box.
- **`/cngamble` is a client command**, so it never reaches the server.
- **No automation and no gameplay advantage.** The animation only plays after the loot is already yours.

That said, Hypixel decides what is allowed on their server, and any mod is used at your own risk.

## Requirements

| | Version |
|---|---|
| Minecraft | 26.2 |
| Fabric Loader | 0.16.10 or newer |
| Fabric API | any version for 26.2 |
| Fabric Language Kotlin | 1.13.12+kotlin.2.4.0 or newer |
| SkyOcean | 1.18.2 or newer |

SkyOcean is required because it ships the libraries this mod builds on (skyblock-api, meowdding-lib), and this mod reuses its case-opening screen effect.

## Installation

1. Install the requirements above. If you already play with SkyOcean, you have them all.
2. Download the latest `CrystalNucleusGamble-x.x.x.jar` from Releases.
3. Put it in your `mods` folder.
4. Launch the game and run `/cngamble test` to check it works.

## Commands

| Command | Description |
|---|---|
| `/cngamble` | Show the current settings |
| `/cngamble test` | Play the animation with fake loot |
| `/cngamble toggle` | Turn the case opening on or off |
| `/cngamble mode` | Switch between landing on the most expensive drop and the rarest drop |
| `/cngamble hidechat` | Toggle hiding chat while the reel spins (on by default) |
| `/cngamble time <2-30>` | Spin length in seconds (default 8) |
| `/cngamble tease <0-100>` | How often near misses happen, in % (default 15, 0 = off) |
| `/cngamble autoclose <0-30>` | Close the screen this many seconds after it stops (default 4, 0 = only with ESC) |

Settings are saved in `config/crystalnucleusgamble.json`.

## Building from source

You need JDK 25.

The build compiles against your local SkyOcean jar and the libraries packed inside it. Put the repository folder next to:

- `SkyOcean-<version>.jar`
- an unpacked copy of that jar, in a folder named `SkyOcean-<version>`

If your files are somewhere else or named differently, change `skyocean_dir`, `skyocean_jar` and `skyocean_unpacked` in `gradle.properties`.

```
gradlew build
```

The jar is written to `build/libs/`.

## Credits

- Drop chances come from the [Hypixel SkyBlock Wiki](https://hypixel-skyblock.fandom.com/wiki/Crystal_Nucleus) Crystal Loot Bundle table.
- Item IDs were checked against the [NotEnoughUpdates item repo](https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO).
- Built on skyblock-api and SkyOcean, used as dependencies only.

**Again: this project is not affiliated with SkyOcean in any way. It only depends on it.**

## License

MIT
