package org.sleepwell.app;

import android.net.Uri;
import java.util.*;

/** Original SleepWell summary contract. No vendor authority or arbitrary SQL is accepted. */
public final class RecordContentContract {
    public static final String AUTHORITY="org.sleepwell.app.records";
    public static final Uri RECORDS=Uri.parse("content://"+AUTHORITY+"/shared/v1/records");
    public static final String READ="org.sleepwell.app.permission.READ_RECORD_SUMMARIES",WRITE="org.sleepwell.app.permission.WRITE_RECORD_SUMMARIES";
    public static final String[] COLUMNS={"_id","start_ms","end_ms","reviewed_duration_ms","active_ms","recorded_zone","source","origin","sensor","edited","interrupted","clock_changed","duration_limited"};
    static final Set<String> NAMES=Set.of(COLUMNS);
    static final class Request {final long id,before;final int limit;Request(long id,long before,int limit){this.id=id;this.before=before;this.limit=limit;}}
    static Request parse(Uri uri){if(uri==null||!"content".equals(uri.getScheme())||!AUTHORITY.equals(uri.getEncodedAuthority())||uri.getFragment()!=null||uri.toString().length()>512)throw new IllegalArgumentException("Use a SleepWell shared/v1/records URI.");String path=uri.getEncodedPath();long id=0;if(!"/shared/v1/records".equals(path)){String prefix="/shared/v1/records/";if(path==null||!path.startsWith(prefix))throw new IllegalArgumentException("Unsupported record path.");id=number(path.substring(prefix.length()),false);}Set<String> keys=uri.getQueryParameterNames();if(!Set.of("limit","before_id").containsAll(keys))throw new IllegalArgumentException("Only limit and before_id are supported.");for(String key:keys)if(uri.getQueryParameters(key).size()!=1)throw new IllegalArgumentException("Duplicate query parameter.");if(id!=0&&!keys.isEmpty())throw new IllegalArgumentException("An individual record URI has no query parameters.");long limit=keys.contains("limit")?number(uri.getQueryParameter("limit"),false):10,before=keys.contains("before_id")?number(uri.getQueryParameter("before_id"),false):0;if(limit>25)throw new IllegalArgumentException("Use a limit of 1–25.");return new Request(id,before,(int)limit);}
    static long number(String text,boolean zero){if(text==null||!text.matches(zero?"0|[1-9][0-9]{0,15}":"[1-9][0-9]{0,15}"))throw new IllegalArgumentException("Use canonical positive decimal numbers.");try{long result=Long.parseLong(text);if(result>9007199254740991L)throw new IllegalArgumentException("Number exceeds the supported range.");return result;}catch(NumberFormatException invalid){throw new IllegalArgumentException("Invalid number.");}}
    static String[] projection(String[] wanted){if(wanted==null)return COLUMNS.clone();if(wanted.length<1||wanted.length>COLUMNS.length)throw new IllegalArgumentException("Choose supported summary columns.");Set<String> seen=new HashSet<>();for(String key:wanted)if(key==null||!NAMES.contains(key)||!seen.add(key))throw new IllegalArgumentException("Unknown or duplicate summary column.");return wanted.clone();}
    private RecordContentContract(){}
}
