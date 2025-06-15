package com.example.passwordmanager.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.passwordmanager.R;
import com.example.passwordmanager.data.Account;
import com.example.passwordmanager.data.AppDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AddAccountFragment extends Fragment {

    private static final String TAG = "AddAccountFragment";
    private EditText inputTitle, inputWebsite, inputUsername, inputPassword;
    private int accountId = -1;
    private MainSharedViewModel sharedViewModel; // Tambahkan SharedViewModel


    public static AddAccountFragment newInstance(Account account) {
        AddAccountFragment fragment = new AddAccountFragment();
        Bundle args = new Bundle();
        args.putInt("id", account.id);
        args.putString("title", account.title);
        args.putString("website", account.website);
        args.putString("username", account.username);
        args.putString("password", account.password);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inisialisasi ViewModel yang di-scope ke Activity (MainActivity)
        sharedViewModel = new ViewModelProvider(requireActivity()).get(MainSharedViewModel.class);
    }


    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "Creating AddAccountFragment view");

        View view = inflater.inflate(R.layout.fragment_add_account, container, false);

        inputTitle = view.findViewById(R.id.input_title);
        inputWebsite = view.findViewById(R.id.input_website);
        inputUsername = view.findViewById(R.id.input_username);
        inputPassword = view.findViewById(R.id.input_password);
        Button saveButton = view.findViewById(R.id.btn_save);
        Button cancelButton = view.findViewById(R.id.btn_cancel);


        // Load data untuk edit mode
        loadArgumentsData();

        saveButton.setOnClickListener(v -> {
            String title = inputTitle.getText().toString().trim();
            String website = inputWebsite.getText().toString().trim();
            String username = inputUsername.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            Log.d(TAG, "Save button clicked");
            Log.d(TAG, "Title: " + title + "Website: " + website + ", Username: " + username);

            if (!title.isEmpty() && !website.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
                saveAccount(title, website, username, password);
            } else {
                showToastOnUiThread("Semua Kolom harus diisi");
            }
        });
        cancelButton.setOnClickListener(v -> {
            // Kembali ke halaman utama
            if (getActivity() != null && !getActivity().isFinishing()) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });


        return view;
    }
    private void loadArgumentsData() {
        Bundle args = getArguments();
        if (args != null) {
            accountId = args.getInt("id", -1);
            String title = args.getString("title", "");
            String website = args.getString("website", "");
            String username = args.getString("username", "");
            String password = args.getString("password", "");

            inputTitle.setText(title);
            inputWebsite.setText(website);
            inputUsername.setText(username);
            inputPassword.setText(password);

            Log.d(TAG, "Loaded edit data for account ID: " + accountId);
        }
    }

    private void saveAccount(String title, String website, String username, String password) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Log.d(TAG, "Starting database operation");
                AppDatabase db = AppDatabase.getInstance(getContext()); // Pastikan getContext() aman di sini
                boolean isUpdate = (accountId != -1);
                Log.d(TAG, "Database instance created. Is update: " + isUpdate);

                Account accountToSave;
                accountToSave = new Account(); // Gunakan constructor default untuk Room
                // Gunakan constructor default untuk Room
                if (isUpdate) {
                    accountToSave.id = accountId;
                    accountToSave.title = title;
                    accountToSave.website = website;
                    accountToSave.username = username;
                    accountToSave.password = password;
                    db.accountDao().update(accountToSave);
                    Log.d(TAG, "Account updated in database with ID: " + accountId);
                } else {
                    accountToSave.title = title;
                    accountToSave.website = website;
                    accountToSave.username = username;
                    accountToSave.password = password;
                    db.accountDao().insert(accountToSave);
                    Log.d(TAG, "New account inserted to database");
                }

                // Kirim notifikasi melalui ViewModel di UI thread
                if (getActivity() != null && !getActivity().isFinishing()) {
                    // Perlu final untuk digunakan di lambda
                    // Perlu final
                    getActivity().runOnUiThread(() -> {
                        // Gunakan SharedViewModel untuk mengirim event
                        sharedViewModel.notifyAccountSaved(isUpdate, title);
                        Log.d(TAG, "Notified ViewModel successfully: " + (isUpdate ? "UPDATE" : "INSERT") + " for " + title);
                        navigateBack();
                    });
                }

            } catch (Exception e) {
                Log.e(TAG, "Error saving account", e);
                showToastOnUiThread("Error: " + e.getMessage());
            } finally {
                executor.shutdown();
            }
        });
    }

    private void navigateBack() {
        try {
            if (getActivity() != null && !getActivity().isFinishing()) {
                getActivity().getSupportFragmentManager().popBackStack();
                Log.d(TAG, "Navigated back successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navigating back", e);
        }
    }


    private void showToastOnUiThread(String message) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().runOnUiThread(() -> {
                try {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "Error showing toast", e);
                }
            });
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Fragment destroyed");
    }
}