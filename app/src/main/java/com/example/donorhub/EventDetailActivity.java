package com.example.donorhub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.donorhub.Models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import android.app.AlertDialog;
import android.util.Log;

public class EventDetailActivity extends AppCompatActivity {

    private TextView eventNameTextView;
    private TextView eventDateTextView;
    private TextView eventTimeTextView;
    private LinearLayout participantListLayout;
    private LinearLayout volunteerParticipantListLayout;
    private Button generateReportButton;
    private TextView eventStatusTextView;
    private ImageButton backbutton;
    private FirebaseFirestore db;

    private int donorCount = 0;
    private int volunteerCount = 0;
    private String eventId;
    private ArrayList<String> userIds; // Declare as class-level variable
    private ArrayList<String> userIdsVolunteer; // Declare as class-level variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_detail_activity);

        // Initialize UI elements
        eventNameTextView = findViewById(R.id.event_name);
        eventDateTextView = findViewById(R.id.event_date);
        eventTimeTextView = findViewById(R.id.event_time);
        participantListLayout = findViewById(R.id.participant_list);
        volunteerParticipantListLayout = findViewById(R.id.volunteer_participant_list);
        generateReportButton = findViewById(R.id.generate_report_button);
        eventStatusTextView = findViewById(R.id.event_status_text);
        backbutton = findViewById(R.id.back_button);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get data from intent
        eventId = getIntent().getStringExtra("eventId");
        String siteId = getIntent().getStringExtra("siteId");
        String eventName = getIntent().getStringExtra("eventName");
        String startDate = getIntent().getStringExtra("startDate");
        String startTime = getIntent().getStringExtra("startTime");
        String endTime = getIntent().getStringExtra("endTime");
        userIds = getIntent().getStringArrayListExtra("userIds"); // Initialize here
        ArrayList<String> bloodTypes = getIntent().getStringArrayListExtra("bloodTypes");
        userIdsVolunteer = getIntent().getStringArrayListExtra("userIdsVolunteer"); // Initialize here

        // Format date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        String formattedDate = formatDate(startDate, dateFormat);
        String formattedStartTime = formatTime(startTime, timeFormat);
        String formattedEndTime = formatTime(endTime, timeFormat);

        // Set data to UI elements
        eventNameTextView.setText(eventName);
        eventDateTextView.setText("Date: " + formattedDate);
        eventTimeTextView.setText("Time: " + formattedStartTime + " - " + formattedEndTime);

        // Load participants
        loadParticipants(userIds, userIdsVolunteer);

        backbutton.setOnClickListener(v -> finish());

        // Check if event has ended
        checkEventStatus(startDate, endTime);

        // Set up generate report button (placeholder functionality)
        generateReportButton.setOnClickListener(v -> {
            // Implement report generation logic here
        });
    }

    private String formatDate(String dateString, SimpleDateFormat dateFormat) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString);
            return dateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return dateString;
        }
    }

    private String formatTime(String timeString, SimpleDateFormat timeFormat) {
        try {
            Date time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(timeString);
            return timeFormat.format(time);
        } catch (Exception e) {
            e.printStackTrace();
            return timeString;
        }
    }

    private void loadParticipants(ArrayList<String> userIds, ArrayList<String> userIdsVolunteer) {
        // Clear existing views
        participantListLayout.removeAllViews();
        volunteerParticipantListLayout.removeAllViews();

        // Reset counts
        donorCount = 0;
        volunteerCount = 0;

        // Load donors
        for (String userId : userIds) {
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                addParticipantToLayout(user.getName(), user.getBloodType(), "Donor", userId);
                                donorCount++;
                                updateParticipantCount();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors
                    });
        }

        // Load volunteers
        for (String userId : userIdsVolunteer) {
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                addParticipantToLayout(user.getName(), user.getBloodType(), "Volunteer", userId);
                                volunteerCount++;
                                updateParticipantCount();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors
                    });
        }
    }

    private void addParticipantToLayout(String name, String bloodType, String role, String userId) {
        View participantView = LayoutInflater.from(this).inflate(R.layout.participant_item,
                role.equals("Donor") ? participantListLayout : volunteerParticipantListLayout, false);

        TextView participantNameTextView = participantView.findViewById(R.id.participant_name);
        TextView participantBloodTypeTextView = participantView.findViewById(R.id.participant_bloodtype);
        Button removeParticipantButton = participantView.findViewById(R.id.remove_participant_button);

        participantNameTextView.setText(name);
        participantBloodTypeTextView.setText(bloodType);

        removeParticipantButton.setOnClickListener(v -> {
            // Show a warning dialog
            new AlertDialog.Builder(this)
                    .setTitle("Remove Participant")
                    .setMessage("Are you sure you want to remove this participant?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // Remove participant from UI
                        if (role.equals("Donor")) {
                            participantListLayout.removeView(participantView);
                            donorCount--;
                        } else {
                            volunteerParticipantListLayout.removeView(participantView);
                            volunteerCount--;
                        }
                        updateParticipantCount();

                        // Remove participant from Firestore
                        removeParticipantFromFirestore(userId, role.equals("Donor") ? "userIds" : "userIdsVolunteer");
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        });

        if (role.equals("Donor")) {
            participantListLayout.addView(participantView);
        } else {
            volunteerParticipantListLayout.addView(participantView);
        }
    }

    private void removeParticipantFromFirestore(String userId, String listName) {
        db.collection("events").document(eventId)
                .update(listName, FieldValue.arrayRemove(userId))
                .addOnSuccessListener(aVoid -> {
                    Log.d("EventDetailActivity", "Successfully removed participant from Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e("EventDetailActivity", "Error removing participant from Firestore", e);
                });
    }

    private void updateParticipantCount() {
        TextView donorCountTextView = findViewById(R.id.donor_count);
        TextView volunteerCountTextView = findViewById(R.id.volunteer_count);

        donorCountTextView.setText("Donors: " + donorCount);
        volunteerCountTextView.setText("Volunteers: " + volunteerCount);
    }

    private void checkEventStatus(String startDate, String endTime) {
        try {
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date eventEndDateTime = dateTimeFormat.parse(startDate + " " + endTime);
            Date currentDateTime = new Date();

            if (currentDateTime.after(eventEndDateTime)) {
                generateReportButton.setVisibility(View.VISIBLE);
                eventStatusTextView.setVisibility(View.GONE);
            } else {
                generateReportButton.setVisibility(View.GONE);
                eventStatusTextView.setText("The event has not ended");
                eventStatusTextView.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}