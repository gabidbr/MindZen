package com.gabidbr.mindzen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class UserDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        // Add the NameFragment to the container
        getSupportFragmentManager().beginTransaction()
                .add(R.id.user_fragment_container, new NameFragment())
                .commit();


    }

    public void onNameSendButtonClick(View view) {
        // Replace the NameFragment with the DobFragment using animations
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.user_fragment_container, new DobFragment())
                .addToBackStack(null)
                .commit();
    }

    public void onDobSendButtonClick(View v) {
        // Replace the NameFragment with the DobFragment using animations
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.user_fragment_container, new GenderFragment())
                .addToBackStack(null)
                .commit();

    }

    public void onGenderSaved(View v){
        Intent intent = new Intent(UserDetailsActivity.this, DashboardActivity.class);
        startActivity(intent);
    }
}