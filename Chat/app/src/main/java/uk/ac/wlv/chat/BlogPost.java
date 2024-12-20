package uk.ac.wlv.chat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;

public class BlogPost implements Parcelable {

    private long postId;
    private String senderUsername;
    private String title;
    private String content;
    private byte[] blogImage;
    private long timestamp;
    private String selectedImagePath;
    // Add this field to store the friend's username
    private String friendUsername;
    private boolean selected; // Add selected field

    public BlogPost() {
        // Empty constructor required by SQLite
    }

    public BlogPost(String senderUsername, String title, String content, Bitmap blogImage, long timestamp, String selectedImagePath, String friendUsername) {
        this.senderUsername = senderUsername;
        this.title = title;
        this.content = content;
        setBlogImage(blogImage);
        this.timestamp = timestamp;
        this.selectedImagePath = selectedImagePath;
        this.friendUsername = friendUsername;
    }

    protected BlogPost(Parcel in) {
        postId = in.readLong();
        senderUsername = in.readString();
        title = in.readString();
        content = in.readString();
        blogImage = in.createByteArray();
        timestamp = in.readLong();
        selectedImagePath = in.readString();
        friendUsername = in.readString();
    }

    public static final Creator<BlogPost> CREATOR = new Creator<BlogPost>() {
        @Override
        public BlogPost createFromParcel(Parcel in) {
            return new BlogPost(in);
        }

        @Override
        public BlogPost[] newArray(int size) {
            return new BlogPost[size];
        }
    };

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getSelectedImagePath() {
        return selectedImagePath;
    }

    public void setSelectedImagePath(String selectedImagePath) {
        this.selectedImagePath = selectedImagePath;
    }

    public void setBlogImage(Bitmap blogImage) {
        // Convert Bitmap to byte array
        if (blogImage != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            blogImage.compress(Bitmap.CompressFormat.PNG, 0, stream);
            this.blogImage = stream.toByteArray();
        } else {
            this.blogImage = null;
        }
    }
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Bitmap getBlogImage() {
        if (blogImage == null) {
            return null;
        }
        return BitmapFactory.decodeByteArray(blogImage, 0, blogImage.length);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(postId);
        dest.writeString(senderUsername);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeByteArray(blogImage);
        dest.writeLong(timestamp);
        dest.writeString(selectedImagePath);
        dest.writeString(friendUsername);
    }
}
