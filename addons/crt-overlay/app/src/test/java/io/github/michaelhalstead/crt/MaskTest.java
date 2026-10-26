package io.github.michaelhalstead.crt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MaskTest {
    private static final int W = 1920, H = 1080;

    @Test
    public void strengthZeroDrawsNothingAwayFromCorners() {
        for (Mask.Preset pr : Mask.Preset.values()) {
            for (int[] p : new int[][] {{100, 100}, {960, 540}, {2, 539}, {960, 541}, {1000, 3}}) {
                assertEquals(0f, Mask.alpha(p[0], p[1], W, H, pr, 0f), 0f);
            }
        }
    }

    @Test
    public void consoleLineCentreIsLighterThanGap() {
        // p = 4.5: row 542 = 120*4.5+2 sits at d = -0.056 (line centre), row 540 at d = -0.5 (gap); vignette is 0 here
        float centre = Mask.alpha(960, 542, W, H, Mask.Preset.CONSOLE, 1f);
        float gap = Mask.alpha(960, 540, W, H, Mask.Preset.CONSOLE, 1f);
        assertTrue(centre < gap);
        assertEquals(.6 * (1 - Math.exp(-Math.pow(2 / 4.5 - .5, 2) / .09)), centre, 1e-3);
    }

    @Test
    public void slotAndGrilleColumns() {
        assertEquals(0f, Mask.slot(0, 1, .22f), 0f);
        assertEquals(.22f, Mask.slot(4, 1, .22f), 0f);   // x%6 >= 4
        assertEquals(.22f, Mask.slot(0, 0, .22f), 0f);   // gap row, left half
        assertEquals(0f, Mask.slot(6, 0, .22f), 0f);     // right half shifted by 2
        assertEquals(.22f, Mask.slot(6, 2, .22f), 0f);
        assertEquals(0f, Mask.grille(1, .15f), 0f);
        assertEquals(.15f, Mask.grille(2, .15f), 0f);
    }

    @Test
    public void cornerPixelIsFullBlack() {
        for (Mask.Preset pr : Mask.Preset.values()) {
            assertEquals(1f, Mask.alpha(0, 0, W, H, pr, 0.1f), 0f);
            assertEquals(1f, Mask.alpha(W - 1, H - 1, W, H, pr, 1.5f), 0f);
        }
    }

    @Test
    public void clampedAndBuildMatchesAlpha() {
        for (Mask.Preset pr : Mask.Preset.values()) {
            int[] px = Mask.build(W, H, pr, 1.5f);
            assertEquals(W * H, px.length);
            for (int[] p : new int[][] {{5, 5}, {100, 1}, {960, 4}, {962, 541}, {1919, 700}, {50, 30}, {30, 50}}) {
                float a = Mask.alpha(p[0], p[1], W, H, pr, 1.5f);
                assertEquals(Math.round(a * 255f), px[p[1] * W + p[0]] >>> 24, 1);
                assertTrue(a <= 0.95f || a == 1f);
            }
        }
    }
}
