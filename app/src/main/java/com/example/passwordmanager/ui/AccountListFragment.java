package com.example.passwordmanager.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.passwordmanager.R;
import com.example.passwordmanager.data.Account;
import com.example.passwordmanager.data.AccountAdapter;
import com.example.passwordmanager.data.AppDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AccountListFragment extends Fragment {

    private static final String TAG = "AccountListFragment";
    private AccountAdapter adapter;
    private List<Account> accountList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Fragment created");
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "Creating AccountListFragment view");

        View view = inflater.inflate(R.layout.fragment_account_list, container, false);
        ListView listView = view.findViewById(R.id.account_list);
        Button addButton = view.findViewById(R.id.add_button);

        // Initialize display list
        accountList = new ArrayList<>();
        adapter = new AccountAdapter(requireContext(), accountList);
        listView.setAdapter(adapter);

        addButton.setOnClickListener(v -> {
            Log.d(TAG, "Add button clicked");
            try {
                ((MainActivity) requireActivity()).loadFragment(new AddAccountFragment(), true);
                Log.d(TAG, "Navigated to AddAccountFragment via MainActivity");
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to AddAccountFragment", e);
            }
        });

        loadAccounts();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Fragment resumed, refreshing data");
        loadAccounts();
    }

    // Method publik untuk refresh dari MainActivity
    public void refreshAccountList() {
        Log.d(TAG, "Refresh requested from MainActivity");
        loadAccounts();
    }

    private void loadAccounts() {
        Log.d(TAG, "Loading accounts from database");

        if (getContext() == null) {
            Log.w(TAG, "Context is null, cannot load accounts");
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getContext());
                List<Account> accounts = db.accountDao().getAll();
                Log.d(TAG, "Loaded " + accounts.size() + " accounts from database");

                if (getActivity() != null && !getActivity().isFinishing()) {
                    getActivity().runOnUiThread(() -> {
                        try {
                            accountList.clear();
                            accountList.addAll(accounts);

                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                                Log.d(TAG, "UI updated with " + accountList.size() + " items");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating UI", e);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading accounts", e);
            } finally {
                executor.shutdown();
            }
        });
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Fragment destroyed");
    }
}