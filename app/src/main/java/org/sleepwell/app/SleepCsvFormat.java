package org.sleepwell.app;

import java.io.*;
import java.math.*;
import java.nio.*;
import java.nio.charset.*;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.regex.*;
import org.json.*;

/** Original implementation of the documented vendor format, not a copy of its converter.
 * https://sleep.urbandroid.org/docs/devs/csv.html
 * Real format example: urbandroid-team/sleep-csv-to-json/master/sleep-test2.csv .
 * Only periods/notes/tags are projected. Actigraphy, noise and stage/event columns remain evidence. */
final class SleepCsvFormat {
    static final int MAX_FILE=4*1024*1024,MAX_RECORDS=2000,MAX_COLUMNS=10000,MAX_FIELD=20000;
    static final List<String> FIXED=List.of("Id","Tz","From","To","Sched","Hours","Rating","Comment","Framerate","Snore","Noise","Cycles","DeepSleep","LenAdjust","Geo");
    private static final Pattern DATE=Pattern.compile("^(\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*(\\d{4})\\s+(\\d{1,2}):(\\d{2})(?::(\\d{2}))?$");
    private static final Pattern CLOCK=Pattern.compile("(?:[01]?\\d|2[0-3]):[0-5]\\d"),EVENT=Pattern.compile("[A-Za-z_][A-Za-z0-9_]*-[0-9]{1,16}(?:-.{1,1024})?");
    static final class Entry {
        final String sourceId,zone,note,raw,sha256,contentKey;final long start,end,period;final Long adjustmentMinutes,netDuration;final BigDecimal rating;final RecordDraft draft;final int actigraphy,noise,events;final boolean dstResolved,manualVendor;
        Entry(String sourceId,String zone,String note,String raw,long start,long end,Long adjustment,BigDecimal rating,RecordDraft draft,int actigraphy,int noise,int events,boolean dstResolved,boolean manualVendor,String contentKey)throws Exception{this.sourceId=sourceId;this.zone=zone;this.note=note;this.raw=raw;this.start=start;this.end=end;period=end-start;adjustmentMinutes=adjustment;netDuration=adjustment==null?null:Math.addExact(period,Math.multiplyExact(adjustment,60000));this.rating=rating;this.draft=draft;this.actigraphy=actigraphy;this.noise=noise;this.events=events;this.dstResolved=dstResolved;this.manualVendor=manualVendor;sha256=hash(raw.getBytes(StandardCharsets.UTF_8));this.contentKey=contentKey;}
        boolean fractionalRating(){return rating.stripTrailingZeros().scale()>0;}
    }
    /** Index is the exact 1-based CSV column; clock is a literal label, never an inferred instant. */
    static final class Sample {
        final int column;final String clock,raw,missing;final BigDecimal value;
        Sample(int column,String clock,String raw){this.column=column;this.clock=clock;this.raw=raw;BigDecimal numeric=null;if(!raw.isEmpty()&&!EVENT.matcher(raw).matches()){try{BigDecimal parsed=decimal(raw,"source signal");if(parsed.signum()>=0)numeric=parsed;}catch(IllegalArgumentException ignored){}}value=numeric;this.missing=numeric!=null?"":raw.isEmpty()?"Missing source field":EVENT.matcher(raw).matches()?"Event field, not a sample":"Negative / unavailable source value";}
        boolean available(){return value!=null;}
    }
    static final class Signals {
        final List<Sample> samples;final boolean clockColumns;final String interpretation;final int available,missing;final BigDecimal maximum;
        Signals(List<Sample> samples,boolean clocks,String interpretation){this.samples=List.copyOf(samples);clockColumns=clocks;this.interpretation=interpretation;int known=0;BigDecimal max=BigDecimal.ZERO;for(Sample sample:samples)if(sample.available()){known++;max=max.max(sample.value);}available=known;missing=samples.size()-known;maximum=max;}
    }
    static final class SourceSignals {
        final Signals actigraphy,noise;final List<String> events;
        SourceSignals(Signals actigraphy,Signals noise,List<String> events){this.actigraphy=actigraphy;this.noise=noise;this.events=List.copyOf(events);}
    }
    /** Called on the source-view worker only: normal imports/backups do not allocate graph samples. */
    static SourceSignals signals(Entry entry,BooleanSupplier active)throws Exception {
        List<Row> original=new ArrayList<>();for(Row row:rows(entry.raw,active))if(!row.blank())original.add(row);if(original.size()<2||original.size()>3)throw new IllegalArgumentException("The source record rows are unavailable.");List<String> header=original.get(0).fields,values=original.get(1).fields;checkHeader(header);List<String> events=new ArrayList<>();for(int n=FIXED.size();n<values.size();n++)if(EVENT.matcher(values.get(n)).matches())events.add(values.get(n));return new SourceSignals(signal(header,values,false),signal(header,original.size()==3?original.get(2).fields:List.of(),true),events);
    }
    private static Signals signal(List<String> header,List<String> values,boolean noise){
        List<Integer> clocks=new ArrayList<>();for(int n=FIXED.size();n<header.size();n++)if(CLOCK.matcher(header.get(n)).matches())clocks.add(n);
        if(values.isEmpty())return new Signals(List.of(),false,noise?"No optional noise row was saved.":"No actigraphy row was saved.");
        boolean aligned=!clocks.isEmpty();int lastClock=clocks.isEmpty()?-1:clocks.get(clocks.size()-1);
        if(noise){aligned&=values.size()==lastClock+1||values.size()==header.size();for(int n=0;n<Math.min(FIXED.size(),values.size());n++)if(!values.get(n).isEmpty())aligned=false;for(int n=lastClock+1;n<values.size();n++)if(n>=0&&!values.get(n).isEmpty())aligned=false;}
        else aligned&=values.size()==header.size();
        for(int n=FIXED.size();n<Math.min(values.size(),header.size());n++){String raw=values.get(n);boolean event=EVENT.matcher(raw).matches();if(CLOCK.matcher(header.get(n)).matches()){if(event)aligned=false;}else if(!raw.isEmpty()&&(!event||noise))aligned=false;}
        List<Sample> samples=new ArrayList<>();if(aligned){for(int column:clocks)samples.add(new Sample(column+1,header.get(column),column<values.size()?values.get(column):""));return new Signals(samples,true,"Source-column order with exact CSV clock labels. Spacing is not elapsed time; no dates or DST offsets are assigned to samples.");}
        int first=noise?0:FIXED.size();if(noise)while(first<values.size()&&values.get(first).isEmpty())first++;int last=-1;for(int n=first;n<values.size();n++)if(!values.get(n).isEmpty()&&!EVENT.matcher(values.get(n)).matches())last=n;
        for(int n=first;n<=last;n++)samples.add(new Sample(n+1,"",values.get(n)));return new Signals(samples,false,noise?"Noise row columns do not prove clock alignment. Original value order and CSV column numbers only; leading metadata placeholders are not treated as measurements.":"Header/value columns do not prove clock alignment. Original source value order and CSV column numbers only.");
    }
    static final class Document {final List<Entry> entries;final int repeated;final String sha256;Document(List<Entry> entries,int repeated,String hash){this.entries=List.copyOf(entries);this.repeated=repeated;sha256=hash;}}
    private static final class Row {final List<String> fields;final int from,to;Row(List<String> fields,int from,int to){this.fields=List.copyOf(fields);this.from=from;this.to=to;}boolean blank(){return fields.size()==1&&fields.get(0).isBlank();}}
    static String hash(byte[] bytes)throws Exception{return NightAudioArchive.hash(MessageDigest.getInstance("SHA-256").digest(bytes));}
    static Document parse(byte[] bytes,long now,BooleanSupplier active)throws Exception {
        if(bytes.length==0||bytes.length>MAX_FILE)throw new IllegalArgumentException("Choose a Sleep as Android CSV of 1 byte to 4 MiB.");String text=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
        if(text.startsWith("\ufeff"))text=text.substring(1);if(text.indexOf('\0')>=0)throw new IllegalArgumentException("CSV contains an invalid NUL character.");List<Row> rows=rows(text,active);List<Entry> result=new ArrayList<>();Map<String,Entry> identities=new HashMap<>();int repeated=0;
        for(int n=0;n<rows.size();){NightAudioArchive.active(active);Row header=rows.get(n++);if(header.blank())continue;if(result.size()>=MAX_RECORDS)throw new IllegalArgumentException("Import at most 2,000 records at a time.");try{
                checkHeader(header.fields);if(n>=rows.size())throw new IllegalArgumentException("The last header has no record values.");Row values=rows.get(n++);if(values.fields.size()<FIXED.size()||values.fields.get(0).equals("Id"))throw new IllegalArgumentException("A repeated header is missing its value row.");Row noise=null;while(n<rows.size()&&rows.get(n).blank())n++;if(n<rows.size()&&!rows.get(n).fields.get(0).equals("Id")){noise=rows.get(n++);checkNoise(noise.fields);}
                Entry entry=entry(header,values,noise,text,now);Entry prior=identities.putIfAbsent(entry.sourceId,entry);if(prior==null)result.add(entry);else if(prior.contentKey.equals(entry.contentKey))repeated++;else throw new IllegalArgumentException("The document repeats an ID with different contents. Resolve that source conflict before importing.");
            }catch(Exception invalid){if(invalid instanceof java.io.InterruptedIOException)throw invalid;throw new IllegalArgumentException("CSV record "+(result.size()+1)+": "+(invalid.getMessage()==null?"unsupported or invalid fields.":invalid.getMessage()),invalid);}}
        if(result.isEmpty())throw new IllegalArgumentException("The document has no complete Sleep as Android records.");return new Document(result,repeated,hash(bytes));
    }
    static Entry one(String csv)throws Exception {Document document=parse(csv.getBytes(StandardCharsets.UTF_8),System.currentTimeMillis(),()->true);if(document.entries.size()!=1||document.repeated!=0)throw new IllegalArgumentException("Source evidence must contain exactly one record.");return document.entries.get(0);}
    private static void checkHeader(List<String> header){if(header.size()<FIXED.size()||!header.subList(0,FIXED.size()).equals(FIXED))throw new IllegalArgumentException("Expected the vendor's Id,Tz,From,To…Geo header, repeated before each record.");for(int n=FIXED.size();n<header.size();n++)if(!header.get(n).equals("Event")&&!CLOCK.matcher(header.get(n)).matches())throw new IllegalArgumentException("Unsupported extra header: "+shortText(header.get(n)));}
    private static void checkNoise(List<String> values){int first=0;while(first<values.size()&&values.get(first).isEmpty())first++;if(first==0)throw new IllegalArgumentException("Expected one optional noise row with empty leading fields.");for(int n=first;n<values.size();n++)if(!values.get(n).isEmpty()){BigDecimal number=decimal(values.get(n),"noise sample");if(number.compareTo(BigDecimal.valueOf(-1))<0||number.compareTo(BigDecimal.valueOf(1e12))>0)throw new IllegalArgumentException("Unsupported noise sample.");}}
    private static Entry entry(Row header,Row values,Row noise,String text,long now)throws Exception {
        List<String> v=values.fields;String id=v.get(0);if(!id.matches("[1-9][0-9]{0,15}"))throw new IllegalArgumentException("Id must be a positive epoch timestamp.");long start=Long.parseLong(id);String zone=v.get(1);if(zone.isBlank()||zone.length()>128)throw new IllegalArgumentException("A source time zone is required; the phone's current zone is never substituted.");ZoneId tz;try{tz=ZoneId.of(zone);}catch(DateTimeException invalid){throw new IllegalArgumentException("Unknown source time zone: "+shortText(zone));}
        BigDecimal hours=decimal(v.get(5),"Hours");if(hours.signum()<=0||hours.compareTo(BigDecimal.valueOf(36))>0)throw new IllegalArgumentException("Hours must be above zero and at most 36.");long duration=hours.multiply(BigDecimal.valueOf(3600000)).setScale(0,RoundingMode.HALF_UP).longValueExact(),end=Math.addExact(start,duration);if(start<=0||end<=start||end>now)throw new IllegalArgumentException("Only valid completed historical periods up to 36 hours are supported.");
        LocalDateTime from=date(v.get(2)),to=date(v.get(3));boolean overlap=resolve(from,tz,start,60000,"From")|resolve(to,tz,end,120000,"To");if(!v.get(4).isEmpty())date(v.get(4));BigDecimal rating=decimal(v.get(6),"Rating");if(rating.signum()<0||rating.compareTo(BigDecimal.valueOf(5))>0||rating.multiply(BigDecimal.valueOf(4)).stripTrailingZeros().scale()>0)throw new IllegalArgumentException("Rating must be 0–5 in quarter-star steps.");
        String note=v.get(7).replace(" \\n ","\n").replace("\r\n","\n").replace('\r','\n');if(note.length()>20000)throw new IllegalArgumentException("Keep comments under 20,000 characters.");bounded(v.get(8),"Framerate",0,1e9);bounded(v.get(9),"Snore",-1,1e9);bounded(v.get(10),"Noise",-1,1e12);BigDecimal cycles=bounded(v.get(11),"Cycles",-1,1000);boolean manual=cycles.compareTo(BigDecimal.valueOf(-1))==0;BigDecimal deep=bounded(v.get(12),"DeepSleep",-2,1);String adjust=v.get(13);Long adjustment;if(adjust.equals("-1.0")&&manual)adjustment=null;else {try{adjustment=decimal(adjust,"LenAdjust").longValueExact();}catch(ArithmeticException invalid){throw new IllegalArgumentException("LenAdjust must contain whole minutes.");}if(adjustment>0||adjustment<-2160||duration+adjustment*60000<0)throw new IllegalArgumentException("LenAdjust must be a supported nonpositive whole-minute adjustment within this period.");}
        int actigraphy=0,events=0,noiseCount=0;for(int n=FIXED.size();n<v.size();n++){String field=v.get(n);if(field.isEmpty())continue;if(EVENT.matcher(field).matches())events++;else{bounded(field,"actigraphy sample",-1,1e12);actigraphy++;}}if(noise!=null)for(String field:noise.fields)if(!field.isEmpty())noiseCount++;
        RecordDraft draft=new RecordDraft(start,end);draft.note=note;if(rating.stripTrailingZeros().scale()<=0&&rating.signum()>0){draft.rating=rating.intValueExact();draft.rated=true;}Matcher tags=Pattern.compile("(?<!\\S)#([^\\s#]+)").matcher(note);while(tags.find())draft.addTag(tags.group(1));draft.validate(start,end,true,now);
        String raw=text.substring(header.from,noise==null?values.to:noise.to);List<String> canonical=new ArrayList<>(v);for(int index:new int[]{2,3,4})canonical.set(index,canonical.get(index).trim().replaceAll("\\s+"," "));String content=hash(new JSONObject().put("header",new JSONArray(header.fields)).put("values",new JSONArray(canonical)).put("noise",noise==null?JSONObject.NULL:new JSONArray(noise.fields)).toString().getBytes(StandardCharsets.UTF_8));return new Entry(id,zone,note,raw,start,end,adjustment,rating,draft,actigraphy,noiseCount,events,overlap,manual,content);
    }
    private static LocalDateTime date(String value){Matcher m=DATE.matcher(value.trim().replaceAll("\\s+"," "));if(!m.matches())throw new IllegalArgumentException("Expected day. month. year hour:minute dates.");try{return LocalDateTime.of(Integer.parseInt(m.group(3)),Integer.parseInt(m.group(2)),Integer.parseInt(m.group(1)),Integer.parseInt(m.group(4)),Integer.parseInt(m.group(5)),m.group(6)==null?0:Integer.parseInt(m.group(6)));}catch(DateTimeException invalid){throw new IllegalArgumentException("Invalid source calendar date.");}}
    private static boolean resolve(LocalDateTime local,ZoneId zone,long expected,long tolerance,String field){List<ZoneOffset> offsets=zone.getRules().getValidOffsets(local);if(offsets.isEmpty())throw new IllegalArgumentException(field+" falls in a nonexistent local DST time.");for(ZoneOffset offset:offsets)if(Math.abs(local.toInstant(offset).toEpochMilli()-expected)<=tolerance)return offsets.size()>1;throw new IllegalArgumentException(field+" disagrees with Id + Hours in the supplied zone. No DST offset is guessed.");}
    private static BigDecimal decimal(String value,String field){if(value.length()>40||!value.matches("-?[0-9]+(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]{1,3})?"))throw new IllegalArgumentException("Invalid numeric "+field+".");try{BigDecimal number=new BigDecimal(value);if(number.scale()>12||number.precision()>24)throw new NumberFormatException();return number;}catch(NumberFormatException invalid){throw new IllegalArgumentException("Unsupported numeric "+field+".");}}
    private static BigDecimal bounded(String value,String field,double min,double max){BigDecimal result=decimal(value,field);if(result.compareTo(BigDecimal.valueOf(min))<0||result.compareTo(BigDecimal.valueOf(max))>0)throw new IllegalArgumentException("Unsupported "+field+" value.");return result;}
    private static String shortText(String text){return text.length()>60?text.substring(0,60)+"…":text;}
    private static List<Row> rows(String text,BooleanSupplier active)throws Exception {List<Row> rows=new ArrayList<>();List<String> fields=new ArrayList<>();StringBuilder field=new StringBuilder();boolean quoted=false,ended=false,started=false;int rowStart=0;
        for(int i=0;i<=text.length();i++){if((i&8191)==0)NightAudioArchive.active(active);boolean eof=i==text.length();char ch=eof?'\n':text.charAt(i);if(quoted){if(eof)throw new IllegalArgumentException("Unclosed quoted CSV field.");if(ch=='"'){if(i+1<text.length()&&text.charAt(i+1)=='"'){field.append('"');i++;}else{quoted=false;ended=true;}}else field.append(ch);}
            else if(ch==','||ch=='\n'||ch=='\r'){fields.add(field.toString());field.setLength(0);started=false;ended=false;if(fields.size()>MAX_COLUMNS)throw new IllegalArgumentException("CSV has too many columns.");if(ch!=','){if(!eof&&ch=='\r'&&i+1<text.length()&&text.charAt(i+1)=='\n')i++;if(!(eof&&rowStart==text.length()))rows.add(new Row(fields,rowStart,eof?i:i+1));fields=new ArrayList<>();rowStart=i+1;if(rows.size()>MAX_RECORDS*3+100)throw new IllegalArgumentException("CSV has too many rows.");}}
            else if(ch=='"'&&!started&&!ended&&field.length()==0){quoted=true;started=true;}else {if(ended||ch=='"')throw new IllegalArgumentException("Unexpected character after a quoted CSV field.");field.append(ch);started=true;}if(field.length()>MAX_FIELD)throw new IllegalArgumentException("A CSV field exceeds 20,000 characters.");}
        return rows;
    }
    private SleepCsvFormat(){}
}
