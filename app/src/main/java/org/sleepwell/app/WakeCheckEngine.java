package org.sleepwell.app;

/** All time, durable state, scheduling, silent delivery and re-ring effects are injected. */
final class WakeCheckEngine {
 enum Stage {PROMPT,EXPIRE}
 interface Store {WakeCheckState load();void save(WakeCheckState state);}
 interface Port {boolean owner(WakeCheckState state);String access();void schedule(WakeCheckState state,Stage stage,long delay);void cancel(WakeCheckState state);void show(WakeCheckState state);void ring(WakeCheckState state);}
 private final Store store;private final Port port;private final WakeCheckState.Clock clock;
 WakeCheckEngine(Store store,Port port,WakeCheckState.Clock clock){this.store=store;this.port=port;this.clock=clock;}
 WakeCheckState read(){return store.load();}
 synchronized void arm(WakeCheckState state){WakeCheckState old=read();if(old!=null&&old.active())cancel(old.nonce);store.save(state);resume(state,false);}
 synchronized void receive(String nonce,Stage stage){WakeCheckState s=owned(nonce);if(s==null||!s.error.isEmpty())return;if(stage==Stage.PROMPT&&s.phase==WakeCheckState.Phase.WAITING){if(s.remaining(clock)>0){schedule(s,Stage.PROMPT);return;}prompt(s);}else if(stage==Stage.EXPIRE&&s.phase==WakeCheckState.Phase.PROMPT&&s.delivered){if(s.remaining(clock)>0)schedule(s,Stage.EXPIRE);else expire(s);}}
 synchronized boolean confirm(String nonce){WakeCheckState s=owned(nonce);if(s==null||s.phase!=WakeCheckState.Phase.PROMPT||s.remaining(clock)==0)return false;store.save(s.update(WakeCheckState.Phase.CONFIRMED,s.delivered,""));quietCancel(s);return true;}
 synchronized void cancel(String nonce){WakeCheckState s=read();if(s==null||!s.nonce.equals(nonce)||!s.active())return;store.save(s.update(WakeCheckState.Phase.CANCELLED,s.delivered,""));quietCancel(s);}
 synchronized void edited(int alarmId,boolean checkEnabled){WakeCheckState s=read();if(!checkEnabled&&s!=null&&s.alarmId==alarmId)cancel(s.nonce);}
 synchronized void supersede(){WakeCheckState s=read();if(s!=null)cancel(s.nonce);}
 synchronized void reconcile(){WakeCheckState s=read();if(s==null||!s.active())return;if(!port.owner(s)){cancel(s.nonce);return;}if(!s.error.isEmpty())return;resume(s,false);}
 synchronized void retry(String nonce){WakeCheckState s=owned(nonce);if(s==null)return;s=s.update(s.phase,s.delivered,"");store.save(s);resume(s,true);}
 private WakeCheckState owned(String nonce){WakeCheckState s=read();if(s==null||nonce==null||!nonce.equals(s.nonce)||!s.active())return null;if(!port.owner(s)){cancel(nonce);return null;}return s;}
 private void resume(WakeCheckState s,boolean retry){String access=access();if(!access.isEmpty()){failure(s,access);return;}if(s.phase==WakeCheckState.Phase.WAITING){if(s.remaining(clock)==0&&retry)prompt(s);else schedule(s,Stage.PROMPT);}else if(s.phase==WakeCheckState.Phase.PROMPT){if(s.remaining(clock)==0){if(s.delivered){if(retry)expire(s);else schedule(s,Stage.EXPIRE);}else{store.save(s.update(WakeCheckState.Phase.CANCELLED,false,"The confirmation window ended before the check could be delivered."));quietCancel(s);}return;}if(!s.delivered)deliver(s);else schedule(s,Stage.EXPIRE);}}
 private void prompt(WakeCheckState s){String access=access();if(!access.isEmpty()){failure(s,access);return;}WakeCheckState prompt=s.prompt(clock);store.save(prompt);deliver(prompt);}
 private void deliver(WakeCheckState s){try{port.show(s);WakeCheckState delivered=s.update(s.phase,true,"");store.save(delivered);schedule(delivered,Stage.EXPIRE);}catch(RuntimeException unavailable){failure(s,"The silent check could not be shown. Open notification settings, then retry.");}}
 private void schedule(WakeCheckState s,Stage stage){try{port.schedule(s,stage,Math.max(1,s.remaining(clock)));}catch(RuntimeException unavailable){failure(s,"The check could not be scheduled. Enable Alarms & reminders, then retry.");}}
 private void expire(WakeCheckState s){store.save(s.update(WakeCheckState.Phase.CLAIMED,true,""));quietCancel(s);try{port.ring(s);store.save(s.update(WakeCheckState.Phase.EXPIRED,true,""));}catch(RuntimeException unavailable){store.save(s.update(WakeCheckState.Phase.CLAIMED,true,"The alarm restart could not be verified. Open Alarms to check it."));}}
 private String access(){try{return port.access();}catch(RuntimeException unavailable){return "Check delivery settings could not be read. Try again.";}}
 private void failure(WakeCheckState s,String error){store.save(s.update(s.phase,s.delivered,error));quietCancel(s);}
 private void quietCancel(WakeCheckState s){try{port.cancel(s);}catch(RuntimeException ignored){/* Durable phase/nonce fences still reject any late broadcast. */}}
}
