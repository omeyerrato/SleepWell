package org.sleepwell.app;

import android.app.Activity;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.concurrent.*;

/** Local saved configuration only. Opening Appearance never contacts or changes a lamp. */
final class AppearanceLightCard implements AutoCloseable {
    private final Activity owner;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final Handler main=new Handler(Looper.getMainLooper());
    private final TextView name,status,duration,intensity;
    private final LinearLayout card;
    private long generation;
    private boolean closed;
    AppearanceLightCard(Activity activity,LinearLayout body,Runnable configure){
        owner=activity;
        LinearLayout heading=Design.row(activity);heading.setMinimumHeight(Design.dp(activity,40));
        LinearLayout.LayoutParams glyph=new LinearLayout.LayoutParams(Design.dp(activity,26),Design.dp(activity,26));glyph.rightMargin=Design.dp(activity,18);
        heading.addView(new Design.Icon(activity,"bulb",Design.INK),glyph);LinearLayout headingCopy=Design.col(activity);Design.text(headingCopy,"Smart lights",15,500,Design.INK);Design.space(headingCopy,4);Design.text(headingCopy,"Fade in your bedroom light with the alarm.",11,400,Design.MUTED);heading.addView(headingCopy,new LinearLayout.LayoutParams(0,-2,1));body.addView(heading);
        card=Design.row(activity);card.setGravity(Gravity.TOP);card.setBaselineAligned(false);card.setMinimumHeight(Design.dp(activity,184));card.setBackground(Design.shape(activity,Design.SOFT,16));card.setClipToOutline(true);
        // The controls determine card height; the decorative bitmap fills that height on the uniform pass.
        ImageView art=new ImageView(activity){@Override protected void onMeasure(int width,int height){if(View.MeasureSpec.getMode(height)==View.MeasureSpec.UNSPECIFIED)setMeasuredDimension(View.MeasureSpec.getSize(width),0);else super.onMeasure(width,height);}};art.setImageResource(R.drawable.bedside_dawn);art.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);art.setScaleType(ImageView.ScaleType.MATRIX);
        art.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{if(art.getDrawable()==null||art.getWidth()==0||art.getHeight()==0)return;float w=art.getDrawable().getIntrinsicWidth(),h=art.getDrawable().getIntrinsicHeight(),scale=Math.max(art.getWidth()/w,art.getHeight()/h);Matrix matrix=new Matrix();matrix.setScale(scale,scale);matrix.postTranslate(Math.max(art.getWidth()-w*scale,Math.min(0,art.getWidth()*.5f-w*.30f*scale)),(art.getHeight()-h*scale)*.5f);art.setImageMatrix(matrix);});
        card.addView(art,new LinearLayout.LayoutParams(Design.dp(activity,112),-1));
        LinearLayout copy=Design.col(activity);Design.pad(copy,12,4);card.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        name=Design.text(copy,"Bedroom lamp",14,600,Design.INK);status=Design.text(copy,"Loading saved settings…",11,400,Design.MUTED);
        duration=detail(copy,"Sunrise duration","—","Configure lamp sunrise duration",configure);
        intensity=detail(copy,"Maximum intensity","—","Configure lamp maximum intensity",configure);
        Button connect=Design.button(copy,"Connect light",false,configure);connect.setTextSize(13*Appearance.scale(activity));connect.setMinHeight(Design.dp(activity,48));connect.setMinimumHeight(Design.dp(activity,48));connect.setPadding(Design.dp(activity,8),0,Design.dp(activity,8),0);LinearLayout.LayoutParams connectSpace=(LinearLayout.LayoutParams)connect.getLayoutParams();connectSpace.height=Design.dp(activity,48);connectSpace.topMargin=0;connect.setLayoutParams(connectSpace);connect.setContentDescription("Configure smart bedroom light");
        card.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(){@Override public void onViewAttachedToWindow(View view){refresh();}@Override public void onViewDetachedFromWindow(View view){generation++;}});
        body.addView(card,new LinearLayout.LayoutParams(-1,-2));
    }
    private TextView detail(LinearLayout parent,String title,String value,String description,Runnable action){
        LinearLayout row=Design.row(owner);row.setMinimumHeight(Design.dp(owner,48));row.setBackground(Design.touch(owner,android.graphics.Color.TRANSPARENT,8));
        TextView label=Design.text(owner,title,11,400,Design.MUTED);row.addView(label,new LinearLayout.LayoutParams(0,-2,1));
        TextView amount=Design.text(owner,value,11,500,Design.INK);LinearLayout.LayoutParams amountSpace=new LinearLayout.LayoutParams(-2,-2);amountSpace.leftMargin=Design.dp(owner,6);row.addView(amount,amountSpace);
        row.addView(new Design.Icon(owner,"chevron",Design.INK),new LinearLayout.LayoutParams(Design.dp(owner,14),Design.dp(owner,24)));
        row.setContentDescription(description);row.setFocusable(true);row.setOnClickListener(v->{InteractionFeedback.action(v);action.run();});parent.addView(row);Design.line(parent);return amount;
    }
    void refresh(){
        // onResume may run before attachment; attachment owns the initial read instead of losing a fast result.
        if(closed||!card.isAttachedToWindow())return;long ticket=++generation;card.setContentDescription(null);
        worker.execute(()->{
            String label="Bedroom lamp",state="Not connected";Integer minutes=null,maximum=null;
            try{
                SmartLightConfig home=SmartLightSettings.load(owner);HueBridgeConfig hue=HueBridgeSettings.load(owner);
                if(hue.enabled||home.endpoint.isEmpty()&&!hue.address.isEmpty()){
                    label=hue.name.isEmpty()?"Bedroom lamp":hue.name;state=hue.enabled?"Hue · enabled":"Hue · off";minutes=hue.minutes;maximum=hue.maximum;
                }else{state=home.endpoint.isEmpty()?"Not connected":home.enabled?"Home Assistant · enabled":"Home Assistant · off";minutes=home.minutes;maximum=home.maximum;}
            }catch(Exception|LinkageError unavailable){state="Review saved connection";}
            String finalLabel=label,finalState=state;Integer finalMinutes=minutes,finalMaximum=maximum;
            main.post(()->{if(closed||ticket!=generation||owner.isDestroyed()||owner.isFinishing()||!card.isAttachedToWindow())return;name.setText(finalLabel);status.setText(finalState);duration.setText(finalMinutes==null?"—":finalMinutes+" min");intensity.setText(finalMaximum==null?"—":finalMaximum+"%");card.setContentDescription("Bedroom lamp summary ready");});
        });
    }
    @Override public void close(){closed=true;generation++;main.removeCallbacksAndMessages(null);worker.shutdownNow();}
}
