package org.sleepwell.app;

import java.time.*;

/** Original implementation of published NOAA fractional-year solar equations; sea-level apparent horizon. */
final class SunriseMath {
    static Long at(LocalDate localDate,double latitude,double longitude,ZoneId zone){
        if(!Double.isFinite(latitude)||!Double.isFinite(longitude)||latitude < -90||latitude>90||longitude < -180||longitude>180)throw new IllegalArgumentException("Invalid coordinates");if(localDate.getYear()<2000||localDate.getYear()>2100)return null;
        Long selected=null;for(int shift=-1;shift<=1;shift++){LocalDate utcDate=localDate.plusDays(shift);double minute=720-4*longitude;boolean available=true;for(int n=0;n<5;n++){Instant sample=utcDate.atStartOfDay(ZoneOffset.UTC).toInstant().plusMillis(Math.round(minute*60000));ZonedDateTime utc=sample.atZone(ZoneOffset.UTC);double gamma=2*Math.PI/utc.toLocalDate().lengthOfYear()*(utc.getDayOfYear()-1+(utc.getHour()+utc.getMinute()/60d-12)/24d);double equation=229.18*(.000075+.001868*Math.cos(gamma)-.032077*Math.sin(gamma)-.014615*Math.cos(2*gamma)-.040849*Math.sin(2*gamma));double declination=.006918-.399912*Math.cos(gamma)+.070257*Math.sin(gamma)-.006758*Math.cos(2*gamma)+.000907*Math.sin(2*gamma)-.002697*Math.cos(3*gamma)+.00148*Math.sin(3*gamma);double lat=Math.toRadians(latitude),denominator=Math.cos(lat)*Math.cos(declination);if(Math.abs(denominator)<1e-12){available=false;break;}double cosine=Math.cos(Math.toRadians(90.833))/denominator-Math.tan(lat)*Math.tan(declination);if(!Double.isFinite(cosine)||cosine < -1||cosine>1){available=false;break;}minute=720-4*(longitude+Math.toDegrees(Math.acos(cosine)))-equation;}if(!available||!Double.isFinite(minute))continue;long epoch=utcDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()+Math.round(minute*60000);long rounded=Math.round(epoch/60000d)*60000;if(Instant.ofEpochMilli(rounded).atZone(zone).toLocalDate().equals(localDate)&&(selected==null||rounded<selected))selected=rounded;}
        return selected;
    }
    private SunriseMath(){}
}
