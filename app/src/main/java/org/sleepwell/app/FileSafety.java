package org.sleepwell.app;

import android.content.Context;
import java.io.*;

final class FileSafety {
    static File clip(Context c,String filename) throws IOException {
        File root=new File(c.getFilesDir(),"clips").getCanonicalFile(); File f=new File(root,filename).getCanonicalFile();
        if(!f.getParentFile().equals(root) || !filename.endsWith(".wav")) throw new IOException("Invalid clip path"); return f;
    }
    static void deleteClip(Context c,String filename) { try { File f=clip(c,filename); if(f.exists() && !f.delete()) Prefs.put(c,"lastError","Could not remove an expired audio clip."); } catch(IOException ignored) {} }
}
