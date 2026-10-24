package org.sleepwell.app;

import android.content.SharedPreferences;
import java.util.*;

/** A recreated task cannot silently replace a previously presented board. */
final class MoonSearchState {
    static MoonSearch open(SharedPreferences prefs,String occurrence,int unit,int round,Random random){
        if(occurrence==null||occurrence.isEmpty()||occurrence.length()>128||unit<0||round<0||round>5)throw new IllegalArgumentException("Invalid search owner");String owner=occurrence+":"+unit;
        if(owner.equals(prefs.getString("moonSearchOwner",""))){MoonSearch saved=MoonSearch.decode(prefs.getString("moonSearchBoard",""));if(saved.columns!=3+round/2)throw new IllegalArgumentException("Search round changed");return saved;}
        MoonSearch created=MoonSearch.create(random,round);if(!prefs.edit().putString("moonSearchOwner",owner).putString("moonSearchBoard",created.encode()).commit())throw new IllegalStateException("Search task could not be saved");return created;
    }
    private MoonSearchState(){}
}
