package org.sleepwell.app;

/** Durable ownership is claimed before delivery. A process death after claim cannot safely be retried. */
final class NapEngine {
 static final class Entry {final NapPlan plan;final boolean enabled;Entry(NapPlan p,boolean on){plan=p;enabled=on;}}
 interface Repository {Entry load(int id);void save(int id,NapPlan plan,boolean enabled);}
 interface Port {String access();void schedule(int id,NapPlan plan,long delay);void cancel(int id,NapPlan plan);void fire(int id,NapPlan plan);}
 private final Repository store;private final Port port;private final NapPlan.Clock clock;
 NapEngine(Repository store,Port port,NapPlan.Clock clock){this.store=store;this.port=port;this.clock=clock;}
 void arm(int id,NapPlan plan){if(plan.phase!=NapPlan.Phase.PENDING)throw new IllegalArgumentException("Start a new nap timer.");store.save(id,plan,true);reconcile(id);}
 void reconcile(int id){Entry row=store.load(id);if(row==null||row.plan==null)return;NapPlan p=row.plan;if(!row.enabled||p.phase!=NapPlan.Phase.PENDING){port.cancel(id,p);return;}String issue=port.access();if(!issue.isEmpty()){store.save(id,p.state(p.phase,issue),true);port.cancel(id,p);return;}try{port.schedule(id,p,Math.max(1,p.remaining(clock)));if(!p.error.isEmpty())store.save(id,p.state(p.phase,""),true);}catch(RuntimeException failed){store.save(id,p.state(p.phase,"Nap saved, but scheduling failed. Check alarm access and retry."),true);}}
 void receive(int id,String nonce,boolean smart){Entry row=store.load(id);if(row==null||row.plan==null||!row.enabled||row.plan.phase!=NapPlan.Phase.PENDING||!row.plan.nonce.equals(nonce))return;NapPlan p=row.plan;if(smart&&!p.smartInterval(clock))return;if(!smart&&p.remaining(clock)>0){reconcile(id);return;}String issue=port.access();if(!issue.isEmpty()){store.save(id,p.state(p.phase,issue),true);return;}NapPlan claimed=p.state(NapPlan.Phase.CLAIMED,"");store.save(id,claimed,false);try{port.cancel(id,p);port.fire(id,claimed);}catch(RuntimeException failed){store.save(id,claimed.state(claimed.phase,"Delivery could not be verified. Start a new nap if needed."),false);}}
 void cancel(int id,String nonce){Entry row=store.load(id);if(row==null||row.plan==null||!row.plan.nonce.equals(nonce)||row.plan.phase==NapPlan.Phase.CLAIMED)return;NapPlan old=row.plan;store.save(id,old.state(NapPlan.Phase.CANCELLED,""),false);port.cancel(id,old);}
}
