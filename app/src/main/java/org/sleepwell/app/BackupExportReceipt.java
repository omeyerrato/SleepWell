package org.sleepwell.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/** Local manual-document receipts, excluded from database/settings backup. No document URI is retained. */
final class BackupExportReceipt {
    enum Kind {JSON,CSV}
    enum Status {NOT_RECORDED,PENDING,RECORDED,UNAVAILABLE}
    interface Factory {SharedPreferences open(Context app);}
    interface Clock {long now();}
    static final Factory PRODUCTION=app->app.getSharedPreferences("manual-export-receipts",Context.MODE_PRIVATE);
    static volatile Factory factory=PRODUCTION;
    static volatile Clock clock=System::currentTimeMillis;
    static final String WARNING="The document was saved, but its last-export receipt could not be saved.";
    static final class Read {
        final Status status;final Kind kind;final long completedAt;final String problem;
        private Read(Status status,Kind kind,long completedAt,String problem){this.status=status;this.kind=kind;this.completedAt=completedAt;this.problem=problem;}
        String format(ZoneId zone,Locale locale){
            if(status==Status.NOT_RECORDED)return "Not recorded";
            if(status==Status.PENDING)return "Recording export receipt…";
            if(status==Status.UNAVAILABLE)return "Last export unavailable";
            return kind.name()+" · "+DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM,FormatStyle.SHORT).withLocale(locale).withZone(zone).format(Instant.ofEpochMilli(completedAt))+" ("+zone.getId()+")";
        }
    }
    /** Pins preference ownership/clock without doing preference I/O on the caller's thread. */
    static final class Scope {
        private final Context app;private final Factory preferences;private final Clock time;
        private volatile boolean jsonPending,csvPending;private volatile String jsonFailure="",csvFailure="";
        Scope(Context context,Factory preferences,Clock time){app=context.getApplicationContext();this.preferences=preferences;this.time=time;}
        long completedAt(){return time.now();}
        void pending(Kind kind){if(kind==Kind.JSON)jsonPending=true;else csvPending=true;}
        private Read transientState(Kind kind){
            boolean pending=kind==Kind.JSON?jsonPending:csvPending;String failure=kind==Kind.JSON?jsonFailure:csvFailure;
            return pending?new Read(Status.PENDING,kind,0,""):!failure.isEmpty()?new Read(Status.UNAVAILABLE,kind,0,failure):null;
        }
        /** Read on an owned worker; an absent key is unknown, never proof of no historical exports. */
        Read read(Kind kind){
            background();Read current=transientState(kind);if(current!=null)return current;
            try{
                SharedPreferences prefs=preferences.open(app);String raw=prefs.getString(key(kind),null);
                Read value;if(raw==null)value=new Read(Status.NOT_RECORDED,kind,0,"");
                else{if(raw.isEmpty()||raw.length()>19)throw new IllegalStateException();long at=Long.parseLong(raw);if(at<=0)throw new IllegalStateException();value=new Read(Status.RECORDED,kind,at,"");}
                current=transientState(kind);return current==null?value:current;
            }catch(RuntimeException failure){return new Read(Status.UNAVAILABLE,kind,0,"The saved export receipt could not be read. Try again.");}
        }
        /** Called only for a generation accepted as DONE after the document's write/flush/close returned. */
        String record(Kind kind,long completedAt){
            background();String failure="";
            try{if(completedAt<=0||!preferences.open(app).edit().putString(key(kind),Long.toString(completedAt)).commit())failure=WARNING;}
            catch(RuntimeException unavailable){failure=WARNING;}
            finished(kind,failure);return failure;
        }
        void finished(Kind kind,String failure){if(kind==Kind.JSON){jsonFailure=failure;jsonPending=false;}else{csvFailure=failure;csvPending=false;}}
        private static String key(Kind kind){return kind==Kind.JSON?"last_json_v1":"last_csv_v1";}
    }
    private static void background(){if(Looper.myLooper()==Looper.getMainLooper())throw new IllegalStateException("Export receipt storage must run on its owned worker.");}
    private BackupExportReceipt(){}
}
