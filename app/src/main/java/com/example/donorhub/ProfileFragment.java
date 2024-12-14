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
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.donorhub.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private ImageView profileImageView;
    private TextView nameTextView, emailTextView, bloodTypeTextView, achievementsTextView;
    private ImageView badgeImageView1, badgeImageView2, badgeImageView3;
    private Button signOutButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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

        // Load user data
        loadUserData();

        // Sign out button logic
        signOutButton.setOnClickListener(v -> handleSignOut());

        return view;
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            db.collection("users").document(firebaseUser.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                User user = document.toObject(User.class);
                                if (user != null) {
                                    nameTextView.setText(user.getName());
                                    emailTextView.setText(user.getEmail());
                                    bloodTypeTextView.setText(user.getBloodType());
                                    achievementsTextView.setText("Achievements: " + user.getAchievements());

                                    // Display badges based on milestone
                                    int milestone = user.getAchievements();
                                    badgeImageView1.setVisibility(milestone >= 1 ? View.VISIBLE : View.GONE);
                                    badgeImageView2.setVisibility(milestone >= 5 ? View.VISIBLE : View.GONE);
                                    badgeImageView3.setVisibility(milestone >= 10 ? View.VISIBLE : View.GONE);
                                }
                            } else {
                                Toast.makeText(getActivity(), "User data not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "Error loading user data", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getActivity(), "User not authenticated", Toast.LENGTH_SHORT).show();
            // Redirect to LoginActivity
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        }
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
