package org.sleepwell.app;
import java.util.*;
import com.google.zxing.*;
import com.google.zxing.common.*;
import com.google.zxing.qrcode.*;
import org.junit.Test;
import static org.junit.Assert.*;
@org.junit.runner.RunWith(org.junit.runners.JUnit4.class)
public class QrDecoderTest {
 @Test public void actualGeneratedUtf8QrDecodesAcrossRotationsAndInversion()throws Exception{String payload="SleepWell:صباح الخير/朝";int size=320;BitMatrix matrix=new QRCodeWriter().encode(payload,BarcodeFormat.QR_CODE,size,size,Map.of(EncodeHintType.CHARACTER_SET,"UTF-8",EncodeHintType.MARGIN,4));byte[] image=new byte[size*size];for(int y=0;y<size;y++)for(int x=0;x<size;x++)image[y*size+x]=(byte)(matrix.get(x,y)?0:255);QrDecoder decoder=new QrDecoder();for(int rotation=0;rotation<4;rotation++){assertEquals(MorningCode.digest(payload),decoder.digest(image,size,size));byte[] inverted=image.clone();for(int n=0;n<inverted.length;n++)inverted[n]=(byte)(255-(inverted[n]&255));assertEquals(MorningCode.digest(payload),decoder.digest(inverted,size,size));byte[] turned=new byte[image.length];for(int y=0;y<size;y++)for(int x=0;x<size;x++)turned[x*size+size-1-y]=image[y*size+x];image=turned;}}
 @Test public void blankAndBadFrameCannotBecomeEnrollmentProof(){QrDecoder decoder=new QrDecoder();byte[] blank=new byte[256*256];Arrays.fill(blank,(byte)255);assertNull(decoder.digest(blank,256,256));assertThrows(IllegalArgumentException.class,()->decoder.digest(new byte[4],3,3));}
}
