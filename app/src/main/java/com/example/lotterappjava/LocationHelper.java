package com.example.lotterappjava;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Utility helper class for managing location permissions and fetching current location.
 * This class follows the Utility design pattern to provide centralized location-related helper methods.
 *
 * Outstanding issues:
 * - None currently identified.
 */
public class LocationHelper {

    /**
     * Interface for handling location result callbacks.
     */
    public interface LocationCallback {
        /**
         * Called when a location is successfully retrieved.
         * @param latitude The latitude of the location.
         * @param longitude The longitude of the location.
         */
        void onLocationResult(Double latitude, Double longitude);

        /**
         * Called when an error occurs during location retrieval.
         * @param message The error message.
         */
        void onError(String message);
    }

    /**
     * Constant request code for location permissions.
     */
    public static final int REQUEST_CODE_LOCATION = 1001;

    /**
     * Checks if the application has been granted fine location permission.
     *
     * @param activity The activity context to check permissions against.
     * @return True if permission is granted, false otherwise.
     */
    public static boolean hasLocationPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests fine location permission from the user.
     *
     * @param activity The activity instance making the request.
     */
    public static void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_CODE_LOCATION
        );
    }

    /**
     * Attempts to retrieve the last known location of the device.
     * Requires fine location permission.
     *
     * @param activity The activity context.
     * @param callback The callback to notify with the location result or error.
     */
    @SuppressLint("MissingPermission")
    public static void getCurrentLocation(Activity activity, LocationCallback callback) {
        if (!hasLocationPermission(activity)) {
            callback.onError("Location permission not granted");
            return;
        }
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(activity);
        client.getLastLocation()
                .addOnSuccessListener(activity, location -> {
                    if (location != null) {
                        callback.onLocationResult(location.getLatitude(), location.getLongitude());
                    } else {
                        callback.onError("Could not get location");
                    }
                })
                .addOnFailureListener(e -> callback.onError("Could not get location"));
    }
}
