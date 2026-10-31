package org.sleepwell.app;

import android.app.Activity;
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
    private final Button connect;
    private final LinearLayout card,lampHeader;
    private long generation;
    private boolean closed;
    AppearanceLightCard(Activity activity,LinearLayout body,Runnable configure){
        owner=activity;
        LinearLayout heading=Design.row(activity);heading.setMinimumHeight(Design.dp(activity,30));
        LinearLayout.LayoutParams glyph=new LinearLayout.LayoutParams(Design.dp(activity,26),Design.dp(activity,26));glyph.rightMargin=Design.dp(activity,18);
        heading.addView(new Design.Icon(activity,"bulb",Design.INK),glyph);LinearLayout headingCopy=Design.col(activity);Design.text(headingCopy,"Smart lights",14,500,Design.INK);Design.text(headingCopy,"Fade in your bedroom light with the alarm.",11,400,Design.MUTED);heading.addView(headingCopy,new LinearLayout.LayoutParams(0,-2,1));body.addView(heading);
        card=Design.row(activity);card.setGravity(Gravity.TOP);card.setBaselineAligned(false);card.setMinimumHeight(Design.dp(activity,184));card.setBackground(Design.shape(activity,Design.SOFT,16));card.setClipToOutline(true);
        // The controls determine card height; the decorative bitmap fills that height on the uniform pass.
        // Framework Activity and bitmap rendering on API 30+; no AppCompat inflation or tint is used.
        @android.annotation.SuppressLint("AppCompatCustomView")
        ImageView art=new ImageView(activity){@Override protected void onMeasure(int width,int height){if(View.MeasureSpec.getMode(height)==View.MeasureSpec.UNSPECIFIED)setMeasuredDimension(View.MeasureSpec.getSize(width),0);else super.onMeasure(width,height);}};art.setImageResource(R.drawable.sunrise_bedroom_raster);art.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);art.setScaleType(ImageView.ScaleType.CENTER_CROP);
        card.addView(art,new LinearLayout.LayoutParams(0,-1,.42f));
        // Four real48dp actions share the card. Reuse its former vertical padding and
        // quiet rules for the routed header, rather than extending under fixed Save.
        LinearLayout copy=Design.col(activity);copy.setPadding(Design.dp(activity,10),0,Design.dp(activity,10),Design.dp(activity,4));card.addView(copy,new LinearLayout.LayoutParams(0,-2,.58f));
        lampHeader=Design.row(activity);lampHeader.setMinimumHeight(Design.dp(activity,48));lampHeader.setGravity(Gravity.CENTER_VERTICAL);lampHeader.setBackground(Design.touch(activity,android.graphics.Color.TRANSPARENT,8));lampHeader.setContentDescription("Configure bedroom lamp");lampHeader.setStateDescription("Bedroom lamp · Loading saved settings…");lampHeader.setFocusable(true);lampHeader.setOnClickListener(view->{InteractionFeedback.action(view);configure.run();});
        LinearLayout headerCopy=Design.col(activity);name=Design.text(headerCopy,"Bedroom lamp",14,600,Design.INK);status=Design.text(headerCopy,"Loading saved settings…",12,400,Design.MUTED);lampHeader.addView(headerCopy,new LinearLayout.LayoutParams(0,-2,1));Design.Icon next=new Design.Icon(activity,"chevron",Design.INK);next.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);lampHeader.addView(next,new LinearLayout.LayoutParams(Design.dp(activity,14),Design.dp(activity,24)));copy.addView(lampHeader,new LinearLayout.LayoutParams(-1,-2));
        duration=detail(copy,"Sunrise duration","—","Configure lamp sunrise duration",configure);
        intensity=detail(copy,"Maximum intensity","—","Configure lamp maximum intensity",configure);
        connect=Design.button(copy,"Connect light",false,configure);connect.setText(RecordUi.withIcon(activity,"Connect light","link",Design.BROWN));connect.setTextSize(13*Appearance.scale(activity));connect.setMinHeight(Design.dp(activity,48));connect.setMinimumHeight(Design.dp(activity,48));connect.setPadding(Design.dp(activity,6),0,Design.dp(activity,6),0);connect.setBackground(Design.touch(activity,0xffead9c7,12));LinearLayout.LayoutParams connectSpace=(LinearLayout.LayoutParams)connect.getLayoutParams();connectSpace.height=Design.dp(activity,48);connectSpace.topMargin=0;connect.setLayoutParams(connectSpace);connect.setContentDescription("Configure smart bedroom light");
        card.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(){@Override public void onViewAttachedToWindow(View view){refresh();}@Override public void onViewDetachedFromWindow(View view){generation++;}});
        body.addView(card,new LinearLayout.LayoutParams(-1,-2));
    }
    private TextView detail(LinearLayout parent,String title,String value,String description,Runnable action){
        LinearLayout row=new DetailRow(owner);row.setMinimumHeight(Design.dp(owner,48));row.setBackground(Design.touch(owner,android.graphics.Color.TRANSPARENT,8));
        TextView label=Design.text(owner,title,12,400,Design.MUTED);TextView amount=Design.text(owner,value,12,500,Design.INK);
        if(Design.fontScale(owner)>1.15f){LinearLayout values=Design.col(owner);values.addView(label);Design.space(values,2);values.addView(amount);row.addView(values,new LinearLayout.LayoutParams(0,-2,1));}
        else{row.addView(label,new LinearLayout.LayoutParams(0,-2,1));LinearLayout.LayoutParams amountSpace=new LinearLayout.LayoutParams(-2,-2);amountSpace.leftMargin=Design.dp(owner,6);row.addView(amount,amountSpace);}
        row.addView(new Design.Icon(owner,"chevron",Design.INK),new LinearLayout.LayoutParams(Design.dp(owner,14),Design.dp(owner,24)));
        row.setContentDescription(description);row.setFocusable(true);row.setOnClickListener(v->{InteractionFeedback.action(v);action.run();});parent.addView(row);return amount;
    }
    /** Quiet rules sit inside the unchanged48dp rows rather than consuming action space. */
    private static final class DetailRow extends LinearLayout {
        private final android.graphics.Paint rule=new android.graphics.Paint();
        DetailRow(android.content.Context context){super(context);setGravity(Gravity.CENTER_VERTICAL);setWillNotDraw(false);rule.setColor(Design.LINE);}
        @Override protected void onDraw(android.graphics.Canvas canvas){super.onDraw(canvas);canvas.drawRect(0,getHeight()-1,getWidth(),getHeight(),rule);}
    }
    void refresh(){
        // onResume may run before attachment; attachment owns the initial read instead of losing a fast result.
        if(closed||!card.isAttachedToWindow())return;long ticket=++generation;card.setContentDescription(null);
        worker.execute(()->{
            String label="Bedroom lamp",state="Not connected",action="Connect light";Integer minutes=null,maximum=null;
            try{
                SmartLightConfig home=SmartLightSettings.load(owner);HueBridgeConfig hue=HueBridgeSettings.load(owner);
                if(!home.endpoint.isEmpty()||!hue.address.isEmpty())action="Configure light";
                if(hue.enabled||home.endpoint.isEmpty()&&!hue.address.isEmpty()){
                    label=hue.name.isEmpty()?"Bedroom lamp":hue.name;state=hue.enabled?"Hue · enabled":"Hue · off";minutes=hue.minutes;maximum=hue.maximum;
                }else{state=home.endpoint.isEmpty()?"Not connected":home.enabled?"Home Assistant · enabled":"Home Assistant · off";minutes=home.minutes;maximum=home.maximum;}
            }catch(Exception|LinkageError unavailable){state="Review saved connection";action="Review light";}
            String finalLabel=label,finalState=state,finalAction=action;Integer finalMinutes=minutes,finalMaximum=maximum;
            main.post(()->{if(closed||ticket!=generation||owner.isDestroyed()||owner.isFinishing()||!card.isAttachedToWindow())return;name.setText(finalLabel);status.setText(finalState);lampHeader.setStateDescription(finalLabel+" · "+finalState);duration.setText(finalMinutes==null?"—":finalMinutes+" min");intensity.setText(finalMaximum==null?"—":finalMaximum+"%");connect.setText(RecordUi.withIcon(owner,finalAction,"link",Design.BROWN));card.setContentDescription("Bedroom lamp summary ready");});
        });
    }
    @Override public void close(){closed=true;generation++;main.removeCallbacksAndMessages(null);worker.shutdownNow();}
}
