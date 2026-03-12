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
 * Small helper to request location permission and fetch a single location fix.
 */
public class LocationHelper {

    public interface LocationCallback {
        void onLocationResult(Double latitude, Double longitude);
        void onError(String message);
    }

    public static final int REQUEST_CODE_LOCATION = 1001;

    public static boolean hasLocationPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_CODE_LOCATION
        );
    }

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

