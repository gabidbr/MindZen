package com.gabidbr.mindzen;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GenderFragment extends Fragment {

    private CardView mMaleCardView;
    private CardView mFemaleCardView;
    private String gender;
    private DatabaseReference mDatabase;

    public GenderFragment() {
        // Required empty public constructor
    }

    public static GenderFragment newInstance(String param1, String param2) {
        GenderFragment fragment = new GenderFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gender, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mMaleCardView = view.findViewById(R.id.male_card_view);
        mFemaleCardView = view.findViewById(R.id.female_card_view);

        mMaleCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectGender("M");
            }
        });

        mFemaleCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectGender("F");
            }
        });

        Button buttonSend = view.findViewById(R.id.button_send_gender);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gender == null) {
                    Toast.makeText(getContext(), "Please select a gender", Toast.LENGTH_SHORT).show();
                } else {
                    saveGender(gender);
                }
                if (getActivity() instanceof UserDetailsActivity) {
                    ((UserDetailsActivity) getActivity()).onGenderSaved(v);
                }
            }
        });

        return view;
    }

    private void selectGender(String newGender) {
        // Update the selected gender
        gender = newGender;

        // Update the card views
        if (gender.equals("M")) {
            mMaleCardView.setCardBackgroundColor(getResources().getColor(R.color.blue_200));
            ((TextView) mMaleCardView.findViewById(R.id.male_text_view)).setTextColor(getResources().getColor(R.color.white));
            mFemaleCardView.setCardBackgroundColor(getResources().getColor(R.color.white));
            ((TextView) mFemaleCardView.findViewById(R.id.female_text_view)).setTextColor(getResources().getColor(R.color.blue_700));
        } else {
            mMaleCardView.setCardBackgroundColor(getResources().getColor(R.color.white));
            ((TextView) mMaleCardView.findViewById(R.id.male_text_view)).setTextColor(getResources().getColor(R.color.blue_700));
            mFemaleCardView.setCardBackgroundColor(getResources().getColor(R.color.blue_200));
            ((TextView) mFemaleCardView.findViewById(R.id.female_text_view)).setTextColor(getResources().getColor(R.color.white));
        }
    }

    private void saveGender(String gender) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Save the gender to the Firebase Realtime Database
        mDatabase.child("users").child(userId).child("gender").setValue(gender)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Gender saved successfully
                            Toast.makeText(getContext(), "Gender saved", Toast.LENGTH_SHORT).show();
                        } else {
                            // Error saving gender
                            Toast.makeText(getContext(), "Error saving gender", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}