package com.gabidbr.mindzen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class AboutActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private SwitchCompat notificationSwitch;
    private TextView nameTextView;
    private TextView ageTextView;
    private TextView dobTextView;
    private TextView emailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        notificationSwitch = findViewById(R.id.notificationSwitch);
        handleNotifications(notificationSwitch);

        nameTextView = findViewById(R.id.profileNameTextView);
        ageTextView = findViewById(R.id.profileAgeTextView);
        dobTextView = findViewById(R.id.profileDobTextView);
        emailTextView = findViewById(R.id.profileEmailTextView);

        setupProfileInfo();

        MLModelStressDetection.useMLModel();
    }

    private void setupProfileInfo() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();
        String emailValue = currentUser.getEmail();
        emailTextView.setText(emailValue);

        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String age = Objects.requireNonNull(snapshot.child("age").getValue(Long.class)).toString();
                    String dob = snapshot.child("dob").getValue(String.class);

                    nameTextView.setText(name);
                    ageTextView.setText(age);
                    dobTextView.setText(dob);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void handleNotifications(SwitchCompat notificationSwitch) {
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Intent intent = new Intent();
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                // For Android 8 and above
                intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());

                startActivity(intent);
            } else {
                // Handle notification disabled state
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                notificationManager.cancelAll();
            }
        });
    }
}