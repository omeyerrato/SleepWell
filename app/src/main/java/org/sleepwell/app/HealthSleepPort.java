package org.sleepwell.app;
import java.util.*;
interface HealthSleepPort {
 String READ="android.permission.health.READ_SLEEP",WRITE="android.permission.health.WRITE_SLEEP";
 final class Access {final int availability;final Set<String> granted;Access(int availability,Set<String> granted){this.availability=availability;this.granted=Set.copyOf(granted);}boolean available(){return availability==2;}}
 final class Page {final List<HealthSleep> records;final String next;Page(List<HealthSleep> records,String next){this.records=List.copyOf(records);this.next=next;}}
 final class Changes {final List<HealthSleep> records;final List<String> deleted;final String next;final boolean more,expired;Changes(List<HealthSleep> records,List<String> deleted,String next,boolean more,boolean expired){this.records=List.copyOf(records);this.deleted=List.copyOf(deleted);this.next=next;this.more=more;this.expired=expired;}}
 Access access()throws Exception;String token()throws Exception;Page read(long start,long end,String page)throws Exception;Changes changes(String token)throws Exception;String insert(String clientId,long version,HealthSleep sleep,boolean manual)throws Exception;void delete(String clientId)throws Exception;
}
