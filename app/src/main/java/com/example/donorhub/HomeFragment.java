package com.example.donorhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
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

public class HomeFragment extends Fragment {

    private TextView greetingText;
    private LinearLayout donationListLayout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private Boolean isAdminSite;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        greetingText = view.findViewById(R.id.greeting_text);
        donationListLayout = view.findViewById(R.id.donation_list);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        progressBar = view.findViewById(R.id.home_progress_bar);

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String name = documentSnapshot.getString("name");
                                isAdminSite = documentSnapshot.getBoolean("adminSite");
                                greetingText.setText("Hello " + name);
                            } else {
                                greetingText.setText("Hello User");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            greetingText.setText("Hello User");
                        }
                    });
        } else {
            greetingText.setText("Hello User");
        }

        // Load donation sites
        loadDonationSites();

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
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            DonationSite donationSite = document.toObject(DonationSite.class);
                            addDonationSiteToLayout(donationSite);
                        }
                    } else {
                        // Handle error
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
            Intent intent = new Intent(getActivity(), DonationSiteDetailActivity.class);
            intent.putExtra("siteId", donationSite.getId());
            intent.putExtra("siteName", donationSite.getName());
            intent.putExtra("siteAddress", donationSite.getAddress());
            intent.putExtra("isAdminSite", isAdminSite);
            startActivity(intent);
        });

        donationListLayout.addView(donationSiteView);
    }
}