package org.sleepwell.app;

import java.nio.ByteBuffer;

/** Safe packing of CameraX's potentially padded/interleaved Y plane, without retaining a frame. */
final class QrLuma {
    static byte[] copy(ByteBuffer source,int rowStride,int pixelStride,int left,int top,int width,int height){
        if(source==null||rowStride<1||pixelStride<1||left<0||top<0||width<1||height<1||(long)width*height>4194304||(long)left+width>Integer.MAX_VALUE||(long)top+height>Integer.MAX_VALUE)throw new IllegalArgumentException("Unsupported camera frame.");
        long column=(long)(left+width-1)*pixelStride;if(column>=rowStride)throw new IllegalArgumentException("Invalid camera row stride.");long last=(long)source.position()+(long)(top+height-1)*rowStride+column;if(last>=source.limit())throw new IllegalArgumentException("Incomplete camera frame.");
        ByteBuffer bytes=source.duplicate();int base=bytes.position();byte[] packed=new byte[width*height];for(int y=0;y<height;y++)for(int x=0;x<width;x++)packed[y*width+x]=bytes.get(base+(top+y)*rowStride+(left+x)*pixelStride);return packed;
    }
    private QrLuma(){}
}
