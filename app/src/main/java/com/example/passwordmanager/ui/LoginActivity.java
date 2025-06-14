package com.example.passwordmanager.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.passwordmanager.R;
import com.example.passwordmanager.utils.AuthPreferences;
import com.example.passwordmanager.utils.BiometricHelper;
import androidx.core.content.ContextCompat;

public class LoginActivity extends AppCompatActivity implements BiometricHelper.BiometricAuthCallback {

    private static final String TAG = "LoginActivity";

    private AuthPreferences authPrefs;
    private BiometricHelper biometricHelper;

    // UI Components
    private LinearLayout setupContainer;
    private LinearLayout loginContainer;
    private EditText setupPinInput;
    private EditText confirmPinInput;
    private EditText loginPinInput;
    private Button setupPinButton;
    private Button loginPinButton;
    private Button biometricButton;
    private ImageView biometricIcon;
    private TextView titleText;
    private TextView subtitleText;
    private TextView biometricStatusText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initComponents();
        setupListeners();

        authPrefs = new AuthPreferences(this);
        biometricHelper = new BiometricHelper(this, this);

        checkAuthenticationState();
    }

    private void initComponents() {
        // Setup views
        setupContainer = findViewById(R.id.setup_container);
        loginContainer = findViewById(R.id.login_container);
        setupPinInput = findViewById(R.id.setup_pin_input);
        confirmPinInput = findViewById(R.id.confirm_pin_input);
        loginPinInput = findViewById(R.id.login_pin_input);
        setupPinButton = findViewById(R.id.setup_pin_button);
        loginPinButton = findViewById(R.id.login_pin_button);
        biometricButton = findViewById(R.id.biometric_button);
        biometricIcon = findViewById(R.id.biometric_icon);
        titleText = findViewById(R.id.title_text);
        subtitleText = findViewById(R.id.subtitle_text);
        biometricStatusText = findViewById(R.id.biometric_status_text);
    }

    private void setupListeners() {
        setupPinButton.setOnClickListener(v -> handlePinSetup());
        loginPinButton.setOnClickListener(v -> handlePinLogin());
        biometricButton.setOnClickListener(v -> startBiometricAuth());

        // Auto-enable login button when PIN is entered
        loginPinInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loginPinButton.setEnabled(s.length() >= 4);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup PIN validation
        TextWatcher setupPinWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSetupButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        setupPinInput.addTextChangedListener(setupPinWatcher);
        confirmPinInput.addTextChangedListener(setupPinWatcher);
    }

    private void updateSetupButtonState() {
        String pin = setupPinInput.getText().toString();
        String confirmPin = confirmPinInput.getText().toString();

        boolean isValid = pin.length() >= 4 &&
                confirmPin.length() >= 4 &&
                pin.equals(confirmPin);

        setupPinButton.setEnabled(isValid);

        // Show PIN mismatch warning
        if (confirmPin.length() >= 4 && !pin.equals(confirmPin)) {
            confirmPinInput.setError("PIN tidak cocok");
        } else {
            confirmPinInput.setError(null);
        }
    }

    private void checkAuthenticationState() {
        Log.d(TAG, "Checking authentication state...");
        boolean firstSetup = authPrefs.isFirstSetup(); // Panggil sekali dan simpan
        Log.d(TAG, "isFirstSetup returned: " + firstSetup);
        if (firstSetup) {
            showSetupMode();
        } else {
            showLoginMode();
        }
    }

    @SuppressLint("SetTextI18n")
    private void showSetupMode() {
        Log.d(TAG, "Showing setup mode");

        setupContainer.setVisibility(View.VISIBLE);
        loginContainer.setVisibility(View.GONE);

        titleText.setText("Setup Password Manager");
        subtitleText.setText("Buat PIN 4 digit untuk masuk ke aplikasi");

        setupPinInput.setText("");
        confirmPinInput.setText("");
        setupPinButton.setEnabled(false);
    }

    @SuppressLint("SetTextI18n")
    private void showLoginMode() {
        Log.d(TAG, "Showing login mode");

        setupContainer.setVisibility(View.GONE);
        loginContainer.setVisibility(View.VISIBLE);

        titleText.setText("Password Manager");
        subtitleText.setText("Masukkan PIN atau gunakan biometrik untuk masuk");

        loginPinInput.setText("");
        loginPinButton.setEnabled(false);

        setupBiometricUI();

        // Auto-start biometric if enabled and available
        if (authPrefs.isBiometricEnabled() && biometricHelper.isBiometricAvailable()) {
            // Delay sedikit untuk UI yang smooth
            biometricButton.postDelayed(this::startBiometricAuth, 500);
        }
    }

    @SuppressLint("SetTextI18n")
    private void setupBiometricUI() {
        boolean biometricAvailable = biometricHelper.isBiometricAvailable();
        boolean biometricEnabled = authPrefs.isBiometricEnabled();

        if (biometricAvailable) {
            biometricButton.setVisibility(View.VISIBLE);
            biometricIcon.setVisibility(View.VISIBLE);
            biometricButton.setEnabled(true);

            if (biometricEnabled) {
                biometricStatusText.setText("Ketuk untuk autentikasi biometrik");
                biometricStatusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
            } else {
                biometricStatusText.setText("Ketuk untuk mengaktifkan biometrik");
                biometricStatusText.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            }
        } else {
            biometricButton.setVisibility(View.GONE);
            biometricIcon.setVisibility(View.GONE);
            biometricStatusText.setText(biometricHelper.getBiometricStatusMessage());
            biometricStatusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }

        biometricStatusText.setVisibility(View.VISIBLE);
    }

    private void handlePinSetup() {
        String pin = setupPinInput.getText().toString();
        String confirmPin = confirmPinInput.getText().toString();

        if (pin.length() < 4) {
            setupPinInput.setError("PIN minimal 4 digit");
            return;
        }

        if (!pin.equals(confirmPin)) {
            confirmPinInput.setError("PIN tidak cocok");
            return;
        }

        if (authPrefs.setupPin(pin)) {
            Toast.makeText(this, "PIN berhasil dibuat!", Toast.LENGTH_SHORT).show();

            // Tanya user apakah mau mengaktifkan biometrik
            if (biometricHelper.isBiometricAvailable()) {
                askEnableBiometric();
            } else {
                proceedToMain();
            }
        } else {
            Toast.makeText(this, "Gagal membuat PIN", Toast.LENGTH_SHORT).show();
        }
    }

    private void askEnableBiometric() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Aktifkan Biometrik?")
                .setMessage("Anda dapat menggunakan sidik jari atau wajah untuk masuk lebih cepat. Aktifkan sekarang?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    authPrefs.setBiometricEnabled(true);
                    Toast.makeText(this, "Biometrik diaktifkan!", Toast.LENGTH_SHORT).show();
                    proceedToMain();
                })
                .setNegativeButton("Nanti", (dialog, which) -> proceedToMain())
                .setCancelable(false)
                .show();
    }

    private void handlePinLogin() {
        String pin = loginPinInput.getText().toString();

        if (pin.length() < 4) {
            loginPinInput.setError("PIN minimal 4 digit");
            return;
        }

        if (authPrefs.verifyPin(pin)) {
            Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show();
            proceedToMain();
        } else {
            loginPinInput.setError("PIN salah");
            loginPinInput.setText("");

            // Shake animation bisa ditambahkan di sini
            Toast.makeText(this, "PIN salah, coba lagi", Toast.LENGTH_SHORT).show();
        }
    }

    private void startBiometricAuth() {
        if (!authPrefs.isBiometricEnabled()) {
            // First time biometric setup
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Aktifkan Biometrik")
                    .setMessage("Aktifkan autentikasi biometrik untuk akses yang lebih cepat?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        authPrefs.setBiometricEnabled(true);
                        biometricHelper.authenticate();
                    })
                    .setNegativeButton("Tidak", null)
                    .show();
        } else {
            biometricHelper.authenticate();
        }
    }

    private void proceedToMain() {
        Log.d(TAG, "Proceeding to MainActivity");
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // BiometricHelper.BiometricAuthCallback implementations
    @Override
    public void onAuthenticationSucceeded() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Autentikasi biometrik berhasil!", Toast.LENGTH_SHORT).show();
            authPrefs.updateLastAuthTime();
            proceedToMain();
        });
    }

    @Override
    public void onAuthenticationFailed() {
        runOnUiThread(() -> Toast.makeText(this, "Autentikasi biometrik gagal, coba lagi", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onAuthenticationError(String error) {
        runOnUiThread(() -> Toast.makeText(this, "Error biometrik: " + error, Toast.LENGTH_LONG).show());
    }

    @Override
    public void onBiometricNotAvailable() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Biometrik tidak tersedia", Toast.LENGTH_SHORT).show();
            setupBiometricUI(); // Refresh UI
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh biometric status when returning from settings
        if (!authPrefs.isFirstSetup()) {
            setupBiometricUI();
        }
    }
}