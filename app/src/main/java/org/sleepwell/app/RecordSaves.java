package org.sleepwell.app;

import android.content.Context;
import java.time.ZoneId;
import java.util.concurrent.*;

/** Save ownership outlives an Activity. Persistent create tokens also cover process recreation. */
final class RecordSaves {
    private static final ExecutorService worker=Executors.newSingleThreadExecutor();
    private static final ConcurrentMap<String,Operation> operations=new ConcurrentHashMap<>();
    static final class Operation {volatile boolean done,awaitingReview,staleReview,abandoned;volatile String error;volatile RecordOverlap.Review review;Runnable confirm;}
    static Operation start(Context context,String token,String createToken,long id,RecordDraft draft,ZoneId zone,long revision){
        return start(context,token,createToken,id,draft,zone,revision,false,false,null);
    }
    static Operation startManual(Context context,String token,String createToken,long id,RecordDraft draft,ZoneId zone,long revision,boolean requireReview,String accepted){
        return start(context,token,createToken,id,draft,zone,revision,true,requireReview,accepted);
    }
    private static Operation start(Context context,String token,String createToken,long id,RecordDraft draft,ZoneId zone,long revision,boolean manual,boolean requireReview,String accepted){
        Operation operation=new Operation(),existing=operations.putIfAbsent(token,operation);if(existing!=null)return existing;
        Context app=context.getApplicationContext();Records.Factory factory=Records.factory;RecordDraft copy=draft.copy();
        worker.execute(()->run(app,factory,operation,token,createToken,id,copy,zone,revision,manual,requireReview,accepted));return operation;
    }
    private static void run(Context app,Records.Factory factory,Operation operation,String token,String createToken,long id,RecordDraft copy,ZoneId zone,long revision,boolean manual,boolean requireReview,String accepted){
        boolean reviewPending=false;
        try(Store store=factory.open(app)){
            if(!manual){commit(store,createToken,id,copy,zone,revision);return;}
            android.database.sqlite.SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{
                // A completed persistent create token wins before overlap re-evaluation. Never duplicate it.
                boolean replay=id==0&&!store.rows("SELECT session FROM record_create_requests WHERE token=?",createToken).isEmpty();
                if(replay){commit(store,createToken,id,copy,zone,revision);db.setTransactionSuccessful();return;}
                if(id>0){RecordEntry original=Records.get(store,id);if(!original.manual()||original.updated!=revision)throw new IllegalArgumentException("This record changed while it was open. Reopen it before saving.");}
                long deadline=android.os.SystemClock.elapsedRealtime()+30000;RecordOverlap.Review current=RecordOverlap.inspect(store,id,copy,()->android.os.SystemClock.elapsedRealtime()<deadline);
                if(accepted!=null){if(!accepted.equals(current.fingerprint)){operation.staleReview=true;throw new IllegalStateException("Saved periods changed after overlap review. Save changes to review them again.");}commit(store,createToken,id,copy,zone,revision);db.setTransactionSuccessful();return;}
                if(current.count>0||requireReview){synchronized(operation){if(operation.abandoned)return;operation.review=current;operation.confirm=()->worker.execute(()->run(app,factory,operation,token,createToken,id,copy,zone,revision,true,true,current.fingerprint));reviewPending=true;operation.awaitingReview=true;}db.setTransactionSuccessful();return;}
                commit(store,createToken,id,copy,zone,revision);db.setTransactionSuccessful();
            }finally{db.endTransaction();}
        }catch(Exception e){reviewPending=false;operation.awaitingReview=false;operation.error=e.getMessage()==null?"Storage unavailable.":e.getMessage();}
        finally{if(!reviewPending){operation.done=true;if(operation.abandoned)operations.remove(token,operation);}}
    }
    private static void commit(Store store,String createToken,long id,RecordDraft draft,ZoneId zone,long revision){if(id==0)Records.create(store,draft,zone,System.currentTimeMillis(),createToken);else Records.save(store,id,draft,revision,System.currentTimeMillis());}
    static boolean confirmReview(String token,String fingerprint){if(token==null)return false;Operation operation=operations.get(token);if(operation==null)return false;synchronized(operation){if(operation.done||operation.abandoned||!operation.awaitingReview||operation.review==null||!operation.review.fingerprint.equals(fingerprint))return false;operation.awaitingReview=false;operation.confirm.run();return true;}}
    static boolean keepEditing(String token){if(token==null)return false;Operation operation=operations.get(token);if(operation==null)return false;synchronized(operation){if(!operation.awaitingReview||operation.done)return false;operation.abandoned=true;operation.awaitingReview=false;operation.done=true;operations.remove(token,operation);return true;}}
    static void release(String token){if(token==null)return;Operation operation=operations.get(token);if(operation==null)return;synchronized(operation){operation.abandoned=true;if(operation.awaitingReview||operation.done){operation.awaitingReview=false;operation.done=true;operations.remove(token,operation);}}}
    /** Instrumentation teardown barrier; never called by a UI lifecycle. */
    static void drain(long timeout)throws Exception{worker.submit(()->{}).get(timeout,TimeUnit.MILLISECONDS);}
    static void forget(String token){operations.remove(token);}
}
