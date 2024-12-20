package uk.ac.wlv.chat;

public class Message {
    private String sender;
    private String content;
    private long timestamp;
    private boolean isFriend;
    private long messageId;
    private String senderUsername;  // Add this field
    private boolean isSelected;
    private String imagePath;

    // Constructor with imagePath
    public Message(long messageId, String sender, String content, boolean isFriend, long timestamp, String imagePath) {
        this.messageId = messageId;
        this.sender = sender;
        this.content = content;
        this.isFriend = isFriend;
        this.timestamp = timestamp;
        this.imagePath = imagePath;  // Add this line
    }
    public String getImagePath() {
        return imagePath;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    public String getSender() {
        return sender;
    }
    public long getMessageId() {
        return messageId;
    }
    public String getSenderUsername() {
        return senderUsername;
    }
    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }
}
