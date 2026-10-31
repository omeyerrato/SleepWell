package org.sleepwell.app;

import android.content.*;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.*;
import java.io.*;

/** Temporary read-only access to an explicitly shared private preview; no public picker authority. */
public final class ReportShareFileProvider extends androidx.core.content.FileProvider {
    private ReportShareFiles.SharedFile allowed(Uri uri)throws IOException{if(getContext()==null)throw new FileNotFoundException();return ReportShareFiles.allowed(getContext(),uri);}
    @Override public String getType(Uri uri){try{allowed(uri);return "text/plain";}catch(IOException unavailable){return null;}}
    @Override public ParcelFileDescriptor openFile(Uri uri,String mode)throws FileNotFoundException{
        if(!"r".equals(mode))throw new FileNotFoundException("Reports are read-only.");try{ReportShareFiles.SharedFile snapshot=allowed(uri);snapshot.verify(getContext());}catch(Exception unavailable){throw new FileNotFoundException("This report is unavailable or expired.");}return super.openFile(uri,"r");
    }
    @Override public Cursor query(Uri uri,String[] projection,String selection,String[] args,String sort){
        ReportShareFiles.SharedFile snapshot;try{snapshot=allowed(uri);}catch(IOException unavailable){throw new IllegalArgumentException("This report is unavailable or expired.");}
        String[] columns=projection==null?new String[]{android.provider.OpenableColumns.DISPLAY_NAME,android.provider.OpenableColumns.SIZE}:projection;
        if(columns.length>2)throw new IllegalArgumentException("Unsupported report projection.");for(String column:columns)if(!android.provider.OpenableColumns.DISPLAY_NAME.equals(column)&&!android.provider.OpenableColumns.SIZE.equals(column))throw new IllegalArgumentException("Unsupported report column.");MatrixCursor cursor=new MatrixCursor(columns,1);MatrixCursor.RowBuilder row=cursor.newRow();
        for(String column:columns)row.add(column,android.provider.OpenableColumns.DISPLAY_NAME.equals(column)?"SleepWell-reviewed-period.txt":android.provider.OpenableColumns.SIZE.equals(column)?snapshot.file.length():null);return cursor;
    }
    @Override public Uri insert(Uri uri,ContentValues values){throw new UnsupportedOperationException("Read-only report.");}
    @Override public int update(Uri uri,ContentValues values,String selection,String[] args){throw new UnsupportedOperationException("Read-only report.");}
    @Override public int delete(Uri uri,String selection,String[] args){throw new UnsupportedOperationException("Read-only report.");}
    @Override public Bundle call(String method,String argument,Bundle extras){throw new UnsupportedOperationException("Read-only report.");}
}
