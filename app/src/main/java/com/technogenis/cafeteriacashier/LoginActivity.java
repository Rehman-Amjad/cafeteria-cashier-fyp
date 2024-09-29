package com.technogenis.cafeteriacashier;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technogenis.cafeteriacashier.admin.AdminDashboardActivity;

public class LoginActivity extends AppCompatActivity {

    Button btnLogin,btnAdmin;
    EditText etUserName,etUserPassword;
    String username, password;
    private MyPreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        preferenceManager = MyPreferenceManager.getInstance(this);

        btnLogin=findViewById(R.id.btnLogin);
        etUserName=findViewById(R.id.etUserName);
        etUserPassword=findViewById(R.id.etUserPassword);
        btnAdmin=findViewById(R.id.btnAdmin);

        btnAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username="admin";
                String userpassword="admin";

                if (username.equals(etUserName.getText().toString()))
                {
                    if (userpassword.equals(etUserPassword.getText().toString()))
                    {
                        Intent intent=new Intent(LoginActivity.this, AdminDashboardActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        etUserPassword.setError("incorrect Password");
                        etUserPassword.requestFocus();
                        etUserPassword.setText("");
                    }
                }
                else
                {
                    etUserName.setError("incorrect username");
                    etUserName.requestFocus();
                    etUserName.setText("");
                }
            }
        });

        btnLogin.setOnClickListener(v -> {

            username = etUserName.getText().toString().trim();
            password = etUserPassword.getText().toString().trim();

            if (username.isEmpty()) {
                showError(etUserName, "Please enter your username");
                return;
            }

            if (password.isEmpty()) {
                showError(etUserPassword, "Please enter your password");
                return;
            }

            handleLogin(username, password);
         });



    }

    private void handleLogin(String username, String password) {
        DatabaseReference callRef = FirebaseDatabase.getInstance()
                .getReference("customers")
                .child(username);

        callRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String  checkPassword = snapshot.child("customerPin").getValue(String.class);

                    assert checkPassword != null;
                    if(checkPassword.equals(password)){
                        preferenceManager.putString("rfid",etUserName.getText().toString().trim());
                        startActivity(new Intent(LoginActivity.this,MainActivity.class));
                    }else{
                        Toast.makeText(LoginActivity.this,"Wrong Pin",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this,"User not found",Toast.LENGTH_SHORT).show();
                    Log.w("Firebase", "Data snapshot doesn't exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Data fetch cancelled", error.toException());
            }
        });
    }

    private void showError(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
        field.setText("");
    }
}