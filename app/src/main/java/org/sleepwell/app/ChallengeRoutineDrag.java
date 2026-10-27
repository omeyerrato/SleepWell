package org.sleepwell.app;

import android.app.Activity;
import android.content.ClipData;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/** Local draft ordering only. The private snapshot never enters ClipData or a global drag. */
final class ChallengeRoutineDrag {
    interface Move {boolean apply(int source,int destination);}
    private final Activity owner;
    private final LinearLayout body;
    private final BooleanSupplier route;
    private final Supplier<String> snapshot;
    private final Move move;
    private final ScrollView scroll;
    private Session active;
    private int direction;
    private boolean scrolling;

    ChallengeRoutineDrag(Activity owner,LinearLayout body,BooleanSupplier route,Supplier<String> snapshot,Move move){
        this.owner=owner;this.body=body;this.route=route;this.snapshot=snapshot;this.move=move;
        scroll=body.getParent() instanceof ScrollView parent?parent:null;
        body.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(){
            public void onViewAttachedToWindow(View view){}
            public void onViewDetachedFromWindow(View view){finish(active);}
        });
        if(scroll!=null)scroll.setOnDragListener((view,event)->{
            Session session=local(event);if(session==null)return false;
            if(event.getAction()==DragEvent.ACTION_DRAG_ENDED){finish(session);return true;}
            if(event.getAction()==DragEvent.ACTION_DRAG_EXITED){direction=0;return true;}
            if(!current(session))return false;
            if(event.getAction()==DragEvent.ACTION_DRAG_LOCATION)pointer(view,event.getY());
            // Blank space can scroll, but can never reorder a task.
            return event.getAction()!=DragEvent.ACTION_DROP;
        });
    }

    View handle(String description,Runnable actions){
        Handle handle=new Handle(owner);
        handle.setContentDescription(description);
        handle.setStateDescription("Hold and drag to reorder; tap for task actions");
        handle.setTooltipText("Hold and drag to reorder. Tap for task actions.");
        handle.setBackground(Design.touch(owner,Color.TRANSPARENT,12));
        // Suppress the framework long-click pulse; owned acknowledgements use the common quiet/settings policy.
        handle.setHapticFeedbackEnabled(false);handle.setFocusable(true);
        handle.setOnClickListener(view->{InteractionFeedback.action(body);actions.run();});
        return handle;
    }

    void bind(View row,View handle,int index){
        Drawable original=row.getForeground();
        GradientDrawable highlight=Design.shape(owner,Color.TRANSPARENT,16);
        highlight.setStroke(Design.dp(owner,2),Design.BROWN);
        handle.setOnLongClickListener(view->{
            if(active!=null||!eligible()||!view.isEnabled())return false;
            String saved;try{saved=snapshot.get();}catch(RuntimeException stale){return false;}
            Session session=new Session(this,index,saved,row);active=session;
            boolean started;
            try{started=view.startDragAndDrop(ClipData.newPlainText("SleepWell task order",""),new View.DragShadowBuilder(row),session,0);}
            catch(RuntimeException unavailable){started=false;}
            if(!started){finish(session);return false;}
            row.setAlpha(.65f);InteractionFeedback.selection(row);return true;
        });
        row.setOnDragListener((view,event)->{
            Session session=local(event);if(session==null)return false;
            int action=event.getAction();
            if(action==DragEvent.ACTION_DRAG_ENDED){row.setForeground(original);finish(session);return true;}
            if(action==DragEvent.ACTION_DRAG_EXITED){row.setForeground(original);direction=0;return true;}
            if(!current(session)){row.setForeground(original);return false;}
            if(action==DragEvent.ACTION_DRAG_ENTERED){if(index!=session.index)row.setForeground(highlight);return true;}
            if(action==DragEvent.ACTION_DRAG_LOCATION){pointer(view,event.getY());return true;}
            if(action==DragEvent.ACTION_DROP){
                row.setForeground(original);direction=0;session.dropped=true;
                if(index==session.index)return true;
                boolean changed=move.apply(session.index,index);
                if(changed){View decor=owner.getWindow().getDecorView();InteractionFeedback.selection(decor);decor.announceForAccessibility("Task moved to position "+(index+1));}
                return changed;
            }
            return true;
        });
    }

    private boolean eligible(){return !owner.isFinishing()&&!owner.isDestroyed()&&owner.hasWindowFocus()&&body.isAttachedToWindow()&&route.getAsBoolean();}
    private Session local(DragEvent event){return event.getLocalState() instanceof Session session&&session.controller==this?session:null;}
    private boolean current(Session session){
        if(session!=active||session.ended||session.dropped||!eligible())return false;
        try{return session.snapshot.equals(snapshot.get());}catch(RuntimeException stale){return false;}
    }
    private void finish(Session session){
        if(session==null)return;
        session.ended=true;session.source.setAlpha(session.alpha);direction=0;
        if(scroll!=null)scroll.removeCallbacks(scrollStep);
        scrolling=false;if(active==session)active=null;
    }
    private void pointer(View target,float y){
        if(scroll==null)return;
        int[] targetAt=new int[2],scrollAt=new int[2];target.getLocationOnScreen(targetAt);scroll.getLocationOnScreen(scrollAt);
        float within=targetAt[1]+y-scrollAt[1];int edge=Design.dp(owner,48);
        direction=within<edge?-1:within>scroll.getHeight()-edge?1:0;
        if(direction!=0&&!scrolling){scrolling=true;scroll.postDelayed(scrollStep,40);}
    }
    private final Runnable scrollStep=new Runnable(){public void run(){
        if(active==null||!current(active)||direction==0||scroll==null){scrolling=false;return;}
        int before=scroll.getScrollY();scroll.scrollBy(0,direction*Design.dp(owner,12));
        if(scroll.getScrollY()==before){scrolling=false;return;}
        scroll.postDelayed(this,40);
    }};
    private static final class Session {
        final ChallengeRoutineDrag controller;final int index;final String snapshot;final View source;final float alpha;
        boolean dropped,ended;
        Session(ChallengeRoutineDrag controller,int index,String snapshot,View source){this.controller=controller;this.index=index;this.snapshot=snapshot;this.source=source;alpha=source.getAlpha();}
    }
    /** Compose the two columns from the app's licensed Phosphor three-dot glyph. */
    private static final class Handle extends View {
        private final Drawable dots;
        Handle(Activity owner){super(owner);dots=owner.getDrawable(R.drawable.ic_phosphor_dots_three_vertical_regular).mutate();dots.setTint(Appearance.color(owner,Design.MUTED));setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);}
        @Override public CharSequence getAccessibilityClassName(){return Button.class.getName();}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);int size=Design.dp(getContext(),24),offset=Design.dp(getContext(),3),top=(getHeight()-size)/2;for(int column:new int[]{-1,1}){int left=(getWidth()-size)/2+column*offset;dots.setBounds(left,top,left+size,top+size);dots.draw(canvas);}}
    }
}
