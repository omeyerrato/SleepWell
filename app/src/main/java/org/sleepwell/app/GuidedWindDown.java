package org.sleepwell.app;
/** Original silent reading guide. Time means foreground practice, never detected sleep. */
final class GuidedWindDown {
 enum State{READY,RUNNING,PAUSED,COMPLETED,ENDED}
 static final String[] TITLES={"Arrive","Notice support","Leave one thought","Return gently","Close the practice"};
 static final String[] WORDS={"Choose a comfortable position. Let your hands rest. You can keep your eyes open and breathe normally.","Notice where the surface supports you. Let your attention rest on one simple point of contact; there is nothing to measure or achieve.","If something from the day needs attention, name it quietly to yourself. You can leave that thought here and return to it another time.","Notice a quiet detail around you, or the ordinary movement of your breath. When attention wanders, return without needing to change anything.","Let the instructions fall away. Decide whether to rest, put the phone down, or end here. This practice does not need a particular result."};
 int minutes;boolean paced;State state=State.READY;private long spent,anchor;
 GuidedWindDown(int minutes,boolean paced){if(minutes!=2&&minutes!=5&&minutes!=10)throw new IllegalArgumentException("Choose 2, 5 or 10 minutes.");this.minutes=minutes;this.paced=paced;}
 long total(){return minutes*60000L;}
 long elapsed(long now){return Math.min(total(),Math.max(0,spent+(state==State.RUNNING?Math.max(0,now-anchor):0)));}
 void tick(long now){if(state==State.RUNNING&&elapsed(now)>=total()){spent=total();state=State.COMPLETED;}}
 void start(long now){spent=0;anchor=now;state=State.RUNNING;}
 void pause(long now){if(state!=State.RUNNING)return;spent=elapsed(now);state=spent>=total()?State.COMPLETED:State.PAUSED;}
 void resume(long now){if(state!=State.PAUSED)return;if(spent>=total()){state=State.COMPLETED;return;}anchor=now;state=State.RUNNING;}
 void end(long now){if(state!=State.RUNNING&&state!=State.PAUSED)return;spent=elapsed(now);state=State.ENDED;}
 int step(long now){return Math.min(4,(int)(elapsed(now)*5/total()));}
 void restore(long elapsed,String saved){spent=Math.max(0,Math.min(total(),elapsed));State prior;try{prior=State.valueOf(saved);}catch(Exception invalid){prior=State.READY;}state=prior==State.RUNNING?State.PAUSED:prior;if(spent>=total()&&state!=State.ENDED)state=State.COMPLETED;if(state==State.READY)spent=0;}
}
