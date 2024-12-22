package com.example.donorhub;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.donorhub.Models.User;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        BottomNavigationView bottomNavbar = findViewById(R.id.bottomNav);
        bottomNavbar.setSelectedItemId(R.id.home);
        bottomNavbar.setOnItemSelectedListener(navListener);

        checkAdminStatus(bottomNavbar);

        Fragment selectedFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, selectedFragment).commit();
    }

    private void checkAdminStatus(BottomNavigationView bottomNavbar) {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    User user = document.toObject(User.class);
                    if (user != null && (user.isAdmin() || user.isAdminSite())) {
                        bottomNavbar.getMenu().findItem(R.id.donation_site).setVisible(true);
                        Log.d(TAG, "Donation site tab visible");
                    } else {
                        bottomNavbar.getMenu().findItem(R.id.donation_site).setVisible(false);
                        Log.d(TAG, "Donation site tab hidden");
                    }
                }
            } else {
                Log.d(TAG, "Failed to get user data", task.getException());
            }
        });
    }

    private NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                int itemId = item.getItemId();
                Fragment selectedFragment;

                if (itemId == R.id.home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.donation_site) {
                    selectedFragment = new DonationSitePage();
                } else if (itemId == R.id.profile) {
                    selectedFragment = new ProfileFragment();
                } else if (itemId == R.id.map) {
                    selectedFragment = new MapFragment();
                } else {
                    selectedFragment = new HomeFragment();
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, selectedFragment).commit();
                return true;
            };

    @Override
    public void onBackPressed() {
        // Show a confirmation dialog when back is pressed
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to leave the app?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Exit the app
                    finishAffinity(); // Close all activities and exit
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Dismiss the dialog and stay in the app
                    dialog.dismiss();
                })
                .setOnCancelListener(dialog -> super.onBackPressed()) // Handle "back" on dialog
                .show();
    }
}