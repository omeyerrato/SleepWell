package org.sleepwell.app;
import android.content.SharedPreferences;import java.util.UUID;
/** Durable completed hurdles only. A new controller invalidates stale callbacks for the same unit. */
final class JumpCheckpoint {
 final SharedPreferences prefs;final String owner,token;final int cleared;final boolean math;
 private JumpCheckpoint(SharedPreferences p,String owner,String token,int cleared,boolean math){prefs=p;this.owner=owner;this.token=token;this.cleared=cleared;this.math=math;}
 static JumpCheckpoint open(SharedPreferences p,String run,int unit,int round){String owner=ChallengePackState.key(run,unit,round);synchronized(JumpCheckpoint.class){int count=0;boolean math=false;if(owner.equals(p.getString("jumpOwner",""))){if(p.getInt("jumpVersion",0)!=1)throw new IllegalArgumentException("Jump progress needs review");count=p.getInt("jumpCleared",-1);math=p.getBoolean("jumpMath",false);if(count<0||count>JumpGame.TARGET)throw new IllegalArgumentException("Jump checkpoint is invalid");}String token=UUID.randomUUID().toString();if(!p.edit().putInt("jumpVersion",1).putString("jumpOwner",owner).putString("jumpToken",token).putInt("jumpCleared",count).putBoolean("jumpMath",math).commit())throw new IllegalStateException("Jump progress could not be saved");return new JumpCheckpoint(p,owner,token,count,math);}}
 static boolean math(SharedPreferences p,String run,int unit,int round){return ChallengePackState.key(run,unit,round).equals(p.getString("jumpOwner",""))&&p.getBoolean("jumpMath",false);}
 boolean owns(){return owner.equals(prefs.getString("jumpOwner",""))&&token.equals(prefs.getString("jumpToken",""));}
 boolean save(int count){synchronized(JumpCheckpoint.class){if(!owns()||count<0||count>JumpGame.TARGET||count<prefs.getInt("jumpCleared",0))return false;return prefs.edit().putInt("jumpCleared",count).commit();}}
 boolean fallback(){synchronized(JumpCheckpoint.class){return owns()&&prefs.edit().putBoolean("jumpMath",true).commit();}}
}
