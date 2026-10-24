package org.sleepwell.app;
import java.math.BigDecimal;import java.util.*;
/** User-entered quantity, never inferred from a tag or note. */
final class TagQuantity {
 final double value;final String unit;
 TagQuantity(double value,String unit){String clean=unit==null?"":unit.trim().replaceAll("\\s+"," ");if(!Double.isFinite(value)||value<0||value>1000000||clean.length()>24||clean.codePoints().anyMatch(Character::isISOControl))throw new IllegalArgumentException("Use a quantity from 0 to 1,000,000 and a unit up to 24 characters.");this.value=value;this.unit=clean;}
 static double parse(String raw){StringBuilder normalized=new StringBuilder();String text=raw.trim();boolean dot=false;for(int cp:text.codePoints().toArray()){if(cp=='.'||cp==0x066b){if(dot)throw new IllegalArgumentException("Invalid decimal");dot=true;normalized.append('.');}else if(Character.getType(cp)==Character.DECIMAL_DIGIT_NUMBER){int digit=Character.digit(cp,10);if(digit<0)throw new IllegalArgumentException("Invalid quantity");normalized.append(digit);}else throw new IllegalArgumentException("Use a decimal number without grouping or exponent.");}if(normalized.length()==0)throw new IllegalArgumentException("Enter a quantity");return Double.parseDouble(normalized.toString());}
 String display(){return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()+(unit.isEmpty()?"":" "+unit);}
 @Override public boolean equals(Object other){return other instanceof TagQuantity&&Double.compare(value,((TagQuantity)other).value)==0&&unit.equals(((TagQuantity)other).unit);}@Override public int hashCode(){return Objects.hash(value,unit);}
}
