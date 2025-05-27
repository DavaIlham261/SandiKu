package com.example.passwordmanager.utils;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class BiometricHelper {
    private static final String TAG = "BiometricHelper";

    public interface BiometricAuthCallback {
        void onAuthenticationSucceeded();
        void onAuthenticationFailed();
        void onAuthenticationError(String error);
        void onBiometricNotAvailable();
    }

    private final FragmentActivity activity;
    private final BiometricAuthCallback callback;
    private BiometricPrompt biometricPrompt;

    public BiometricHelper(FragmentActivity activity, BiometricAuthCallback callback) {
        this.activity = activity;
        this.callback = callback;
        setupBiometricPrompt();
    }

    private void setupBiometricPrompt() {
        BiometricPrompt.AuthenticationCallback authCallback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d(TAG, "Biometric authentication succeeded");
                callback.onAuthenticationSucceeded();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.d(TAG, "Biometric authentication failed");
                callback.onAuthenticationFailed();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.e(TAG, "Biometric authentication error: " + errString);

                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_CANCELED) {
                    // User canceled, don't show error
                    return;
                }

                callback.onAuthenticationError(errString.toString());
            }
        };

        biometricPrompt = new BiometricPrompt(activity,
                ContextCompat.getMainExecutor(activity), authCallback);
    }

    public boolean isBiometricAvailable() {
        BiometricManager biometricManager = BiometricManager.from(activity);

        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d(TAG, "Biometric authentication is available");
                return true;

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.d(TAG, "No biometric hardware available");
                return false;

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.d(TAG, "Biometric hardware unavailable");
                return false;

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.d(TAG, "No biometric credentials enrolled");
                return false;

            default:
                Log.d(TAG, "Biometric authentication not available");
                return false;
        }
    }

    public void authenticate() {
        if (!isBiometricAvailable()) {
            callback.onBiometricNotAvailable();
            return;
        }

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autentikasi Biometrik")
                .setSubtitle("Gunakan sidik jari atau wajah untuk membuka Password Manager")
                .setDescription("Letakkan jari Anda pada sensor atau lihat ke kamera")
                .setNegativeButtonText("Gunakan PIN")
                .setConfirmationRequired(true)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    public String getBiometricStatusMessage() {
        BiometricManager biometricManager = BiometricManager.from(activity);

        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return "Biometrik tersedia";

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return "Perangkat tidak mendukung biometrik";

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                return "Sensor biometrik tidak tersedia";

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return "Belum ada biometrik yang terdaftar. Silakan daftar di Pengaturan.";

            default:
                return "Biometrik tidak tersedia";
        }
    }
}