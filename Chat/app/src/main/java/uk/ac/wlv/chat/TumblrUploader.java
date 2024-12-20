package uk.ac.wlv.chat;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.TextPost;

public class TumblrUploader {

    private static final String TAG = "TumblrUploader";

    private static final String TUMBLR_CONSUMER_KEY = "A9edLVRny4dEaSNWQPSiY7QlMobB7LtNRLGESfKLPdWwacbGMQ";
    private static final String TUMBLR_CONSUMER_SECRET = "2jFE7WFL9nBBGQgD0RByvfixWV60gkn2p8tH3VkMH6gc2t4Z2V";
    private static final String TUMBLR_OAUTH_TOKEN = "OawpmKjIcDpUsEwOphP3Bw99dIo7xkAqNYqIgVOQsieOGmnmSo";
    private static final String TUMBLR_OAUTH_TOKEN_SECRET = "hzZBBNC0jsFgIGiikPBji9eX1gAlMc3xl53XiiV1dGKf6l4m8z";

    public static void uploadMessageToTumblr(Context context, String message) {
        new AsyncTask<Object, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Object... params) {
                if (params.length >= 2) {
                    String postTitle = (String) params[0];
                    String postContent = (String) params[1];

                    JumblrClient client = new JumblrClient(TUMBLR_CONSUMER_KEY, TUMBLR_CONSUMER_SECRET);
                    client.setToken(TUMBLR_OAUTH_TOKEN, TUMBLR_OAUTH_TOKEN_SECRET);

                    try {
                        TextPost post = client.newPost(client.user().getBlogs().get(0).getName(), TextPost.class);
                        post.setTitle(postTitle);
                        post.setBody(postContent);

                        // Save the post
                        post.save();

                        return true;
                    } catch (Exception e) {
                        Log.e(TAG, "Error uploading text post to Tumblr", e);
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    showToast(context, "Text post upload successful!");
                } else {
                    showToast(context, "Failed to upload text post to Tumblr. Please try again.");
                }
            }

            private void showToast(Context context, String message) {
                // Use Toast or any other UI mechanism to show a message to the user
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        }.execute(message, null);
    }

    public static void uploadMessageToTumblr(BlogActivity blogActivity, String postTitle, String postContent) {
        // Implement this method for blog post upload
    }

    // Add this method to upload text only to Tumblr
    public static void uploadTextToTumblr(Context context, String postTitle, String postContent) {
        new AsyncTask<Object, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Object... params) {
                JumblrClient client = new JumblrClient(TUMBLR_CONSUMER_KEY, TUMBLR_CONSUMER_SECRET);
                client.setToken(TUMBLR_OAUTH_TOKEN, TUMBLR_OAUTH_TOKEN_SECRET);

                try {
                    TextPost post = client.newPost(client.user().getBlogs().get(0).getName(), TextPost.class);
                    post.setTitle(postTitle);
                    post.setBody(postContent);

                    // Save the post
                    post.save();

                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "Error uploading text post to Tumblr", e);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    showToast(context, "Text post upload successful!");
                } else {
                    showToast(context, "Failed to upload text post to Tumblr. Please try again.");
                }
            }

            private void showToast(Context context, String message) {
                // Use Toast or any other UI mechanism to show a message to the user
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        }.execute(postTitle, postContent);
    }
}