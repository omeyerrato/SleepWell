package org.sleepwell.app;

/** Serial command ownership, not an exclusive lease on Android's physical flashlight. */
final class PhoneTorchCoordinator {
 enum Kind {TRAVEL,ALARM}
 interface Port {void set(boolean enabled);}
 interface Events {void ready(Lease lease);void lost(String reason);}
 final class Lease {
  final long generation;final Kind kind;final java.util.function.BooleanSupplier alive;final Events events;boolean closed,borrowed,pending,pendingOff,owned,desired;
  Lease(long generation,Kind kind,java.util.function.BooleanSupplier alive,Events events){this.generation=generation;this.kind=kind;this.alive=alive;this.events=events;}
  void set(boolean enabled){if(closed||active!=this)return;if(borrowed)return;if(enabled&&!alive.getAsBoolean()){lose(this,"The light session ended.",true);return;}if(enabled){desired=true;pendingOff=false;pending=true;try{port.set(true);}catch(RuntimeException failure){lose(this,"The flashlight request failed.",true);}}else{boolean off=owned||pending||desired;desired=pending=owned=false;pendingOff=off;if(off)try{port.set(false);}catch(RuntimeException failure){lose(this,"The flashlight could not be released.",false);}}}
  void close(){release(this,"",true);}
 }
 private final Port port;private Lease active;private boolean known,lit,available=true;private long generation;
 PhoneTorchCoordinator(Port port){this.port=port;}
 Lease acquire(Kind kind,java.util.function.BooleanSupplier alive,Events events){if(!alive.getAsBoolean()){events.lost("The light session ended.");return null;}if(!available||!known){events.lost("Flashlight status is unavailable.");return null;}if(generation==Long.MAX_VALUE){events.lost("Flashlight ownership is exhausted.");return null;}boolean takeover=false;if(active!=null){if(active.kind==Kind.ALARM&&kind==Kind.TRAVEL){events.lost("The alarm is using the flashlight.");return null;}takeover=active.kind==Kind.TRAVEL&&kind==Kind.ALARM;release(active,"Another light session took priority.",true);}if(kind==Kind.TRAVEL&&lit&&!takeover){events.lost("The flashlight is already on in another app.");return null;}
  Lease lease=new Lease(++generation,kind,alive,events);active=lease;lease.borrowed=lit&&!takeover;if(kind==Kind.ALARM&&!lease.borrowed)lease.set(true);if(active==lease&&!lease.closed)events.ready(lease);return active==lease?lease:null;
 }
 void observed(boolean enabled){known=true;available=true;lit=enabled;Lease lease=active;if(lease==null||lease.closed||lease.borrowed)return;if(enabled&&lease.pendingOff)return;if(enabled&&lease.pending){lease.pending=false;lease.owned=true;}else if(enabled&&!lease.desired){lose(lease,"The flashlight changed outside this session.",false);}else if(!enabled&&!lease.pending){boolean interrupted=lease.desired;lease.desired=false;lease.owned=false;lease.pendingOff=false;if(lease.kind==Kind.TRAVEL&&interrupted)lose(lease,"The flashlight was turned off outside this session.",false);}}
 void unavailable(){known=false;available=false;lit=false;if(active!=null)lose(active,"The flashlight is busy or unavailable.",false);}
 boolean idle(){return active==null;}
 private void lose(Lease lease,String reason,boolean off){release(lease,reason,off);}
 private void release(Lease lease,String reason,boolean off){if(lease==null||lease.closed)return;boolean ours=active==lease;lease.closed=true;if(ours)active=null;boolean release=ours&&!lease.borrowed&&(lease.owned||lease.pending||lease.desired);lease.owned=lease.pending=lease.pendingOff=lease.desired=false;if(release&&off)try{port.set(false);lit=false;}catch(RuntimeException failure){if(reason.isEmpty())reason="The flashlight could not be released.";}if(!reason.isEmpty())lease.events.lost(reason);}
}
