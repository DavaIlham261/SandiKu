// File: com/example/passwordmanager/ui/MainSharedViewModel.java
package com.example.passwordmanager.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainSharedViewModel extends ViewModel {

    // Event untuk menandakan akun disimpan/diperbarui
    public static class AccountSavedEvent {
        public final boolean isUpdate;
        public final String accountTitle;

        public AccountSavedEvent(boolean isUpdate, String accountTitle) {
            this.isUpdate = isUpdate;
            this.accountTitle = accountTitle;
        }
    }

    private final MutableLiveData<AccountSavedEvent> _accountSavedEvent = new MutableLiveData<>();
    public LiveData<AccountSavedEvent> accountSavedEvent = _accountSavedEvent;

    public void notifyAccountSaved(boolean isUpdate, String accountTitle) {
        _accountSavedEvent.setValue(new AccountSavedEvent(isUpdate, accountTitle));
    }

    // Anda bisa menambahkan event lain di sini jika diperlukan
}