package org.sleepwell.app;

/** Presentation-only tokens for design.md. No preferences, clocks, capture or alarm effects. */
final class NightDialTokens {
    static final int GROUND=0xff080808, SURFACE=0xff171717, RAISED=0xff242424,
            PRESSED=0xff303030, TEXT=0xfff3f3ef, SECONDARY=0xffb8b8b3,
            META=0xff9c9c97, ACCENT=0xfff2bf6d, ACCENT_PRESSED=0xffdda95b,
            ACCENT_INK=0xff17120b, ACTION=0xfff3f3ef, ACTION_INK=0xff111111,
            SECONDARY_ACTION=0xff2a2a2a, RULE=0xff2c2c2c, OUTLINE=0xff767671,
            NEGATIVE=0xfff18b8b, NEGATIVE_SURFACE=0xff321d1d,
            POSITIVE=0xff91cda5, POSITIVE_SURFACE=0xff1a2b20, SCRIM=0x99000000;
    static final int MARGIN=20, PAD=16, GAP=12, SECTION=28, TARGET=48,
            ACTION_HEIGHT=52, ROW=64, CARD_RADIUS=20, SHEET_RADIUS=24, FIELD_RADIUS=12;
    static int foreground(int background){
        return background==ACTION||background==ACCENT||background==ACCENT_PRESSED||background==NEGATIVE
                ?ACTION_INK:TEXT;
    }
    private NightDialTokens(){}
}
