package uk.ac.wlv.chat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import java.io.IOException;
import uk.ac.wlv.chat.database.DatabaseHelper;

public class BlogAddPostDialogFragment extends DialogFragment implements View.OnClickListener {
    private EditText editTextTitle;
    private EditText editTextContent;
    private ImageView imageViewSelectedImage;
    private Bitmap selectedImageBitmap;
    private static final int REQUEST_CAMERA_PERMISSION = 3; // Use any unique value

    private String senderUsername;
    private Button buttonSelectImage;
    private static final int REQUEST_IMAGE_GALLERY = 1;
    private static final int REQUEST_IMAGE_CAMERA = 2;

    public BlogAddPostDialogFragment() {
        // Required empty public constructor
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextTitle = view.findViewById(R.id.editTextTitle);
        editTextContent = view.findViewById(R.id.editTextContent);
        imageViewSelectedImage = view.findViewById(R.id.imageViewSelectedImage);
        buttonSelectImage = view.findViewById(R.id.buttonSelectImage);
        view.findViewById(R.id.buttonAddPost).setOnClickListener(this);
        buttonSelectImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Log.d("BlogAddPostDialog", "Button clicked: " + v.getId());
        if (v.getId() == R.id.buttonSelectImage) {
            showImageSelectionDialog();
        } else if (v.getId() == R.id.buttonAddPost) {
            validateAndAddBlogPost();
        }
    }
    private void addBlogPost() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        // Assuming DatabaseHelper is already initialized
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getActivity());

        // Only add the blog post for the sender
        databaseHelper.addBlogPost(senderUsername, title, content, selectedImageBitmap);

        // Notify the calling activity to refresh the blog posts immediately
        if (getActivity() instanceof BlogActivity) {
            ((BlogActivity) getActivity()).refreshBlogPosts();
        }

        dismiss();
    }

    private void showImageSelectionDialog() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose an option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    takePhoto();
                } else if (options[item].equals("Choose from Gallery")) {
                    chooseFromGallery();
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void takePhoto() {
        // Check if the CAMERA permission is granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Request the CAMERA permission if it is not granted
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            // If the CAMERA permission is already granted, proceed with taking a photo
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAMERA);
            }
        }
    }

    private void chooseFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_GALLERY) {
                handleGalleryResult(data);
            } else if (requestCode == REQUEST_IMAGE_CAMERA) {
                handleCameraResult(data);
            }
        }
    }

    private void handleGalleryResult(Intent data) {
        if (data != null) {
            Uri selectedImageUri = data.getData();
            try {
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                imageViewSelectedImage.setImageBitmap(selectedImageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleCameraResult(Intent data) {
        if (data != null) {
            Bundle extras = data.getExtras();
            selectedImageBitmap = (Bitmap) extras.get("data");
            imageViewSelectedImage.setImageBitmap(selectedImageBitmap);
        }
    }
    // Add this method to handle the result of permission requests
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, take a photo
                takePhoto();
            } else {
                // Permission denied, show a message or handle it accordingly
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void validateAndAddBlogPost() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (title.isEmpty() && content.isEmpty() && selectedImageBitmap == null) {
            // All fields are empty
            showToast("All fields are empty.");
        } else if (title.isEmpty() || content.isEmpty() || selectedImageBitmap == null) {
            // Title, content, or image is missing
            showToast("Please fill in all fields and select an image.");
        } else {
            // All fields are filled
            addBlogPost();
            showToast("Blog post created successfully.");
        }
    }
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

}
