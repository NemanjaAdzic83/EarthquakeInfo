package com.example.earthquakeinfo;

import android.content.Context;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    Splits a USGS location like "73km WSW of Rumoi, Japan" into two parts
    and translates the first part into the phone's language:
    offset:     "73 km WSW of" (Serbian: "73 km ЗЈЗ од")
    primaryLocation: "Rumoi, Japan"
    The place name itself comes from the USGS server in English and is not traslated
 */
public class LocationFormatter {

    private static final Pattern OFFEST_PATTERN = Pattern.compile(
            "^(\\d+(?:\\.\\d+)?)\\s*km\\s+([NSEW]{1,3})\\s+of\\s+(.+)$",
            Pattern.CASE_INSENSITIVE);

    // Compass letters as USGS sends them, in this exact order
    private static final String ENGLISH_COMPASS = "NSEW";

    private static final String SEPARATOR = " of ";

    private final String compassLetters;
    private final String offsetFormat;
    private final String nearThe;

    public LocationFormatter(String compassLetters, String offsetFormat, String nearThe) {
        this.compassLetters = compassLetters;
        this.offsetFormat = offsetFormat;
        this.nearThe = nearThe;
    }

    public static LocationFormatter from(Context context){
        return new LocationFormatter(
                context.getString(R.string.compass_letters),
                context.getString(R.string.location_offset_format),
                context.getString(R.string.near_the));
    }

    public Parts format(String location){
        Matcher matcher = OFFEST_PATTERN.matcher(location);
        if (matcher.matches()){
            String distance = matcher.group(1);
            String direction = translateDirection(matcher.group(2));
            String place = matcher.group(3);
            return new Parts(String.format(offsetFormat,distance,direction),place);
        }
        int separatorIndex = location.indexOf(SEPARATOR);
        if (separatorIndex >= 0){
            int splitIndex = separatorIndex + SEPARATOR.length();
            return new Parts(location.substring(0,splitIndex).trim(), location.substring(splitIndex));
        }
        return new Parts(nearThe,location);
    }

    private String translateDirection(String direction) {
        if (compassLetters.length() != ENGLISH_COMPASS.length()){
            return direction;
        }
        StringBuilder translated = new StringBuilder();
        for (char letter : direction.toUpperCase(Locale.ROOT).toCharArray()){
            int index = ENGLISH_COMPASS.indexOf(letter);
            translated.append(index >= 0 ? compassLetters.charAt(index) : letter);
        }
        return translated.toString();
    }



    public static final class Parts{
        private final String offset;
        private final String primaryLocation;

        Parts(String offset, String primaryLocation){
            this.offset = offset;
            this.primaryLocation = primaryLocation;
        }

        public String getOffset() {
            return offset;
        }

        public String getPrimaryLocation() {
            return primaryLocation;
        }
    }


















}
