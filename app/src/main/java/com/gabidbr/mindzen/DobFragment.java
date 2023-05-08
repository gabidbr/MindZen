package com.gabidbr.mindzen;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class DobFragment extends Fragment {

    private EditText editTextDob;
    private DatabaseReference mDatabase;

    public DobFragment() {
        // Required empty public constructor
    }

    public static DobFragment newInstance() {
        DobFragment fragment = new DobFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dob, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        editTextDob = view.findViewById(R.id.editTextDob);
        editTextDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        Button buttonSendDob = view.findViewById(R.id.button_send_dob);
        buttonSendDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendDobClick();
            }
        });

        return view;
    }

    private void onSendDobClick() {
        // Check if the date of birth field is empty
        if (TextUtils.isEmpty(editTextDob.getText())) {
            // Show a toast message indicating that the date of birth must be entered
            Toast.makeText(getContext(), "Please enter your date of birth", Toast.LENGTH_SHORT).show();
        } else {
            // Call the saveDob function with the entered date of birth
            String dateOfBirth = editTextDob.getText().toString();
            saveDob(dateOfBirth);

            // Call the onDobSendButtonClick function in the parent activity
            if (getActivity() instanceof UserDetailsActivity) {
                ((UserDetailsActivity) getActivity()).onDobSendButtonClick(getView());
            }
        }
    }

    private void saveDob(String dateOfBirth) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        int age = calculateAge(dateOfBirth);

        // Save the date of birth and age to the Firebase Realtime Database
        mDatabase.child("users").child(userId).child("dob").setValue(dateOfBirth);
        mDatabase.child("users").child(userId).child("age").setValue(age)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Age saved successfully
                            Toast.makeText(getContext(), "Age saved", Toast.LENGTH_SHORT).show();
                        } else {
                            // Error saving age
                            Toast.makeText(getContext(), "Error saving age", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view,theYear, monthOfYear, dayOfMonth) -> {
            String selectedDate = String.format("%04d-%02d-%02d", theYear, monthOfYear + 1, dayOfMonth);
            editTextDob.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    private int calculateAge(String dateOfBirth) {
        // Parse the date of birth string into a Calendar object
        Calendar dob = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            dob.setTime(Objects.requireNonNull(sdf.parse(dateOfBirth)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Get the current date and time
        Calendar now = Calendar.getInstance();

        // Calculate the age based on the difference between the current date/time and the date of birth
        int age = now.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (now.get(Calendar.MONTH) < dob.get(Calendar.MONTH) ||
                (now.get(Calendar.MONTH) == dob.get(Calendar.MONTH) && now.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }

        return age;
    }
}