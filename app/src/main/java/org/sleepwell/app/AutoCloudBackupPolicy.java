package org.sleepwell.app;
/** Retry only when creation definitely has not begun. No guessed exactly-once provider effects. */
final class AutoCloudBackupPolicy {
 static final int MAX_ATTEMPTS=3,MAX_PENDING=16;
 static boolean oversized(Throwable error){for(Throwable at=error;at!=null;at=at.getCause()){String m=at.getMessage();if("Export exceeds 8 MiB.".equals(m)||"Backup exceeds 8 MiB.".equals(m)||"Backup exceeds the 8 MB import limit.".equals(m)||"Sound metadata exceeds the backup limit".equals(m))return true;}return false;}
 static String failure(boolean creationStarted,int attempts){return creationStarted?"UNCERTAIN":attempts<MAX_ATTEMPTS?"PENDING":"FAILED";}
 static long retryAt(long now,int attempts){return Math.addExact(now,Math.min(6*3600000L,15*60000L*(1L<<Math.min(4,Math.max(0,attempts-1)))));}
 static boolean eligible(long id,long start,long end,boolean interrupted,String source,String origin){return id>0&&start>0&&end>start&&!interrupted&&"tracked".equals(source)&&"local".equals(origin);}
 static String key(long id,long start,long end,long revision,String tree){if(!eligible(id,start,end,false,"tracked","local")||revision<1||tree==null||!tree.startsWith("content://"))throw new IllegalArgumentException("Invalid backup trigger");return id+":"+start+":"+end+":"+revision+":"+tree;}
 private AutoCloudBackupPolicy(){}
}
