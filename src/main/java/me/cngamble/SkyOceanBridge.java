package me.cngamble;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Re-uses SkyOcean's own "case_screen" post effect (blurred edges + zoomed middle), the
 * exact look of SkyOcean's Dungeon Chest gambling.
 *
 * Written in Java on purpose: it calls SkyOcean's compiled method directly, and if a
 * future SkyOcean update renames it, the animation just plays without the effect instead
 * of crashing.
 */
public final class SkyOceanBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger("CrystalNucleusGamble");
    private static final Identifier CASE_SCREEN = Identifier.fromNamespaceAndPath("skyocean", "case_screen");
    private static boolean broken = false;

    private SkyOceanBridge() {
    }

    public static void applyCaseEffect(GuiGraphicsExtractor graphics) {
        if (broken) return;
        try {
            me.owdding.skyocean.utils.rendering.RenderUtilsKt.applyPostEffect(graphics, CASE_SCREEN);
        } catch (Throwable t) {
            broken = true;
            LOGGER.warn("SkyOcean's case_screen effect is unavailable, continuing without it", t);
        }
    }
}
