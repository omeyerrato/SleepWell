package org.sleepwell.app;

import android.app.Activity;
import android.content.*;
import android.os.*;
import android.view.View;
import android.widget.*;
import java.util.function.LongSupplier;

/** Foreground-only preview view/lifetime. Recreated views never resume a decoder. */
final class AlarmGentleUi implements AutoCloseable {
    static LongSupplier clock=SystemClock::elapsedRealtime;
    private final Activity activity;private final AlarmGentlePreview preview;private final Handler main=new Handler(Looper.getMainLooper());private Alarm alarm;private Button action;private TextView status;private boolean closed,paused,gentle;
    private final BroadcastReceiver noisy=new BroadcastReceiver(){public void onReceive(Context c,Intent intent){stop("Preview stopped after the audio output changed.");}};
    private final Runnable tick=new Runnable(){public void run(){if(closed)return;preview.tick();if(preview.active())main.postDelayed(this,50);}};
    AlarmGentleUi(Activity activity,Bundle state){this.activity=activity;String restored=state==null?"":state.getBoolean("active")?"Preview stopped after recreation. Tap to restart.":state.getString("message","");preview=new AlarmGentlePreview(()->AlarmToneAudio.provider.create(activity.getApplicationContext()),clock,this::update,restored,state!=null&&!state.getBoolean("active")&&state.getBoolean("error"));androidx.core.content.ContextCompat.registerReceiver(activity,noisy,new IntentFilter("android.media.AUDIO_BECOMING_NOISY"),androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);}
    Bundle save(){Bundle out=new Bundle();out.putBoolean("active",preview.active());out.putString("message",preview.message());out.putBoolean("error",preview.phase()==AlarmGentlePreview.Phase.ERROR);return out;}
    void render(LinearLayout footer,Alarm draft){alarm=draft;gentle=true;preview.visible(!paused&&activity.hasWindowFocus());action=new Button(activity);action.setSoundEffectsEnabled(false);action.setHapticFeedbackEnabled(false);action.setAllCaps(false);action.setTextSize(14*Appearance.scale(activity));action.setTypeface(Design.font(activity,600));action.setTextColor(Design.INK);action.setStateListAnimator(null);action.setElevation(0);action.setMinimumHeight(Design.dp(activity,48));action.setMinHeight(Design.dp(activity,48));action.setPadding(Design.dp(activity,12),Design.dp(activity,4),Design.dp(activity,12),Design.dp(activity,4));action.setBackground(Design.touch(activity,Design.SOFT,14));action.setOnClickListener(v->{preview.start(alarm);main.removeCallbacks(tick);if(preview.active())main.post(tick);});footer.addView(action,0,new LinearLayout.LayoutParams(-1,-2));status=Design.text(activity,"",12,400,Design.MUTED);status.setContentDescription("Gentle preview status");status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);footer.addView(status,1,new LinearLayout.LayoutParams(-1,-2));update();}
    private void update(){if(closed||action==null||alarm==null)return;AlarmGentlePreview.Sample sample=new AlarmGentlePreview.Sample(alarm);String title=preview.phase()==AlarmGentlePreview.Phase.LOADING?"Cancel preview":preview.phase()==AlarmGentlePreview.Phase.PLAYING?"Stop preview":"Preview at low volume";android.text.SpannableString label=new android.text.SpannableString(title+"\n"+sample.disclosure());label.setSpan(new android.text.style.RelativeSizeSpan(.76f),title.length()+1,label.length(),android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);action.setText(label);action.setContentDescription(title);action.setEnabled(sample.issue.isEmpty());action.setAlpha(action.isEnabled()?1:.5f);String text=!sample.issue.isEmpty()?sample.issue:preview.phase()==AlarmGentlePreview.Phase.LOADING?"Opening low-volume preview...":preview.message();status.setTextColor(preview.phase()==AlarmGentlePreview.Phase.ERROR?0xffa93919:Design.MUTED);status.setText(text);status.setVisibility(text.isEmpty()?View.GONE:View.VISIBLE);}
    void route(boolean current){gentle=current;if(!current){main.removeCallbacks(tick);preview.stop("");preview.visible(false);}}
    void focus(boolean focused){preview.visible(gentle&&focused&&!paused);if(!focused)main.removeCallbacks(tick);}
    void resume(){paused=false;preview.visible(gentle&&activity.hasWindowFocus());}
    void pause(){paused=true;preview.visible(false);main.removeCallbacks(tick);}
    void stop(String message){main.removeCallbacks(tick);if(preview.active())preview.stop(message);}
    @Override public void close(){if(closed)return;closed=true;main.removeCallbacks(tick);preview.close();activity.unregisterReceiver(noisy);}
}
