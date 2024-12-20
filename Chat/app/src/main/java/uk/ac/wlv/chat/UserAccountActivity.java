package uk.ac.wlv.chat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import uk.ac.wlv.chat.database.DatabaseHelper;

public class UserAccountActivity extends AppCompatActivity {

    private TextView usernameTextView;
    private Button addFriendButton;
    private ListView listViewFriends;
    private FriendAdapter friendAdapter;

    private DatabaseHelper databaseHelper;
    private String loggedInUsername;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        usernameTextView = findViewById(R.id.textViewUsername);
        addFriendButton = findViewById(R.id.buttonAddFriend);
        listViewFriends = findViewById(R.id.listViewFriends);

        databaseHelper = new DatabaseHelper(this);

        // Retrieve the username passed from LoginActivity
        loggedInUsername = getIntent().getStringExtra("USERNAME");

        // Display the username in the TextView
        usernameTextView.setText("Welcome, " + loggedInUsername + "!");

        // Initialize ListView and Adapter
        friendAdapter = new FriendAdapter(this, new ArrayList<String>(), databaseHelper, loggedInUsername);

        listViewFriends.setAdapter(friendAdapter);

        // Display the friend list
        displayFriendList(loggedInUsername);

        // Set item click listener for future actions on friend items
        listViewFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Handle item click by opening the ChatActivity
                String selectedFriend = (String) parent.getItemAtPosition(position);
                openChatActivity(selectedFriend);
            }
        });

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptAddFriend();
            }
        });
        ImageButton logOutButton = findViewById(R.id.LogOutImageButton);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle logout button click
                navigateToLoginActivity();
            }
        });
        Button buttonAddBlog = findViewById(R.id.buttonAddBlog);
        buttonAddBlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle "BLOG" button click
                openBlogActivity();
            }
        });

    }
    private void navigateToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

        // Optional: If you want to finish the current activity when navigating to LoginActivity
        finish();
    }
    public void openChatActivity(String friendUsername) {
        Intent intent = new Intent(UserAccountActivity.this, ChatActivity.class);
        intent.putExtra("USERNAME", loggedInUsername);
        intent.putExtra("FRIEND_USERNAME", friendUsername);
        startActivity(intent);
    }

    private void displayFriendList(String loggedInUsername) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String[] projection = {DatabaseHelper.COLUMN_FRIEND_USERNAME};
        String selection = DatabaseHelper.COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {loggedInUsername};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_FRIENDS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        List<String> friends = new ArrayList<>();
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String friendUsername = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_FRIEND_USERNAME));
            friends.add(friendUsername);
        }
        // Check if there are users from the chat table (to whom the user has sent messages) but not in the friend's list
        List<String> chatFriends = getFriendsFromChatTable(loggedInUsername);
        for (String friend : chatFriends) {
            if (!friends.contains(friend)) {
                // Friend from the chat table is not in the list, add them
                long newRowId = databaseHelper.addFriend(loggedInUsername, friend);
                if (newRowId != -1) {
                    Toast.makeText(this, "Friend added automatically", Toast.LENGTH_SHORT).show();
                }
                friends.add(friend); // Add them to the list for display

            }
        }
        cursor.close();
        db.close();

        // Update the data in the adapter
        friendAdapter.updateData(friends, loggedInUsername);
    }
    private List<String> getFriendsFromChatTable(String loggedInUsername) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String[] projection = {DatabaseHelper.COLUMN_SENDER, DatabaseHelper.COLUMN_RECEIVER};
        String selection = DatabaseHelper.COLUMN_SENDER + " = ? OR " + DatabaseHelper.COLUMN_RECEIVER + " = ?";
        String[] selectionArgs = {loggedInUsername, loggedInUsername};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_CHAT,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        List<String> friends = new ArrayList<>();
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String sender = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SENDER));
            @SuppressLint("Range") String receiver = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIVER));

            // Add the user to the friend's list (excluding the current user)
            if (!sender.equals(loggedInUsername)) {
                friends.add(sender);
            }
            if (!receiver.equals(loggedInUsername)) {
                friends.add(receiver);
            }
        }

        cursor.close();
        db.close();

        return friends;
    }
    // Change the access modifier to public
    public void showRemoveFriendDialogWrapper(String friendUsername) {
        showRemoveFriendDialog(friendUsername);
    }

    // Private method to show the dialog
    private void showRemoveFriendDialog(final String friendUsername) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Friend");
        builder.setMessage("Are you sure you want to remove " + friendUsername + "?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeFriend(friendUsername);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void removeFriend(String friendUsername) {
        // Remove the friend from the UI list
        List<String> updatedFriends = new ArrayList<>(friendAdapter.getData());
        updatedFriends.remove(friendUsername);
        friendAdapter.updateData(updatedFriends, loggedInUsername);

        // Remove the friend from the database (TABLE_FRIENDS)
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String selectionFriends = DatabaseHelper.COLUMN_USERNAME + " = ? AND " + DatabaseHelper.COLUMN_FRIEND_USERNAME + " = ?";
        String[] selectionArgsFriends = {loggedInUsername, friendUsername};
        db.delete(DatabaseHelper.TABLE_FRIENDS, selectionFriends, selectionArgsFriends);

        // Remove only the logged user's side of the chat history
        String selectionChat = "(" + DatabaseHelper.COLUMN_SENDER + " = ? AND " + DatabaseHelper.COLUMN_RECEIVER + " = ?) OR (" +
                DatabaseHelper.COLUMN_SENDER + " = ? AND " + DatabaseHelper.COLUMN_RECEIVER + " = ?)";
        String[] selectionArgsChat = {loggedInUsername, friendUsername, friendUsername, loggedInUsername};
        db.delete(DatabaseHelper.TABLE_CHAT, selectionChat, selectionArgsChat);

        db.close();
    }

    private void promptAddFriend() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Friend");
        builder.setMessage("Enter friend's mobile number:");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String friendMobile = input.getText().toString().trim();
                if (!friendMobile.isEmpty()) {
                    checkAndAddFriend(friendMobile);
                } else {
                    Toast.makeText(UserAccountActivity.this, "Please enter a mobile number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void checkAndAddFriend(String friendMobile) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String[] projection = {DatabaseHelper.COLUMN_USER_ID, DatabaseHelper.COLUMN_USERNAME};
        String selection = DatabaseHelper.COLUMN_MOBILE + " = ?";
        String[] selectionArgs = {friendMobile};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USER,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            // Friend found in the database
            @SuppressLint("Range") final String friendUsername = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USERNAME));

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirm");
            builder.setMessage("Are you sure you want to add " + friendUsername + " as your friend?");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    addFriendToDatabase(friendUsername);
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        } else {
            // Friend not found in the database
            Toast.makeText(this, "User with mobile number " + friendMobile + " not found", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
        db.close();
    }
    private void addFriendToDatabase(String friendUsername) {
        long newRowId = databaseHelper.addFriend(loggedInUsername, friendUsername);

        if (newRowId != -1) {
            Toast.makeText(this, "Friend added successfully", Toast.LENGTH_SHORT).show();
            // Refresh the friend list after adding a friend
            displayFriendList(loggedInUsername);
        } else {
            Toast.makeText(this, "Failed to add friend", Toast.LENGTH_SHORT).show();
        }
    }
    private void openBlogActivity() {
        Intent intent = new Intent(UserAccountActivity.this, BlogActivity.class);
        intent.putExtra("USERNAME", loggedInUsername);
        startActivity(intent);
    }

}