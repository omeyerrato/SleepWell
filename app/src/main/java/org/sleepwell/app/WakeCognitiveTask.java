package org.sleepwell.app;

import android.os.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Actual matching, sequence and retype interactions; the caller alone owns advancement. */
abstract class WakeCognitiveTask implements AutoCloseable {
    final LinearLayout card;
    final String key;
    final BooleanSupplier current;
    final Runnable complete;
    final Handler main=new Handler(Looper.getMainLooper());
    boolean closed,delivered;

    WakeCognitiveTask(LinearLayout card,String run,int unit,BooleanSupplier current,Runnable complete){
        this.card=card;key=run+":"+unit;this.current=current;this.complete=complete;
    }
    final boolean live(){return Looper.myLooper()==Looper.getMainLooper()&&!closed&&!delivered&&current.getAsBoolean()
            &&card.isAttachedToWindow()&&card.isShown()&&card.getContext() instanceof android.app.Activity activity
            &&card.getRootView()==activity.getWindow().getDecorView();}
    final void finish(){if(!live())return;delivered=true;main.removeCallbacksAndMessages(null);complete.run();}
    final boolean restores(Bundle state,String type){return state!=null&&key.equals(state.getString("key"))&&type.equals(state.getString("type"));}
    final Bundle base(String type){Bundle state=new Bundle();state.putString("key",key);state.putString("type",type);return state;}
    abstract Bundle state();
    void pause(){main.removeCallbacksAndMessages(null);}
    void resume(){}
    @Override public void close(){closed=true;main.removeCallbacksAndMessages(null);}

    static final class Memory extends WakeCognitiveTask {
        private static final String[] SYMBOLS={"☀","☽","✿"};
        final int[] order;
        final Button[] buttons=new Button[6];
        int matched,first=-1;
        boolean waiting;

        Memory(LinearLayout card,String run,int unit,Random random,Bundle restored,BooleanSupplier current,Runnable complete){
            super(card,run,unit,current,complete);
            int[] saved=restores(restored,"Memory")?restored.getIntArray("order"):null;
            if(valid(saved)){order=saved.clone();matched=restored.getInt("matched");if(!validPairs(matched))matched=0;int shown=restored.getInt("first",-1);if(shown>=0&&shown<6&&(matched&(1<<shown))==0)first=shown;}
            else{List<Integer> values=new ArrayList<>(List.of(0,0,1,1,2,2));Collections.shuffle(values,random);order=values.stream().mapToInt(Integer::intValue).toArray();}
            Ui.heading(card,"Find the matching pairs",24);
            GridLayout grid=new GridLayout(card.getContext());grid.setColumnCount(3);card.addView(grid);
            for(int n=0;n<6;n++){
                final int index=n;Button button=new Button(card.getContext());buttons[n]=button;
                button.setStateListAnimator(null);button.setElevation(0);button.setTextSize(25*Appearance.scale(card.getContext()));button.setTextColor(Ui.BG);
                GridLayout.LayoutParams size=new GridLayout.LayoutParams();size.width=Ui.dp(card.getContext(),76);size.height=Ui.dp(card.getContext(),68);size.setMargins(3,3,3,3);grid.addView(button,size);
                refresh(n);button.setOnClickListener(ignored->tap(index));
            }
            // A focus interruption can occur between earning the final pair and owner admission.
            // Keep the earned board, then require a fresh visible action to deliver that unit.
            if(matched==63)Design.button(card,"Continue",true,this::finish);
        }
        private static boolean valid(int[] values){if(values==null||values.length!=6)return false;int[] counts=new int[3];for(int value:values){if(value<0||value>=3)return false;counts[value]++;}return counts[0]==2&&counts[1]==2&&counts[2]==2;}
        private boolean validPairs(int mask){if(mask<0||mask>=64)return false;int[] counts=new int[3];for(int n=0;n<6;n++)if((mask&(1<<n))!=0)counts[order[n]]++;for(int count:counts)if(count!=0&&count!=2)return false;return true;}
        private void refresh(int n){boolean paired=(matched&(1<<n))!=0,revealed=paired||first==n;buttons[n].setText(revealed?SYMBOLS[order[n]]:"?");buttons[n].setContentDescription(revealed?SYMBOLS[order[n]]:"Hidden memory card");buttons[n].setEnabled(!paired);}
        private void tap(int index){
            if(!live()||waiting||first==index||(matched&(1<<index))!=0)return;
            buttons[index].setText(SYMBOLS[order[index]]);buttons[index].setContentDescription(SYMBOLS[order[index]]);
            if(first<0){first=index;return;}
            int previous=first;first=-1;
            if(order[previous]==order[index]){matched|=(1<<previous)|(1<<index);refresh(previous);refresh(index);if(matched==63)finish();}
            else{waiting=true;main.postDelayed(()->{if(!live())return;refresh(previous);refresh(index);waiting=false;},700);}
        }
        @Override Bundle state(){Bundle state=base("Memory");state.putIntArray("order",order.clone());state.putInt("matched",matched);state.putInt("first",waiting?-1:first);return state;}
        @Override void pause(){super.pause();if(waiting){waiting=false;first=-1;for(int n=0;n<6;n++)refresh(n);}}
    }

    static final class Sequence extends WakeCognitiveTask {
        final Random random;
        final int length;
        final TextView prompt;
        String expected,entered="";
        long revealUntil;

        Sequence(LinearLayout card,String run,int unit,int round,Random random,Bundle restored,BooleanSupplier current,Runnable complete){
            super(card,run,unit,current,complete);this.random=random;length=3+Math.min(3,round);
            if(round<0||round>5)throw new IllegalArgumentException("Unsupported sequence round");
            boolean restoring=restores(restored,"Sequence")&&digits(restored.getString("expected",""),length);
            if(restoring){
                expected=restored.getString("expected");String answer=restored.getString("entered","");if(answer.length()<length&&expected.startsWith(answer))entered=answer;
            }else fresh();
            Ui.heading(card,"Watch. Remember. Repeat.",22);prompt=Ui.heading(card,"",32);
            long remaining=restoring?Math.max(0,Math.min(2500,restored.getLong("revealRemaining"))):2500;
            if(!entered.isEmpty())prompt.setText(entered);else if(remaining>0)reveal(remaining);else prompt.setText("Now repeat the sequence");
            LinearLayout row=new LinearLayout(card.getContext());card.addView(row);
            for(int n=1;n<=4;n++){final int value=n;Button button=new Button(card.getContext());button.setStateListAnimator(null);button.setElevation(0);button.setText(Integer.toString(n));button.setTextColor(Ui.BG);row.addView(button,new LinearLayout.LayoutParams(0,Ui.dp(card.getContext(),56),1));button.setOnClickListener(ignored->tap(value));}
            Ui.button(card,"Show sequence again",()->{if(!live())return;entered="";reveal(2500);});
        }
        private static boolean digits(String value,int length){if(value.length()!=length)return false;for(int n=0;n<length;n++)if(value.charAt(n)<'1'||value.charAt(n)>'4')return false;return true;}
        private void fresh(){StringBuilder value=new StringBuilder();for(int n=0;n<length;n++)value.append(1+random.nextInt(4));expected=value.toString();entered="";}
        private void reveal(long duration){main.removeCallbacksAndMessages(null);revealUntil=SystemClock.elapsedRealtime()+duration;prompt.setText(expected);main.postDelayed(()->{if(!live())return;revealUntil=0;prompt.setText(entered.isEmpty()?"Now repeat the sequence":entered);},duration);}
        private void tap(int value){if(!live())return;entered+=value;prompt.setText(entered);if(!expected.startsWith(entered)){Ui.toast(card.getContext(),"Watch once more");fresh();reveal(2500);}else if(entered.equals(expected))finish();}
        @Override Bundle state(){Bundle state=base("Sequence");state.putString("expected",expected);state.putString("entered",entered);state.putLong("revealRemaining",Math.max(0,Math.min(2500,revealUntil-SystemClock.elapsedRealtime())));return state;}
        @Override void resume(){if(!live())return;long remaining=revealUntil-SystemClock.elapsedRealtime();if(revealUntil>0&&remaining>0)reveal(remaining);else{revealUntil=0;prompt.setText(entered.isEmpty()?"Now repeat the sequence":entered);}}
    }

    static final class Retype extends WakeCognitiveTask {
        final EditText input;
        final String phrase;
        Retype(LinearLayout card,String run,int unit,String phrase,boolean practice,Bundle restored,BooleanSupplier current,Runnable complete){
            super(card,run,unit,current,complete);this.phrase=phrase;
            Design.text(card,practice?"Retype the phrase":"Retype to dismiss",22,600,Design.INK).setGravity(Gravity.CENTER);
            Design.space(card,16);Design.text(card,phrase,26,700,Design.INK).setGravity(Gravity.CENTER);
            String saved=restores(restored,"Retype")&&phrase.equals(restored.getString("phrase"))?restored.getString("answer",""):"";
            input=Ui.input(card,"Your answer",saved,InputType.TYPE_CLASS_TEXT);
            input.setMinimumHeight(Design.dp(card.getContext(),48));
            if(practice)input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(120)});
            Ui.button(card,"Check answer",()->{if(!live())return;if(input.getText().toString().trim().equals(phrase))finish();else input.setError("Try again");},true);
        }
        @Override Bundle state(){Bundle state=base("Retype");state.putString("phrase",phrase);state.putString("answer",input.getText().toString());return state;}
    }
}
