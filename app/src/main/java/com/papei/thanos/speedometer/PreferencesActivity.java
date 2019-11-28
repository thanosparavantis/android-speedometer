package com.papei.thanos.speedometer;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * The activity that allows the user to change app preferences.
 */
public class PreferencesActivity extends AppCompatActivity {
    /**
     * The speed preferences reference.
     */
    private SpeedPreferences speedPreferences;

    /**
     * The speed limit field reference.
     */
    private EditText speedLimitText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        getSupportActionBar().setSubtitle(R.string.preferences);

        // Create a new preferences instance for this context.
        this.speedPreferences = new SpeedPreferences(this);

        // Get the speed limit field reference.
        this.speedLimitText = findViewById(R.id.speedLimitText);

        // Update the current speed limit with the saved value.
        this.updateSpeedLimitText();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Update the current speed limit with the saved value
        // in case it was changed from a duplicate activity.
        this.updateSpeedLimitText();
    }

    private void updateSpeedLimitText() {
        String speed = Float.toString(this.speedPreferences.getSpeedLimit());
        this.speedLimitText.setText(speed);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        // Add the standard menu for navigation.
        inflater.inflate(R.menu.menu_options, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return Utilities.handleNavigation(this, item.getItemId());
    }

    public void onButtonClick(View view) {
        float speed = Float.parseFloat(this.speedLimitText.getText().toString());

        if (speed != this.speedPreferences.getSpeedLimit()) {
            // If the value has changed, update the speed limit.

            this.speedPreferences.setSpeedLimit(speed);
            Toast.makeText(this, R.string.speed_limit_updated, Toast.LENGTH_LONG).show();
        }
    }
}
