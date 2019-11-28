package com.papei.thanos.speedometer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    /**
     * The google maps reference.
     */
    private GoogleMap googleMap;

    /**
     * The database helper reference.
     */
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        getSupportActionBar().setSubtitle(R.string.map);

        // Create a new connection with the database through the helper.
        this.dbHelper = new DatabaseHelper(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Close the database connection.
        this.dbHelper.close();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Update the map reference when it's ready.
        this.googleMap = googleMap;

        this.dbHelper.getViolations().thenAcceptAsync(list -> {
            // Fetch all violations asynchronously, then populate the map with markers.

            for (DatabaseHelper.ViolationRecord record : list) {
                LatLng position = new LatLng(record.getLatitude(), record.getLongitude());
                String title = "Violation " + record.getId() + ": " + Utilities.formatSpeed(this, record.getSpeed());
                this.googleMap.addMarker(new MarkerOptions().position(position).title(title));
            }

            // If we receive an intent, check if the longitude and latitude extras are set.
            Intent intent = getIntent();
            boolean hasLongitude = intent.hasExtra("longitude");
            boolean hasLatitude = intent.hasExtra("latitude");

            if (hasLongitude && hasLatitude) {
                // If that's the case, we should zoom on a specific marker on the map.

                LatLng position = new LatLng(
                        intent.getDoubleExtra("latitude", 0),
                        intent.getDoubleExtra("longitude", 0));

                this.googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(position, 15)
                );
            } else {
                // If the extras are missing, then we zoom on the last marker on the map.

                DatabaseHelper.ViolationRecord record = list.get(list.size() - 1);
                LatLng position = new LatLng(record.getLatitude(), record.getLongitude());

                this.googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(position, 15)
                );


            }
        }, getMainExecutor());
    }
}
