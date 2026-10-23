package org.sleepwell.app;

import org.json.JSONObject;

/** Immutable worker-produced selection; each event may cover only part of its real WAV. */
final class ClipTarget implements ClipReviewPlayer.Target {
    final JSONObject clip,event;final long fromMs,durationMs,wholeDurationMs;
    ClipTarget(JSONObject clip,JSONObject event,long fromMs,long durationMs,long wholeDurationMs){this.clip=clip;this.event=event;this.fromMs=fromMs;this.durationMs=durationMs;this.wholeDurationMs=wholeDurationMs;}
    public String key(){return event==null?"clip:"+clip.optLong("id"):"event:"+event.optLong("id");}
    public long duration(){return durationMs;}
}
