package org.sleepwell.app;

import android.content.*;
import android.media.*;
import android.net.Uri;
import android.os.CancellationSignal;
import android.provider.OpenableColumns;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** User-picked content grants only; bounded worker validation, no player and no output. */
final class AlarmToneImport {
    static final int REQUEST=410;static final AtomicBoolean ACTIVE=new AtomicBoolean();
    interface GrantRelease {void release(Context context,Uri uri);}
    static final class Result {final String uri,name;final boolean newGrant;private final GrantRelease release;private final AtomicInteger state=new AtomicInteger();Result(String uri,String name){this(uri,name,false);}Result(String uri,String name,boolean newGrant){this(uri,name,newGrant,(context,value)->context.getContentResolver().releasePersistableUriPermission(value,Intent.FLAG_GRANT_READ_URI_PERMISSION));}Result(String uri,String name,boolean newGrant,GrantRelease release){this.uri=uri;this.name=name;this.newGrant=newGrant;this.release=release;}boolean accept(){return state.compareAndSet(0,1);}void discard(Context context,Records.Factory factory,SharedPreferences sounds){if(!state.compareAndSet(0,2)||!newGrant)return;try(Store store=factory.open(context)){for(Alarm alarm:store.alarms())if(uri.equals(alarm.tone))return;}catch(RuntimeException unavailable){return;}synchronized(SoundPreferences.class){try{if(uri.equals(sounds.getString("mixUri","")))return;}catch(RuntimeException uncertain){return;}try{release.release(context,Uri.parse(uri));}catch(RuntimeException ignored){}}}}
    interface Source {Result read(Context context,Uri uri,int flags,CancellationSignal cancelled)throws Exception;}
    static Source source=AlarmToneImport::read;
    static boolean allowed(Uri uri){return uri!=null&&"content".equals(uri.getScheme())&&uri.getAuthority()!=null&&!uri.getAuthority().isEmpty();}
    private static Result read(Context context,Uri uri,int flags,CancellationSignal cancelled)throws Exception{
        cancelled.throwIfCanceled();if(!allowed(uri)||(flags&Intent.FLAG_GRANT_READ_URI_PERMISSION)==0||(flags&Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)==0)throw new IOException("Persistent audio access unavailable");ContentResolver resolver=context.getContentResolver();String type=resolver.getType(uri);if(type==null||!type.startsWith("audio/"))throw new IOException("Not an audio document");cancelled.throwIfCanceled();
        try(android.content.res.AssetFileDescriptor descriptor=resolver.openAssetFileDescriptor(uri,"r",cancelled)){if(descriptor==null)throw new IOException("Audio unavailable");long length=descriptor.getDeclaredLength();if(length<0){long stat=descriptor.getParcelFileDescriptor().getStatSize();length=stat<0?-1:stat-descriptor.getStartOffset();}if(length<=0||length>32L*1024*1024)throw new IOException("Unsupported or unknown audio size");MediaExtractor extractor=new MediaExtractor();try{extractor.setDataSource(descriptor.getFileDescriptor(),descriptor.getStartOffset(),length);boolean audio=false;for(int n=0;n<extractor.getTrackCount();n++){String mime=extractor.getTrackFormat(n).getString(MediaFormat.KEY_MIME);if(mime!=null&&mime.startsWith("audio/"))audio=true;}if(!audio)throw new IOException("No audio track");}finally{extractor.release();}}
        cancelled.throwIfCanceled();String name="My audio";try(android.database.Cursor cursor=resolver.query(uri,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null,cancelled)){if(cursor!=null&&cursor.moveToFirst()){String value=cursor.getString(0);if(value!=null&&!value.isBlank())name=value.replaceAll("[\\p{Cntrl}]"," ").substring(0,Math.min(100,value.length()));}}
        cancelled.throwIfCanceled();boolean existing=false;for(android.content.UriPermission grant:resolver.getPersistedUriPermissions())if(grant.getUri().equals(uri)&&grant.isReadPermission())existing=true;resolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);return new Result(uri.toString(),name,!existing);
    }
    private AlarmToneImport(){}
}
