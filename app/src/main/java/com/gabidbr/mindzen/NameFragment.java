package com.gabidbr.mindzen;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

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

public class NameFragment extends Fragment {

    private DatabaseReference mDatabase;


    public NameFragment() {
        // Required empty public constructor
    }

    public static NameFragment newInstance() {
        NameFragment fragment = new NameFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_name, container, false);

        Button buttonSend = view.findViewById(R.id.button_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the name entered by the user
                EditText editTextName = view.findViewById(R.id.editText);
                String name = editTextName.getText().toString();

                //Check if the name is empty
                if (name.isEmpty()) {
                    // Show a Toast message asking the user to enter their name
                    Toast.makeText(getContext(), "Please enter your name", Toast.LENGTH_SHORT).show();
                } else {
                    // Save the name to the Firebase Realtime Database
                    saveName(name);

                    if (getActivity() instanceof UserDetailsActivity) {
                        ((UserDetailsActivity) getActivity()).onNameSendButtonClick(v);
                    }
                }
            }
        });

        return view;
    }

    private void saveName(String name) {
        // Get the current user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Save the name to the Firebase Realtime Database
        mDatabase.child("users").child(userId).child("name").setValue(name)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Name saved successfully
                            Toast.makeText(getContext(), "Name saved", Toast.LENGTH_SHORT).show();
                        } else {
                            // Error saving name
                            Toast.makeText(getContext(), "Error saving name", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}