package org.sleepwell.app;

import java.io.*;

/** Bounds actual bytes, including providers that report no size or the wrong size. */
final class AudioCopy {
    interface Check { void run() throws IOException; }
    interface Progress { void bytes(long count); }
    static long copy(InputStream in,OutputStream out,long limit,Check check,Progress progress)throws IOException{
        if(limit<1)throw new IllegalArgumentException("Positive byte limit required");
        byte[] buffer=new byte[32768];long count=0;int empty=0;
        while(true){
            check.run();int n=in.read(buffer,0,(int)Math.min(buffer.length,limit-count+1));check.run();
            if(n<0)break;
            if(n==0){if(++empty>16)throw new IOException("The document stopped responding");continue;}
            empty=0;if(n>limit-count)throw new IOException("Audio exceeds the import limit");
            out.write(buffer,0,n);count+=n;progress.bytes(count);
        }
        if(count==0)throw new IOException("Empty audio document");return count;
    }
}
