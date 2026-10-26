package org.sleepwell.app;

import android.content.*;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import java.io.FileNotFoundException;
import java.util.*;

/** Original opt-in ordinary-alarm provider. Android URI access alone is insufficient. */
public final class AlarmContentProvider extends ContentProvider {
    @Override public boolean onCreate(){return true;}
    private Context app(){Context context=getContext();if(context==null)throw new IllegalStateException("Alarm provider is unavailable.");return context.getApplicationContext();}
    private Cursor read(Uri uri,String[] projection,CancellationSignal cancellation){AlarmContentContract.Request request=AlarmContentContract.parse(uri,"query");String[] columns=AlarmContentContract.projection(projection);int uid=Binder.getCallingUid(),pid=Binder.getCallingPid();long identity=Binder.clearCallingIdentity();try{Context context=app();AlarmAccessGrants.Admission admission=AlarmAccessGrants.admit(context,uid,pid,uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);Cursor result=AlarmContentStore.read(context,request,columns,cancellation,()->AlarmAccessGrants.fastCurrent(context,admission));try{AlarmAccessGrants.current(context,admission,uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);return result;}catch(RuntimeException changed){result.close();throw changed;}}catch(SecurityException|IllegalArgumentException|OperationCanceledException expected){throw expected;}catch(RuntimeException unavailable){throw new IllegalStateException("Alarm summaries are unavailable; no partial page was returned.");}finally{Binder.restoreCallingIdentity(identity);}}
    private static void noSql(String selection,String[] args,String order){if(selection!=null||args!=null||order!=null)throw new IllegalArgumentException("Selection, selectionArgs and sortOrder are unsupported. Use the documented URI cursor.");}
    @Override public Cursor query(Uri uri,String[] projection,String selection,String[] args,String order){noSql(selection,args,order);return read(uri,projection,null);}
    @Override public Cursor query(Uri uri,String[] projection,String selection,String[] args,String order,CancellationSignal cancellation){noSql(selection,args,order);return read(uri,projection,cancellation);}
    @Override public Cursor query(Uri uri,String[] projection,Bundle args,CancellationSignal cancellation){if(args!=null){Set<String> sql=Set.of(ContentResolver.QUERY_ARG_SQL_SELECTION,ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,ContentResolver.QUERY_ARG_SQL_SORT_ORDER);if(args.size()>3)throw new IllegalArgumentException("Query bundles are unsupported.");for(String key:args.keySet())if(!sql.contains(key)||args.get(key)!=null)throw new IllegalArgumentException("Query bundles are unsupported. Use documented URI parameters.");}return read(uri,projection,cancellation);}
    @Override public String getType(Uri uri){AlarmContentContract.Request request=AlarmContentContract.parse(uri,"query");int uid=Binder.getCallingUid(),pid=Binder.getCallingPid();long identity=Binder.clearCallingIdentity();try{AlarmAccessGrants.admit(app(),uid,pid,uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);return request.id==0?"vnd.android.cursor.dir/vnd.org.sleepwell.alarm-summary.v1":"vnd.android.cursor.item/vnd.org.sleepwell.alarm-summary.v1";}finally{Binder.restoreCallingIdentity(identity);}}
    @Override public String getTypeAnonymous(Uri uri){return null;}
    private static UnsupportedOperationException readOnly(){return new UnsupportedOperationException("Use documented single-alarm methods only. Files, RPC and batches are unsupported.");}
    private int change(Uri uri,ContentValues values,String operation){AlarmContentContract.Request request=AlarmContentContract.parse(uri,operation);ContentValues fields=operation.equals("delete")?null:AlarmContentContract.fields(values,operation.equals("insert"));int uid=Binder.getCallingUid(),pid=Binder.getCallingPid();long identity=Binder.clearCallingIdentity();try{Context context=app();AlarmAccessGrants.Admission admission=AlarmAccessGrants.admit(context,uid,pid,uri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION);return AlarmContentStore.mutate(context,request,fields,admission,uri);}finally{Binder.restoreCallingIdentity(identity);}}
    private static void extras(Bundle extras){if(extras!=null&&!extras.isEmpty())throw new IllegalArgumentException("Mutation extras are unsupported.");}
    @Override public Uri insert(Uri uri,ContentValues values){return ContentUris.withAppendedId(AlarmContentContract.ALARMS,change(uri,values,"insert"));}
    @Override public Uri insert(Uri uri,ContentValues values,Bundle extras){extras(extras);return insert(uri,values);}
    @Override public int update(Uri uri,ContentValues values,String selection,String[] args){noSql(selection,args,null);change(uri,values,"update");return 1;}
    @Override public int update(Uri uri,ContentValues values,Bundle extras){extras(extras);return update(uri,values,null,null);}
    @Override public int delete(Uri uri,String selection,String[] args){noSql(selection,args,null);change(uri,null,"delete");return 1;}
    @Override public int delete(Uri uri,Bundle extras){extras(extras);return delete(uri,null,null);}
    @Override public int bulkInsert(Uri uri,ContentValues[] values){throw readOnly();}
    @Override public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations){throw readOnly();}
    @Override public ContentProviderResult[] applyBatch(String authority,ArrayList<ContentProviderOperation> operations){throw readOnly();}
    @Override public Bundle call(String method,String arg,Bundle extras){throw readOnly();}
    @Override public Bundle call(String authority,String method,String arg,Bundle extras){throw readOnly();}
    @Override public ParcelFileDescriptor openFile(Uri uri,String mode)throws FileNotFoundException{throw new FileNotFoundException("Alarm management has no file access.");}
    @Override public ParcelFileDescriptor openFile(Uri uri,String mode,CancellationSignal cancellation)throws FileNotFoundException{throw new FileNotFoundException("Alarm management has no file access.");}
    @Override public AssetFileDescriptor openAssetFile(Uri uri,String mode)throws FileNotFoundException{throw new FileNotFoundException("Alarm management has no file access.");}
    @Override public AssetFileDescriptor openAssetFile(Uri uri,String mode,CancellationSignal cancellation)throws FileNotFoundException{throw new FileNotFoundException("Alarm management has no file access.");}
    @Override public AssetFileDescriptor openTypedAssetFile(Uri uri,String mime,Bundle options)throws FileNotFoundException{throw new FileNotFoundException("Alarm management has no file access.");}
    @Override public AssetFileDescriptor openTypedAssetFile(Uri uri,String mime,Bundle options,CancellationSignal cancellation)throws FileNotFoundException{throw new FileNotFoundException("Alarm management has no file access.");}
    @Override public String[] getStreamTypes(Uri uri,String mime){return null;}
    @Override public Uri canonicalize(Uri uri){throw readOnly();}
    @Override public Uri uncanonicalize(Uri uri){throw readOnly();}
    @Override public boolean refresh(Uri uri,Bundle args,CancellationSignal cancellation){throw readOnly();}
}
