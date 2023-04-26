package com.gabidbr.mindzen;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class VideoActivity extends AppCompatActivity {

    private static final String TAG = "VideoActivity";

    private VideoView videoView;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        // Get a reference to the VideoView in your layout
        videoView = findViewById(R.id.videoView);
        String videoName = getIntent().getStringExtra("videoName");
        playVideoFromFirebaseStorage(videoName);
    }

    private void playVideoFromFirebaseStorage(String videoName) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl("gs://mindzen-1bea1.appspot.com");
        StorageReference videoRef = storageReference.child(videoName);

        videoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Set the video path and start playback
                MediaController mediaController = new MediaController(VideoActivity.this);
                mediaController.setAnchorView(videoView);
                videoView.setMediaController(mediaController);
                videoView.setVideoURI(uri);
                videoView.requestFocus();
                videoView.start();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to get video download URL", e);
            }
        });
    }
}