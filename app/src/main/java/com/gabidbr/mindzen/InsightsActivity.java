package com.gabidbr.mindzen;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class InsightsActivity extends AppCompatActivity {

    // Global variables
    SharedPreferences sharedPreferences;
    public static final String STEPS_PREF = "steps_pref";
    public static final String TODAY_STEPS_KEY = "today_steps";
    public static final String LAST_UPDATE_KEY = "last_update";

    TextView screenTimeTextView, stepsTextView;
    ImageView backButton;
    SensorManager sensorManager;
    Sensor stepCounterSensor;
    AlertDialog.Builder builder;
    private TabLayout tabLayout;
    private TextView totalStepsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insights);

        screenTimeTextView = findViewById(R.id.screenTimeView);
        stepsTextView = findViewById(R.id.stepsTextView);
        backButton = findViewById(R.id.backButtonInsights);
        // Initialize the TabLayout and TextView
        tabLayout = findViewById(R.id.tabLayout);
        totalStepsTextView = findViewById(R.id.stepsTextViewInTab);

        setUpTabLayoutListener();

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(InsightsActivity.this, DashboardActivity.class);
            startActivity(intent);
        });

        getSteps();

        requestUsageStatsPermission();

        long todayUsageTime = getTodayUsageTime(this);

        screenTimeTextView.setText(formatUsageTime(todayUsageTime));

    }

    private void setUpTabLayoutListener() {
        // Set up the listener for the TabLayout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateTotalSteps(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void updateTotalSteps(int position) {
        // Get the total steps based on the selected tab
        long totalSteps = 0;
        switch (position) {
            case 0: // Day
                totalSteps = getDailySteps();
                break;
            case 1: // Week
                totalSteps = getWeeklySteps();
                break;
            case 2: // Month
                totalSteps = getMonthlySteps();
                break;
            case 3: // 6 Months
                totalSteps = getSixMonthsSteps();
                break;
            case 4: // Year
                totalSteps = getYearlySteps();
                break;
        }
        totalStepsTextView.setText(String.format(Locale.getDefault(), "Total Steps: %d", totalSteps));
    }

    private void requestUsageStatsPermission() {
        if (!hasUsageStatsPermission(this)) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    private boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }


    private void getSteps() {
        sharedPreferences = getSharedPreferences(STEPS_PREF, Context.MODE_PRIVATE);
        // Check for step counter sensor availability
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepCounterSensor == null) {
            // If step counter sensor is not available, prompt user to enter steps manually
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter Steps");

            // Set up the input
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);

            // Set the width of the EditText to be 75% of the screen width
            int dialogWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.75);
            input.setLayoutParams(new ViewGroup.LayoutParams(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                String stepsStr = input.getText().toString();
                int steps = 0;
                if (!stepsStr.isEmpty()) {
                    steps = Integer.parseInt(stepsStr);
                }
                updateStepsTextView(steps);
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        } else {
            // If step counter sensor is available, register listener to update stepsTextView
            SensorEventListener stepCounterEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    int steps = (int) event.values[0];
                    updateStepsTextView(steps);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }
            };
            sensorManager.registerListener(stepCounterEventListener, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void updateStepsTextView(int steps) {
        String stepsStr = String.format(Locale.getDefault(), "%d", steps);
        stepsTextView.setText(stepsStr);
    }

    private void mapScreenTimeToTextView(long screenTime) {
        long hours = screenTime / (60 * 60 * 1000);
        long minutes = (screenTime % (60 * 60 * 1000)) / (60 * 1000);
        String screenTimeStr = String.format(Locale.getDefault(), "%dh %02dm", hours, minutes);
        screenTimeTextView.setText(screenTimeStr);
    }

    private UsageStatsManager getUsageStatsManager(Context context) {
        return (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
    }

    public long getTodayUsageTime(Context context) {
        long currentTime = System.currentTimeMillis();
        long startTime = getStartTimeOfTheDay(currentTime);
        UsageStatsManager usageStatsManager = getUsageStatsManager(context);
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, currentTime);

        long totalTime = 0;
        for (UsageStats usageStats : usageStatsList) {
            totalTime += usageStats.getTotalTimeInForeground();
        }
        return totalTime;
    }

    private long getStartTimeOfTheDay(long currentTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public String formatUsageTime(long usageTimeMillis) {
        long usageTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(usageTimeMillis);
        long hours = TimeUnit.SECONDS.toHours(usageTimeSeconds);
        long minutes = TimeUnit.SECONDS.toMinutes(usageTimeSeconds) - TimeUnit.HOURS.toMinutes(hours);

        return String.format(Locale.getDefault(), "%dh %02dm", hours, minutes);
    }

    private long getDailySteps() {
        return 5000; // Replace with actual daily steps
    }

    private long getWeeklySteps() {
        return 35000; // Replace with actual weekly steps
    }

    private long getMonthlySteps() {
        return 150000; // Replace with actual monthly steps
    }

    private long getSixMonthsSteps() {
        return 900000; // Replace with actual steps for 6 months
    }

    private long getYearlySteps() {
        return 1800000; // Replace with actual yearly steps
    }


}