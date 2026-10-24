package org.sleepwell.app;
import java.nio.*;
import org.junit.Test;
import static org.junit.Assert.*;
@org.junit.runner.RunWith(org.junit.runners.JUnit4.class)
public class QrLumaTest {
 @Test public void croppedPaddedInterleavedLumaUsesAbsolutePlaneCoordinatesWithoutMovingBuffer(){byte[] bytes=new byte[55];for(int y=0;y<4;y++)for(int x=0;x<5;x++)bytes[7+y*12+x*2]=(byte)(y*10+x);ByteBuffer source=ByteBuffer.wrap(bytes);source.position(7);assertArrayEquals(new byte[]{11,12,13,21,22,23},QrLuma.copy(source,12,2,1,1,3,2));assertEquals(7,source.position());}
 @Test public void invalidCropStrideTruncationAndOverflowCannotReadOutsidePlane(){assertThrows(IllegalArgumentException.class,()->QrLuma.copy(ByteBuffer.allocate(4),2,1,0,0,3,1));assertThrows(IllegalArgumentException.class,()->QrLuma.copy(ByteBuffer.allocate(4),2,1,0,0,2,3));assertThrows(IllegalArgumentException.class,()->QrLuma.copy(ByteBuffer.allocate(4),2,1,Integer.MAX_VALUE,0,2,1));assertThrows(IllegalArgumentException.class,()->QrLuma.copy(ByteBuffer.allocate(4),2,0,0,0,2,1));assertThrows(IllegalArgumentException.class,()->QrLuma.copy(ByteBuffer.allocate(4),2,1,-1,0,2,1));assertThrows(IllegalArgumentException.class,()->QrLuma.copy(ByteBuffer.allocate(4),2,1,0,0,3000,3000));}
}
