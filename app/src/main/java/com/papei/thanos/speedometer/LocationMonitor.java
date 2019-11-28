package com.papei.thanos.speedometer;

import android.content.Context;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/**
 * Establishes a continuous location listener through Google Play Location services.
 */
public final class LocationMonitor {
    /**
     * The current context.
     */
    private final Context context;

    /**
     * The callback called when a new location is fetched.
     */
    private final CustomCallback callback;

    /**
     * The Google Play Location client reference.
     */
    private final FusedLocationProviderClient client;

    /**
     * The request settings used to fetch new locations.
     */
    private final LocationRequest request;

    /**
     * Default constructor.
     *
     * @param context  The current context.
     * @param callback The callback called when a new location is fetched.
     */
    public LocationMonitor(Context context, CustomCallback callback) {
        this.context = context;
        this.callback = callback;
        this.client = LocationServices.getFusedLocationProviderClient(this.context);

        this.request = new LocationRequest();
        this.request.setMaxWaitTime(500);
        this.request.setInterval(500);
        this.request.setFastestInterval(500);
        this.request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Opens the location listener.
     *
     * @param startSignal The start signal callback is called when the location setting is available and the listener has opened.
     * @param errorSignal The error signal callback is called when the location setting is unavailable.
     */
    public void start(StartSignal startSignal, ErrorSignal errorSignal) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(this.request);

        SettingsClient client = LocationServices.getSettingsClient(this.context);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(new SuccessListener(startSignal));
        task.addOnFailureListener(new FailureListener(errorSignal));
    }

    /**
     * Closes the location listener.
     *
     * @param stopSignal The stop signal is called when the location listener is closed.
     */
    public void stop(StopSignal stopSignal) {
        this.client.removeLocationUpdates(this.callback);
        stopSignal.handle();
    }

    /**
     * A custom callback used every time a new location is fetched.
     */
    public abstract static class CustomCallback extends LocationCallback {
        /**
         * Called every time a new location result is available.
         *
         * @param result The location result that holds data.
         */
        public abstract void onLocationReceived(LocationResult result);

        /**
         * Called when location becomes unavailable.
         */
        public abstract void onLocationDisabled();

        @Override
        public void onLocationAvailability(LocationAvailability status) {
            if (!status.isLocationAvailable()) {
                this.onLocationDisabled();
            }
        }

        @Override
        public void onLocationResult(LocationResult result) {
            if (result != null) {
                this.onLocationReceived(result);
            }
        }
    }

    /**
     * Base interface used for signals (callbacks).
     */
    public interface Signal {
        void handle();
    }

    /**
     * A start signal callback is called when the location setting is available and the listener has opened.
     */
    public interface StartSignal extends Signal {
        //
    }

    /**
     * The stop signal is called when the location listener is closed.
     */
    public interface StopSignal extends Signal {
        //
    }

    /**
     * The error signal callback is called when the location setting is unavailable.
     */
    public interface ErrorSignal extends Signal {
        //
    }

    /**
     * A listener for successful location setting requests.
     */
    public final class SuccessListener implements OnSuccessListener<LocationSettingsResponse> {
        /**
         * The start signal reference used as a callback.
         */
        private final StartSignal startSignal;

        /**
         * Default constructor.
         *
         * @param startSignal The start signal reference used as a callback.
         */
        public SuccessListener(StartSignal startSignal) {
            this.startSignal = startSignal;
        }

        @Override
        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
            client.requestLocationUpdates(request, callback, Looper.getMainLooper());
            this.startSignal.handle();
        }
    }

    /**
     * A listener for failed location setting requests.
     */
    public final class FailureListener implements OnFailureListener {
        /**
         * The error signal reference used as a callback.
         */
        private final ErrorSignal errorSignal;

        /**
         * Default constructor.
         *
         * @param errorSignal The error signal reference used as a callback.
         */
        public FailureListener(ErrorSignal errorSignal) {
            this.errorSignal = errorSignal;
        }

        @Override
        public void onFailure(@NonNull Exception e) {
            this.errorSignal.handle();
        }
    }
}
