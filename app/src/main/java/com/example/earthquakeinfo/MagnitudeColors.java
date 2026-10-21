package com.example.earthquakeinfo;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

/*
    Color of the magnitude circle (1 = blue ... 10+ = dark red).
    Used by the list (EarthquakeAdapter) and by the details panel (EarthquakeDetailsBottomSheet),
    so the same rule is written only once.
 */
public final class MagnitudeColors {

    private MagnitudeColors() {
        // only static methods - no objects of this class
    }

    @ColorInt
    public static int get(Context context, double magnitude) {
        int colorResId;
        switch ((int) Math.floor(magnitude)) {
            case 0:
            case 1:
                colorResId = R.color.magnitude1;
                break;
            case 2:
                colorResId = R.color.magnitude2;
                break;
            case 3:
                colorResId = R.color.magnitude3;
                break;
            case 4:
                colorResId = R.color.magnitude4;
                break;
            case 5:
                colorResId = R.color.magnitude5;
                break;
            case 6:
                colorResId = R.color.magnitude6;
                break;
            case 7:
                colorResId = R.color.magnitude7;
                break;
            case 8:
                colorResId = R.color.magnitude8;
                break;
            case 9:
                colorResId = R.color.magnitude9;
                break;
            default:
                colorResId = R.color.magnitude10plus;
                break;
        }
        // Converts the color resource ID into an actual color value
        return ContextCompat.getColor(context, colorResId);
    }

    /*
        Text colors for the number inside the circle
        Yellow and orange circles(magnitude 4 and 5) are light so white text would  be hard to read
     */
    public static int getTextColor(Context context, double magnitude){
        int floor = (int) Math.floor(magnitude);
        boolean lightCircle = floor == 4 || floor == 5;
        return ContextCompat.getColor(context,lightCircle ? R.color.magnitude_text_dark : android.R.color.white);
    }
}