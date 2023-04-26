package com.gabidbr.mindzen;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.Nullable;

public class AccountFragment extends Fragment {

    public AccountFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        setupAccountItem(view, R.id.aboutMeLayout, R.drawable.ic_about_me, "About me");
        setupAccountItem(view, R.id.faqLayout, R.drawable.ic_faq, "F.A.Q");
        setupAccountItem(view, R.id.notificationsLayout, R.drawable.ic_notifications, "Notifications");
        setupAccountItem(view, R.id.logoutLayout, R.drawable.ic_logout, "Log out");

        View logoutLayout = view.findViewById(R.id.logoutLayout);
        logoutLayout.setOnClickListener(v-> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);

            requireActivity().finish();
        });
        return view;
    }

    private void setupAccountItem(View view, int layoutId, int iconResId, String text) {
        View itemLayout = view.findViewById(layoutId);
        ImageView icon = itemLayout.findViewById(R.id.icon);
        TextView textView = itemLayout.findViewById(R.id.text);

        icon.setImageResource(iconResId);
        icon.setColorFilter(getResources().getColor(R.color.blue_200));
        textView.setText(text);
    }
}