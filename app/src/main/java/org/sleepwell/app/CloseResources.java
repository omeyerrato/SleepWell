package org.sleepwell.app;

import java.util.List;

/** Close independently in dependency order, retaining evidence of every failed cleanup. */
final class CloseResources {
    static void close(List<? extends AutoCloseable> handles){RuntimeException failure=null;for(AutoCloseable handle:handles){if(handle==null)continue;try{handle.close();}catch(Exception|LinkageError error){if(failure==null)failure=new IllegalStateException("Could not release sound analysis resources",error);else failure.addSuppressed(error);}}if(failure!=null)throw failure;}
}
