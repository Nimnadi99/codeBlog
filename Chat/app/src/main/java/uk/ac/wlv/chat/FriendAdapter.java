package uk.ac.wlv.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import uk.ac.wlv.chat.database.DatabaseHelper;

public class FriendAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> friends;
    private DatabaseHelper databaseHelper;
    private String loggedInUsername;

    public FriendAdapter(Context context, List<String> friends, DatabaseHelper databaseHelper, String loggedInUsername) {
        super(context, R.layout.item_friend, friends);
        this.context = context;
        this.friends = friends;
        this.databaseHelper = databaseHelper;
        this.loggedInUsername = loggedInUsername;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.friendUsernameTextView = convertView.findViewById(R.id.textViewFriendUsername);
            viewHolder.newMessageIndicator = convertView.findViewById(R.id.newMessageIndicator);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Set data to views
        String friend = friends.get(position);
        viewHolder.friendUsernameTextView.setText(friend);

        // Check if there is an unread message for this friend
        boolean hasUnreadMessage = databaseHelper.hasUnreadMessage(loggedInUsername, friend);
        boolean isFriend = databaseHelper.isFriendshipExists(loggedInUsername, friend);

        // Add a label to indicate whether the message is from a friend or not
        String label = isFriend ? " (Friend)" : " (Unknown)";
        viewHolder.friendUsernameTextView.setText(friend + label);

        // Display the new message indicator for any unread message
        viewHolder.newMessageIndicator.setVisibility(hasUnreadMessage ? View.VISIBLE : View.GONE);
        // Inside FriendAdapter's getView method
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Access the UserAccountActivity to show the dialog
                if (context instanceof UserAccountActivity) {
                    ((UserAccountActivity) context).showRemoveFriendDialogWrapper(friend);
                }
                return true;
            }
        });
        // Inside FriendAdapter's getView method
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open ChatActivity
                if (context instanceof UserAccountActivity) {
                    ((UserAccountActivity) context).openChatActivity(friend);
                }
            }
        });


        return convertView;
    }
    // Inside FriendAdapter
    public List<String> getData() {
        return friends;
    }


    public void updateData(List<String> updatedFriends, String loggedInUsername) {
        friends.clear();
        friends.addAll(updatedFriends);
        this.loggedInUsername = loggedInUsername;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView friendUsernameTextView;
        View newMessageIndicator;
    }
}
