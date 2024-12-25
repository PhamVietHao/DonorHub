package com.example.donorhub;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.example.donorhub.Models.DonationSite;
import android.widget.ProgressBar;

import android.text.Editable;
import android.text.TextWatcher;

import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;

    private TextView greetingText;
    private LinearLayout donationListLayout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private Boolean isAdminSite;
    private EditText searchBox;
    private List<DonationSite> donationSiteList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        greetingText = view.findViewById(R.id.greeting_text);
        donationListLayout = view.findViewById(R.id.donation_list);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        progressBar = view.findViewById(R.id.home_progress_bar);
        searchBox = view.findViewById(R.id.search_box);

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            Log.d("HomeFragment", "User ID: " + uid);
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String name = documentSnapshot.getString("name");
                                isAdminSite = documentSnapshot.getBoolean("adminSite");
                                greetingText.setText("Hello " + name);
                                Log.d("HomeFragment", "User name: " + name);
                            } else {
                                greetingText.setText("Hello User");
                                Log.d("HomeFragment", "Document does not exist");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            greetingText.setText("Hello User");
                            Log.e("HomeFragment", "Error fetching document", e);
                        }
                    });
        } else {
            greetingText.setText("Hello User");
            Log.d("HomeFragment", "FirebaseUser is null");
        }

        // Load donation sites
        loadDonationSites();

        // Add TextWatcher to search box
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDonationSites(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Check and request notification permission
        checkNotificationPermission();

        return view;
    }

    private void loadDonationSites() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("donation_sites")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        donationListLayout.removeAllViews();
                        donationSiteList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            DonationSite donationSite = document.toObject(DonationSite.class);
                            donationSiteList.add(donationSite);
                            addDonationSiteToLayout(donationSite);
                        }
                    } else {
                        Log.e("HomeFragment", "Error getting donation sites", task.getException());
                    }
                });
    }

    private void addDonationSiteToLayout(DonationSite donationSite) {
        View donationSiteView = LayoutInflater.from(getContext()).inflate(R.layout.donation_site_item, donationListLayout, false);

        TextView siteNameTextView = donationSiteView.findViewById(R.id.site_name);
        TextView siteAddressTextView = donationSiteView.findViewById(R.id.site_address);

        siteNameTextView.setText(donationSite.getName());
        siteAddressTextView.setText(donationSite.getAddress());

        donationSiteView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DonationSiteHome.class);
            intent.putExtra("siteId", donationSite.getId());
            intent.putExtra("siteName", donationSite.getName());
            intent.putExtra("siteAddress", donationSite.getAddress());
            intent.putExtra("siteLatitude", donationSite.getLatitude());
            intent.putExtra("siteLongitude", donationSite.getLongitude());
            intent.putExtra("isAdminSite", isAdminSite);
            startActivity(intent);
        });

        donationListLayout.addView(donationSiteView);
    }

    private void filterDonationSites(String query) {
        donationListLayout.removeAllViews();
        for (DonationSite site : donationSiteList) {
            if (site.getName().toLowerCase().contains(query.toLowerCase()) ||
                    site.getAddress().toLowerCase().contains(query.toLowerCase())) {
                addDonationSiteToLayout(site);
            }
        }
    }

    private void checkNotificationPermission() {
        if (!NotificationManagerCompat.from(getContext()).areNotificationsEnabled()) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("HomeFragment", "Notification permission granted");
            } else {
                Log.d("HomeFragment", "Notification permission denied");
            }
        }
    }
}