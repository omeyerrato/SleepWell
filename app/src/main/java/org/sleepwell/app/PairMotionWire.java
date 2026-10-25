package org.sleepwell.app;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/** Versioned measurement-only messages. Transport encryption/authentication belongs to Nearby. */
final class PairMotionWire {
    static final int HELLO=1,PING=2,PONG=3,MOTION=4,MAX_BYTES=128;
    final int type;final UUID stream;final long sequence,a,b,c;final Double movement;final int samples;
    private PairMotionWire(int type,UUID stream,long sequence,long a,long b,long c,Double movement,int samples){this.type=type;this.stream=stream;this.sequence=sequence;this.a=a;this.b=b;this.c=c;this.movement=movement;this.samples=samples;}
    static PairMotionWire hello(UUID stream){return new PairMotionWire(HELLO,stream,0,0,0,0,null,0);}
    static PairMotionWire ping(UUID stream,long sequence,long sent){return new PairMotionWire(PING,stream,sequence,sent,0,0,null,0);}
    static PairMotionWire pong(UUID stream,long sequence,long sent,long received,long reply){return new PairMotionWire(PONG,stream,sequence,sent,received,reply,null,0);}
    static PairMotionWire motion(UUID stream,long sequence,long from,long to,Double movement,int samples){return new PairMotionWire(MOTION,stream,sequence,from,to,0,movement,samples);}
    byte[] bytes(){validate();ByteBuffer b=ByteBuffer.allocate(66).order(ByteOrder.BIG_ENDIAN);b.putInt(0x53575031).put((byte)1).put((byte)type).putLong(stream.getMostSignificantBits()).putLong(stream.getLeastSignificantBits()).putLong(sequence).putLong(a).putLong(this.b).putLong(c).putDouble(movement==null?Double.NaN:movement).putInt(samples);return b.array();}
    static PairMotionWire read(byte[] bytes){if(bytes==null||bytes.length!=66)throw new IllegalArgumentException("Unsupported pairing message");ByteBuffer b=ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);if(b.getInt()!=0x53575031||b.get()!=1)throw new IllegalArgumentException("Unsupported pairing protocol");int type=b.get()&255;UUID stream=new UUID(b.getLong(),b.getLong());long sequence=b.getLong(),a=b.getLong(),from=b.getLong(),c=b.getLong();double value=b.getDouble();int samples=b.getInt();PairMotionWire result=new PairMotionWire(type,stream,sequence,a,from,c,Double.isNaN(value)?null:value,samples);result.validate();return result;}
    private void validate(){if(stream==null||sequence<0||a<0||b<0||c<0||type<HELLO||type>MOTION)throw new IllegalArgumentException("Invalid paired measurement");if(type==HELLO&&(sequence!=0||a!=0||b!=0||c!=0)||type==PING&&(sequence<=0||b!=0||c!=0)||type==PONG&&(sequence<=0||c<b)||type==MOTION&&(sequence<=0||b<=a||b-a>15000||c!=0||samples<0||samples>1000||movement!=null&&(!Double.isFinite(movement)||movement<0||movement>1000)||samples==0&&movement!=null||samples>0&&movement==null))throw new IllegalArgumentException("Invalid paired measurement fields");if(type!=MOTION&&(movement!=null||samples!=0))throw new IllegalArgumentException("Unexpected paired measurement payload");}
}
