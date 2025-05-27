package com.example.passwordmanager.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AuthPreferences {
    private static final String TAG = "AuthPreferences";
    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_PIN_HASH = "pin_hash";
    private static final String KEY_PIN_SALT = "pin_salt";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    private static final String KEY_FIRST_SETUP = "first_setup";
    public static final String KEY_LAST_AUTH_TIME = "last_auth_time";

    private final SharedPreferences prefs;

    public AuthPreferences(Context context) {
        Context context1 = context.getApplicationContext();
        this.prefs = context1.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Setup PIN pertama kali
    @SuppressLint("ApplySharedPref")
    public boolean setupPin(String pin) {
        try {
            // Generate salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            // Hash PIN dengan salt
            String hashedPin = hashPinWithSalt(pin, salt);

            // Simpan ke SharedPreferences
            long currentTime = System.currentTimeMillis(); // Dapatkan waktu saat ini
            boolean success = prefs.edit()
                    .putString(KEY_PIN_HASH, hashedPin)
                    .putString(KEY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
                    .putBoolean(KEY_FIRST_SETUP, false)
                    .putLong(KEY_LAST_AUTH_TIME, currentTime) // PASTIKAN INI DITAMBAHKAN
                    .commit(); // <<-- UBAH KE .commit() UNTUK TESTING INI

            if (success) {
                Log.d(TAG, "PIN setup completed and committed. Last auth time set to: " + currentTime);
            } else {
                Log.e(TAG, "Failed to commit PIN setup to SharedPreferences");
            }
            return success;

        } catch (Exception e) {
            Log.e(TAG, "Error setting up PIN", e);
            return false;
        }
    }

    // Tambahkan atau pastikan method ini ada:
    public void resetLastAuthTimeForLogout() {
        prefs.edit().putLong(KEY_LAST_AUTH_TIME, 0).apply();
        Log.d(TAG, "Last auth time reset for logout.");
    }

    // Verifikasi PIN
    public boolean verifyPin(String inputPin) {
        try {
            String storedHash = prefs.getString(KEY_PIN_HASH, null);
            String storedSaltBase64 = prefs.getString(KEY_PIN_SALT, null); // Ganti nama variabel agar lebih jelas

            // Logging tambahan untuk debug
            Log.d(TAG, "verifyPin called with inputPin: " + inputPin);
            Log.d(TAG, "verifyPin - Stored Hash from prefs: " + storedHash);
            Log.d(TAG, "verifyPin - Stored Salt (Base64) from prefs: " + storedSaltBase64);

            if (storedHash == null || storedSaltBase64 == null) {
                Log.w(TAG, "verifyPin - No PIN found in SharedPreferences (hash or salt is null).");
                return false;
            }

            byte[] salt = Base64.decode(storedSaltBase64, Base64.DEFAULT);
            String inputHash = hashPinWithSalt(inputPin, salt);
            Log.d(TAG, "verifyPin - Input PIN hashed: " + inputHash);

            boolean isValid = storedHash.equals(inputHash);

            if (isValid) {
                updateLastAuthTime(); // Ini sudah benar
                Log.d(TAG, "PIN verification successful.");
            } else {
                // Log yang lebih detail jika gagal
                Log.e(TAG, "PIN verification FAILED. Stored Hash: " + storedHash + " vs Input Hashed: " + inputHash);
            }
            return isValid;

        } catch (Exception e) {
            Log.e(TAG, "Error during verifyPin", e);
            return false;
        }
    }

    // Hash PIN dengan salt menggunakan SHA-256
    private String hashPinWithSalt(String pin, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(salt);
        byte[] hash = digest.digest(pin.getBytes());
        return Base64.encodeToString(hash, Base64.NO_WRAP);
    }

    // Cek apakah ini setup pertama kali
    public boolean isFirstSetup() {
        boolean isFirst = prefs.getBoolean(KEY_FIRST_SETUP, true);
        Log.d(TAG, "isFirstSetup() called. Value of " + KEY_FIRST_SETUP + ": " + prefs.contains(KEY_FIRST_SETUP) + " (is " + isFirst + ")");
        return isFirst;
    }

    // Enable/disable biometric
    public void setBiometricEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply();
        Log.d(TAG, "Biometric " + (enabled ? "enabled" : "disabled"));
    }

    public boolean isBiometricEnabled() {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }

    // Update waktu autentikasi terakhir
    public void updateLastAuthTime() {
        long currentTime = System.currentTimeMillis();
        prefs.edit().putLong(KEY_LAST_AUTH_TIME, currentTime).apply();
        Log.d(TAG, "Last auth time updated: " + currentTime);
    }

    // Cek apakah perlu autentikasi ulang (contoh: setelah 5 menit)
    public boolean needsReAuthentication() {
        long lastAuthTime = prefs.getLong(KEY_LAST_AUTH_TIME, 0);
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - lastAuthTime;

        // 5 menit = 5 * 60 * 1000 milliseconds
        boolean needsAuth = timeDifference > (5 * 60 * 1000);

        Log.d(TAG, "Time since last auth: " + timeDifference + "ms, needs reauth: " + needsAuth);
        return needsAuth;
    }

    // Reset semua data (untuk logout atau reset)
    public void clearAuthData() {
        prefs.edit().clear().apply();
        Log.d(TAG, "Auth data cleared");
    }

    // Ubah PIN
    public boolean changePin(String oldPin, String newPin) {
        if (verifyPin(oldPin)) {
            return setupPin(newPin);
        }
        return false;
    }

    // Cek apakah PIN sudah di-setup
    public boolean isPinSetup() {
        boolean hasPin = prefs.getString(KEY_PIN_HASH, null) != null;
        Log.d(TAG, "isPinSetup() called. Value of " + KEY_PIN_HASH + " found: " + prefs.contains(KEY_PIN_HASH) + " (is " + hasPin + ")");
        return hasPin;
    }


}