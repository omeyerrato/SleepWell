package org.sleepwell.app;

import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import java.util.*;

/** Pinned ZXing core, exactly one selected QR/barcode format, offline. Results are digested immediately, never launched or stored. */
final class QrDecoder {
    private final MultiFormatReader reader=new MultiFormatReader();
    private final String format;
    QrDecoder(){this(MorningBarcode.QR);}
    QrDecoder(String format){if(!MorningBarcode.supported(format))throw new IllegalArgumentException("Unsupported morning-code format");this.format=format;reader.setHints(Map.of(DecodeHintType.POSSIBLE_FORMATS,List.of(BarcodeFormat.valueOf(format)),DecodeHintType.TRY_HARDER,true,DecodeHintType.CHARACTER_SET,"UTF-8",DecodeHintType.ALSO_INVERTED,true));}
    String digest(byte[] luma,int width,int height){
        if(luma==null||width<1||height<1||(long)width*height!=luma.length||(long)width*height>4194304)throw new IllegalArgumentException("Unsupported camera frame.");
        String result=read(luma,width,height);if(result!=null||format.equals(MorningBarcode.QR))return result;byte[] transformed=luma.clone();invert(transformed);result=read(transformed,width,height);if(result!=null)return result;for(int y=0;y<height;y++)for(int x=0;x<width;x++)transformed[x*height+height-1-y]=luma[y*width+x];result=read(transformed,height,width);if(result!=null)return result;invert(transformed);return read(transformed,height,width);
    }
    private static void invert(byte[] pixels){for(int n=0;n<pixels.length;n++)pixels[n]=(byte)(255-(pixels[n]&255));}
    private String read(byte[] luma,int width,int height){try{Result result=reader.decodeWithState(new BinaryBitmap(new HybridBinarizer(new PlanarYUVLuminanceSource(luma,width,height,0,0,width,height,false))));if(!result.getBarcodeFormat().name().equals(format))return null;return MorningBarcode.fingerprint(format,result.getText());}catch(NotFoundException absent){return null;}finally{reader.reset();}}
}
