package uk.ac.wlv.chat;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.PhotoPost;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import uk.ac.wlv.chat.database.DatabaseHelper;

public class BlogActivity extends AppCompatActivity {
    private String loggedInUsername;
    private DatabaseHelper databaseHelper;
    private TextView blogWelcomeUsername;
    private BlogPostAdapter adapter;
    private List<BlogPost> blogPosts; // Declare blogPosts as a member variable
    private static final String TUMBLR_CONSUMER_KEY = "A9edLVRny4dEaSNWQPSiY7QlMobB7LtNRLGESfKLPdWwacbGMQ";
    private static final String TUMBLR_CONSUMER_SECRET = "2jFE7WFL9nBBGQgD0RByvfixWV60gkn2p8tH3VkMH6gc2t4Z2V";
    private static final String TUMBLR_OAUTH_TOKEN = "OawpmKjIcDpUsEwOphP3Bw99dIo7xkAqNYqIgVOQsieOGmnmSo";
    private static final String TUMBLR_OAUTH_TOKEN_SECRET = "hzZBBNC0jsFgIGiikPBji9eX1gAlMc3xl53XiiV1dGKf6l4m8z";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);

        databaseHelper = new DatabaseHelper(this);

        ImageButton addPostButton = findViewById(R.id.blogAddPost);
        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPostDialog();
            }
        });

        blogWelcomeUsername = findViewById(R.id.blogWelcome);
        loggedInUsername = getIntent().getStringExtra("USERNAME");

        if (loggedInUsername != null) {
            List<BlogPost> blogPosts = databaseHelper.getAllBlogPosts(loggedInUsername);
            adapter = new BlogPostAdapter(this, blogPosts, loggedInUsername);
            ListView listViewBlogPosts = findViewById(R.id.listViewBlogPosts);
            listViewBlogPosts.setAdapter(adapter);
        } else {
            Log.e("BlogActivity", "loggedInUsername is null");
        }
        blogWelcomeUsername.setText("Welcome to the Blog " + loggedInUsername + "!");
        // Retrieve the friend list for the logged-in user
        List<String> friendList = databaseHelper.getFriendList(loggedInUsername);

        // Retrieve blog posts for each friend and display them
        for (String friendUsername : friendList) {
            List<BlogPost> friendBlogPosts = databaseHelper.getAllBlogPosts(friendUsername);
            adapter.addAll(friendBlogPosts);
        }
        // Find the blogBackButton by its ID
        ImageButton blogBackButtonPress = findViewById(R.id.blogBackButton);

        blogBackButtonPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // This will simulate a back button press and navigate back
            }
        });
    }

    private void showAddPostDialog() {
        Log.d("BlogActivity", "loggedInUsername: " + loggedInUsername);

        FragmentManager fragmentManager = getSupportFragmentManager();
        BlogAddPostDialogFragment dialogFragment = new BlogAddPostDialogFragment();
        dialogFragment.setSenderUsername(loggedInUsername);

        dialogFragment.show(fragmentManager, "AddPostDialogFragment");
    }
    public void refreshBlogPosts() {
        if (adapter != null) {
            List<String> friendList = databaseHelper.getFriendList(loggedInUsername);

            // Combine the logged-in user and friend blog posts
            List<BlogPost> allBlogPosts = new ArrayList<>();

            // Add posts of the logged-in user
            List<BlogPost> userBlogPosts = databaseHelper.getAllBlogPosts(loggedInUsername);
            allBlogPosts.addAll(userBlogPosts);

            // Add posts of friends without duplicating the logged-in user's posts
            for (String friendUsername : friendList) {
                if (!friendUsername.equals(loggedInUsername)) {
                    List<BlogPost> friendBlogPosts = databaseHelper.getAllBlogPosts(friendUsername);
                    for (BlogPost friendPost : friendBlogPosts) {
                        if (!allBlogPosts.contains(friendPost)) {
                            allBlogPosts.add(friendPost);
                        }
                    }
                }
            }

            adapter.clear();
            adapter.addAll(allBlogPosts);
            adapter.notifyDataSetChanged();
        }
    }

    private void showEditPostDialog(BlogPost selectedPost) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        BlogEditPostDialogFragment editDialogFragment = new BlogEditPostDialogFragment();

        // Pass the selected post details to the dialog fragment
        Bundle bundle = new Bundle();
        bundle.putParcelable("selected_post", selectedPost);
        editDialogFragment.setArguments(bundle);

        editDialogFragment.show(fragmentManager, "EditPostDialogFragment");
    }
    private List<BlogPost> getSelectedPosts() {
        List<BlogPost> selectedPosts = new ArrayList<>();
        for (BlogPost post : blogPosts) {
            if (post.isSelected()) {
                selectedPosts.add(post);
            }
        }
        return selectedPosts;
    }
    public void uploadToTumblr(long postId, TumblrUploadListener listener) {
        // Fetch the blog post details from the database based on the postId
        BlogPost blogPost = getBlogPostById(postId);

        // Check if the blog post exists
        if (blogPost != null) {
            // Get title, content, and image
            String postTitle = blogPost.getTitle();
            String postContent = blogPost.getContent();
            byte[] postImage = getBytesFromBitmap(blogPost.getBlogImage());

            // Perform the upload in the background
            new AsyncTask<Object, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Object... params) {
                    if (params.length >= 3) {
                        String postTitle = (String) params[0];
                        String postContent = (String) params[1];
                        byte[] postImage = (byte[]) params[2];

                        JumblrClient client = new JumblrClient(TUMBLR_CONSUMER_KEY, TUMBLR_CONSUMER_SECRET);
                        client.setToken(TUMBLR_OAUTH_TOKEN, TUMBLR_OAUTH_TOKEN_SECRET);

                        try {
                            // Create a temporary file from the byte array
                            File tempFile = File.createTempFile("image", ".jpg", getCacheDir());
                            FileOutputStream fos = new FileOutputStream(tempFile);
                            fos.write(postImage);
                            fos.close();

                            PhotoPost post = client.newPost(client.user().getBlogs().get(0).getName(), PhotoPost.class);
                            post.setCaption(postTitle + "\n" + postContent);
                            post.setData(tempFile);  // Use the temporary file

                            post.save();

                            // Delete the temporary file after upload
                            tempFile.delete();

                            return true;
                        } catch (Exception e) {
                            Log.e(TAG, "Error uploading to Tumblr", e);
                            return false;
                        }
                    }
                    return false;
                }

                @Override
                protected void onPostExecute(Boolean success) {
                    super.onPostExecute(success);

                    // Notify the listener about the upload completion
                    if (listener != null) {
                        listener.onUploadComplete(success);
                    }
                }
            }.execute(postTitle, postContent, postImage);
        } else {
            Log.e(TAG, "Blog post not found for postId: " + postId);
        }
    }



    // Define a listener interface for Tumblr upload completion
    public interface TumblrUploadListener {
        void onUploadComplete(boolean success);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
    private BlogPost getBlogPostById(long postId) {
        // Use your databaseHelper or any other method to retrieve the BlogPost based on postId
        return databaseHelper.getBlogPostById(postId);
    }

}