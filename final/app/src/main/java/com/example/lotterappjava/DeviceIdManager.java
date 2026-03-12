package com.example.lotterappjava;

import android.content.Context;
import android.provider.Settings;

public class DeviceIdManager {

    public static String getDeviceId(Context context) {
        // US 01.07.01: Entrant is identified by device (no username/password).
        // Using ANDROID_ID as a unique device identifier.
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
