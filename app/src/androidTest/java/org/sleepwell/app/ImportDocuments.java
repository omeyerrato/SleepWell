package org.sleepwell.app;

import android.database.*;
import android.content.Context;
import android.os.*;
import android.provider.*;
import java.io.*;
import java.nio.*;

/** Read-only synthetic fixtures, available only in the disposable API36 emulator. Never in the app APK. */
public final class ImportDocuments extends DocumentsProvider {
    static final String AUTHORITY="org.sleepwell.app.test.documents",ROOT="SleepWell test audio";
    private static final String[] IDS={"valid","unknown","broken","large","overflow","denied","delayed"};
    private static final String[] NAMES={"Quiet sea.wav","Unknown size.wav","Broken audio.wav","Too large.wav","Hidden large.wav","Denied.wav","Delayed.wav"};
    private boolean enabled(){return Build.MODEL.startsWith("sdk_gphone64")&&Build.HARDWARE.equals("ranchu");}
    private void guard(){if(!enabled())throw new SecurityException("CI emulator fixture only");}
    static File analysisFile(Context context,String uuid){if(uuid==null||!java.util.UUID.fromString(uuid).toString().equals(uuid))throw new IllegalArgumentException("Canonical fixture UUID required");return new File(context.getCacheDir(),"recorded-analysis-"+uuid+".m4a");}
    private File analysisDocument(String id){return id!=null&&id.startsWith("recorded-analysis-")?analysisFile(getContext(),id.substring("recorded-analysis-".length())):null;}
    @Override public boolean onCreate(){return true;}
    private MatrixCursor cursor(String[] projection,String... defaults){return new MatrixCursor(projection==null?defaults:projection);}
    @Override public Cursor queryRoots(String[] projection){MatrixCursor c=cursor(projection,DocumentsContract.Root.COLUMN_ROOT_ID,DocumentsContract.Root.COLUMN_TITLE,DocumentsContract.Root.COLUMN_DOCUMENT_ID,DocumentsContract.Root.COLUMN_FLAGS,DocumentsContract.Root.COLUMN_MIME_TYPES);if(enabled()){MatrixCursor.RowBuilder row=c.newRow();for(String column:c.getColumnNames())row.add(column,switch(column){case DocumentsContract.Root.COLUMN_ROOT_ID->"audio";case DocumentsContract.Root.COLUMN_TITLE->ROOT;case DocumentsContract.Root.COLUMN_DOCUMENT_ID->"root";case DocumentsContract.Root.COLUMN_FLAGS->DocumentsContract.Root.FLAG_LOCAL_ONLY;case DocumentsContract.Root.COLUMN_MIME_TYPES->"audio/*";default->null;});}return c;}
    private int index(String id)throws FileNotFoundException{for(int i=0;i<IDS.length;i++)if(IDS[i].equals(id))return i;throw new FileNotFoundException("Unknown fixture");}
    private void document(MatrixCursor c,String id)throws FileNotFoundException{File analysis=analysisDocument(id);if(analysis!=null){if(!analysis.isFile())throw new FileNotFoundException("Missing encoded fixture");MatrixCursor.RowBuilder row=c.newRow();for(String column:c.getColumnNames())row.add(column,switch(column){case DocumentsContract.Document.COLUMN_DOCUMENT_ID->id;case DocumentsContract.Document.COLUMN_DISPLAY_NAME->"Licensed laughter.m4a";case DocumentsContract.Document.COLUMN_MIME_TYPE->"audio/mp4";case DocumentsContract.Document.COLUMN_FLAGS->0;case DocumentsContract.Document.COLUMN_SIZE->analysis.length();default->null;});return;}int i=id.equals("root")?-1:index(id);MatrixCursor.RowBuilder row=c.newRow();for(String column:c.getColumnNames())row.add(column,switch(column){case DocumentsContract.Document.COLUMN_DOCUMENT_ID->id;case DocumentsContract.Document.COLUMN_DISPLAY_NAME->i<0?ROOT:NAMES[i];case DocumentsContract.Document.COLUMN_MIME_TYPE->i<0?DocumentsContract.Document.MIME_TYPE_DIR:"audio/wav";case DocumentsContract.Document.COLUMN_FLAGS->0;case DocumentsContract.Document.COLUMN_SIZE->id.equals("unknown")||id.equals("overflow")||id.equals("delayed")?null:id.equals("large")?100*1024*1024L+1:id.equals("broken")?12L:32044L;default->null;});}
    @Override public Cursor queryDocument(String id,String[] projection)throws FileNotFoundException{guard();MatrixCursor c=cursor(projection,DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME,DocumentsContract.Document.COLUMN_MIME_TYPE,DocumentsContract.Document.COLUMN_FLAGS,DocumentsContract.Document.COLUMN_SIZE);document(c,id);return c;}
    @Override public Cursor queryChildDocuments(String parent,String[] projection,String sort)throws FileNotFoundException{guard();if(!parent.equals("root"))throw new FileNotFoundException();MatrixCursor c=cursor(projection,DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME,DocumentsContract.Document.COLUMN_MIME_TYPE,DocumentsContract.Document.COLUMN_FLAGS,DocumentsContract.Document.COLUMN_SIZE);for(String id:IDS)document(c,id);return c;}
    @Override public boolean isChildDocument(String parent,String child){return enabled()&&parent.equals("root")&&java.util.Arrays.asList(IDS).contains(child);}
    private byte[] wave(){ByteBuffer b=ByteBuffer.allocate(32044).order(ByteOrder.LITTLE_ENDIAN);b.put(new byte[]{'R','I','F','F'}).putInt(32036).put(new byte[]{'W','A','V','E','f','m','t',' '}).putInt(16).putShort((short)1).putShort((short)1).putInt(8000).putInt(16000).putShort((short)2).putShort((short)16).put(new byte[]{'d','a','t','a'}).putInt(32000);return b.array();}
    @Override public ParcelFileDescriptor openDocument(String id,String mode,CancellationSignal signal)throws FileNotFoundException{
        guard();File analysis=analysisDocument(id);if(analysis!=null){if(!"r".equals(mode))throw new FileNotFoundException("Read only");if(signal!=null)signal.throwIfCanceled();return ParcelFileDescriptor.open(analysis,ParcelFileDescriptor.MODE_READ_ONLY);}index(id);if(!"r".equals(mode)||id.equals("denied"))throw new FileNotFoundException("Read unavailable");
        try{
            if(id.equals("delayed")){ParcelFileDescriptor[] pipe=ParcelFileDescriptor.createReliablePipe();Thread writer=new Thread(()->{try(OutputStream out=new ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])){Thread.sleep(8000);out.write(wave());}catch(Exception ignored){}},"fixture-writer");writer.setDaemon(true);writer.start();return pipe[0];}
            File f=new File(getContext().getCacheDir(),"fixture-"+id+".wav");synchronized(ImportDocuments.class){if(!f.exists()){if(id.equals("overflow")||id.equals("large")){try(RandomAccessFile out=new RandomAccessFile(f,"rw")){out.setLength(100*1024*1024L+1);}}else try(FileOutputStream out=new FileOutputStream(f)){out.write(id.equals("broken")?new byte[12]:wave());}}}
            return ParcelFileDescriptor.open(f,ParcelFileDescriptor.MODE_READ_ONLY);
        }catch(IOException e){throw new FileNotFoundException("Fixture read failed");}
    }
}
