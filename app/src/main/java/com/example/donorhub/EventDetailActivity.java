package com.example.donorhub;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.donorhub.Models.User;
import com.example.donorhub.Models.Report;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import android.app.AlertDialog;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class EventDetailActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;

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
    private String eventName;
    private String siteId;
    private ArrayList<String> userIds; // Declare as class-level variable
    private ArrayList<String> userIdsVolunteer; // Declare as class-level variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_detail_activity);

        // Check and request storage permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
        }

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
        siteId = getIntent().getStringExtra("siteId");
        eventName = getIntent().getStringExtra("eventName");
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

        // Set up generate report button
        generateReportButton.setOnClickListener(v -> generateReport(eventName, siteId));
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

    private void generateReport(String eventName, String siteId) {
        db.collection("reports").document(eventId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Report already exists, do not create a new one
                    Toast.makeText(this, "The report already exists in the database", Toast.LENGTH_LONG).show();
                } else {
                    // Report does not exist, proceed with creating and saving the report
                    int[] bloodAmounts = new int[4]; // Index 0: A, 1: B, 2: O, 3: AB
                    AtomicInteger remainingUsers = new AtomicInteger(userIds.size());

                    for (String userId : userIds) {
                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        User user = documentSnapshot.toObject(User.class);
                                        if (user != null) {
                                            switch (user.getBloodType()) {
                                                case "A":
                                                    bloodAmounts[0] += 350;
                                                    break;
                                                case "B":
                                                    bloodAmounts[1] += 350;
                                                    break;
                                                case "O":
                                                    bloodAmounts[2] += 350;
                                                    break;
                                                case "AB":
                                                    bloodAmounts[3] += 350;
                                                    break;
                                            }
                                        }
                                    }
                                    if (remainingUsers.decrementAndGet() == 0) {
                                        createAndSaveReport(eventName, siteId, bloodAmounts);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (remainingUsers.decrementAndGet() == 0) {
                                        createAndSaveReport(eventName, siteId, bloodAmounts);
                                    }
                                });
                    }
                }
            } else {
                Log.e("EventDetailActivity", "Error checking report existence", task.getException());
                Toast.makeText(this, "Error checking report existence", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createAndSaveReport(String eventName, String siteId, int[] bloodAmounts) {
        Report report = new Report(
                eventId,
                eventId,
                siteId,
                bloodAmounts[0],
                bloodAmounts[1],
                bloodAmounts[2],
                bloodAmounts[3],
                donorCount + volunteerCount,
                donorCount,
                eventName + " Report"
        );

        db.collection("reports").document(report.getId()).set(report)
                .addOnSuccessListener(aVoid -> {
                    Log.d("EventDetailActivity", "Report successfully written!");
                    generateReportFile(report);
                })
                .addOnFailureListener(e -> {
                    Log.e("EventDetailActivity", "Error writing report", e);
                    Toast.makeText(EventDetailActivity.this, "Failed to generate report.", Toast.LENGTH_LONG).show();
                });
    }

    private void generateReportFile(Report report) {
        String reportContent = "Report Title: " + report.getReportTitle() + "\n" +
                "Event ID: " + report.getEventId() + "\n" +
                "Donation Site ID: " + report.getDonationSiteId() + "\n" +
                "Number of Participants: " + report.getNumberOfParticipants() + "\n" +
                "Number of Donors: " + report.getNumberOfDonors() + "\n" +
                "Amount of Blood A: " + report.getAmountOfBloodA() + " ml\n" +
                "Amount of Blood B: " + report.getAmountOfBloodB() + " ml\n" +
                "Amount of Blood O: " + report.getAmountOfBloodO() + " ml\n" +
                "Amount of Blood AB: " + report.getAmountOfBloodAB() + " ml\n";

        File reportFile = new File(getExternalFilesDir(null), report.getReportTitle() + ".txt");

        if (reportFile.exists()) {
            Toast.makeText(this, "The file already exists on your device", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(reportFile);
            fos.write(reportContent.getBytes());
            fos.close();

            Log.d("EventDetailActivity", "Report file generated: " + reportFile.getAbsolutePath());
            Toast.makeText(this, "Report file generated: " + reportFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("EventDetailActivity", "Error generating report file", e);
            Toast.makeText(this, "Error generating report file.", Toast.LENGTH_LONG).show();
        }
    }
}