package org.sleepwell.app;
import android.content.Intent;import android.os.Bundle;import android.provider.AlarmClock;
/** Parse only standard scalar fields; never forward a foreign Intent, ClipData, URI or grants. */
final class TimerRequestInput {
 static TimerRequest read(Intent source){if(source==null||!AlarmClock.ACTION_SET_TIMER.equals(source.getAction())||source.getData()!=null||source.getClipData()!=null)throw new IllegalArgumentException("This timer request is unsupported.");Bundle extras=source.getExtras();Object length=extras==null?null:extras.get(AlarmClock.EXTRA_LENGTH),message=extras==null?null:extras.get(AlarmClock.EXTRA_MESSAGE),skip=extras==null?null:extras.get(AlarmClock.EXTRA_SKIP_UI);if(length!=null&&!(length instanceof Integer)||message!=null&&!(message instanceof String)||skip!=null&&!(skip instanceof Boolean))throw new IllegalArgumentException("Use a whole-number timer length in seconds and a text message.");return new TimerRequest((Integer)length,(String)message,Boolean.TRUE.equals(skip));}
 private TimerRequestInput(){}
}
