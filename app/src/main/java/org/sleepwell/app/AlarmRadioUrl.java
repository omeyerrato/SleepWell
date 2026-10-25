package org.sleepwell.app;
import java.net.URI;import java.util.Locale;
/** Direct public streams only. Authentication, query-token links and web-player pages are unsupported. */
final class AlarmRadioUrl {
 static final long TIMEOUT=8000;
 static String parse(String input){if(input==null)throw new IllegalArgumentException("Enter a direct HTTPS stream URL.");String value=input.strip();try{URI uri=URI.create(value);String host=uri.getHost();if(value.length()>2048||!value.equals(input)||!"https".equals(uri.getScheme())||host==null||host.isBlank()||uri.getRawUserInfo()!=null||uri.getRawQuery()!=null||uri.getRawFragment()!=null||uri.getPort()==0||uri.getPort()>65535||value.chars().anyMatch(ch->Character.isWhitespace(ch)||Character.isISOControl(ch)))throw new IllegalArgumentException();String name=host.toLowerCase(Locale.ROOT),path=(uri.getPath()==null?"":uri.getPath()).toLowerCase(Locale.ROOT);if(name.equals("spotify.com")||name.endsWith(".spotify.com")||path.endsWith(".html")||path.endsWith(".htm"))throw new IllegalArgumentException();return value;}catch(IllegalArgumentException invalid){throw new IllegalArgumentException("Use a direct public HTTPS audio URL without sign-in, query tokens or a web-player page.");}}
 static boolean source(String tone){if(tone==null||!tone.startsWith("radio:"))return false;try{parse(tone.substring(6));return true;}catch(IllegalArgumentException invalid){return false;}}
 static String host(String url){try{return URI.create(url).getHost();}catch(IllegalArgumentException invalid){return "Radio stream";}}
}
