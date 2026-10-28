package org.sleepwell.app;

import android.app.Activity;
import android.content.Intent;
import java.util.*;

/** Allowlisted local settings destinations. Lookup and route creation never start a feature. */
final class SearchSettings {
    static final String ALARM_SETTING="alarmSetting";
    static final String ALARMS="Alarms",SLEEP="Sleep & bedtime",SOUND="Sound & recordings",APPEARANCE="Appearance & feedback",RELIABILITY="Reliability",CONNECTED="Connected apps",PRIVACY="Data & privacy",HOME="Home screen";
    static final List<String> CATEGORIES=List.of(ALARMS,SLEEP,SOUND,APPEARANCE,RELIABILITY,CONNECTED,PRIVACY,HOME);
    private static final class Destination {
        final String title,category,path,description,terms,page,extra,alarmKey;final Class<?> target;
        Destination(String title,String category,String location,String description,String terms,Class<?> target,String page){this(title,category,location,description,terms,target,page,"settingsPage");}
        Destination(String title,String category,String location,String description,String terms,Class<?> target,String page,String extra){this(title,category,location,description,terms,target,page,extra,"");}
        Destination(String title,String category,String location,String description,String terms,Class<?> target,String page,String extra,String alarmKey){this.title=title;this.category=category;this.path=!alarmKey.isEmpty()?"Alarms › Choose an alarm › "+location:target==HistoryActivity.class&&page.equals("records")?"Report › Choose a night › Edit report":"Settings › "+category+(location.isEmpty()?"":" › "+location);this.description=description;this.terms=terms;this.target=target;this.page=page;this.extra=extra;this.alarmKey=alarmKey;}
    }
    private static Destination direct(String title,String category,String location,String description,String terms,Class<?> target){return new Destination(title,category,location,description,terms,target,"");}
    private static Destination page(String title,String category,String location,String description,String terms,String page){return new Destination(title,category,location,description,terms,MainActivity.class,page);}
    private static Destination detail(String title,String category,String location,String description,String terms,Class<?> target,String extra,String page){return new Destination(title,category,location,description,terms,target,page,extra);}
    private static Destination alarm(String title,String location,String terms,String key){return new Destination(title,ALARMS,location,"This alarm only · choose a saved alarm or start a new draft",terms,AlarmEditorActivity.class,"","",key);}
    // Keep the first 30 original search destinations in their original order; entries are not saved IDs.
    private static final List<Destination> DESTINATIONS=List.of(
        direct("Sleep plan",SLEEP,"Sleep goals","Bedtime, wake time, sleep goal and reminders","goal hours duration bedtime reminder schedule",PlanActivity.class),
        direct("Auto-detect sleep",SLEEP,"Automatic tracking","Review automatic phone tracking and its start conditions","automatic tracking autodetect autodetectsleep auto detect sleep sleeping asleep stillness charging screen off smart window",AutomaticTrackingActivity.class),
        direct("Nightly tracking schedule",SLEEP,"Sleep tracking","Choose recurring tracking days and times","automatic start bedtime weekly recurring charging",RecurringTrackingActivity.class),
        direct("Motion sensor",SLEEP,"Sleep tracking","Phone movement sensitivity and calibration","accelerometer movement sensor mattress sensitivity",MotionSensorActivity.class),
        direct("Sound sensitivity",SOUND,"Sound analysis","Accepted categories and minimum event duration","sleep noise analysis snoring talking speech sounds microphone recording sensitivity",SoundSensitivityActivity.class),
        direct("Anti-snoring cues",SOUND,"Snoring cues","Optional sound or vibration after a snoring estimate","anti snoring antisnoring antisnore stop snore vibration contactless headphones",SnoringCuesActivity.class),
        direct("Bedtime guard",SLEEP,"Wind-down","Choose distracting apps to block at bedtime","scrolling prevent leave app block apps youtube instagram accessibility wind down",GuardActivity.class),
        direct("Jet lag & travel mode",SLEEP,"Travel mode","Time zones, trip plans and daylight reminders","jet lag prevention jetlag travel timezone sun light circadian",TravelActivity.class),
        direct("Health Connect",CONNECTED,"Health Connect","Review sleep record import and export","android health sync integration permissions",HealthConnectActivity.class),
        direct("Automations",CONNECTED,"Automations","Outgoing Tasker, webhook and MQTT rules","tasker intents event trigger http mqtt webhook tracking alarm snooze",AutomationActivity.class),
        direct("Calendar rules",ALARMS,"Calendar rules","Skip dates or wake before selected meetings","calendar holiday holidays skip exclude meetings event workdays",CalendarRulesActivity.class),
        direct("Bedroom light",CONNECTED,"Home Assistant","Configure optional Home Assistant lamp sunrise","smart light home assistant sunrise brightness lamp restore",SmartLightActivity.class),
        direct("Cloud backup",PRIVACY,"Backup","User-selected storage folder and backup versions","cloud backup restore drive storage automatic retention",CloudBackupActivity.class),
        direct("Appearance",APPEARANCE,"Appearance","Theme, text size, contrast and reduced motion","dark light changing midnight dawn theme font text accessibility reduce motion contrast",AppearanceActivity.class),
        direct("Tags and quantities",PRIVACY,"Sleep records","Manage local tags and quantity units","tags caffeine coffee alcohol exercise quantity mg units",TagManagementActivity.class),
        direct("Export sound archive",PRIVACY,"Backup","Review a metadata and WAV audio library export","audio backup recordings wav zip export archive",LibraryAudioExportActivity.class),
        direct("Dream diary",SLEEP,"Dream diary","Private dream notes and remembered dreams","journal dreams notes lucid diary",DiaryActivity.class),
        direct("Smart Bedtime reminders",SLEEP,"Wind-down","Review behavior-based bedtime reminders","smart bedtime reminder notification wind down automatic",SmartBedtimeActivity.class),
        direct("Awake estimates",SLEEP,"Sleep tracking","Optional phone-use, light and snooze clues","awake detection estimate phone screen use sensor lux",AwakeSettingsActivity.class),
        direct("Breathing experiment",SLEEP,"Sleep tracking","Optional experimental contactless breathing estimates","breathing respiration sonar echo contactless experimental",EchoSettingsActivity.class),
        direct("Self-reported sleep timing",SLEEP,"Sleep rhythm","Your own habitual bedtime and wake time","chronotype timing habitual early late rhythm",HabitualTimingActivity.class),
        direct("Sleep timing groups",SLEEP,"Sleep rhythm","Workday and free-day comparison settings","social jetlag jet lag workdays weekends free days rhythm",RhythmGroupsActivity.class),
        direct("Tracking start delay",SLEEP,"Sleep tracking","Configure a reviewed delayed manual start","delay start tracking countdown timer",TrackingDelayActivity.class),
        direct("Flip to pause",SLEEP,"Sleep tracking","Face-down tracking pause controls","flip orientation face down pause sensor",TrackingFlipActivity.class),
        direct("Tracking Do Not Disturb",SLEEP,"Sleep tracking","Optional DND policy while recording","do not disturb dnd quiet notification permission",TrackingDndActivity.class),
        direct("Restore recording archive",PRIVACY,"Backup","Review a ZIP before replacing local data","import restore audio recordings backup wav zip archive",LibraryAudioImportActivity.class),
        direct("Alarm protection",ALARMS,"Alarm protection","All alarms · app-leaving and power-menu deterrence options","prevent prevention leaving preventleave leave exit app power off poweroff shutdown button protection accessibility",AlarmProtectionActivity.class),
        direct("Paired movement tracking",CONNECTED,"Paired phone","Configure an optional second Android phone","pair paired bluetooth ble second phone movement sensor",PairMotionActivity.class),
        direct("Research signal export",PRIVACY,"Backup","Review relative movement signal exports","research raw signal data csv export movement",ResearchExportActivity.class),
        direct("Binaural tones",SOUND,"Sleep sounds","Configure optional headphone tones","binaural headphones frequency beat tones audio",BinauralSettingsActivity.class),
        page("Alarms",ALARMS,"","Choose a saved alarm to edit its settings","alarm time clock set edit add enable disable", "alarms"),
        alarm("Extra loud","Alarm sound › More","extra loud extraloud louder maximum alarm sound volume boost ringtone tone","extra-loud"),
        alarm("Gentle volume increase","Alarm sound › Gentle wake-up","gentle increase gentleincrease gradual gradually volume increase ramp fade sound delay crescendo","gentle-increase"),
        alarm("Screen sunrise","Alarm sound › Gentle wake-up › Screen sunrise","screen sunrise wake up wakeup brightness light dawn brighten","screen-sunrise"),
        alarm("Challenge tasks","Challenges","tasks captcha math memory qr barcode nfc shake walk smile pose picture object musical note jumping type light spin","challenges"),
        alarm("Snooze, backup & wake-up check","Snooze","snooze duration limit total cap minutes decreasing backup escalate wake check still awake","snooze"),
        alarm("Alarm repeat & next date","Schedule & dates","odd even week every interval days repeat sunrise solar coordinates next date skip exclude","schedule"),
        direct("New alarm defaults",ALARMS,"Alarm defaults","Future alarms only · existing saved alarms stay unchanged","default template future new alarms factory reset behavior",AlarmDefaultsActivity.class),
        page("Bedside NFC tag",ALARMS,"Alarm options","Open alarm options; enrollment needs your explicit action","nfc tag enroll bedside bathroom challenge scan", "alarm_options"),
        direct("Uninstall protection",RELIABILITY,"Alarm protection","Review Android device-admin protection","uninstall delete app administrator device admin protection",UninstallProtectionActivity.class),
        page("Alarm readiness",RELIABILITY,"","Battery, exact-alarm and notification permission status","reliability battery optimization unrestricted permissions exact alarm notification full screen microphone", "reliability"),
        direct("Tracking setup",SLEEP,"Sleep tracking","Review sensor and recording options before Start","tracking setup movement microphone record sleep start pause stop",TrackingActivity.class),
        direct("Contactless setup",SLEEP,"Sleep tracking","Sonar placement, capability and explicit signal test","contactless sonar ultrasound speaker microphone gain calibration distance",ContactlessSetupActivity.class),
        direct("Motion calibration",SLEEP,"Sleep tracking › Motion sensor","Review sensitivity and explicitly start calibration","motion sensor accelerometer calibrate sensitivity mattress",MotionCalibrationActivity.class),
        direct("External PIR input",CONNECTED,"Home Assistant","Configure an optional room movement sensor","pir passive infrared room sensor motion home assistant external",PirSettingsActivity.class),
        page("Sound analysis",SOUND,"","Review saved analysis and recording choices","sleep noise analysis sleepnoiseanalysis snoring speech talking recording clips microphone classify sound events", "sound_analysis"),
        direct("Snoring sound output",SOUND,"Sound analysis › Snoring cues","Choose headphone-only sound routing","anti snore snoring headphones speaker wired usb ble sound route output",SnoreSoundRouteActivity.class),
        direct("Recording retention",PRIVACY,"Audio retention","Retention days, favorites and reviewed cleanup","delete old recordings retention storage keep favorites starred sounds space audio cleanup",SoundLibrarySettingsActivity.class),
        direct("Favorite sounds",SOUND,"Recordings","Open saved favorite recordings","favorites favourites starred snoring talking recordings clips",FavoriteSoundsActivity.class),
        direct("Cloud audio archive",PRIVACY,"Backup › Cloud backup","Review saved audio archives in your chosen folder","cloud audio archive recording backup export restore",CloudAudioActivity.class),
        direct("Laughter & instruments",SOUND,"Recorded audio","Explicit recorded-audio analysis; uncertain experimental estimates","laughter laugh instrument piano guitar recorded audio classify analysis",RecordedRecognitionActivity.class),
        direct("Sleep sounds",SOUND,"Sleep sounds","Review lullabies, mixer and timers; Play is explicit","lullabies lullaby rain ocean wind brown noise white noise mixer timer fade audio radio stations internet stream",SleepSoundsActivity.class),
        direct("Fade external music",SOUND,"Sleep sounds","Opt-in device media-volume fade for eligible tracking","spotify external music audio player fade stillness media volume",ExternalMediaFadeActivity.class),
        page("Wind-down",SLEEP,"Wind-down","Open the bedtime screen without starting tracking","wind down winddown bedtime guard routine relax", "wind_down"),
        direct("Guided wind-down",SLEEP,"Wind-down","Silent timed practice and optional explicit narration","guided breathing relaxation narration speech rate pitch tts offline bedtime",GuidedWindDownActivity.class),
        direct("Offline narration voice",SLEEP,"Wind-down › Guided practice","Choose an installed offline English voice","voice narration text to speech tts engine offline language pitch rate",NarrationVoiceActivity.class),
        direct("Dream cues",SLEEP,"Dream cues","Review optional timed cues during tracking","dream lucid dreaming cue sound vibration timed",DreamCueActivity.class),
        direct("Sleep rhythm",SLEEP,"Sleep rhythm","Review bedtime consistency and workday settings","rhythm bedtime consistency social jetlag charts",SleepRhythmActivity.class),
        direct("Goal bank",SLEEP,"Sleep goals","Review saved goal commitments and day boundaries","goal bank goal debt cutoff day boundary duration bedtime",GoalBankActivity.class),
        direct("Trends & chart settings",SLEEP,"Reports › Trend options","Open Trends, then use Trend options","graphs charts report trends week month year cutoff settings statistics insights",HistoryActivity.class),
        direct("Nap",SLEEP,"Naps","Review a timer or minimum-rest nap before starting","nap timer duration power nap minimum rest smart wake",NapActivity.class),
        direct("Nap profiles",SLEEP,"Naps","Review saved one-time or weekly nap profiles","polyphasic nap naps multiple schedule profiles weekly recurring",NapProfilesActivity.class),
        direct("Philips Hue bridge",CONNECTED,"Smart lights","Configure a user-owned LAN bridge","philips hue bridge lamp sunrise smart light pair lan certificate",HueBridgeActivity.class),
        direct("Incoming automation requests",CONNECTED,"Automations","Review opt-in external start requests","tasker incoming intent start tracking automation external app",IncomingAutomationSettingsActivity.class),
        direct("Automatic calendar export",CONNECTED,"Calendar","Opt-in saved-night calendar export","calendar events automatic export sync saved record night",AutoCalendarActivity.class),
        direct("Record-sharing access",CONNECTED,"Connected apps","Allow or revoke read-only record-summary access","content provider api share permissions trusted apps read sleep records",RecordAccessActivity.class),
        direct("Alarm-management access",CONNECTED,"Connected apps","Allow or revoke trusted app alarm management","content provider api crud alarm permissions automation trusted apps",AlarmAccessActivity.class),
        direct("Backup & restore",PRIVACY,"Backup","Choose a reviewed export or import operation","backup restore import export json csv saved data phone transfer",BackupActivity.class),
        direct("Portable settings",PRIVACY,"Backup","Export or review supported preference categories","settings profile preferences backup restore theme home layout sleep plan narration",SettingsProfileActivity.class),
        direct("Import sleep CSV",PRIVACY,"Backup","Review a sleep-data CSV without automatic replacement","sleep as android csv import transfer records",SleepCsvActivity.class),
        direct("Import dream diary",PRIVACY,"Dream diary","Review diary entries before merging","diary dream journal import json notes merge",DiaryImportActivity.class),
        page("Data & privacy",PRIVACY,"","Local storage, optional sharing and reviewed deletion","privacy data delete erase history recordings account cloud local storage", "privacy"),
        direct("Tonight layout",HOME,"Tonight layout","Choose home rows, their order and illustration","home tonight layout hide show order customize customise illustration art",TonightLayoutActivity.class),
        direct("Widget & launcher shortcuts",HOME,"Widgets","Review home widgets and app shortcuts","widget next alarm snooze home screen launcher shortcut add pin",HomeWidgetSettingsActivity.class),
        direct("Tap feedback",APPEARANCE,"Touch feedback","Choose optional feedback for app controls","vibration haptic tap touch feedback button clicks",FeedbackSettingsActivity.class),
        detail("Automatic start conditions",SLEEP,"Automatic tracking › Start conditions","Review charging and screen-off waits for tonight","charging charger screen off unplugged wait automatic armed",AutomaticTrackingActivity.class,"trackingOptionsPage","conditions"),
        detail("Automatic sound recording",SLEEP,"Automatic tracking › Sound recording","Review analysis and clip choices for tonight's armed start","automatic microphone sound record clips keep analysis",AutomaticTrackingActivity.class,"trackingOptionsPage","sounds"),
        detail("Sleep confirmation",SLEEP,"Automatic tracking › Sleep confirmation","Review Google estimate and awake-start rejection","google sleep detect asleep stillness false start rejection confirmation physical activity",AutomaticTrackingActivity.class,"trackingOptionsPage","detection"),
        detail("Nightly start conditions",SLEEP,"Repeat nightly › Start conditions","Review whether an unplugged night is skipped","nightly recurring charging charger unplugged skip conditions",RecurringTrackingActivity.class,"trackingOptionsPage","conditions"),
        detail("Nightly start reliability",SLEEP,"Repeat nightly › Start reliability","Review timing permissions and sensor availability","nightly recurring exact permission notifications movement sensor reliability",RecurringTrackingActivity.class,"trackingOptionsPage","reliability"),
        detail("Detected sound categories",SOUND,"Sound sensitivity › Detected categories","Choose categories in a draft; Save applies next night","sound categories snoring speech cough baby crying laughter other include exclude",SoundSensitivityActivity.class,"soundSettingsPage","categories"),
        detail("Sound event duration",SOUND,"Sound sensitivity › Event duration","Review minimum accepted durations in a draft","sound minimum duration sensitivity accepted windows seconds high low",SoundSensitivityActivity.class,"soundSettingsPage","durations"),
        detail("Contactless snoring output",SOUND,"Snoring cues › Contactless & output","Review contactless opt-ins and sound routing","contactless sonar antisnore snoring sound vibration output headphones",SnoringCuesActivity.class,"soundSettingsPage","options"),
        detail("Sleep sound sources",SOUND,"Sleep sounds › Sources & playlists","Open local audio, playlist, ambient rotation and radio options","radio station internet stream local audio import playlist ambient rotation sources library",SleepSoundsActivity.class,"sleepSoundPage","sources"),
        detail("Sleep sound playback",SOUND,"Sleep sounds › Playback & tracking","Review tracking playback and stillness fade","sleep sound autoplay with tracking stillness fade brown noise playback",SleepSoundsActivity.class,"sleepSoundPage","playback"),
        detail("Sleep sound timer",SOUND,"Sleep sounds › Timer","Select a timer draft; Use applies without starting sound","lullaby sleep sound timer stop minutes duration countdown",SleepSoundsActivity.class,"sleepSoundPage","timer"),
        detail("Sleep sound fade",SOUND,"Sleep sounds › Fade out","Select a fade draft; Use applies without starting sound","lullaby sleep sound fade out taper volume timer minutes",SleepSoundsActivity.class,"sleepSoundPage","fade"),
        page("Clip recording threshold",SOUND,"Sound analysis › Clip recording threshold","Open Sound analysis to adjust the clip amplitude threshold","clip recording threshold relative amplitude quiet loud minimum noise capture", "sound_analysis"),
        direct("Room conditions",SLEEP,"Room conditions","Live light, optional sound level and screen dimming","room conditions ambient light lux brightness sound level rms microphone screen dimming dim wind down bedtime",RoomConditionsActivity.class),
        alarm("Flashlight wake-up","Alarm sound › Gentle wake-up › Flashlight wake-up","flashlight torch flashing flash wake up wakeup light","flashlight"),
        alarm("Alarm sound","Alarm sound","ringtone tone playlist radio import music audio volume sound","sound"),
        detail("Report editing",SLEEP,"Reports","Choose a saved night, then Edit report; changes stay in a draft until Save","edit editing report reportediting sleep period awake annotations bedtime wake up tags notes manual history",HistoryActivity.class,"historyPage","records")
    );
    static List<SearchIndex.Entry> entries(){List<SearchIndex.Entry> rows=new ArrayList<>();for(int n=0;n<DESTINATIONS.size();n++){Destination d=DESTINATIONS.get(n);rows.add(new SearchIndex.Entry(SearchIndex.Kind.SETTING,n,d.title,d.path+"\n"+d.description,d.terms,d.category));}return List.copyOf(rows);}
    static Intent intent(Activity owner,long id){if(id<0||id>=DESTINATIONS.size())throw new IllegalArgumentException("This setting is no longer available. Search again.");Destination d=DESTINATIONS.get((int)id);Intent intent=RecordUi.intent(owner,d.target);if(!d.alarmKey.isEmpty())throw new IllegalArgumentException("Choose an alarm before opening this setting.");if(d.target==HistoryActivity.class){intent.putExtra("trends",!d.page.equals("records"));return intent;}if(!d.page.isEmpty())intent.putExtra(d.extra,d.page);return intent;}
    static String alarmKey(long id){return id>=0&&id<DESTINATIONS.size()?DESTINATIONS.get((int)id).alarmKey:"";}
    static SearchIndex.Entry entry(long id){if(id<0||id>=DESTINATIONS.size())throw new IllegalArgumentException("Search again for this setting.");return entries().get((int)id);}
    static boolean category(String value){return value!=null&&(value.isEmpty()||CATEGORIES.contains(value));}
    private SearchSettings(){}
}
