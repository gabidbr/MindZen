package com.gabidbr.mindzen;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class DashboardActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;


    LinearLayout exercisesButton, aboutButton, insightsButton;
    BarChart barChart;
    CardView cardView;
    ImageButton imageButton;
    TextView userName;
    CircleImageView circleImageView;
    private DatabaseReference mDatabase;
    FirebaseStorage storage;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        exercisesButton = findViewById(R.id.exercisesLinearLayout);
        aboutButton = findViewById(R.id.aboutLinearLayout);
        insightsButton = findViewById(R.id.insightsLinearLayout);
        barChart = findViewById(R.id.bar_chart);
        cardView = findViewById(R.id.screen_time_card);
        imageButton = findViewById(R.id.avatarButton);
        userName = findViewById(R.id.userName_textView);

        circleImageView = findViewById(R.id.imageView11);
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
                builder.setTitle("Choose an option")
                        .setItems(new CharSequence[]{"Take a photo", "Choose from gallery"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        // Take a photo from camera
                                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                                        break;
                                    case 1:
                                        // Select an existing photo from gallery
                                        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                        pickPhotoIntent.setType("image/*");
                                        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
                                        break;
                                }
                            }
                        })
                        .show();
            }
        });

        setupUserName(userName);

        setupBarChart(barChart);

        exercisesButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ExercisesActivity.class);
            startActivity(intent);
        });

        aboutButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AboutActivity.class);
            startActivity(intent);
        });

        insightsButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, InsightsActivity.class);
            startActivity(intent);
        });

        cardView.setOnClickListener(v -> openScreenTimeSettings());

        imageButton.setOnClickListener(v -> {
            openAccountFragment();
        });

        displayNotificationIfStressed();

    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        StorageReference storageReference = storageRef.child("images/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/profile.jpg");

        UploadTask uploadTask = storageReference.putFile(imageUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Image uploaded successfully
                // Get the download URL of the image and save it in Firebase Realtime Database
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageUrl = uri.toString();
                        mDatabase.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("image_url").setValue(imageUrl);
                        // Update the CircleImageView with the uploaded image
                        Glide.with(DashboardActivity.this)
                                .load(imageUrl)
                                .into(circleImageView);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure( Exception exception) {
                // Image upload failed
                Toast.makeText(DashboardActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Get the image taken from camera
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");

                // Set the image as the source of CircleImageView
                circleImageView.setImageBitmap(imageBitmap);

                // Upload the image to Firebase Storage
                Uri imageUri = getImageUri(this, imageBitmap);
                if (imageUri != null) {
                    uploadImageToFirebaseStorage(imageUri);
                }
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                // Get the image selected from gallery
                Uri imageUri = data.getData();
                Bitmap imageBitmap;
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    // Set the image as the source of CircleImageView
                    circleImageView.setImageBitmap(imageBitmap);

                    // Upload the image to Firebase Storage
                    if (imageUri != null) {
                        uploadImageToFirebaseStorage(imageUri);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setupUserName(TextView userName) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDatabase.child("users").child(userId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.getValue(String.class);
                    userName.setText(name);
                    String imageUrl = snapshot.child("image_url").getValue(String.class);
                    if (imageUrl != null) {
                        Glide.with(DashboardActivity.this)
                                .load(imageUrl)
                                .into(circleImageView);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }



    private void displayNotificationIfStressed() {
        //TODO if stress level greater than a certain value
        if (8 > 7) {
            // Set up the intent to launch the ExercisesActivity
            Intent intent = new Intent(this, ExercisesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            // Set up the notification channel
            String CHANNEL_ID = "my_channel_id";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "My Notification Channel", NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("My notification channel description");
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }

            Bitmap notificationImage = BitmapFactory.decodeResource(getResources(), R.drawable.breathing);

            // Set up the notification details
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
                    .setContentTitle("High Level of stress detected")
                    .setContentText("Take a break and try some exercises to calm down.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setLargeIcon(notificationImage)
                    .setContentIntent(pendingIntent);

            // Display the notification
            int notificationId = (int) System.currentTimeMillis();
            NotificationManagerCompat notificationMgr = NotificationManagerCompat.from(this);
            notificationMgr.notify(notificationId, builder.build());
        }
    }

    private void openAccountFragment() {
        AccountFragment accountFragment = new AccountFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.setCustomAnimations(
                R.anim.slide_in_bottom, // Enter animation
                R.anim.slide_out_top,   // Exit animation
                R.anim.slide_in_bottom, // Pop enter animation
                R.anim.slide_out_top    // Pop exit animation
        );

        transaction.replace(R.id.fragment_container, accountFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openScreenTimeSettings() {
        Intent intent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
        startActivity(intent);
    }

    private void setupBarChart(BarChart barChart) {
        long[] screenTimeDataArray = getScreenTimeForLastWeek();
        ArrayList<BarEntry> screenTimeData = new ArrayList<>();
        for (int i = 0; i < screenTimeDataArray.length; i++) {
            screenTimeData.add(new BarEntry(i, TimeUnit.MILLISECONDS.toMinutes(screenTimeDataArray[i])));
        }

        BarDataSet barDataSet = new BarDataSet(screenTimeData, "Screen Time");
        barDataSet.setColor(getResources().getColor(R.color.blue_200));
        barDataSet.setValueTextSize(12f);
        barDataSet.setBarBorderWidth(0);
        barDataSet.setBarShadowColor(Color.TRANSPARENT);

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.9f);

        barChart.setData(barData);
        barChart.notifyDataSetChanged(); // notify the chart that the data has changed

        for (IBarDataSet dataSet : barData.getDataSets()) {
            dataSet.setDrawValues(false);
        }

        // format the Y-axis labels as minutes and hours with minutes
        barChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value < 60) {
                    return (int) value + "min";
                } else {
                    int hours = (int) value / 60;
                    int minutes = (int) value % 60;
                    return String.format(Locale.getDefault(), "%dh%02dmin", hours, minutes);
                }
            }
        });

        barChart.invalidate(); // redraw the chart

        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);
        xAxis.setValueFormatter(new ValueFormatter() {
            final String[] daysOfWeek = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return daysOfWeek[(int) value % daysOfWeek.length];
            }
        });

        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setEnabled(true);
        barChart.getAxisLeft().setDrawGridLines(false);

        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (hasUsageStatsPermission()) {
            setupBarChart(barChart);
        }
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void requestUsageStatsPermission() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

    private long[] getScreenTimeForLastWeek() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        long[] screenTimeArray = new long[7];

        for (int i = 0; i < 7; i++) {
            long endTime = calendar.getTimeInMillis();
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            long startTime = calendar.getTimeInMillis();

            List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
            long screenTime = 0;

            for (UsageStats usageStats : usageStatsList) {
                screenTime += usageStats.getTotalTimeInForeground();
            }

            screenTimeArray[i] = screenTime;
        }

        return screenTimeArray;
    }

    //TODO sa preiau datele pentru fiecare zi din saptamana corect
    private long[] getScreenTimeForEachDayLastWeek() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        long[] screenTimeForEachDay = new long[7];

        // Find the current day of the week
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Calculate the difference between the current day and the last Monday
        int daysToLastMonday = currentDayOfWeek - Calendar.MONDAY;
        if (daysToLastMonday < 0) {
            daysToLastMonday += 7; // If the current day is before Monday, go back to the previous week
        }

        // Set the calendar to the last Monday
        calendar.add(Calendar.DAY_OF_YEAR, -daysToLastMonday);


        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long endTime = calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(1) - 1; // End of the day
        long startTime = calendar.getTimeInMillis(); // Beginning of the day

        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
        for (UsageStats usageStats : usageStatsList) {
            screenTimeForEachDay[0] += usageStats.getTotalTimeInForeground();
        }
        for (int i = 1; i < 7; i++) {
            screenTimeForEachDay[i] = 7;
        }


        return screenTimeForEachDay;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayNotificationIfStressed();
            } else {
                Toast.makeText(this, "Not granted", Toast.LENGTH_LONG).show();
            }
        }
    }


}