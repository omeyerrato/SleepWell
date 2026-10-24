package org.sleepwell.app;

import org.junit.Test;
import static org.junit.Assert.*;

/** Provider display text remains bounded, plain text, and never becomes a URI. */
public class AlarmToneNameTest {
    @Test public void plainNamesKeepUnicodeButLoseInvisibleControls() {
        assertEquals("ليل 🌙.wav", Alarm.cleanToneName("  ليل 🌙.wav  "));
        assertEquals("a b c.wav", Alarm.cleanToneName("a\u202eb\nc.wav"));
        assertEquals("a b c.wav", Alarm.cleanToneName("a\u2028b\u2029c.wav"));
        assertEquals("", Alarm.cleanToneName(null));
        assertEquals("", Alarm.cleanToneName(7));
    }

    @Test public void longNamesAreLimitedWithoutSplittingSurrogatePairs() {
        String glyph = new String(Character.toChars(0x1f319));
        String result = Alarm.cleanToneName(glyph.repeat(101));
        assertEquals(100, result.codePointCount(0, result.length()));
        assertEquals(glyph.repeat(100), result);
    }
}
