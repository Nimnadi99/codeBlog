package uk.ac.wlv.chat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uk.ac.wlv.chat.database.DatabaseHelper;

public class ChatAdapter extends ArrayAdapter<Message> {

    private List<Message> messages;
    private DatabaseHelper databaseHelper;
    private String loggedInUsername; // Add this instance variable
    private String searchQuery = ""; // Add this variable to store the search query



    public ChatAdapter(Context context, List<Message> messages, DatabaseHelper databaseHelper, String loggedInUsername) {
        super(context, 0, messages);
        this.messages = messages;
        this.databaseHelper = databaseHelper;
        this.loggedInUsername = loggedInUsername; // Initialize loggedInUsername
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_message, parent, false);
        }

        TextView textViewSender = convertView.findViewById(R.id.textViewSender);
        TextView textViewMessage = convertView.findViewById(R.id.textViewMessage);
        CheckBox checkboxSelect = convertView.findViewById(R.id.checkboxSelect);


        textViewSender.setText(message.getSender());
        textViewMessage.setText(message.getContent());
        // Set "You" for messages sent by the logged-in user
        if (message.getSender().equals(loggedInUsername)) {
            textViewSender.setText("You");
        } else {
            // Set the actual sender's name for messages sent by the friend
            textViewSender.setText(message.getSender());
        }

        textViewMessage.setText(message.getContent());

        // Check if the message sender is the logged-in user or the friend
        if (message.getSender().equals(loggedInUsername)) {
            // Set background for messages sent by the logged-in user
            textViewMessage.setBackgroundResource(R.drawable.sender);
            // Align text to the right for messages sent by the logged-in user
            textViewMessage.setGravity(Gravity.START | Gravity.LEFT);
            textViewSender.setGravity(Gravity.END | Gravity.RIGHT);
            // Set layout_width for messages sent by the logged-in user
            ViewGroup.LayoutParams params = textViewMessage.getLayoutParams();
            params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 247, getContext().getResources().getDisplayMetrics());
            textViewMessage.setLayoutParams(params);
            // Set layout_gravity to right for messages sent by the logged-in user
            ((LinearLayout.LayoutParams) textViewMessage.getLayoutParams()).gravity = Gravity.END;
            ImageView imageView = convertView.findViewById(R.id.imageViewAttachment);
            ((LinearLayout.LayoutParams) imageView.getLayoutParams()).gravity = Gravity.END;
            ImageView shareView = convertView.findViewById(R.id.imageViewShare);
            ((LinearLayout.LayoutParams) shareView.getLayoutParams()).gravity = Gravity.END;

        } else {
            // Set background for messages sent by the friend
            textViewMessage.setBackgroundResource(R.drawable.reciver);
            // Align text to the right for messages sent by the friend
            textViewMessage.setGravity(Gravity.START | Gravity.LEFT);
            textViewSender.setGravity(Gravity.START | Gravity.LEFT);

            // Set layout_width for messages sent by the friend
            ViewGroup.LayoutParams params = textViewMessage.getLayoutParams();
            params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 247, getContext().getResources().getDisplayMetrics());
            textViewMessage.setLayoutParams(params);
            // Set layout_gravity to left for messages sent by the friend
            ((LinearLayout.LayoutParams) textViewMessage.getLayoutParams()).gravity = Gravity.START;
            ImageView imageView = convertView.findViewById(R.id.imageViewAttachment);
            ((LinearLayout.LayoutParams) imageView.getLayoutParams()).gravity = Gravity.START;
            ImageView shareView = convertView.findViewById(R.id.imageViewShare);
            ((LinearLayout.LayoutParams) shareView.getLayoutParams()).gravity = Gravity.START;
        }

        // Add long click listener to show edit dialog
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Message clickedMessage = getItem(position);
                handleLongPress(clickedMessage);
                return true;
            }
        });
// In the ChatAdapter's getView method, where you load images using Glide
// Replace the existing code with this updated version
        ImageView imageView = convertView.findViewById(R.id.imageViewAttachment);

        if (message.getImagePath() != null && !message.getImagePath().isEmpty()) {
            // Load and display the image using Glide
            Glide.with(getContext())
                    .load(new File(message.getImagePath()))
                    .into(imageView);
            imageView.setVisibility(View.VISIBLE);
            textViewMessage.setVisibility(View.GONE);
        } else {
            imageView.setVisibility(View.GONE);
            textViewMessage.setVisibility(View.VISIBLE);
        }


        // Check if the message sender is the loggedInUser or friendUsername
        if (message.getSender().equals(loggedInUsername)) {
            checkboxSelect.setVisibility(View.VISIBLE); // Show checkbox for loggedInUser's messages
            checkboxSelect.setChecked(message.isSelected());
            checkboxSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleSelection(position);
                }
            });
            // Align checkbox to the right for messages sent by the loggedInUser
            ((LinearLayout.LayoutParams) checkboxSelect.getLayoutParams()).gravity = Gravity.END;
        } else {
            checkboxSelect.setVisibility(View.GONE); // Hide checkbox for friendUsername's messages
        }

        // Set "You" for messages sent by the loggedInUser
        if (message.getSender().equals(loggedInUsername)) {
            textViewSender.setText("You");

        } else {
            textViewSender.setText(message.getSender());
        }
        // Check if the message contains the search query and highlight it
        if (!searchQuery.isEmpty() && message.getContent().toLowerCase().contains(searchQuery.toLowerCase())) {
            String highlightedText = highlightText(message.getContent(), searchQuery);
            textViewMessage.setText(Html.fromHtml(highlightedText));
            convertView.setVisibility(View.VISIBLE); // Show the item
        } else if (searchQuery.isEmpty()) {
            // Display all messages before searching
            textViewMessage.setText(message.getContent());
            convertView.setVisibility(View.VISIBLE); // Show the item
        } else {
            // Hide the item for non-matching messages after searching
            convertView.setVisibility(View.GONE);
        }
        ImageView imageViewShare = convertView.findViewById(R.id.imageViewShare);
        imageViewShare.setTag(position);
        imageViewShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                Message message = getItem(position);
                handleShareButtonClick(message);
            }
        });


        return convertView;
    }

    // Add a method to highlight text in a message
    // Add a method to highlight text in a message
    private String highlightText(String text, String query) {
        return text.replaceAll("(?i)" + query, "<font color='#FF0000'>$0</font>");
    }

    private void handleLongPress(final Message message) {
        long currentTime = System.currentTimeMillis();
        long messageTime = message.getTimestamp();
        long elapsedTime = currentTime - messageTime;

        // Allow editing within a certain time window (e.g., 5 minutes)
        long editTimeWindow = 3 * 60 * 1000; // 3 minutes in milliseconds

        if (elapsedTime <= editTimeWindow) {
            if (message.getSender().equals(loggedInUsername)) {
                // Allow the sender to edit the message
                showEditDialog(message);
            } else {
                // Display an error Toast for unauthorized access
                Toast.makeText(getContext(), message.getSender() + " can only edit their own messages. You have no access.", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (!message.getSender().equals(loggedInUsername)) {
                Toast.makeText(getContext(), "You do not have permission to edit this message.", Toast.LENGTH_SHORT).show();
            } else {
                // Display an error Toast for editing too late
                Toast.makeText(getContext(), "Can't edit too late", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void showEditDialog(final Message message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Message");

        final EditText input = new EditText(getContext());
        input.setText(message.getContent());
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String editedContent = input.getText().toString();
                if (!editedContent.isEmpty()) {
                    // Update the message content in the database
                    updateMessageInDatabase(message, editedContent);

                    // Update the message in the adapter and refresh the list
                    message.setContent(editedContent);
                    notifyDataSetChanged();
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
    private void updateMessageInDatabase(Message message, String editedContent) {
        // Use the DatabaseHelper method to update the message in the database
        databaseHelper.updateMessageContent(message.getMessageId(), editedContent);
    }
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
    public void addMessage(Message message) {
        messages.add(message);
    }
    // Add a method to toggle the selection of a message
    public void toggleSelection(int position) {
        Message message = getItem(position);
        message.setSelected(!message.isSelected());
        notifyDataSetChanged();
    }

    // Add a method to delete selected messages from the database and the adapter
    public void deleteSelectedMessages() {
        List<Message> selectedMessages = new ArrayList<>();

        // Collect selected messages
        for (Message message : messages) {
            if (message.isSelected() && message.getSender().equals(loggedInUsername)) {
                selectedMessages.add(message);
            }
        }

        // Delete selected messages from the database
        for (Message selectedMessage : selectedMessages) {
            databaseHelper.deleteChatMessage(selectedMessage.getMessageId());
        }

        // Remove selected messages from the adapter
        messages.removeAll(selectedMessages);
        notifyDataSetChanged();
    }
    // Add a method to set the search query
    public void setSearchQuery(String query) {
        searchQuery = query;
        notifyDataSetChanged();
    }

    // Add a method to filter messages based on the search query
    private List<Message> getFilteredMessages() {
        List<Message> filteredMessages = new ArrayList<>();
        for (Message message : messages) {
            if (message.getContent().toLowerCase().contains(searchQuery.toLowerCase())) {
                filteredMessages.add(message);
            }
        }
        return filteredMessages;
    }
    private void handleShareButtonClick(Message message) {
        if (message != null) {
            if (message.getImagePath() != null && !message.getImagePath().isEmpty()) {
                // Share the image
                shareImage(message);
            } else {
                // Share the text
                shareText(message.getContent());
            }
        }
    }


    private void shareText(String text) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        getContext().startActivity(Intent.createChooser(shareIntent, "Share message"));
    }

    private void shareImage(Message message) {
        if (message.getImagePath() != null) {
            File imageFile = new File(message.getImagePath());
            Uri imageUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", imageFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            getContext().startActivity(Intent.createChooser(shareIntent, "Share Image"));
        }
    }
    public List<Message> getSelectedMessages() {
        List<Message> selectedMessages = new ArrayList<>();

        for (Message message : messages) {
            if (message.isSelected()) {
                selectedMessages.add(message);
            }
        }

        return selectedMessages;
    }

}
