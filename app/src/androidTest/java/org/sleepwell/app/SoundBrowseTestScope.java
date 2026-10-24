package org.sleepwell.app;
import android.content.*;import androidx.test.platform.app.InstrumentationRegistry;import org.junit.rules.ExternalResource;import java.util.*;import static org.junit.Assert.*;
/** Each native method gets private optional view state; personal sound browsing is never read/written. */
public final class SoundBrowseTestScope extends ExternalResource {
 private SoundBrowseState.Factory previous;private boolean captured;private Context context;private String name;SharedPreferences prefs;
 @Override protected void before()throws Throwable{try{context=InstrumentationRegistry.getInstrumentation().getTargetContext();name="verification-sound-browse-"+UUID.randomUUID();prefs=context.getSharedPreferences(name,0);InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{previous=SoundBrowseState.factory;captured=true;SoundBrowseState.factory=app->prefs;});}catch(Throwable failure){throw cleanup(failure);}}
 @Override protected void after(){Throwable failure=cleanup(null);if(failure instanceof Error)throw (Error)failure;if(failure instanceof RuntimeException)throw (RuntimeException)failure;if(failure!=null)throw new AssertionError("Private browsing cleanup failed",failure);}
 private static Throwable combine(Throwable first,Throwable next){if(first==null)return next;if(first!=next)first.addSuppressed(next);return first;}
 private Throwable cleanup(Throwable failure){
  try{if(prefs!=null)assertTrue("Private browsing writes drained",prefs.edit().clear().commit());}catch(Throwable clear){failure=combine(failure,clear);}
  finally{try{if(captured)InstrumentationRegistry.getInstrumentation().runOnMainSync(()->SoundBrowseState.factory=previous);}catch(Throwable restore){failure=combine(failure,restore);}
   finally{try{if(context!=null&&name!=null)assertTrue("Owned browsing namespace removed",context.deleteSharedPreferences(name));}catch(Throwable remove){failure=combine(failure,remove);}}}
  return failure;
 }
}
