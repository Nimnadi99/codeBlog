package uk.ac.wlv.chat;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import uk.ac.wlv.chat.database.DatabaseHelper;

public class RegistrationActivity extends AppCompatActivity {

    private EditText usernameEditText, mobileEditText, passwordEditText;
    private Button registerButton, registerLoginButton;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        usernameEditText = findViewById(R.id.editTextUsername);
        mobileEditText = findViewById(R.id.editTextMobile);
        passwordEditText = findViewById(R.id.editTextPassword);
        registerButton = findViewById(R.id.buttonRegister);
        registerLoginButton = findViewById(R.id.registerLogin); // Initialize the "LOGIN" button

        databaseHelper = new DatabaseHelper(this);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
        registerLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLoginActivity();
            }
        });
    }

    private void registerUser() {
        String username = usernameEditText.getText().toString().trim();
        String mobile = mobileEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || mobile.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the username already exists
        if (databaseHelper.isUsernameExists(username)) {
            Toast.makeText(this, "Username already exists, please choose a different one", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the mobile number already exists
        if (databaseHelper.isMobileExists(mobile)) {
            Toast.makeText(this, "Mobile number already registered, please use a different one", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register the user
        long newRowId = databaseHelper.insertUser(username, mobile, password);

        if (newRowId != -1) {
            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
            navigateToLoginActivity();
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }


    private void navigateToLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
        finish(); // Optional: finish the current activity so the user cannot go back to the registration screen using the back button
    }
}