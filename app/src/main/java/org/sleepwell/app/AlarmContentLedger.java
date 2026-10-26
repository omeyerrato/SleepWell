package org.sleepwell.app;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;

/** A prepared external request is never automatically repeated after ambiguous process failure. */
final class AlarmContentLedger extends SQLiteOpenHelper {
    static final class Receipt {final String payload,state,status;final int id;Receipt(String payload,String state,String status,int id){this.payload=payload;this.state=state;this.status=status;this.id=id;}}
    AlarmContentLedger(Context c){super(c,"alarm_content_requests_v1.db",null,1);}
    @Override public void onCreate(SQLiteDatabase db){db.execSQL("CREATE TABLE requests(principal TEXT NOT NULL,request TEXT NOT NULL,payload TEXT NOT NULL,alarm INTEGER NOT NULL,operation TEXT NOT NULL,state TEXT NOT NULL,status TEXT NOT NULL,created INTEGER NOT NULL,PRIMARY KEY(principal,request))");}
    @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){throw new IllegalStateException("Alarm request journal version is unsupported.");}
    Receipt find(String principal,String request,String payload){try(Cursor c=getReadableDatabase().rawQuery("SELECT payload,state,status,alarm FROM requests WHERE principal=? AND request=?",new String[]{principal,request})){if(!c.moveToFirst())return null;if(!payload.equals(c.getString(0)))throw new IllegalArgumentException("The request_id was already used for a different operation or payload.");return new Receipt(c.getString(0),c.getString(1),c.getString(2),c.getInt(3));}}
    int maximum(){try(Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(MAX(alarm),0) FROM requests",null)){return c.moveToFirst()?c.getInt(0):0;}}
    void prepare(String principal,AlarmContentContract.Request request,String payload,int id){SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{if(find(principal,request.request,payload)!=null)throw new IllegalStateException("The request was already prepared.");try(Cursor c=db.rawQuery("SELECT COUNT(*) FROM requests",null)){if(!c.moveToFirst()||c.getLong(0)>=4096)throw new IllegalStateException("The bounded alarm request journal is full. Use SleepWell for further edits.");}ContentValues v=new ContentValues();v.put("principal",principal);v.put("request",request.request);v.put("payload",payload);v.put("alarm",id);v.put("operation",request.operation);v.put("state","prepared");v.put("status","Interrupted preparation is not automatically replayed. Inspect the alarm in SleepWell.");v.put("created",System.currentTimeMillis());db.insertOrThrow("requests",null,v);db.setTransactionSuccessful();}finally{db.endTransaction();}}
    void finish(String principal,String request,String payload,String state,String status){ContentValues values=new ContentValues();values.put("state",state);values.put("status",status);if(getWritableDatabase().update("requests",values,"principal=? AND request=? AND payload=? AND state='prepared'",new String[]{principal,request,payload})!=1)throw new IllegalStateException("The saved operation receipt could not be confirmed.");}
}
