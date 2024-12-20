package uk.ac.wlv.chat;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import uk.ac.wlv.chat.database.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton, loginBackButton;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.editTextUsernameLogin);
        passwordEditText = findViewById(R.id.editTextPasswordLogin);
        loginButton = findViewById(R.id.buttonLogin);
        loginBackButton = findViewById(R.id.loginBack);

        databaseHelper = new DatabaseHelper(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
        loginBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (checkCredentials(username, password)) {
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
            navigateToUserAccount(username);
        } else {
            Toast.makeText(this, "Invalid credentials. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkCredentials(String username, String password) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String[] projection = {DatabaseHelper.COLUMN_USERNAME, DatabaseHelper.COLUMN_PASSWORD};
        String selection = DatabaseHelper.COLUMN_USERNAME + " = ? AND " + DatabaseHelper.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USER,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean isValid = cursor.moveToFirst();
        cursor.close();
        db.close();
        return isValid;
    }

    private void navigateToUserAccount(String username) {
        // Pass the username to the UserAccountActivity (replace UserAccountActivity.class with your actual activity)
        Intent intent = new Intent(this, UserAccountActivity.class);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
        finish(); // Optional: finish the current activity
    }
}