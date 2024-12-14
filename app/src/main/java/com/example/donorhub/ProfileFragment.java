package com.example.donorhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private ImageView profileImageView;
    private TextView nameTextView, emailTextView, bloodTypeTextView, achievementsTextView;
    private ImageView badgeImageView1, badgeImageView2, badgeImageView3;
    private Button signOutButton;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        profileImageView = view.findViewById(R.id.profileImageView);
        nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        bloodTypeTextView = view.findViewById(R.id.bloodTypeTextView);
        achievementsTextView = view.findViewById(R.id.achievementsTextView);
        badgeImageView1 = view.findViewById(R.id.badgeImageView1);
        badgeImageView2 = view.findViewById(R.id.badgeImageView2);
        badgeImageView3 = view.findViewById(R.id.badgeImageView3);
        signOutButton = view.findViewById(R.id.signOutButton);

        // Load user data from arguments
        Bundle args = getArguments();
        if (args != null) {
            nameTextView.setText(args.getString("userName"));
            emailTextView.setText(args.getString("userEmail"));
            bloodTypeTextView.setText(args.getString("userBloodType"));
            achievementsTextView.setText("Achievements");

            // Display badges based on milestone
            int milestone = args.getInt("userMilestone", 0);
            if (milestone >= 1) {
                badgeImageView1.setVisibility(View.VISIBLE);
            }
            if (milestone >= 5) {
                badgeImageView2.setVisibility(View.VISIBLE);
            }
            if (milestone >= 10) {
                badgeImageView3.setVisibility(View.VISIBLE);
            }
        }

        // Sign out button logic
        signOutButton.setOnClickListener(v -> handleSignOut());

        return view;
    }

    private void handleSignOut() {
        mAuth.signOut();
        Toast.makeText(getActivity(), "Signed out successfully", Toast.LENGTH_SHORT).show();
        // Redirect to LoginActivity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}