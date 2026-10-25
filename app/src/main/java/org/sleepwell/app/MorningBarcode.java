package org.sleepwell.app;

import java.util.*;

/** Explicit bounded offline formats. No product lookup and no content/URL execution. */
final class MorningBarcode {
    static final String QR="QR_CODE";
    static final List<String> FORMATS=List.of("EAN_13","EAN_8","UPC_A","UPC_E","CODE_128","CODE_39");
    static boolean supported(String format){return format!=null&&(QR.equals(format)||FORMATS.contains(format));}
    static String label(String format){return switch(format){case QR->"QR code";case "EAN_13"->"EAN-13";case "EAN_8"->"EAN-8";case "UPC_A"->"UPC-A";case "UPC_E"->"UPC-E";case "CODE_128"->"Code 128";case "CODE_39"->"Code 39";default->throw new IllegalArgumentException("Unsupported morning-code format");};}
    static String normalized(String format,String payload){if(format==null||!FORMATS.contains(format)||payload==null||payload.isBlank())throw new IllegalArgumentException("This barcode format or content is unsupported.");int length=switch(format){case "EAN_13"->13;case "EAN_8","UPC_E"->8;case "UPC_A"->12;default->0;};if(length>0){if(payload.length()!=length||!payload.matches("[0-9]+"))throw new IllegalArgumentException("Read a complete "+label(format)+" barcode.");String digits=payload;if(format.equals("UPC_E")){if(payload.charAt(0)!='0'&&payload.charAt(0)!='1')throw new IllegalArgumentException("Unsupported UPC-E number system.");digits=com.google.zxing.oned.UPCEReader.convertUPCEtoUPCA(payload);}int sum=digits.charAt(digits.length()-1)-'0',weight=3;for(int n=digits.length()-2;n>=0;n--){sum+=(digits.charAt(n)-'0')*weight;weight=4-weight;}if(sum%10!=0)throw new IllegalArgumentException("Barcode check digit is invalid.");}else{if(payload.length()>80||payload.codePoints().anyMatch(c->c<32||c>126))throw new IllegalArgumentException("Use a barcode with 1–80 printable ASCII characters.");if(format.equals("CODE_39")&&!payload.matches("[0-9A-Z .$/+%\\-]+"))throw new IllegalArgumentException("Use standard uppercase Code 39 content.");}return payload;}
    static String fingerprint(String format,String payload){if(QR.equals(format))return MorningCode.digest(payload);return MorningCode.digest("SleepWell barcode v1\u0000"+format+"\u0000"+normalized(format,payload));}
}
