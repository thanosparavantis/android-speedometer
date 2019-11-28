package com.papei.thanos.speedometer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationResult;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * The main application activity for the speedometer and voice commands.
 */
public class MainActivity extends AppCompatActivity {
    /**
     * The location monitor reference.
     */
    private LocationMonitor locationMonitor;

    /**
     * The speed preferences reference.
     */
    private SpeedPreferences speedPreferences;

    /**
     * The database helper reference.
     */
    private DatabaseHelper dbHelper;

    /**
     * The text to speech reference.
     */
    private TextToSpeech tts;

    /**
     * The media player for the beep sound.
     */
    private MediaPlayer mediaPlayer;

    /**
     * The speed text view reference.
     */
    private TextView speedTextView;

    /**
     * The toggle button reference.
     */
    private Button toggleButton;

    /**
     * Whether speedometer is opened or closed.
     */
    private boolean enabled = false;

    /**
     * The timestamp of the last violation.
     */
    private long lastViolation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setSubtitle(R.string.home);

        // Get a new location monitor object once we set it up.
        this.locationMonitor = this.setupLocationMonitor();

        // Create a new preferences instance for this context.
        this.speedPreferences = new SpeedPreferences(this);

        // Get the speed limit field reference.
        this.dbHelper = new DatabaseHelper(this);

        // Setup text to speech locale.
        this.tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                this.tts.setLanguage(Locale.US);
            }
        });

        // Create a new media player for the beep sound.
        this.mediaPlayer = MediaPlayer.create(this, R.raw.beep);

        // Get the speed text view reference.
        this.speedTextView = findViewById(R.id.speedText);

        // Get the toggle button reference.
        this.toggleButton = findViewById(R.id.toggleButton);

        // Set the speed text as zero.
        this.speedTextView.setText(Utilities.formatSpeed(this, 0));

        // Check if the user has the granted location usage permission.
        this.checkLocationPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Disable the speedometer if paused.
        this.disable();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Close the database connection.
        this.dbHelper.close();
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

    private LocationMonitor setupLocationMonitor() {
        return new LocationMonitor(this, new LocationMonitor.CustomCallback() {
            @Override
            public void onLocationReceived(LocationResult result) {
                // When a new location is received, handle speed accordingly.

                Location location = result.getLastLocation();

                float speed = location.getSpeed();
                float kmSpeed = Utilities.speedToKm(speed);

                // Update the speed text to the current km/h speed.
                speedTextView.setText(
                        Utilities.formatSpeed(getApplicationContext(), kmSpeed)
                );

                float limit = speedPreferences.getSpeedLimit();

                // Check if 10 seconds have passed since the last violation.
                // We do this so we can prevent the app from spamming violations.
                boolean threshold = System.currentTimeMillis() - lastViolation >= TimeUnit.SECONDS.toMillis(10);

                if (kmSpeed >= limit && threshold) {
                    // Store the violation in the database.
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();
                    dbHelper.addViolation(longitude, latitude, speed);

                    // Play the beep sound.
                    mediaPlayer.start();

                    // Show a toast and echo a text to speech message.
                    Toast.makeText(
                            getApplicationContext(),
                            R.string.speed_violation,
                            Toast.LENGTH_SHORT).show();

                    tts.speak(
                            getString(R.string.speed_violation),
                            TextToSpeech.QUEUE_ADD,
                            null,
                            "speed_violation"
                    );

                    // Update the last violation timestamp to prevent spamming.
                    lastViolation = System.currentTimeMillis();
                }
            }

            @Override
            public void onLocationDisabled() {
                // When the location becomes unavailable then stop and show a message.

                showNoLocationDialog();
                disable();
            }
        });
    }

    private void enable() {
        this.locationMonitor.start(
                () -> {
                    mediaPlayer.start();
                    toggleButton.setText(R.string.stop_button);
                    enabled = true;
                },
                () -> {
                    showNoLocationDialog();
                });
    }

    private void disable() {
        this.locationMonitor.stop(
                () -> {
                    this.speedTextView.setText(Utilities.formatSpeed(this, 0));
                    toggleButton.setText(R.string.start_button);
                    enabled = false;
                });
    }

    public void onButtonClick(View view) {
        if (this.enabled) {
            this.disable();
        } else {
            this.enable();
        }
    }

    public void onVoiceButtonClick(View view) {
        // When the voice button is clicked, send a new intent for speech to text.

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            // When speech to text sends us a new result, iterate through the parsed speech text
            // and decide if one of the options can be mapped to an action.

            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            boolean recognized = Utilities.handleTextFromSpeech(this, results.get(0));

            if (!recognized) {
                // If the speech text cannot be mapped to an action,
                // show a toast and echo a text to speech message.

                Toast.makeText(
                        getApplicationContext(),
                        R.string.unrecognized_command,
                        Toast.LENGTH_SHORT).show();

                tts.speak(
                        getString(R.string.unrecognized_command),
                        TextToSpeech.QUEUE_ADD,
                        null,
                        "unrecognized_command"
                );
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkLocationPermission() {
        // Check for fine location permission and request if needed.

        boolean hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!hasPermission) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            // If fine location permission was denied, then disable
            // the speedometer button and show a message.

            showNoPermissionDialog();
            disableButton();
        } else {
            enableButton();
        }
    }

    private void enableButton() {
        toggleButton.setEnabled(true);
    }

    private void disableButton() {
        toggleButton.setEnabled(false);
    }

    private void showNoLocationDialog() {
        // The dialog shown when the user has turned off the device location.

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.no_location);

        builder.setNeutralButton(R.string.dismiss, (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void showNoPermissionDialog() {
        // The dialog shown when the user hasn't granted the required permissions.

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.no_permission);

        builder.setNeutralButton(R.string.dismiss, (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}
