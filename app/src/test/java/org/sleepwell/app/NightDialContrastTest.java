package org.sleepwell.app;

import org.junit.Test;
import static org.junit.Assert.*;

/** Readability of the actual composited token pairs, including action and disabled labels. */
public final class NightDialContrastTest {
    private static double channel(int value){double s=value/255d;return s<=.04045?s/12.92:Math.pow((s+.055)/1.055,2.4);}
    private static double luminance(int color){return .2126*channel((color>>16)&255)+.7152*channel((color>>8)&255)+.0722*channel(color&255);}
    private static double ratio(int first,int second){double a=luminance(first),b=luminance(second);return (Math.max(a,b)+.05)/(Math.min(a,b)+.05);}
    @Test public void readingLabelsRemainLegibleOnAllNativeSurfaces(){for(int ground:new int[]{NightDialTokens.GROUND,NightDialTokens.SURFACE,NightDialTokens.RAISED})for(int ink:new int[]{NightDialTokens.TEXT,NightDialTokens.SECONDARY,NightDialTokens.META,NightDialTokens.NEGATIVE})assertTrue("Reading pair must reach 4.5:1",ratio(ink,ground)>=4.5);}
    @Test public void filledActionsUseDarkInkAndDisabledLabelsStayReadable(){for(int fill:new int[]{NightDialTokens.ACTION,NightDialTokens.ACCENT,NightDialTokens.ACCENT_PRESSED,NightDialTokens.NEGATIVE})assertTrue("Action label must reach 4.5:1",ratio(NightDialTokens.foreground(fill),fill)>=4.5);assertTrue(ratio(NightDialTokens.META,NightDialTokens.RAISED)>=4.5);}
    @Test public void essentialBoundariesAndFocusAreVisible(){for(int ground:new int[]{NightDialTokens.GROUND,NightDialTokens.SURFACE,NightDialTokens.RAISED}){assertTrue(ratio(NightDialTokens.OUTLINE,ground)>=3);assertTrue(ratio(NightDialTokens.ACCENT,ground)>=3);}assertTrue(ratio(NightDialTokens.ACTION_INK,NightDialTokens.ACTION)>=3);}
}
