package uk.ac.wlv.chat.database;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import uk.ac.wlv.chat.BlogActivity;
import uk.ac.wlv.chat.BlogPost;
import uk.ac.wlv.chat.Message;
import uk.ac.wlv.chat.TumblrUploader;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ChatApp.db";
    private static final int DATABASE_VERSION = 3;  // Incremented version to trigger an upgrade

    // User table
    public static final String TABLE_USER = "user";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_MOBILE = "mobile";
    public static final String COLUMN_PASSWORD = "password";

    // Friends table
    public static final String TABLE_FRIENDS = "friends";
    public static final String COLUMN_FRIEND_ID = "id";
    public static final String COLUMN_FRIEND_USERNAME = "friend_username";

    // Chat table
    public static final String TABLE_CHAT = "chat";
    public static final String COLUMN_CHAT_ID = "id";
    public static final String COLUMN_SENDER = "sender_username";
    public static final String COLUMN_RECEIVER = "receiver_username";
    public static final String COLUMN_MESSAGE_CONTENT = "message_content";
    public static final String TABLE_MESSAGES = "messages";
    public static final String COLUMN_MESSAGE_ID = "message_id";
    public static final String COLUMN_SENDER_USERNAME = "sender_username";
    public static final String COLUMN_RECEIVER_USERNAME = "receiver_username";
    public static final String COLUMN_MESSAGE_TEXT = "message_text";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_STATUS = "status";
    // Add this column to the chat table
    public static final String COLUMN_IS_FRIEND = "is_friend";
    // Add this column to the chat table
    public static final String COLUMN_IMAGE_PATH = "image_path";

    // Blog post table
    public static final String TABLE_BLOG_POST = "blog_post";
    public static final String COLUMN_POST_ID = "post_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_BLOG_IMAGE = "blog_image";
    private static final String TABLE_BLOG_POSTS = "blog_posts";
    public static final String COLUMN_SELECTED_IMAGE_PATH = "selected_image_path";
    // Inside your DatabaseHelper class
    private Context context;  // Add this line
    private static DatabaseHelper instance;


    // Create friends table query
    private static final String CREATE_TABLE_FRIENDS = "CREATE TABLE " + TABLE_FRIENDS +
            "(" + COLUMN_FRIEND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USERNAME + " TEXT, " +  // Add this line to include the username column
            COLUMN_FRIEND_USERNAME + " TEXT);";

    // Create user table query
    private static final String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER +
            "(" + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USERNAME + " TEXT, " +
            COLUMN_MOBILE + " TEXT, " +
            COLUMN_PASSWORD + " TEXT);";

    // Create chat table query
    public static final String CREATE_TABLE_MESSAGES = "CREATE TABLE " + TABLE_MESSAGES + " (" +
            COLUMN_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_SENDER_USERNAME + " TEXT," +
            COLUMN_RECEIVER_USERNAME + " TEXT," +
            COLUMN_MESSAGE_TEXT + " TEXT," +
            COLUMN_TIMESTAMP + " TEXT," +
            COLUMN_STATUS + " INTEGER DEFAULT 0" + // 0 for unread, 1 for read
            ")";
    // Modify the create chat table query
    public static final String CREATE_TABLE_CHAT = "CREATE TABLE " + TABLE_CHAT + " (" +
            COLUMN_CHAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_SENDER + " TEXT," +
            COLUMN_RECEIVER + " TEXT," +
            COLUMN_MESSAGE_CONTENT + " TEXT," +
            COLUMN_TIMESTAMP + " TEXT," +
            COLUMN_STATUS + " INTEGER DEFAULT 0," +
            COLUMN_IS_FRIEND + " INTEGER DEFAULT 0," +
            COLUMN_IMAGE_PATH + " TEXT" + // Add this line for image paths
            ")";

    // Create table query
    // SQL statement to create the blog_posts table
    private static final String CREATE_TABLE_BLOG_POSTS =
            "CREATE TABLE " + TABLE_BLOG_POSTS + " (" +
                    COLUMN_POST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_SENDER_USERNAME + " TEXT," +
                    COLUMN_TITLE + " TEXT," +
                    COLUMN_CONTENT + " TEXT," +
                    COLUMN_BLOG_IMAGE + " BLOB," +
                    COLUMN_TIMESTAMP + " INTEGER," +
                    COLUMN_SELECTED_IMAGE_PATH + " TEXT" +
                    ")";
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;  // Add this line

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL(CREATE_TABLE_USER);
        db.execSQL(CREATE_TABLE_FRIENDS);
        db.execSQL(CREATE_TABLE_MESSAGES);
        db.execSQL(CREATE_TABLE_CHAT); // Add this line to create the chat table
        db.execSQL(CREATE_TABLE_BLOG_POSTS); // Add this line to create the blog_post table

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLOG_POST); // Use the correct table name


        // Create tables again
        onCreate(db);
    }

    // Method to insert a new user into the database
    public long insertUser(String username, String mobile, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_MOBILE, mobile);
        values.put(COLUMN_PASSWORD, password);
        long newRowId = db.insert(TABLE_USER, null, values);
        db.close();
        return newRowId;
    }

    // Method to check user credentials during login
    public boolean checkCredentials(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {COLUMN_USERNAME, COLUMN_PASSWORD};
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(
                TABLE_USER,
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
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {COLUMN_USERNAME};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(
                TABLE_USER,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    public boolean isMobileExists(String mobile) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {COLUMN_MOBILE};
        String selection = COLUMN_MOBILE + " = ?";
        String[] selectionArgs = {mobile};

        Cursor cursor = db.query(
                TABLE_USER,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    public long addFriend(String loggedInUsername, String friendUsername) {
        SQLiteDatabase db = null;
        long newRowId = -1;

        try {
            db = this.getWritableDatabase();

            // Check if the friendship already exists
            if (!isFriendshipExists(db, loggedInUsername, friendUsername)) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_USERNAME, loggedInUsername);
                values.put(COLUMN_FRIEND_USERNAME, friendUsername);
                newRowId = db.insert(TABLE_FRIENDS, null, values);
            } else {
                // Friendship already exists, handle accordingly
            }
        } catch (Exception e) {
            // Handle the exception, log, or throw it as needed
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return newRowId;
    }

    private boolean isFriendshipExists(SQLiteDatabase db, String loggedInUsername, String friendUsername) {
        String query = "SELECT * FROM " + TABLE_FRIENDS +
                " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_FRIEND_USERNAME + " = ?";
        String[] selectionArgs = {loggedInUsername, friendUsername};

        Cursor cursor = null;
        boolean friendshipExists = false;

        try {
            cursor = db.rawQuery(query, selectionArgs);
            friendshipExists = cursor.getCount() > 0;
        } catch (Exception e) {
            // Handle the exception, log, or throw it as needed
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return friendshipExists;
    }

    public boolean isFriendshipExists(String loggedInUsername, String friendUsername) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {COLUMN_USERNAME, COLUMN_FRIEND_USERNAME};
        String selection = "(" + COLUMN_USERNAME + " = ? AND " + COLUMN_FRIEND_USERNAME + " = ?) OR (" +
                COLUMN_USERNAME + " = ? AND " + COLUMN_FRIEND_USERNAME + " = ?)";
        String[] selectionArgs = {loggedInUsername, friendUsername, friendUsername, loggedInUsername};

        Cursor cursor = db.query(
                TABLE_FRIENDS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    public long insertChatMessage(String sender, String receiver, String content) {
        return insertChatMessage(sender, receiver, content, false, null);
    }

    public long insertChatMessage(String sender, String receiver, String content, boolean isFriend, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the sender-receiver pair already exists in the friend's table
        if (!isFriend) {
            ContentValues friendValues = new ContentValues();
            friendValues.put(COLUMN_USERNAME, receiver);
            friendValues.put(COLUMN_FRIEND_USERNAME, sender);

            // If the pair doesn't exist, insert it as a friend in the recipient's friend list
            if (!isFriendshipExists(db, receiver, sender)) {
                db.insert(TABLE_FRIENDS, null, friendValues);
            }
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_SENDER, sender);
        values.put(COLUMN_RECEIVER, receiver);

        // Check if it's a text message or an image message
        if (imagePath != null && !imagePath.isEmpty()) {
            values.put(COLUMN_MESSAGE_CONTENT, ""); // Set an empty string for image messages
            values.put(COLUMN_IMAGE_PATH, imagePath); // Set the image path
        } else {
            values.put(COLUMN_MESSAGE_CONTENT, content); // Set the text message content
        }

        values.put(COLUMN_IS_FRIEND, isFriend ? 1 : 0);
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());

        // Insert the message into the chat table with the timestamp
        long newRowId = db.insert(TABLE_CHAT, null, values);
        db.close();
        return newRowId;
    }

    public List<Message> getChatMessages(String loggedInUsername, String friendUsername) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {COLUMN_CHAT_ID, COLUMN_SENDER, COLUMN_MESSAGE_CONTENT, COLUMN_IS_FRIEND, COLUMN_TIMESTAMP, COLUMN_IMAGE_PATH};
        String selection = "(" + COLUMN_SENDER + " = ? AND " + COLUMN_RECEIVER + " = ?) OR (" +
                COLUMN_SENDER + " = ? AND " + COLUMN_RECEIVER + " = ?)";
        String[] selectionArgs = {loggedInUsername, friendUsername, friendUsername, loggedInUsername};

        Cursor cursor = db.query(
                TABLE_CHAT,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                COLUMN_TIMESTAMP + " ASC"
        );

        List<Message> messages = new ArrayList<>();
        while (cursor.moveToNext()) {
            long messageId = cursor.getLong(cursor.getColumnIndex(COLUMN_CHAT_ID));
            String messageSender = cursor.getString(cursor.getColumnIndex(COLUMN_SENDER));
            String messageContent = cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_CONTENT));
            boolean isFriend = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_FRIEND)) == 1;
            long timestamp = cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP));
            String imagePath = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_PATH));  // Add this line

            // Check if the sender is a friend or not and label accordingly
            messages.add(new Message(messageId, messageSender, messageContent, isFriend, timestamp, imagePath));
        }

        cursor.close();
        db.close();

        return messages;
    }

    public void updateMessageStatus(long messageId, int status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, status);

        String selection = COLUMN_MESSAGE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(messageId)};

        db.update(TABLE_MESSAGES, values, selection, selectionArgs);

        db.close();
    }
// Add this method in your DatabaseHelper class

    public boolean hasUnreadMessage(String loggedInUsername, String friendUsername) {
        SQLiteDatabase db = getReadableDatabase();

        // Query to check if there is any unread message for the given friend
        String query = "SELECT COUNT(*) FROM " + TABLE_MESSAGES +
                " WHERE " + COLUMN_SENDER_USERNAME + " = ?" +
                " AND " + COLUMN_RECEIVER_USERNAME + " = ?" +
                " AND " + COLUMN_STATUS + " = 0"; // Assuming 0 represents unread status

        String[] selectionArgs = {loggedInUsername, friendUsername};

        try (Cursor cursor = db.rawQuery(query, selectionArgs)) {
            if (cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                return count > 0;
            }
        } finally {
            db.close();
        }

        return false;
    }
    public void updateMessageContent(long messageId, String editedContent) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_MESSAGE_CONTENT, editedContent);

        String selection = COLUMN_CHAT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(messageId)};

        db.update(TABLE_CHAT, values, selection, selectionArgs);

        db.close();
    }

    public void deleteChatMessage(long messageId) {
        SQLiteDatabase db = getWritableDatabase();
        String selection = COLUMN_CHAT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(messageId)};
        db.delete(TABLE_CHAT, selection, selectionArgs);
        db.close();
    }
    public long addBlogPost(String senderUsername, String title, String content, Bitmap blogImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_SENDER_USERNAME, senderUsername);
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);

        // Check if the blogImage is not null
        if (blogImage != null) {
            // Resize the image to ensure it is within the 2MB limit
            Bitmap resizedImage = resizeBitmap(blogImage, 1024, 1024); // Adjust the dimensions as needed

            // Check if the resized image is not null and its size is within the limit
            if (resizedImage != null) {
                byte[] imageBytes = getBytesFromBitmap(resizedImage);
                if (imageBytes.length <= 2 * 1024 * 1024) {  // Check if image size is within 2MB
                    values.put(COLUMN_BLOG_IMAGE, imageBytes);
                } else {
                    // Handle the case where the resized image size still exceeds the limit
                    Log.e(TAG, "Resized image size exceeds the 2MB limit");
                    return -1;  // Return -1 to indicate failure
                }
            } else {
                // Handle the case where resizing the image failed
                Log.e(TAG, "Error resizing image");
                return -1;  // Return -1 to indicate failure
            }
        }

        long id = db.insert(TABLE_BLOG_POSTS, null, values);
        db.close();
        return id;
    }

    // Helper method to resize a bitmap
    private Bitmap resizeBitmap(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        float scaleWidth = ((float) maxWidth) / width;
        float scaleHeight = ((float) maxHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(originalBitmap, 0, 0, width, height, matrix, true);
    }
    // Helper method to convert byte array to Bitmap
    private Bitmap getBitmapFromBytes(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
    public List<BlogPost> getAllBlogPosts(String username) {
        List<BlogPost> blogPosts = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_BLOG_POSTS +
                " WHERE " + COLUMN_SENDER_USERNAME + " = '" + username + "'" +
                " ORDER BY " + COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(selectQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    BlogPost blogPost = new BlogPost();
                    blogPost.setPostId(cursor.getLong(cursor.getColumnIndex(COLUMN_POST_ID)));
                    blogPost.setSenderUsername(cursor.getString(cursor.getColumnIndex(COLUMN_SENDER_USERNAME)));
                    blogPost.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
                    blogPost.setContent(cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT)));

                    // Retrieve the image bytes from the cursor
                    int blogImageColumnIndex = cursor.getColumnIndex(COLUMN_BLOG_IMAGE);
                    if (blogImageColumnIndex != -1) { // Check if the column exists
                        byte[] imageData = cursor.getBlob(blogImageColumnIndex);
                        if (imageData != null) {
                            // Convert the image bytes to a Bitmap and set it
                            Bitmap bitmap = getBitmapFromBytes(imageData);
                            blogPost.setBlogImage(bitmap);
                        }
                    }

                    blogPost.setTimestamp(cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP)));
                    blogPost.setSelectedImagePath(cursor.getString(cursor.getColumnIndex(COLUMN_SELECTED_IMAGE_PATH)));

                    blogPosts.add(blogPost);
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error reading data from database: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return blogPosts;
    }


    // Helper method to decode a sampled bitmap from a byte array
    private Bitmap decodeSampledBitmapFromByteArray(byte[] data, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    // Helper method to calculate the sample size for downsampling
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    // Method to update a blog post
    public int updateBlogPost(BlogPost blogPost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, blogPost.getTitle());
        values.put(COLUMN_CONTENT, blogPost.getContent());

        // Convert Bitmap to byte array
        byte[] blogImageBytes = null;
        Bitmap blogImage = blogPost.getBlogImage();
        if (blogImage != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            blogImage.compress(Bitmap.CompressFormat.PNG, 0, stream);
            blogImageBytes = stream.toByteArray();
        }

        // Check if blog image bytes are not null before putting them in ContentValues
        if (blogImageBytes != null) {
            values.put(COLUMN_BLOG_IMAGE, blogImageBytes);
        }

        values.put(COLUMN_TIMESTAMP, blogPost.getTimestamp());
        values.put(COLUMN_SELECTED_IMAGE_PATH, blogPost.getSelectedImagePath());

        return db.update(TABLE_BLOG_POSTS, values, COLUMN_POST_ID + " = ?",
                new String[]{String.valueOf(blogPost.getPostId())});
    }


    // Method to delete a blog post
    public void deleteBlogPost(long postId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BLOG_POSTS, COLUMN_POST_ID + " = ?",
                new String[]{String.valueOf(postId)});
        db.close();
    }

    // Method to get a single instance of the helper
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }
    public boolean isUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    // Update image path for a specific blog post
    public void updateBlogImage(long postId, byte[] blogImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BLOG_IMAGE, blogImage); // Use the correct column name

        // Assuming "blog_post" is the table name, and "post_id" is the primary key column
        String selection = COLUMN_POST_ID + " = ?";
        String[] selectionArgs = {String.valueOf(postId)};

        db.update(TABLE_BLOG_POST, values, selection, selectionArgs); // Use the correct table name
        db.close();
    }
    // Method to get a list of friends for a user
    public List<String> getFriendList(String username) {
        List<String> friendList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT friend_username FROM friends WHERE username=?", new String[]{username});

        if (cursor.moveToFirst()) {
            do {
                String friendUsername = cursor.getString(0);
                friendList.add(friendUsername);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return friendList;
    }
    // Update blog post with a new image
    public void updateBlogPostWithImage(long postId, String title, String content, Bitmap blogImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, title);
            values.put(COLUMN_CONTENT, content);
            values.put(COLUMN_BLOG_IMAGE, getBytesFromBitmap(blogImage));

            // Update the blog post in the database
            db.update(TABLE_BLOG_POSTS, values, COLUMN_POST_ID + " = ?", new String[]{String.valueOf(postId)});
        } finally {
            db.close();
        }
    }

    // Update blog post without changing the image
    public void updateBlogPostWithoutImage(long postId, String title, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, title);
            values.put(COLUMN_CONTENT, content);

            // Update the blog post in the database
            db.update(TABLE_BLOG_POSTS, values, COLUMN_POST_ID + " = ?", new String[]{String.valueOf(postId)});
        } finally {
            db.close();
        }
    }


    // Helper method to convert Bitmap to byte array
    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }


    public BlogPost getBlogPostById(long postId) {
        SQLiteDatabase db = this.getReadableDatabase();
        BlogPost blogPost = null;

        // Define the columns to be retrieved
        String[] projection = {
                COLUMN_POST_ID,
                COLUMN_SENDER_USERNAME,
                COLUMN_TITLE,
                COLUMN_CONTENT,
                COLUMN_BLOG_IMAGE,
                COLUMN_TIMESTAMP,
                COLUMN_SELECTED_IMAGE_PATH
        };

        // Define the selection criteria
        String selection = COLUMN_POST_ID + " = ?";
        String[] selectionArgs = {String.valueOf(postId)};

        // Query the database
        Cursor cursor = db.query(
                TABLE_BLOG_POSTS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        // Check if a blog post was found
        if (cursor.moveToFirst()) {
            blogPost = new BlogPost();
            blogPost.setPostId(cursor.getLong(cursor.getColumnIndex(COLUMN_POST_ID)));
            blogPost.setSenderUsername(cursor.getString(cursor.getColumnIndex(COLUMN_SENDER_USERNAME)));
            blogPost.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
            blogPost.setContent(cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT)));

            // Retrieve the image bytes from the cursor
            int blogImageColumnIndex = cursor.getColumnIndex(COLUMN_BLOG_IMAGE);
            if (blogImageColumnIndex != -1) { // Check if the column exists
                byte[] imageData = cursor.getBlob(blogImageColumnIndex);
                if (imageData != null) {
                    // Convert the image bytes to a Bitmap and set it
                    Bitmap bitmap = getBitmapFromBytes(imageData);
                    blogPost.setBlogImage(bitmap);
                }
            }

            blogPost.setTimestamp(cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP)));
            blogPost.setSelectedImagePath(cursor.getString(cursor.getColumnIndex(COLUMN_SELECTED_IMAGE_PATH)));
        }

        // Close the cursor and the database
        cursor.close();
        db.close();

        return blogPost;
    }

}
