package com.example.donorhub;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.donorhub.Models.Event;
import com.example.donorhub.Models.Report;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DonationSiteDetailActivity extends AppCompatActivity {

    private static final String TAG = "DonationSiteDetail";
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;

    private TextView siteNameTextView;
    private TextView siteAddressTextView;
    private LinearLayout eventListLayout;
    private Button createEventButton;
    private ImageButton generateReportButton;
    private ImageButton navigateToMapButton;
    private FirebaseFirestore db;
    private String siteId;
    private double siteLatitude;
    private double siteLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_site_detail);

        // Create notification channel
        createNotificationChannel();

        // Initialize UI elements
        siteNameTextView = findViewById(R.id.site_name);
        siteAddressTextView = findViewById(R.id.site_address);
        eventListLayout = findViewById(R.id.event_list);
        createEventButton = findViewById(R.id.create_event_button);
        generateReportButton = findViewById(R.id.generate_donationsite_report_button);
        navigateToMapButton = findViewById(R.id.navigate_to_map_button);

        ImageButton backButton = findViewById(R.id.donationsite_back_button);
        backButton.setOnClickListener(v -> finish());

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get data from intent
        siteId = getIntent().getStringExtra("siteId");
        String siteName = getIntent().getStringExtra("siteName");
        String siteAddress = getIntent().getStringExtra("siteAddress");
        siteLatitude = getIntent().getDoubleExtra("siteLatitude", 0.0);
        siteLongitude = getIntent().getDoubleExtra("siteLongitude", 0.0);

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

        // Set up generate report button
        generateReportButton.setOnClickListener(v -> {
            // Check and request storage permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
            } else {
                generateReport();
            }
        });

        // Set up navigate to map button
        navigateToMapButton.setOnClickListener(v -> openMapsGuide());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents(siteId);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            generateReport();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
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
        TextView eventTimeTextView = eventView.findViewById(R.id.event_time);
        TextView eventBloodTypesTextView = eventView.findViewById(R.id.event_blood_types);
        TextView eventStatusTextView = eventView.findViewById(R.id.event_status);
        Button joinAsDonorButton = eventView.findViewById(R.id.join_as_donor_button);
        Button joinAsVolunteerButton = eventView.findViewById(R.id.join_as_volunteer_button);
        Button cancelJoiningButton = eventView.findViewById(R.id.cancel_joining_button);
        Button deleteEventButton = eventView.findViewById(R.id.delete_event_button);

        eventNameTextView.setText(event.getEventName());

        // Format the date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String formattedDate = dateFormat.format(event.getStartDate());
        String formattedStartTime = timeFormat.format(event.getStartTime());
        String formattedEndTime = timeFormat.format(event.getEndTime());

        eventDateTextView.setText("Date: " + formattedDate);
        eventTimeTextView.setText("Time: " + formattedStartTime + " - " + formattedEndTime);

        // Display the required blood types
        eventBloodTypesTextView.setText("Required Blood Types: " + String.join(", ", event.getBloodTypes()));
        eventBloodTypesTextView.setVisibility(View.VISIBLE);

        // Hide the buttons and event status
        joinAsDonorButton.setVisibility(View.GONE);
        joinAsVolunteerButton.setVisibility(View.GONE);
        cancelJoiningButton.setVisibility(View.GONE);
        eventStatusTextView.setVisibility(View.GONE);

        // Check if the event has ended
        Date currentDate = new Date();
        if (currentDate.after(event.getStartDate()) && currentDate.after(event.getEndTime())) {
            deleteEventButton.setVisibility(View.GONE);
        } else {
            deleteEventButton.setVisibility(View.VISIBLE);
            deleteEventButton.setOnClickListener(v -> showDeleteConfirmationDialog(event.getId(), event.getEventName()));
        }

        eventView.setOnClickListener(v -> {
            Intent intent = new Intent(DonationSiteDetailActivity.this, EventDetailActivity.class);
            intent.putExtra("eventId", event.getId());
            intent.putExtra("siteId", event.getSiteId());
            intent.putExtra("eventName", event.getEventName());
            intent.putExtra("startDate", formattedDate);
            intent.putExtra("startTime", formattedStartTime);
            intent.putExtra("endTime", formattedEndTime);
            intent.putStringArrayListExtra("userIds", new ArrayList<>(event.getUserIds()));
            intent.putStringArrayListExtra("bloodTypes", new ArrayList<>(event.getBloodTypes()));
            intent.putStringArrayListExtra("userIdsVolunteer", new ArrayList<>(event.getUserIdsVolunteer()));
            startActivity(intent);
        });

        eventListLayout.addView(eventView);
    }

    private void showDeleteConfirmationDialog(String eventId, String eventName) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteEvent(eventId, eventName))
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void deleteEvent(String eventId, String eventName) {
        db.collection("events").document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Event successfully deleted!");
                    Toast.makeText(DonationSiteDetailActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
                    loadEvents(siteId); // Refresh the event list

                    // Send a broadcast to notify that the event has been deleted
                    sendEventDeletedNotification(eventName);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error deleting event", e);
                    Toast.makeText(DonationSiteDetailActivity.this, "Error deleting event", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendEventDeletedNotification(String eventName) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("title", "Event Cancelled");
        intent.putExtra("message", "The event \"" + eventName + "\" has been cancelled. We sincerely apologize for any inconvenience.");
        sendBroadcast(intent);
    }

    private void generateReport() {
        db.collection("reports")
                .whereEqualTo("donationSiteId", siteId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int totalBloodA = 0;
                        int totalBloodB = 0;
                        int totalBloodO = 0;
                        int totalBloodAB = 0;
                        int totalDonors = 0;
                        int totalVolunteers = 0;

                        StringBuilder reportContent = new StringBuilder("Donation Site: " + siteNameTextView.getText().toString() + "\n\n");

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Report report = document.toObject(Report.class);
                            totalBloodA += report.getAmountOfBloodA();
                            totalBloodB += report.getAmountOfBloodB();
                            totalBloodO += report.getAmountOfBloodO();
                            totalBloodAB += report.getAmountOfBloodAB();
                            totalDonors += report.getNumberOfDonors();
                            totalVolunteers += (report.getNumberOfParticipants() - report.getNumberOfDonors());

                            reportContent.append("Report Title: ").append(report.getReportTitle()).append("\n")
                                    .append("Blood A: ").append(report.getAmountOfBloodA()).append(" ml\n")
                                    .append("Blood B: ").append(report.getAmountOfBloodB()).append(" ml\n")
                                    .append("Blood O: ").append(report.getAmountOfBloodO()).append(" ml\n")
                                    .append("Blood AB: ").append(report.getAmountOfBloodAB()).append(" ml\n")
                                    .append("Donors: ").append(report.getNumberOfDonors()).append("\n")
                                    .append("Volunteers: ").append(report.getNumberOfParticipants() - report.getNumberOfDonors()).append("\n\n");
                        }

                        reportContent.append("Total Blood A: ").append(totalBloodA).append(" ml\n")
                                .append("Total Blood B: ").append(totalBloodB).append(" ml\n")
                                .append("Total Blood O: ").append(totalBloodO).append(" ml\n")
                                .append("Total Blood AB: ").append(totalBloodAB).append(" ml\n")
                                .append("Total Donors: ").append(totalDonors).append("\n")
                                .append("Total Volunteers: ").append(totalVolunteers).append("\n");

                        showDownloadConfirmationDialog(siteNameTextView.getText().toString(), reportContent.toString());
                    } else {
                        Log.e(TAG, "Error getting reports: ", task.getException());
                        Toast.makeText(this, "Error getting reports", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDownloadConfirmationDialog(String siteName, String reportContent) {
        new AlertDialog.Builder(this)
                .setTitle("Download Report")
                .setMessage("Do you want to download the report file?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> saveReportToFile(siteName, reportContent))
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void saveReportToFile(String siteName, String reportContent) {
        String fileName = siteName + "_Report.txt";
        File reportFile = new File(getExternalFilesDir(null), fileName);

        try {
            FileOutputStream fos = new FileOutputStream(reportFile);
            fos.write(reportContent.getBytes());
            fos.close();

            Log.d(TAG, "Report file generated: " + reportFile.getAbsolutePath());
            Toast.makeText(this, "Report file generated: " + reportFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e(TAG, "Error generating report file", e);
            Toast.makeText(this, "Error generating report file.", Toast.LENGTH_LONG).show();
        }
    }

    private void openMapsGuide() {
        Intent intent = new Intent(DonationSiteDetailActivity.this, MapsGuideActivity.class);
        intent.putExtra("siteLatitude", siteLatitude);
        intent.putExtra("siteLongitude", siteLongitude);
        startActivity(intent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "DonorHub Notifications";
            String description = "Channel for DonorHub notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("donorhub_notifications", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}