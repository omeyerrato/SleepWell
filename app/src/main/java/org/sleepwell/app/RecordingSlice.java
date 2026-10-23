package org.sleepwell.app;

/** Exact recorded interval; a classification without complete PCM coverage is not playable. */
final class RecordingSlice {
    final long fromMs,durationMs;
    private RecordingSlice(long fromMs,long durationMs){this.fromMs=fromMs;this.durationMs=durationMs;}
    static RecordingSlice contained(long clipEnd,long actualDuration,long eventStart,long eventDuration){
        if(actualDuration<=0||eventDuration<=0)throw new IllegalArgumentException("Recording duration is unavailable");
        try{long clipStart=Math.subtractExact(clipEnd,actualDuration),eventEnd=Math.addExact(eventStart,eventDuration);if(eventStart<clipStart||eventEnd>clipEnd)throw new IllegalArgumentException("Recording does not contain this sound event");return new RecordingSlice(eventStart-clipStart,eventDuration);}catch(ArithmeticException error){throw new IllegalArgumentException("Recording time is invalid",error);}
    }
}
