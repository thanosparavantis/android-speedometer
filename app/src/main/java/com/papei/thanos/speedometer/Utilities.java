package com.papei.thanos.speedometer;

import android.content.Context;
import android.content.Intent;

import java.text.DateFormat;
import java.util.Locale;

/**
 * A class that holds static utility methods.
 */
public final class Utilities {

    /**
     * This class should not be instantiated.
     */
    private Utilities() {
        //
    }

    /**
     * Receives menu clicks and starts the appropriate activity.
     *
     * @param context The current context.
     * @param id The menu item ID.
     * @return Whether a new activity was started.
     */
    public static boolean handleNavigation(Context context, int id) {
        switch (id) {
            case R.id.home:
                context.startActivity(new Intent(context, MainActivity.class));
                return true;
            case R.id.records:
                context.startActivity(new Intent(context, RecordsActivity.class));
                return true;
            case R.id.map:
                context.startActivity(new Intent(context, MapActivity.class));
                return true;
            case R.id.preferences:
                context.startActivity(new Intent(context, PreferencesActivity.class));
                return true;
            default:
                return false;
        }
    }

    /**
     * Receives text from speech and executes the appropriate action.
     *
     * @param context The current context.
     * @param spokenText The spoken text.
     * @return Whether an action was taken.
     */
    public static boolean handleTextFromSpeech(Context context, String spokenText) {
        if (spokenText.contains("home")) {
            context.startActivity(new Intent(context, MainActivity.class));
            return true;
        } else if (spokenText.contains("records")) {
            context.startActivity(new Intent(context, RecordsActivity.class));
            return true;
        } else if (spokenText.contains("map")) {
            context.startActivity(new Intent(context, MapActivity.class));
            return true;
        } else if (spokenText.contains("preferences")) {
            context.startActivity(new Intent(context, PreferencesActivity.class));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Receives a speed value in m/s and converts it to km/h.
     *
     * @param speed The speed value in m/s.
     * @return The speed value in km/h.
     */
    public static float speedToKm(float speed) {
        return (speed * 3600) / 1000;
    }

    /**
     * Receives a speed value in km/h and converts it to displayable text.
     *
     * @param context The current context.
     * @param speed The speed value in km/h.
     * @return Displayable text of the speed.
     */
    public static String formatSpeed(Context context, float speed) {
        return context.getString(
                R.string.speed_label, String.format(Locale.US, "%.2f", speed));
    }

    /**
     * Receives a longitude value and converts it to displayable text.
     *
     * @param context The current context.
     * @param value The longitude value.
     * @return Displayable text of the longitude.
     */
    public static String formatLongitude(Context context, double value) {
        return context.getString(
                R.string.longitude_label, String.format(Locale.US, "%f", value));
    }

    /**
     * Receives a latitude value and converts it to displayable text.
     *
     * @param context The current context.
     * @param value The latitude value.
     * @return Displayable text of the latitude.
     */
    public static String formatLatitude(Context context, double value) {
        return context.getString(
                R.string.latitude_label, String.format(Locale.US, "%f", value));
    }

    /**
     * Receives a timestamp and converts it to displayable text.
     *
     * @param timestamp The timestamp value.
     * @return Displayable text of the timestamp.
     */
    public static String formatTimestamp(long timestamp) {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        return dateFormat.format(timestamp);
    }
}
