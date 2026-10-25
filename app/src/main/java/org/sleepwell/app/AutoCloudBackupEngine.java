package org.sleepwell.app;
import java.util.function.BooleanSupplier;
/** Ordered worker transition boundary. Provider and scheduler can be replaced without using an account. */
final class AutoCloudBackupEngine {
 interface Environment {
  CloudBackupSettings.Destination destination();boolean granted(CloudBackupSettings.Destination d);void verify(CloudBackupSettings.Destination d)throws Exception;
  boolean valid(AutoCloudBackupStore.Task t)throws Exception;byte[] snapshot(BooleanSupplier active)throws Exception;CloudBackupCopy.Provider provider(String tree)throws Exception;
  void history(AutoCloudBackupStore.Task t,CloudBackupCopy.Document d,CloudBackupCopy.Receipt r)throws Exception;
  long now();void schedule(String token,long due)throws Exception;void cancel();
 }
 private static final Object CONFIGURATION=new Object();
 final AutoCloudBackupStore store;final Environment env;private volatile CloudBackupCopy.Provider provider;
 AutoCloudBackupEngine(AutoCloudBackupStore store,Environment env){this.store=store;this.env=env;}
 void configure(boolean on)throws Exception{configure(on,()->true);}
 void configure(boolean on,BooleanSupplier active)throws Exception{CloudBackupSettings.Destination d=env.destination();if(on){if(d.tree.isEmpty()||!env.granted(d))throw new IllegalStateException("Choose a folder with remembered read/write access first.");env.verify(d);}synchronized(CONFIGURATION){BackupIo.active(active);store.configure(on,d);}env.cancel();}
 void ended(long id,long start,long end)throws Exception{if(store.enqueue(id,start,end,env.now()))reconcile(false);}
 void reconcile(boolean recovering)throws Exception{if(recovering)store.recover();AutoCloudBackupStore.Owner o=store.owner();if(!o.enabled){env.cancel();return;}CloudBackupSettings.Destination d=env.destination();if(o.revision!=d.revision||!o.tree.equals(d.tree)||!env.granted(d)){store.configure(false,d);store.message("Automatic backup turned off: folder changed or access was lost. Choose a destination and enable it again.");env.cancel();return;}AutoCloudBackupStore.Task t=store.next();if(t==null){env.cancel();return;}try{env.schedule(o.token,t.due);}catch(Exception failure){store.message("Android could not schedule automatic backup. Use Retry automatic backup.");throw failure;}}
 boolean owns(AutoCloudBackupStore.Task t,BooleanSupplier active){AutoCloudBackupStore.Owner o=store.owner();CloudBackupSettings.Destination d=env.destination();return active.getAsBoolean()&&o.enabled&&o.token.equals(t.token)&&o.revision==d.revision&&o.tree.equals(d.tree)&&env.granted(d);}
 void run(String token,BooleanSupplier active)throws Exception{
  AutoCloudBackupStore.Owner owner=store.owner();if(!owner.enabled||!owner.token.equals(token))return;
  AutoCloudBackupStore.Task t=store.next();if(t==null)return;if(t.due>env.now()){reconcile(false);return;}
  if(!owns(t,active)){reconcile(false);return;}if(!env.valid(t)){store.reject(t,"Saved night changed or was removed. Automatic request cancelled.");reconcile(false);return;}
  Object lease=new Object();if(!CloudBackupLease.acquire(lease)){store.message("Another metadata backup is active · automatic delivery will wait");env.schedule(owner.token,Math.addExact(env.now(),15*60000L));return;}
  try{
   store.claim(t);BooleanSupplier owned=()->owns(t,active);BackupIo.active(owned);
   CloudBackupCopy.Provider selected=env.provider(t.tree);provider=selected;
   CloudBackupCopy.Provider tracked=new CloudBackupCopy.Provider(){public CloudBackupCopy.Document create(String name)throws Exception{BackupIo.active(owned);store.stage(t,"CREATING",null);return selected.create(name);}public java.io.OutputStream output(CloudBackupCopy.Document d)throws Exception{return selected.output(d);}public java.io.InputStream input(CloudBackupCopy.Document d)throws Exception{return selected.input(d);}public void cancel(){selected.cancel();}};
   String name="sleepwell-auto-"+t.session+"-"+java.util.UUID.randomUUID()+".json";
   CloudBackupCopy.Receipt receipt=CloudBackupCopy.save(env::snapshot,tracked,name,owned,d->{store.stage(t,"CREATED",d);env.history(t,d,null);});
   store.complete(t,receipt);
   try{env.history(t,receipt.document,receipt);}catch(Exception history){store.message("Automatic file read-back verified; local version history needs attention. Existing provider files were kept.");}
  }catch(Exception error){store.failure(t,env.now(),AutoCloudBackupPolicy.oversized(error)?"oversize":error.getClass().getSimpleName());}
  finally{provider=null;CloudBackupLease.release(lease);}
  reconcile(false);
 }
 void cancelProvider(){CloudBackupCopy.Provider p=provider;if(p!=null)p.cancel();}
 void retry()throws Exception{store.retry(env.now());reconcile(false);}
}
