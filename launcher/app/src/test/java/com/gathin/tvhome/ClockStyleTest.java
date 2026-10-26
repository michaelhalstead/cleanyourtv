package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** clock_style from a theme: led by name, the default look for anything else. */
public class ClockStyleTest {

    @Test
    public void ledByNameAnyCase() {
        assertEquals(Config.CLOCK_LED, Config.clockStyle("led"));
        assertEquals(Config.CLOCK_LED, Config.clockStyle(" LED "));
    }

    @Test
    public void anythingElseIsDefault() {
        assertEquals("default", Config.clockStyle("default"));
        assertEquals("default", Config.clockStyle(""));
        assertEquals("default", Config.clockStyle(null));
        assertEquals("default", Config.clockStyle("nixie"));
    }
}
