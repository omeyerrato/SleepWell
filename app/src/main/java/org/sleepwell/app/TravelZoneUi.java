package org.sleepwell.app;

import java.text.Collator;
import java.text.Normalizer;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Human-readable zone labels. The exact Java ZoneId remains the selected value. */
final class TravelZoneUi {
    static final class Entry {
        final String id, city, country, search;

        Entry(String id, String city, String country, String search) {
            this.id = id;
            this.city = city;
            this.country = country;
            this.search = search;
        }

        String place() { return country.isEmpty() ? id : country; }
    }

    static final class Catalog {
        private final List<Entry> entries = new ArrayList<>();
        private final Map<String, Entry> byId = new HashMap<>();
        private final Map<String, Integer> labels = new HashMap<>();

        Catalog(Locale locale) {
            Map<String, String> localCountries = new HashMap<>();
            Map<String, String> englishCountries = new HashMap<>();
            for (String id : ZoneId.getAvailableZoneIds()) {
                String region = region(id), local = "", english = "";
                if (!region.isEmpty()) {
                    local = localCountries.computeIfAbsent(region, value -> country(value, locale));
                    english = englishCountries.computeIfAbsent(region, value -> country(value, Locale.ENGLISH));
                }
                String city = TravelPlan.city(id);
                Entry entry = new Entry(id, city, local, normalize(city + " " + id + " " + local + " " + english + " " + region));
                entries.add(entry);
                byId.put(id, entry);
                labels.merge(entry.city + "\n" + entry.country, 1, Integer::sum);
            }
            Collator order = Collator.getInstance(locale);
            order.setStrength(Collator.PRIMARY);
            entries.sort((left, right) -> {
                int city = order.compare(left.city, right.city);
                if (city != 0) return city;
                int country = order.compare(left.country, right.country);
                return country == 0 ? left.id.compareTo(right.id) : country;
            });
        }

        Entry get(String id) {
            Entry entry = byId.get(id);
            return entry == null ? new Entry(id, TravelPlan.city(id), "", normalize(id)) : entry;
        }

        boolean needsId(Entry entry) { return !entry.country.isEmpty() && labels.getOrDefault(entry.city + "\n" + entry.country, 0) > 1; }

        List<Entry> matches(String query, String selected) {
            String search = normalize(query);
            List<Entry> result = new ArrayList<>();
            Entry current = byId.get(selected);
            if (search.isEmpty() && current != null) result.add(current);
            String[] terms = search.isEmpty() ? new String[0] : search.split(" +");
            for (Entry entry : entries) {
                if (search.isEmpty() && entry == current) continue;
                boolean match = true;
                for (String term : terms) if (!entry.search.contains(term)) { match = false; break; }
                if (match) result.add(entry);
            }
            return result;
        }
    }

    // Android ICU supplies ISO country metadata; non-location zones (001),
    // unsupported aliases or unavailable locale data fall back to their exact ID.
    // https://developer.android.com/reference/android/icu/util/TimeZone#getRegion(java.lang.String)
    private static String region(String id) {
        try {
            String value = android.icu.util.TimeZone.getRegion(id);
            return value != null && value.length() == 2 ? value : "";
        } catch (RuntimeException unavailable) { return ""; }
    }

    private static String country(String region, Locale locale) {
        try { return new Locale.Builder().setRegion(region).build().getDisplayCountry(locale); }
        catch (RuntimeException unavailable) { return ""; }
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT).replaceAll("[_\\s/]+", " ").trim();
    }

    private TravelZoneUi() {}
}
