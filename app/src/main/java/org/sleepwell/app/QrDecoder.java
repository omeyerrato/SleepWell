package org.sleepwell.app;

import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import java.util.*;

/** Pinned ZXing core, QR-only, offline. Results are digested immediately, never launched or stored. */
final class QrDecoder {
    private final MultiFormatReader reader=new MultiFormatReader();
    QrDecoder(){reader.setHints(Map.of(DecodeHintType.POSSIBLE_FORMATS,List.of(BarcodeFormat.QR_CODE),DecodeHintType.TRY_HARDER,true,DecodeHintType.CHARACTER_SET,"UTF-8",DecodeHintType.ALSO_INVERTED,true));}
    String digest(byte[] luma,int width,int height){
        if(luma==null||width<1||height<1||(long)width*height!=luma.length||(long)width*height>4194304)throw new IllegalArgumentException("Unsupported camera frame.");
        try{Result result=reader.decodeWithState(new BinaryBitmap(new HybridBinarizer(new PlanarYUVLuminanceSource(luma,width,height,0,0,width,height,false))));return MorningCode.digest(result.getText());}catch(NotFoundException absent){return null;}finally{reader.reset();}
    }
}
