package com.papei.thanos.speedometer;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * A wrapper around shared preferences that is used to save speed settings.
 */
public final class SpeedPreferences {
    /**
     * The current context.
     */
    private final Context context;

    /**
     * A reference to shared preferences.
     */
    private final SharedPreferences preferences;

    /**
     * Default constructor.
     *
     * @param context The current context.
     */
    public SpeedPreferences(Context context) {
        this.context = context;
        this.preferences = this.context.getSharedPreferences("speed_preferences", Context.MODE_PRIVATE);
    }

    /**
     * Gets the saved speed limit.
     *
     * @return The speed limit value.
     */
    public float getSpeedLimit() {
        return this.preferences.getFloat("speed_limit", 30.0f);
    }

    /**
     * Sets a new speed limit.
     *
     * @param limit The new speed limit value.
     */
    public void setSpeedLimit(float limit) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putFloat("speed_limit", limit);
        editor.apply();
    }
}
