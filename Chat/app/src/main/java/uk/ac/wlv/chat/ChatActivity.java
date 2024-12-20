package uk.ac.wlv.chat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.List;

import uk.ac.wlv.chat.database.DatabaseHelper;

public class ChatActivity extends AppCompatActivity {

    private String loggedInUsername;
    private String friendUsername;
    private DatabaseHelper databaseHelper;
    private ChatAdapter chatAdapter;
    private long timestamp;
    private static final int REQUEST_PICK_IMAGE = 1;
    private static final int REQUEST_CAPTURE_IMAGE = 2;

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 3;

    private String imagePath;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_CAMERA_CAPTURE = 2;
    private static final int REQUEST_GALLERY_PICK = 3;

    private List<Message> messages;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        loggedInUsername = getIntent().getStringExtra("USERNAME");
        friendUsername = getIntent().getStringExtra("FRIEND_USERNAME");

        if (loggedInUsername == null || friendUsername == null) {
            Toast.makeText(this, "Invalid user or friend", Toast.LENGTH_SHORT).show();
            finish();
        }
        // Find the textViewFriendUsername view and update its text
        TextView textViewFriendUsername = findViewById(R.id.textViewFriendUsername);
        textViewFriendUsername.setText(friendUsername);
        databaseHelper = new DatabaseHelper(this);

        setTitle("Chat with " + friendUsername);

        // Initialize ListView and Adapter
        ListView listViewChat = findViewById(R.id.listViewChat);
        List<Message> messages = loadChatMessages();
        chatAdapter = new ChatAdapter(this, messages, databaseHelper, loggedInUsername);
        listViewChat.setAdapter(chatAdapter);

        // Setup send button
        ImageButton sendButton = findViewById(R.id.buttonSend);
        final EditText editTextMessage = findViewById(R.id.editTextMessage);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = editTextMessage.getText().toString().trim();
                if (!messageText.isEmpty()) {
                    sendMessage(messageText, loggedInUsername, friendUsername, imagePath);
                    editTextMessage.getText().clear();
                } else {
                    // Handle the case where the message text is empty
                    Toast.makeText(ChatActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Setup delete button
        ImageButton deleteButton = findViewById(R.id.buttonDelete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the delete method in the adapter to delete selected messages
                chatAdapter.deleteSelectedMessages();
            }
        });
        ImageButton searchButton = findViewById(R.id.buttonSearchMessage);
        final EditText editTextSearch = findViewById(R.id.editTextSearchFind);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = editTextSearch.getText().toString().trim();
                chatAdapter.setSearchQuery(searchQuery);
            }
        });
        // Add a TextWatcher to the search bar
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // No action needed before text changes
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // No action needed as the user types
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // When the text is changed, update the search query and notify the adapter
                String searchQuery = editable.toString().trim();
                chatAdapter.setSearchQuery(searchQuery);
            }
        });
        ImageButton attachImageButton = findViewById(R.id.buttonAttachImage);
        attachImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageAttachmentDialog();
            }
        });
        ImageButton btnLoadTumblrPost = findViewById(R.id.btnloadTumblrPost);
        btnLoadTumblrPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get selected messages
                List<Message> selectedMessages = chatAdapter.getSelectedMessages();

                if (!selectedMessages.isEmpty()) {
                    // Call a method to upload selected messages to Tumblr
                    uploadSelectedMessagesToTumblr(selectedMessages);
                } else {
                    // Inform the user that no messages are selected
                    Toast.makeText(ChatActivity.this, "No messages selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void uploadSelectedMessagesToTumblr(List<Message> selectedMessages) {
        // Check network connectivity
        if (isNetworkConnected()) {
            // Create a TumblrUploader instance (assuming such class exists)
            TumblrUploader tumblrUploader = new TumblrUploader();

            // Iterate through selected messages and upload them
            for (Message message : selectedMessages) {
                tumblrUploader.uploadMessageToTumblr(this, message.getContent());

                // Update isSelected property after successful upload
                message.setSelected(false);
            }

            // Optionally, inform the user that the upload is complete
            Toast.makeText(ChatActivity.this, "Messages uploaded to Tumblr", Toast.LENGTH_SHORT).show();

            // After uploading, navigate to the Tumblr app or website
            navigateToTumblr();

            // Update the adapter after modifying the selected status
            chatAdapter.notifyDataSetChanged();
        } else {
            // Display a message to the user when the network is not available
            Toast.makeText(ChatActivity.this, "Network is not available. Unable to upload messages", Toast.LENGTH_SHORT).show();

            // Check if the network becomes available again
            // You can use a BroadcastReceiver to listen for network changes
            // For simplicity, here is a basic implementation using a runnable
            Runnable checkNetworkRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isNetworkConnected()) {
                        // Network is available again
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ChatActivity.this, "Back to device online", Toast.LENGTH_SHORT).show();
                            }
                        });

                        // Retry uploading the messages
                        uploadSelectedMessagesToTumblr(selectedMessages);
                    } else {
                        // Continue checking network status
                        // You can adjust the delay based on your requirements
                        // This example checks every 2 seconds
                        handler.postDelayed(this, 2000);
                    }
                }
            };

            // Start checking network status
            handler.postDelayed(checkNetworkRunnable, 2000);
        }
    }

    // Define a handler for delayed execution
    private final Handler handler = new Handler();

    // Check network connectivity
    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    // Add this method to navigate to the Tumblr app or website
    private void navigateToTumblr() {
        // You can use an implicit intent to open the Tumblr app or website
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.tumblr.com"));

        // Check if there is an app to handle the intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // If there is no app to handle the intent, you can open the Tumblr website in a browser
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.tumblr.com"));
            startActivity(intent);
        }
    }
    private void showImageAttachmentDialog() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    checkCameraPermissionAndCaptureImage();
                } else if (options[item].equals("Choose from Gallery")) {
                    checkGalleryPermissionAndPickImage();
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void checkCameraPermissionAndCaptureImage() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCameraCapture();
        } else {
            ActivityCompat.requestPermissions(ChatActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }


    private void checkGalleryPermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            launchGalleryPicker();
        } else {
            ActivityCompat.requestPermissions(ChatActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("Permission", "onRequestPermissionsResult: requestCode = " + requestCode);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCameraCapture();
            } else {
                Toast.makeText(getApplicationContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchGalleryPicker();
            } else {
                Toast.makeText(getApplicationContext(), "Gallery permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void launchCameraCapture() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_CAMERA_CAPTURE);

    }

    private void launchGalleryPicker() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_GALLERY_PICK);
    }

    private List<Message> loadChatMessages() {
        // Load chat messages from the database
        List<Message> messages = databaseHelper.getChatMessages(loggedInUsername, friendUsername);
        boolean isFriend = databaseHelper.isFriendshipExists(loggedInUsername, friendUsername);

        // Update the isFriend property for each message
        for (Message message : messages) {
            message.setFriend(isFriend);
        }

        return messages;
    }

    // Add this method to handle the result of image selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA_CAPTURE && data != null) {
                // Camera capture successful, get the image
                Bitmap capturedImage = (Bitmap) data.getExtras().get("data");
                handleImageAttachment(capturedImage);
            } else if (requestCode == REQUEST_GALLERY_PICK && data != null) {
                // Gallery pick successful, get the image URI
                Uri selectedImageUri = data.getData();
                handleImageAttachment(selectedImageUri);
            }
        }
    }

    // Add this method to handle the image attachment
    private void handleImageAttachment(Bitmap imageBitmap) {
        // Save the captured image to a file
        File imageFile = FileHelper.saveBitmapToFile(imageBitmap, this);

        // Display the image in the chat
        if (imageFile != null) {
            sendMessage(null, loggedInUsername, friendUsername, imageFile.getAbsolutePath());
        } else {
            Toast.makeText(this, "Failed to save image. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleImageAttachment(Uri imageUri) {
        if (imageUri != null) {
            // Use FileHelper to get a content URI
            String imagePath = FileHelper.getPathFromUri(this, imageUri);
            Toast.makeText(this, "Selected image: " + imagePath, Toast.LENGTH_SHORT).show();
            sendMessage(null, loggedInUsername, friendUsername, imagePath);
        } else {
            Toast.makeText(this, "Failed to get image. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // Add this method to get the file path from an image URI
    private String getImagePathFromUri(Uri uri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }

    // Update your sendMessage method in ChatActivity
// Update your sendMessage method in ChatActivity
    private void sendMessage(String messageText, String sender, String receiver, String imagePath) {
        boolean isFriend = databaseHelper.isFriendshipExists(sender, receiver);

        long newRowId = databaseHelper.insertChatMessage(sender, receiver, messageText, isFriend, imagePath);

        if (newRowId != -1) {
            // Set the status of the new message to unread (0)
            databaseHelper.updateMessageStatus(newRowId, 0);

            // Retrieve the messageId from the newRowId
            long messageId = newRowId;

            // Update the timestamp when sending a new message
            timestamp = System.currentTimeMillis();

            // If only an image is being sent, set messageText to an empty string
            if (messageText == null) {
                messageText = "";
            }

            // Add the new message to the list and update the adapter
            Message newMessage = new Message(messageId, sender, messageText, isFriend, timestamp, imagePath);
            chatAdapter.addMessage(newMessage);
            chatAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
        }
    }
}