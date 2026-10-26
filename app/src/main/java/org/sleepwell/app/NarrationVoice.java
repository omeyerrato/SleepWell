package org.sleepwell.app;
import android.content.*;import android.speech.tts.*;import java.util.*;
/** Device-local identity only. Existing rate/pitch and portable profiles do not contain these keys. */
final class NarrationVoice {
 static final NarrationVoice AUTO=new NarrationVoice("","","");final String engine,name,locale;
 NarrationVoice(String engine,String name,String locale){if(engine==null||name==null||locale==null||engine.length()>255||name.length()>512||locale.length()>80||(!engine.isEmpty()&&(!engine.matches("[A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)+")||name.isBlank()||!"en".equals(Locale.forLanguageTag(locale).getLanguage())))||engine.isEmpty()&&(!name.isEmpty()||!locale.isEmpty()))throw new IllegalArgumentException("Choose an installed offline English narration voice.");this.engine=engine;this.name=name;this.locale=locale;}
 boolean automatic(){return engine.isEmpty();}String label(){return automatic()?"Auto · offline English":name+" · "+locale;}
 static NarrationVoice load(Context context){SharedPreferences p=context.getSharedPreferences("narration_voice_v1",Context.MODE_PRIVATE);if(!p.contains("engine")&&!p.contains("name")&&!p.contains("locale"))return AUTO;return new NarrationVoice(p.getString("engine",""),p.getString("name",""),p.getString("locale",""));}
 void save(Context context){SharedPreferences.Editor edit=context.getSharedPreferences("narration_voice_v1",Context.MODE_PRIVATE).edit();if(automatic())edit.remove("engine").remove("name").remove("locale");else edit.putString("engine",engine).putString("name",name).putString("locale",locale);if(!edit.commit())throw new IllegalStateException("Voice selection could not be saved.");}
 static boolean offline(Voice voice){return voice!=null&&voice.getLocale()!=null&&"en".equals(voice.getLocale().getLanguage())&&!voice.isNetworkConnectionRequired()&&(voice.getFeatures()==null||!voice.getFeatures().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED));}
 boolean matches(Voice voice){return offline(voice)&&name.equals(voice.getName())&&locale.equals(voice.getLocale().toLanguageTag());}
 static NarrationVoice of(String engine,Voice voice){if(!offline(voice))throw new IllegalArgumentException("Voice is unavailable or requires network synthesis.");return new NarrationVoice(engine,voice.getName(),voice.getLocale().toLanguageTag());}
}
