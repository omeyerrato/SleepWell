package org.sleepwell.app;

/** Optional input actions never dismiss a challenge or exceed its remaining snoozes. */
final class AlarmInputs {
    enum Keys {VOLUME,SNOOZE,IGNORE}
    static final AlarmInputs DEFAULT=new AlarmInputs(false,Keys.VOLUME);
    final boolean flip,chooseSnooze;final Keys keys;
    AlarmInputs(boolean flip,Keys keys){this(flip,keys,false);}
    AlarmInputs(boolean flip,Keys keys,boolean chooseSnooze){if(keys==null)throw new IllegalArgumentException("Choose a volume-key action");this.flip=flip;this.keys=keys;this.chooseSnooze=chooseSnooze;}
    AlarmInputs flip(boolean value){return new AlarmInputs(value,keys,chooseSnooze);}AlarmInputs keys(Keys value){return new AlarmInputs(flip,value,chooseSnooze);}
    AlarmInputs chooseSnooze(boolean value){return new AlarmInputs(flip,keys,value);}
    boolean interceptKeys(){return keys!=Keys.VOLUME;}
    boolean keySnooze(boolean down,int repeat,boolean current,int used,int limit){return keys==Keys.SNOOZE&&down&&repeat==0&&canSnooze(current,used,limit);}
    static boolean canSnooze(boolean current,int used,int limit){return current&&used>=0&&limit>=1&&limit<=5&&used<limit;}
    @Override public boolean equals(Object other){return other instanceof AlarmInputs that&&flip==that.flip&&keys==that.keys&&chooseSnooze==that.chooseSnooze;}
    @Override public int hashCode(){return (keys.hashCode()*31+(flip?1:0))*31+(chooseSnooze?1:0);}
}
