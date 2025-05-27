package com.example.passwordmanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.passwordmanager.R;
import com.example.passwordmanager.utils.AuthPreferences;
import com.google.android.material.textfield.TextInputLayout; // Import TextInputLayout

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AuthPreferences authPrefs;
    private MainSharedViewModel sharedViewModel; // Tambahkan SharedViewModel


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authPrefs = new AuthPreferences(this);

        // Inisialisasi ViewModel
        sharedViewModel = new ViewModelProvider(this).get(MainSharedViewModel.class);

        // Check authentication status
        checkAuthenticationRequired();

        // Setup observer untuk event dari ViewModel
        setupObservers();

        if (savedInstanceState == null) { // Hanya load fragment jika bukan recreate activity
            loadFragment(new AccountListFragment(), false);
        }
    }

    private void setupObservers() {
        sharedViewModel.accountSavedEvent.observe(this, event -> {
            if (event != null) {
                Log.d(TAG, "Observed AccountSavedEvent");
                String message;
                if (event.accountTitle != null && !event.accountTitle.isEmpty()) {
                    message = "Akun '" + event.accountTitle + "' berhasil " + (event.isUpdate ? "diperbarui!" : "disimpan!");
                } else {
                    message = "Akun berhasil " + (event.isUpdate ? "diperbarui!" : "disimpan!");
                }
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Toast shown: " + message);
                refreshCurrentFragment();
            }
        });
    }

    private void checkAuthenticationRequired() {
        // Jika user perlu autentikasi ulang, kembali ke LoginActivity
        if (authPrefs.needsReAuthentication() && !authPrefs.isFirstSetup()) { // Tambahkan !authPrefs.isFirstSetup() agar tidak redirect saat setup awal
            Log.d(TAG, "Re-authentication required, redirecting to LoginActivity");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Update waktu akses terakhir hanya jika tidak sedang setup dan tidak perlu re-autentikasi
        if (!authPrefs.isFirstSetup()) {
            authPrefs.updateLastAuthTime();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check auth setiap kali app kembali ke foreground
        // Hanya periksa jika PIN sudah disetup untuk menghindari loop ke LoginActivity saat setup pertama
        if (authPrefs.isPinSetup() && authPrefs.needsReAuthentication()) {
            checkAuthenticationRequired();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menu_settings) {
            showSettingsDialog();
            return true;
        } else if (itemId == R.id.menu_logout) {
            showLogoutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSettingsDialog() {
        String[] options = {"Ubah PIN", "Pengaturan Biometrik"};

        new AlertDialog.Builder(this)
                .setTitle("Pengaturan")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showChangePinDialog(); // Panggil method yang sudah diperbarui
                            break;
                        case 1:
                            showBiometricSettings();
                            break;
                    }
                })
                .show();
    }

    private void showChangePinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_pin, null); // Kita akan buat layout ini
        builder.setView(dialogView);

        final TextInputLayout oldPinLayout = dialogView.findViewById(R.id.old_pin_layout);
        final EditText oldPinEditText = dialogView.findViewById(R.id.old_pin_edit_text);
        final TextInputLayout newPinLayout = dialogView.findViewById(R.id.new_pin_layout);
        final EditText newPinEditText = dialogView.findViewById(R.id.new_pin_edit_text);
        final TextInputLayout confirmNewPinLayout = dialogView.findViewById(R.id.confirm_new_pin_layout);
        final EditText confirmNewPinEditText = dialogView.findViewById(R.id.confirm_new_pin_edit_text);

        builder.setTitle("Ubah PIN");
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            // Akan di-override nanti untuk validasi
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setEnabled(false); // Disable tombol simpan secara default

            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String oldPin = oldPinEditText.getText().toString().trim();
                    String newPin = newPinEditText.getText().toString().trim();
                    String confirmNewPin = confirmNewPinEditText.getText().toString().trim();

                    boolean isOldPinValid = oldPin.length() >= 4;
                    boolean isNewPinValid = newPin.length() >= 4;
                    boolean isConfirmNewPinValid = confirmNewPin.length() >= 4;
                    boolean newPinsMatch = newPin.equals(confirmNewPin);

                    positiveButton.setEnabled(isOldPinValid && isNewPinValid && isConfirmNewPinValid && newPinsMatch);

                    if (newPin.length() >= 4 && !confirmNewPin.isEmpty() && !newPinsMatch) {
                        confirmNewPinLayout.setError("PIN baru tidak cocok");
                    } else {
                        confirmNewPinLayout.setError(null);
                    }
                    if (!newPin.isEmpty() && newPin.length() < 4) {
                        newPinLayout.setError("PIN minimal 4 digit");
                    } else {
                        newPinLayout.setError(null);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            };

            oldPinEditText.addTextChangedListener(textWatcher);
            newPinEditText.addTextChangedListener(textWatcher);
            confirmNewPinEditText.addTextChangedListener(textWatcher);

            positiveButton.setOnClickListener(v -> {
                String oldPin = oldPinEditText.getText().toString().trim();
                String newPin = newPinEditText.getText().toString().trim();
                // String confirmNewPin = confirmNewPinEditText.getText().toString().trim(); // Sudah divalidasi

                if (!authPrefs.verifyPin(oldPin)) {
                    oldPinLayout.setError("PIN lama salah");
                    Toast.makeText(MainActivity.this, "PIN lama salah.", Toast.LENGTH_SHORT).show();
                    return;
                }
                oldPinLayout.setError(null);

                if (newPin.length() < 4) {
                    newPinLayout.setError("PIN baru minimal 4 digit");
                    Toast.makeText(MainActivity.this, "PIN baru minimal 4 digit.", Toast.LENGTH_SHORT).show();
                    return;
                }
                newPinLayout.setError(null);

                if (authPrefs.changePin(oldPin, newPin)) {
                    Toast.makeText(MainActivity.this, "PIN berhasil diubah.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    // Seharusnya ini tidak terjadi jika verifyPin di atas berhasil,
                    // tapi sebagai fallback.
                    Toast.makeText(MainActivity.this, "Gagal mengubah PIN.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }


    private void showBiometricSettings() {
        boolean currentStatus = authPrefs.isBiometricEnabled();
        String message = currentStatus ?
                "Biometrik saat ini AKTIF. Nonaktifkan?" :
                "Biometrik saat ini NONAKTIF. Aktifkan?";
        String positiveText = currentStatus ? "Nonaktifkan" : "Aktifkan";

        new AlertDialog.Builder(this)
                .setTitle("Pengaturan Biometrik")
                .setMessage(message)
                .setPositiveButton(positiveText, (dialog, which) -> {
                    authPrefs.setBiometricEnabled(!currentStatus);
                    String statusText = !currentStatus ? "diaktifkan" : "dinonaktifkan";
                    Toast.makeText(this, "Biometrik berhasil " + statusText, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Anda yakin ingin keluar dari aplikasi?")
                .setPositiveButton("Ya", (dialog, which) -> performLogout())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void performLogout() {
        Log.d(TAG, "Performing logout");

        // Untuk logout, kita tidak menghapus PIN, hanya waktu autentikasi terakhir
        // agar saat buka aplikasi lagi, diminta login (PIN atau Biometrik).
        // Jika ingin menghapus semua (termasuk PIN), maka uncomment clearAuthData().
        // authPrefs.clearAuthData();

        // Panggil method dari authPrefs untuk mereset waktu autentikasi
        authPrefs.resetLastAuthTimeForLogout();

        // Kembali ke LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show();
    }

    private void refreshCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof AccountListFragment) {
            ((AccountListFragment) currentFragment).refreshAccountList();
            Log.d(TAG, "AccountListFragment refreshed");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(fragment.getClass().getSimpleName());
        }

        transaction.commit();
        Log.d(TAG, "Fragment loaded: " + fragment.getClass().getSimpleName());
    }

    public void loadFragment(Fragment fragment) {
        loadFragment(fragment, false);
    }
}