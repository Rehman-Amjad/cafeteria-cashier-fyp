package com.technogenis.cafeteriacashier;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technogenis.cafeteriacashier.admin.AdminDashboardActivity;
import com.technogenis.cafeteriacashier.util.EdgeToEdgeHelper;
import com.technogenis.cafeteriacashier.util.FirebasePathSanitizer;

public class LoginActivity extends AppCompatActivity {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    private MaterialButton btnLogin, btnAdmin;
    private EditText etUserName, etUserPassword;
    private View root;
    private MyPreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        preferenceManager = MyPreferenceManager.getInstance(this);

        root = findViewById(R.id.main);
        btnLogin = findViewById(R.id.btnLogin);
        btnAdmin = findViewById(R.id.btnAdmin);
        etUserName = findViewById(R.id.etUserName);
        etUserPassword = findViewById(R.id.etUserPassword);

        EdgeToEdgeHelper.applySystemBarsPadding(root, true, true);

        btnAdmin.setOnClickListener(v -> attemptAdminLogin());
        btnLogin.setOnClickListener(v -> attemptCustomerLogin());
    }

    private void attemptAdminLogin() {
        String username = etUserName.getText().toString().trim();
        String password = etUserPassword.getText().toString();

        if (!ADMIN_USERNAME.equals(username)) {
            showError(etUserName, getString(R.string.error_incorrect_username));
            return;
        }
        if (!ADMIN_PASSWORD.equals(password)) {
            showError(etUserPassword, getString(R.string.error_incorrect_password));
            return;
        }

        startActivity(new Intent(this, AdminDashboardActivity.class));
        finish();
    }

    private void attemptCustomerLogin() {
        String username = etUserName.getText().toString().trim();
        String password = etUserPassword.getText().toString();

        if (username.isEmpty()) {
            showError(etUserName, getString(R.string.error_enter_username));
            return;
        }
        if (!FirebasePathSanitizer.isValid(username)) {
            showError(etUserName, getString(R.string.error_invalid_username));
            return;
        }
        if (password.isEmpty()) {
            showError(etUserPassword, getString(R.string.error_enter_pin));
            return;
        }

        setBusy(true);
        handleLogin(username, password);
    }

    private void handleLogin(final String username, final String password) {
        DatabaseReference callRef = FirebaseDatabase.getInstance()
                .getReference("customers")
                .child(username);

        callRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                setBusy(false);
                if (!snapshot.exists()) {
                    Toast.makeText(LoginActivity.this,
                            R.string.error_user_not_found, Toast.LENGTH_SHORT).show();
                    return;
                }
                // FYP: PIN compared in plaintext against RTDB. See util/PinHash for an
                // opt-in hashing helper to use once you migrate the DB.
                String checkPassword = snapshot.child("customerPin").getValue(String.class);
                if (checkPassword == null) {
                    Toast.makeText(LoginActivity.this,
                            R.string.error_account_misconfigured, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!checkPassword.equals(password)) {
                    Toast.makeText(LoginActivity.this,
                            R.string.error_wrong_pin, Toast.LENGTH_SHORT).show();
                    return;
                }

                preferenceManager.putString("rfid", username);
                startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setBusy(false);
                Log.e("LoginActivity", "Login query cancelled", error.toException());
                Snackbar.make(root, error.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void setBusy(boolean busy) {
        btnLogin.setEnabled(!busy);
        btnAdmin.setEnabled(!busy);
        etUserName.setEnabled(!busy);
        etUserPassword.setEnabled(!busy);
    }

    private void showError(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
    }
}
