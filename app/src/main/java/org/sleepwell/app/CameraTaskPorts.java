package org.sleepwell.app;

import android.view.View;

/** Shared release contract: close revokes callbacks; released attests actual owned cleanup. */
final class CameraTaskPorts {
 interface Capture {
  View preview();
  void close();
  /** Synchronous legacy fake ports remain compatible; actual drivers override this. */
  default boolean released(){return true;}
 }
 private CameraTaskPorts(){}
}
