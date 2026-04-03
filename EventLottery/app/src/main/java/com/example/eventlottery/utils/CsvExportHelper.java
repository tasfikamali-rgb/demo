package com.example.eventlottery.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;
import com.example.eventlottery.models.Registration;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CsvExportHelper {

    public static void exportRegistrations(Context ctx, String eventTitle,
                                           List<Registration> registrations) {
        String fileName = "enrolled_" + eventTitle.replaceAll("[^a-zA-Z0-9]", "_")
                + "_" + new SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                .format(new Date()) + ".csv";

        StringBuilder csv = new StringBuilder();
        csv.append("Name,Email,Status,Joined Date,Responded Date,Latitude,Longitude\n");
        for (Registration r : registrations) {
            csv.append(escapeCsv(r.getUserName())).append(",");
            csv.append(escapeCsv(r.getUserEmail())).append(",");
            csv.append(escapeCsv(r.getStatus())).append(",");
            csv.append(DateUtils.formatDateTime(r.getJoinedAt())).append(",");
            csv.append(r.getRespondedAt() > 0
                    ? DateUtils.formatDateTime(r.getRespondedAt()) : "").append(",");
            csv.append(r.isHasGeolocation() ? r.getLatitude() : "").append(",");
            csv.append(r.isHasGeolocation() ? r.getLongitude() : "").append("\n");
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = ctx.getContentResolver()
                        .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    try (OutputStream os = ctx.getContentResolver().openOutputStream(uri);
                         OutputStreamWriter writer = new OutputStreamWriter(os)) {
                        writer.write(csv.toString());
                    }
                    Toast.makeText(ctx, "CSV saved to Downloads: " + fileName,
                            Toast.LENGTH_LONG).show();

                    // Share intent — FLAG_ACTIVITY_NEW_TASK required when starting from
                    // a non-Activity context (e.g. called from OrganizerEventManagementActivity
                    // which passes "this" but safety-proofing for Application context usage)
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/csv");
                    share.putExtra(Intent.EXTRA_STREAM, uri);
                    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Intent chooser = Intent.createChooser(share, "Share CSV");
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(chooser);
                }
            }
        } catch (IOException e) {
            Toast.makeText(ctx, "Failed to export CSV: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
