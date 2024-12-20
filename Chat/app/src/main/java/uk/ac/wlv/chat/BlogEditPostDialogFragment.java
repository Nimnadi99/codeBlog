package uk.ac.wlv.chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.io.IOException;

import uk.ac.wlv.chat.database.DatabaseHelper;

public class BlogEditPostDialogFragment extends DialogFragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etTitle;
    private EditText etContent;
    private ImageView ivBlogImage;
    private Button btnUpdateImage;
    private Button btnSave;

    private Uri selectedImageUri;
    private DatabaseHelper databaseHelper;  // Declare the DatabaseHelper object
    private BlogPost selectedPost;  // Declare the BlogPost object

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_post, container, false);

        // Initialize your UI components
        etTitle = view.findViewById(R.id.editTextTitle);
        etContent = view.findViewById(R.id.editTextContent);
        ivBlogImage = view.findViewById(R.id.editImageViewSelectedImage);
        btnUpdateImage = view.findViewById(R.id.editButtonSelectImage);
        btnSave = view.findViewById(R.id.editButtonUpdatePost);

        // Initialize the DatabaseHelper object
        databaseHelper = new DatabaseHelper(getActivity().getApplicationContext());

        // Retrieve the selected post details from arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            selectedPost = bundle.getParcelable("selected_post");

            // Pre-fill the UI components with selected post details
            if (selectedPost != null) {
                etTitle.setText(selectedPost.getTitle());
                etContent.setText(selectedPost.getContent());

                // Load and display the image if applicable
                Bitmap blogImage = selectedPost.getBlogImage();
                if (blogImage != null) {
                    ivBlogImage.setImageBitmap(blogImage);
                }
            }
        }

        // Set OnClickListener for update image button
        btnUpdateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        // Set OnClickListener for save button
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePostInDatabaseAndUI();
                dismiss();  // Dismiss the dialog after updating the post
            }
        });

        return view;
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                ivBlogImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updatePostInDatabaseAndUI() {
        if (selectedPost != null) {
            // Handle the logic to update the post in the database
            String updatedTitle = etTitle.getText().toString();
            String updatedContent = etContent.getText().toString();

            // Check if the user selected a new image
            if (selectedImageUri != null) {
                // Handle the logic to update the post with a new image
                updateBlogPostWithImage(updatedTitle, updatedContent);
            } else {
                // Handle the logic to update the post without changing the image
                updateBlogPostWithoutImage(updatedTitle, updatedContent);
            }

            // Refresh the UI in BlogActivity
            ((BlogActivity) getActivity()).refreshBlogPosts();
        }
    }

    private void updateBlogPostWithImage(String updatedTitle, String updatedContent) {
        // Convert the Uri to a Bitmap
        Bitmap originalBitmap;
        try {
            originalBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Failed to load image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Resize the image
        Bitmap resizedBitmap = resizeImage(originalBitmap);

        // Save the resized image to your database
        long postId = selectedPost.getPostId();
        databaseHelper.updateBlogPostWithImage(postId, updatedTitle, updatedContent, resizedBitmap);

        // Update the UI
        ivBlogImage.setImageBitmap(resizedBitmap);
        selectedPost.setTitle(updatedTitle);
        selectedPost.setContent(updatedContent);
    }


    private void updateBlogPostWithoutImage(String updatedTitle, String updatedContent) {
        // Save the updated information to your database
        long postId = selectedPost.getPostId();
        databaseHelper.updateBlogPostWithoutImage(postId, updatedTitle, updatedContent);

        // Update the UI
        selectedPost.setTitle(updatedTitle);
        selectedPost.setContent(updatedContent);
    }
    private Bitmap resizeImage(Bitmap originalImage) {
        int maxWidth = 1024; // Set your desired maximum width
        int maxHeight = 1024; // Set your desired maximum height

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        float scaleWidth = ((float) maxWidth) / width;
        float scaleHeight = ((float) maxHeight) / height;

        float scale = Math.min(scaleWidth, scaleHeight);

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        return Bitmap.createBitmap(originalImage, 0, 0, width, height, matrix, true);
    }

}
