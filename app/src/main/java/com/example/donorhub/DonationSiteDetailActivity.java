package com.example.donorhub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.donorhub.Models.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class DonationSiteDetailActivity extends AppCompatActivity {

    private static final String TAG = "DonationSiteDetail";
    private TextView siteNameTextView;
    private TextView siteAddressTextView;
    private LinearLayout eventListLayout;
    private Button createEventButton;
    private FirebaseFirestore db;
    private String siteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_site_detail);

        // Initialize UI elements
        siteNameTextView = findViewById(R.id.site_name);
        siteAddressTextView = findViewById(R.id.site_address);
        eventListLayout = findViewById(R.id.event_list);
        createEventButton = findViewById(R.id.create_event_button);

        ImageButton backButton = findViewById(R.id.donationsite_back_button);
        backButton.setOnClickListener(v -> finish());

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get data from intent
        siteId = getIntent().getStringExtra("siteId");
        String siteName = getIntent().getStringExtra("siteName");
        String siteAddress = getIntent().getStringExtra("siteAddress");

        // Log the siteId to ensure it's being passed correctly
        Log.d(TAG, "Site ID: " + siteId);

        // Set data to UI elements
        siteNameTextView.setText(siteName);
        siteAddressTextView.setText(siteAddress);

        // Always display the create event button
        createEventButton.setVisibility(View.VISIBLE);

        // Load events for this site
        loadEvents(siteId);

        // Set up create event button
        createEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(DonationSiteDetailActivity.this, CreateEventActivity.class);
            intent.putExtra("siteId", siteId);
            startActivity(intent);
        });
    }

    private void loadEvents(String siteId) {
        db.collection("events")
                .whereEqualTo("siteId", siteId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventListLayout.removeAllViews();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            Log.d(TAG, "Event loaded: " + event.getEventName());
                            addEventToLayout(event);
                        }
                    } else {
                        Log.e(TAG, "Error getting events: ", task.getException());
                    }
                });
    }

    private void addEventToLayout(Event event) {
        View eventView = LayoutInflater.from(this).inflate(R.layout.event_item, eventListLayout, false);

        TextView eventNameTextView = eventView.findViewById(R.id.event_name);
        TextView eventDateTextView = eventView.findViewById(R.id.event_date);

        eventNameTextView.setText(event.getEventName());
        eventDateTextView.setText(event.getStartDate().toString());

        eventListLayout.addView(eventView);
    }
}