package org.sleepwell.app;

import java.nio.*;
import java.nio.charset.*;
import java.security.*;
import java.util.*;

/** Only label, digest and optional exact barcode format survive enrollment. Payload text is never persisted or opened. */
final class MorningCode {
    static final int MAX_BYTES=2048;
    final String label,hash,format;final MathSettings fallback;
    MorningCode(String label,String hash,MathSettings fallback){this(label,hash,fallback,MorningBarcode.QR);}
    MorningCode(String label,String hash,MathSettings fallback,String format){this.label=label(label);if(!validHash(hash)||fallback==null||!MorningBarcode.supported(format))throw new IllegalArgumentException("Capture a valid morning code.");this.hash=hash;this.fallback=fallback;this.format=format;}
    boolean sameIdentity(MorningCode other){return other!=null&&format.equals(other.format)&&same(hash,other.hash);}
    static String label(String raw){if(raw==null)throw new IllegalArgumentException("Name this code's location.");String clean=raw.trim();if(clean.isEmpty()||clean.codePointCount(0,clean.length())>60||clean.codePoints().anyMatch(Character::isISOControl))throw new IllegalArgumentException("Use a location name of 1–60 characters.");return clean;}
    static boolean validHash(String hash){return hash!=null&&hash.matches("[0-9a-f]{64}");}
    static boolean same(String expected,String actual){return validHash(expected)&&validHash(actual)&&MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),actual.getBytes(StandardCharsets.US_ASCII));}
    static String digest(String payload){
        if(payload==null||payload.isBlank())throw new IllegalArgumentException("This QR code is empty.");
        try{ByteBuffer bytes=StandardCharsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).encode(CharBuffer.wrap(payload));if(bytes.remaining()>MAX_BYTES)throw new IllegalArgumentException("Use a QR code with at most 2 KB of content.");byte[] value=new byte[bytes.remaining()];bytes.get(value);byte[] digest=MessageDigest.getInstance("SHA-256").digest(value);StringBuilder text=new StringBuilder(64);for(byte b:digest)text.append(String.format(Locale.ROOT,"%02x",b&255));return text.toString();}catch(CharacterCodingException invalid){throw new IllegalArgumentException("This QR code has unsupported text encoding.");}catch(NoSuchAlgorithmException impossible){throw new AssertionError(impossible);}
    }
}
