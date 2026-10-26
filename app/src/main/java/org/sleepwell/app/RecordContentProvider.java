package org.sleepwell.app;

import android.content.*;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import java.io.FileNotFoundException;
import java.util.*;

/** Original read-only completed-summary provider. Android URI access alone is insufficient. */
public final class RecordContentProvider extends ContentProvider {
    @Override public boolean onCreate(){return true;}
    private Context app(){Context context=getContext();if(context==null)throw new IllegalStateException("Record provider is unavailable.");return context.getApplicationContext();}
    private Cursor read(Uri uri,String[] projection,CancellationSignal cancellation){RecordContentContract.Request request=RecordContentContract.parse(uri);String[] columns=RecordContentContract.projection(projection);int uid=Binder.getCallingUid(),pid=Binder.getCallingPid();long identity=Binder.clearCallingIdentity();try{Context context=app();RecordAccessGrants.Admission admission=RecordAccessGrants.admit(context,uid,pid,uri);return RecordContentRows.read(context,request,columns,cancellation,()->RecordAccessGrants.current(context,admission,uri));}catch(SecurityException|IllegalArgumentException|OperationCanceledException expected){throw expected;}catch(RuntimeException unavailable){throw new IllegalStateException("Completed record summaries are unavailable; no partial page was returned.");}finally{Binder.restoreCallingIdentity(identity);}}
    private static void noSql(String selection,String[] args,String order){if(selection!=null||args!=null||order!=null)throw new IllegalArgumentException("Selection, selectionArgs and sortOrder are unsupported. Use the documented URI cursor.");}
    @Override public Cursor query(Uri uri,String[] projection,String selection,String[] args,String order){noSql(selection,args,order);return read(uri,projection,null);}
    @Override public Cursor query(Uri uri,String[] projection,String selection,String[] args,String order,CancellationSignal cancellation){noSql(selection,args,order);return read(uri,projection,cancellation);}
    @Override public Cursor query(Uri uri,String[] projection,Bundle args,CancellationSignal cancellation){if(args!=null){Set<String> sql=Set.of(ContentResolver.QUERY_ARG_SQL_SELECTION,ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,ContentResolver.QUERY_ARG_SQL_SORT_ORDER);if(args.size()>3)throw new IllegalArgumentException("Query bundles are unsupported.");for(String key:args.keySet())if(!sql.contains(key)||args.get(key)!=null)throw new IllegalArgumentException("Query bundles are unsupported. Use documented URI parameters.");}return read(uri,projection,cancellation);}
    @Override public String getType(Uri uri){RecordContentContract.Request request=RecordContentContract.parse(uri);int uid=Binder.getCallingUid(),pid=Binder.getCallingPid();long identity=Binder.clearCallingIdentity();try{RecordAccessGrants.admit(app(),uid,pid,uri);return request.id==0?"vnd.android.cursor.dir/vnd.org.sleepwell.record-summary.v1":"vnd.android.cursor.item/vnd.org.sleepwell.record-summary.v1";}finally{Binder.restoreCallingIdentity(identity);}}
    @Override public String getTypeAnonymous(Uri uri){return null;}
    private static UnsupportedOperationException readOnly(){return new UnsupportedOperationException("This provider only queries completed summary rows. Writes, files, RPC and batches are unsupported.");}
    @Override public Uri insert(Uri uri,ContentValues values){throw readOnly();}
    @Override public Uri insert(Uri uri,ContentValues values,Bundle extras){throw readOnly();}
    @Override public int update(Uri uri,ContentValues values,String selection,String[] args){throw readOnly();}
    @Override public int update(Uri uri,ContentValues values,Bundle extras){throw readOnly();}
    @Override public int delete(Uri uri,String selection,String[] args){throw readOnly();}
    @Override public int delete(Uri uri,Bundle extras){throw readOnly();}
    @Override public int bulkInsert(Uri uri,ContentValues[] values){throw readOnly();}
    @Override public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations){throw readOnly();}
    @Override public ContentProviderResult[] applyBatch(String authority,ArrayList<ContentProviderOperation> operations){throw readOnly();}
    @Override public Bundle call(String method,String arg,Bundle extras){throw readOnly();}
    @Override public Bundle call(String authority,String method,String arg,Bundle extras){throw readOnly();}
    @Override public ParcelFileDescriptor openFile(Uri uri,String mode)throws FileNotFoundException{throw new FileNotFoundException("Record summaries have no file access.");}
    @Override public ParcelFileDescriptor openFile(Uri uri,String mode,CancellationSignal cancellation)throws FileNotFoundException{throw new FileNotFoundException("Record summaries have no file access.");}
    @Override public AssetFileDescriptor openAssetFile(Uri uri,String mode)throws FileNotFoundException{throw new FileNotFoundException("Record summaries have no file access.");}
    @Override public AssetFileDescriptor openAssetFile(Uri uri,String mode,CancellationSignal cancellation)throws FileNotFoundException{throw new FileNotFoundException("Record summaries have no file access.");}
    @Override public AssetFileDescriptor openTypedAssetFile(Uri uri,String mime,Bundle options)throws FileNotFoundException{throw new FileNotFoundException("Record summaries have no file access.");}
    @Override public AssetFileDescriptor openTypedAssetFile(Uri uri,String mime,Bundle options,CancellationSignal cancellation)throws FileNotFoundException{throw new FileNotFoundException("Record summaries have no file access.");}
    @Override public String[] getStreamTypes(Uri uri,String mime){return null;}
    @Override public Uri canonicalize(Uri uri){throw readOnly();}
    @Override public Uri uncanonicalize(Uri uri){throw readOnly();}
    @Override public boolean refresh(Uri uri,Bundle args,CancellationSignal cancellation){throw readOnly();}
}
