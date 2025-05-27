package com.example.passwordmanager.data;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.method.PasswordTransformationMethod; // Import
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap; // Import

import androidx.annotation.NonNull;

import com.example.passwordmanager.R;
import com.example.passwordmanager.ui.MainActivity;
import com.example.passwordmanager.ui.AddAccountFragment;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountAdapter extends ArrayAdapter<Account> {
    private static final String TAG = "AccountAdapter";
    private final Context context;
    private final List<Account> accountList;
    private final HashMap<Integer, Boolean> passwordVisibilityState = new HashMap<>();


    public AccountAdapter(@NonNull Context context, List<Account> accounts) {
        super(context, 0,accounts);
        this.context = context;
        this.accountList = accounts;
    }

    private static class ViewHolder {
        TextView textTitle;
        TextView textUsername;
        TextView textWebsite;
        TextView textPassword;
        ImageButton btnCopyPassword;
        ImageButton btnEdit;
        ImageButton btnDelete;
        ImageButton btnTogglePasswordVisibility; // Tambahkan ini
    }


    @SuppressLint("CutPasteId")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        ViewHolder holder;
        Account account = accountList.get(position);

        // Jika belum ada view, buat dari XML
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.account_item, parent, false);
            holder = new ViewHolder();
            holder.textTitle = convertView.findViewById(R.id.text_title);
            holder.textUsername = convertView.findViewById(R.id.text_username);
            holder.textWebsite = convertView.findViewById(R.id.text_website);
            holder.textPassword = convertView.findViewById(R.id.text_password);
            holder.btnCopyPassword = convertView.findViewById(R.id.button_copy);
            holder.btnEdit = convertView.findViewById(R.id.button_edit);
            holder.btnDelete = convertView.findViewById(R.id.button_delete);
            holder.btnTogglePasswordVisibility = convertView.findViewById(R.id.btn_toggle_password_visibility); // Inisialisasi
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Set data
        if (account != null) {
            holder.textTitle.setText(account.title);
            holder.textWebsite.setText(account.website);
            holder.textUsername.setText(account.username);
            holder.textPassword.setText(account.password);

            // Atur status visibilitas awal berdasarkan state map atau default (tersembunyi)
            boolean isPasswordVisible = Boolean.TRUE.equals(passwordVisibilityState.getOrDefault(account.id, false));
            setPasswordVisibility(holder.textPassword, holder.btnTogglePasswordVisibility, isPasswordVisible);

            holder.btnCopyPassword.setOnClickListener(v -> copyPassword(account));
            holder.btnEdit.setOnClickListener(v -> editAccount(account));
            holder.btnDelete.setOnClickListener(v -> showDeleteConfirmation(account, position));

            // Listener untuk tombol toggle visibilitas password
            holder.btnTogglePasswordVisibility.setOnClickListener(v -> {
                boolean currentVisibility = Boolean.TRUE.equals(passwordVisibilityState.getOrDefault(account.id, false));
                boolean newVisibility = !currentVisibility;
                passwordVisibilityState.put(account.id, newVisibility);
                setPasswordVisibility(holder.textPassword, holder.btnTogglePasswordVisibility, newVisibility);
            });
        } else {
            Log.w(TAG, "Account object is null at position: " + position);
        }
        return convertView;
    }

    private void setPasswordVisibility(TextView passwordTextView, ImageButton toggleButton, boolean isVisible) {
        if (isVisible) {
            passwordTextView.setTransformationMethod(null); // Tampilkan teks asli
            toggleButton.setImageResource(R.drawable.ic_visibility_off); // Ikon mata dicoret
        } else {
            passwordTextView.setTransformationMethod(PasswordTransformationMethod.getInstance()); // Sembunyikan (titik-titik)
            toggleButton.setImageResource(R.drawable.ic_visibility_on); // Ikon mata terbuka
        }
    }

    private void copyPassword(Account account) {
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            assert account != null;
            ClipData clip = ClipData.newPlainText("password", account.password);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Password disalin", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Password copied for account: " + account.title);
        } catch (Exception e) {
            Log.e(TAG, "Error copying password", e);
            Toast.makeText(context, "Gagal menyalin password", Toast.LENGTH_SHORT).show();
        }
    }

    private void editAccount(Account account) {
        try {
            if (context instanceof MainActivity) {
                assert account != null;
                ((MainActivity) context).loadFragment(AddAccountFragment.newInstance(account), true);
                Log.d(TAG, "Edit clicked for account: " + account.title);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening edit fragment", e);
            Toast.makeText(context, "Gagal membuka form edit", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation(final Account account, final int position) {
        // ... (kode showDeleteConfirmation yang sudah ada)
        new android.app.AlertDialog.Builder(context)
                .setTitle("Hapus Akun")
                .setMessage("Anda yakin ingin menghapus akun '" + account.title + "'?")
                .setPositiveButton("Hapus", (dialog, which) -> deleteAccount(account, position))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteAccount(Account account, int position) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Log.d(TAG, "Deleting account: " + account.title);

                // Delete from database
                AppDatabase.getInstance(context).accountDao().delete(account);
                Log.d(TAG, "Account deleted from database: " + account.title);

                // Update UI on main thread
                if (context instanceof MainActivity) {
                    ((MainActivity) context).runOnUiThread(() -> {
                        try {
                            // Remove from list
                            accountList.remove(position);
                            notifyDataSetChanged();

                            Toast.makeText(context, "Akun '" + account.title + "' dihapus", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "UI updated after delete");
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating UI after delete", e);
                        }
                    });
                }

            } catch (Exception e) {
                Log.e(TAG, "Error deleting account", e);
                if (context instanceof MainActivity) {
                    ((MainActivity) context).runOnUiThread(() -> Toast.makeText(context, "Gagal menghapus akun", Toast.LENGTH_SHORT).show());
                }
            } finally {
                executor.shutdown();
            }
        });
    }

}
