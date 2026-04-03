package com.example.eventlottery.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

public class QRCodeHelper {

    private static final int DEFAULT_SIZE = 512;

    /**
     * Generate a QR code Bitmap from content string.
     * Uses ZXing QRCodeWriter (Zebra Crossing library).
     */
    public static Bitmap generateQRCode(String content) {
        return generateQRCode(content, DEFAULT_SIZE, DEFAULT_SIZE);
    }

    public static Bitmap generateQRCode(String content, int width, int height) {
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        try {
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Build the deep-link content that gets encoded into the QR code.
     * Format: eventlottery://event/{eventId}
     * Scannable by ZXing/ML Kit and handled via intent-filter in manifest.
     */
    public static String buildEventDeepLink(String eventId) {
        return "eventlottery://event/" + eventId;
    }

    /**
     * Extract eventId from a deep link string.
     */
    public static String extractEventId(String deepLink) {
        if (deepLink == null) return null;
        String prefix = "eventlottery://event/";
        if (deepLink.startsWith(prefix)) {
            return deepLink.substring(prefix.length());
        }
        return null;
    }
}
