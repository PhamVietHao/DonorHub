package com.example.donorhub;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.donorhub.Models.DonationSite;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class DonationSitePage extends Fragment {

    private ImageButton addDonationSiteButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private LinearLayout donationListLayout;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.donation_site, container, false);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        addDonationSiteButton = view.findViewById(R.id.add_donation_site_button);
        donationListLayout = view.findViewById(R.id.donation_list);
        progressBar = view.findViewById(R.id.progress_bar);

        addDonationSiteButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RegisterDonationSiteActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load donation sites when the fragment resumes
        loadDonationSites();
    }

    private void loadDonationSites() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(getActivity(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String adminId = firebaseUser.getUid();

        // Show the ProgressBar
        progressBar.setVisibility(View.VISIBLE);

        db.collection("donation_sites")
                .whereEqualTo("adminId", adminId)
                .get()
                .addOnCompleteListener(task -> {
                    // Hide the ProgressBar
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        donationListLayout.removeAllViews();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            DonationSite donationSite = document.toObject(DonationSite.class);
                            addDonationSiteToLayout(donationSite);
                        }
                    } else {
                        Toast.makeText(getActivity(), "Error getting donation sites", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addDonationSiteToLayout(DonationSite donationSite) {
        View donationSiteView = LayoutInflater.from(getActivity()).inflate(R.layout.donation_site_item, donationListLayout, false);

        TextView siteNameTextView = donationSiteView.findViewById(R.id.site_name);
        TextView siteAddressTextView = donationSiteView.findViewById(R.id.site_address);

        siteNameTextView.setText(donationSite.getName());
        siteAddressTextView.setText(donationSite.getAddress());

        donationSiteView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DonationSiteDetailActivity.class);
            intent.putExtra("siteId", donationSite.getId());
            intent.putExtra("siteName", donationSite.getName());
            intent.putExtra("siteAddress", donationSite.getAddress());
            startActivity(intent);
        });

        donationListLayout.addView(donationSiteView);
    }
}