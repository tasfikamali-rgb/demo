package com.example.lotterappjava;

import android.content.Context;
import android.provider.Settings;

/**
 * Utility class for managing and retrieving the unique device identifier.
 * This class follows the Utility design pattern to provide a centralized way
 * to obtain the device ID for user identification as per US 01.07.01.
 *
 * Outstanding issues:
 * - ANDROID_ID can change upon factory reset.
 */
public class DeviceIdManager {

    /**
     * Retrieves a unique identifier for the device.
     * Uses Settings.Secure.ANDROID_ID to identify entrants who do not use email/password.
     *
     * @param context The application context.
     * @return A unique string identifier for the device.
     */
    public static String getDeviceId(Context context) {
        // US 01.07.01: Entrant is identified by device (no username/password).
        // Using ANDROID_ID as a unique device identifier.
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
