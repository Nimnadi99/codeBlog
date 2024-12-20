package uk.ac.wlv.chat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.ac.wlv.chat.database.DatabaseHelper;

public class BlogPostAdapter extends ArrayAdapter<BlogPost> {
    private Context context;
    private List<BlogPost> blogPosts;
    private String loggedInUsername;  // Declare loggedInUsername
    private List<BlogPost> selectedPosts = new ArrayList<>();


    public BlogPostAdapter(Context context, List<BlogPost> blogPosts, String loggedInUsername) {
        super(context, R.layout.list_item_blog, blogPosts);
        this.context = context;
        this.blogPosts = blogPosts;
        this.loggedInUsername = loggedInUsername;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_blog, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textSenderName = convertView.findViewById(R.id.textSenderName);
            viewHolder.textTitle = convertView.findViewById(R.id.textTitle);
            viewHolder.imageBlog = convertView.findViewById(R.id.imageBlog);
            viewHolder.textContent = convertView.findViewById(R.id.textContent);
            viewHolder.btnSharePost = convertView.findViewById(R.id.btnSharePost);
            viewHolder.btnEditPost = convertView.findViewById(R.id.btnEditPost);
            viewHolder.btnDeletePost = convertView.findViewById(R.id.btnDeletePost);
            viewHolder.shareEditDeleteItem = convertView.findViewById(R.id.shareEditDeleteItem); // Initialize shareEditDeleteItem
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        BlogPost blogPost = blogPosts.get(position);

        viewHolder.textSenderName.setText(blogPost.getSenderUsername());
        viewHolder.textTitle.setText(blogPost.getTitle());
        viewHolder.textContent.setText(blogPost.getContent());

        // Set image if available
        if (blogPost.getBlogImage() != null) {
            viewHolder.imageBlog.setImageBitmap(blogPost.getBlogImage());
        } else {
            viewHolder.imageBlog.setVisibility(View.GONE);
        }

        // Set click listeners or perform actions for share, edit, and delete buttons
        viewHolder.btnDeletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog(blogPost, position);
            }
        });

        // Check if the blog post belongs to the currently logged-in user
        if (blogPost.getSenderUsername().equals(loggedInUsername)) {
            // If it's the user's own post, show the buttons
            viewHolder.shareEditDeleteItem.setVisibility(View.VISIBLE);
        } else {
            // If it's not the user's post, hide the buttons
            viewHolder.shareEditDeleteItem.setVisibility(View.GONE);
        }
        // Set an OnClickListener for the edit button
        viewHolder.btnEditPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the selected blog post
                BlogPost selectedPost = getItem(position);

                // Open the edit dialog fragment
                showEditPostDialog(selectedPost);
            }
        });
        viewHolder.btnSharePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if there's an image to share
                if (blogPost.getBlogImage() != null) {
                    // If there's an image, use the existing shareBlogPost method
                    shareBlogPost(blogPost);
                } else {
                    // If there's no image, share only the text content to Tumblr
                    shareTextToTumblr(blogPost);
                }
            }
        });



        return convertView;
    }

    private static class ViewHolder {
        TextView textSenderName;
        TextView textTitle;
        ImageView imageBlog;
        TextView textContent;
        ImageButton btnSharePost;
        ImageButton btnEditPost;
        ImageButton btnDeletePost;
        RelativeLayout shareEditDeleteItem;  // Declare shareEditDeleteItem
        CheckBox checkbox;

    }
    // Add this method to show the delete confirmation dialog
    private void showDeleteConfirmationDialog(final BlogPost blogPost, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Post");
        builder.setMessage("Are you sure you want to delete this post?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delete the post from the database
                deleteBlogPost(blogPost);
                // Update the UI
                removeItem(position);
                notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    // Add this method to delete the post from the database
    private void deleteBlogPost(BlogPost blogPost) {
        // Assuming DatabaseHelper is already initialized
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        databaseHelper.deleteBlogPost(blogPost.getPostId());
    }

    // Add this method to remove the item from the list
    private void removeItem(int position) {
        blogPosts.remove(position);
    }
    // New method to show edit post dialog
    // New method to show edit post dialog
    private void showEditPostDialog(BlogPost selectedPost) {
        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        BlogEditPostDialogFragment editDialogFragment = new BlogEditPostDialogFragment();

        // Pass the selected post details to the dialog fragment
        Bundle bundle = new Bundle();
        bundle.putParcelable("selected_post", selectedPost);
        editDialogFragment.setArguments(bundle);

        editDialogFragment.show(fragmentManager, "EditPostDialogFragment");
    }
    private void shareBlogPost(BlogPost blogPost) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        // Add title and content to the share intent
        String title = blogPost.getTitle();
        String content = blogPost.getContent();
        Bitmap imageBitmap = blogPost.getBlogImage();

        // Combine title and content in the text
        String shareContent = title + "\n\n" + content;

        // Attach the image as an attachment
        if (imageBitmap != null) {
            Uri imageUri = getImageUri(context, imageBitmap);
            if (imageUri != null) {
                // Attach the image URI to the intent as a stream
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Set the type of the attached data (image)
                shareIntent.setType("image/*");
            }
        }

        // Set the combined content as the text of the share intent
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);

        try {
            // Start the activity with the share intent
            context.startActivity(Intent.createChooser(shareIntent, "Share via"));
        } catch (android.content.ActivityNotFoundException ex) {
            // Handle the case where no suitable app is installed
            showToast("No app found to handle the share action.");
        }
    }

    // Method to get the image URI
    private Uri getImageUri(Context context, Bitmap bitmap) {
        Uri imageUri = null;
        try {
            // Save the bitmap to a file
            File imageFile = saveBitmapToFile(context, bitmap);

            // Get the content URI using FileProvider
            imageUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageUri;
    }

    // Method to save bitmap to a file
    private File saveBitmapToFile(Context context, Bitmap bitmap) throws IOException {
        File cachePath = new File(context.getExternalFilesDir(null), "images");
        cachePath.mkdirs(); // Ensure the directory exists

        // Create a temporary image file
        File imageFile = new File(cachePath, "image.png");
        imageFile.createNewFile();

        // Write the bitmap data to the file
        try (FileOutputStream stream = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        }

        return imageFile;
    }

    // Show toast method
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    // Add this method to save text content to a file
    private File saveTextToFile(String text) throws IOException {
        File cachePath = new File(context.getExternalFilesDir(null), "text");
        cachePath.mkdirs(); // don't forget to make the directory
        File textFile = new File(cachePath, "text.txt");

        try (FileOutputStream stream = new FileOutputStream(textFile)) {
            stream.write(text.getBytes());
        }

        return textFile;
    }


    // Method to add a selected post
    public void addSelectedPost(BlogPost post) {
        selectedPosts.add(post);
    }

    // Method to remove a selected post
    public void removeSelectedPost(BlogPost post) {
        selectedPosts.remove(post);
    }

    // Method to get the list of selected posts
    public List<BlogPost> getSelectedPosts() {
        return selectedPosts;
    }

    // Add this method to share only text content to Tumblr
    private void shareTextToTumblr(BlogPost blogPost) {
        String postTitle = blogPost.getTitle();
        String postContent = blogPost.getContent();

        // Get the BlogActivity context
        Context context = getContext();

        // Call the TumblrUploader method to share text only
        TumblrUploader.uploadTextToTumblr(context, postTitle, postContent);
    }
}