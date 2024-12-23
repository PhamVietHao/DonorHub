package com.example.donorhub;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.donorhub.Models.Event;
import com.example.donorhub.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DonationSiteHome extends AppCompatActivity {

    private static final String TAG = "DonationSiteDetail";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    private TextView siteNameTextView;
    private TextView siteAddressTextView;
    private LinearLayout eventListLayout;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String siteId;
    private Boolean isAdminSite;
    private double siteLatitude;
    private double siteLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donationsite_detail_home);

        // Initialize UI elements
        siteNameTextView = findViewById(R.id.site_name);
        siteAddressTextView = findViewById(R.id.site_address);
        eventListLayout = findViewById(R.id.event_list);

        ImageButton backButton = findViewById(R.id.donationsite_back_button);
        backButton.setOnClickListener(v -> finish());

        // Initialize Firestore and Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get data from intent
        siteId = getIntent().getStringExtra("siteId");
        String siteName = getIntent().getStringExtra("siteName");
        String siteAddress = getIntent().getStringExtra("siteAddress");
        siteLatitude = getIntent().getDoubleExtra("siteLatitude", 0.0);
        siteLongitude = getIntent().getDoubleExtra("siteLongitude", 0.0);
        isAdminSite = getIntent().getBooleanExtra("isAdminSite", false);

        // Log the siteId to ensure it's being passed correctly
        Log.d(TAG, "Site ID: " + siteId);

        // Set data to UI elements
        siteNameTextView.setText(siteName);
        siteAddressTextView.setText(siteAddress);

        // Request notification permission
        requestNotificationPermission();

        // Load events for this site
        loadEvents(siteId);

        // Set up navigate to map button
        ImageButton navigateToMapButton = findViewById(R.id.navigate_to_map_button);
        navigateToMapButton.setOnClickListener(v -> openMapsGuide());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload events when the activity resumes
        loadEvents(siteId);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.d(TAG, "Notification permission granted");
            } else {
                // Permission denied
                Log.d(TAG, "Notification permission denied");
            }
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

        // Format the dates and times
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String startDate = dateFormat.format(event.getStartDate());
        String startTime = timeFormat.format(event.getStartTime());
        String endTime = timeFormat.format(event.getEndTime());

        eventNameTextView.setText(event.getEventName());
        eventDateTextView.setText("Date: " + startDate);
        eventTimeTextView.setText("Time: " + startTime + " - " + endTime);
        eventBloodTypesTextView.setText("Required: " + String.join(", ", event.getBloodTypes()));

        // Combine date and time for comparison
        try {
            Date eventEndDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .parse(startDate + " " + endTime);
            Date currentDate = new Date();

            // Check if the event has ended
            if (currentDate.after(eventEndDateTime)) {
                eventStatusTextView.setText("The event has ended.");
                joinAsDonorButton.setVisibility(View.GONE);
                joinAsVolunteerButton.setVisibility(View.GONE);
                cancelJoiningButton.setVisibility(View.GONE);
            } else {
                // Get current user
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid();

                    // Check if the user's blood type is suitable for the event
                    db.collection("users").document(userId).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    User user = documentSnapshot.toObject(User.class);
                                    if (user != null && !event.getBloodTypes().contains("ALL") && !event.getBloodTypes().contains(user.getBloodType())) {
                                        eventStatusTextView.setText("Your blood type is not suitable for this event.");
                                        joinAsDonorButton.setVisibility(View.GONE);
                                        joinAsVolunteerButton.setVisibility(View.GONE);
                                        cancelJoiningButton.setVisibility(View.GONE);
                                    } else {
                                        // Check if the user is already a donor or volunteer for this event
                                        if (event.getUserIds().contains(userId) || event.getUserIdsVolunteer().contains(userId)) {
                                            cancelJoiningButton.setVisibility(View.VISIBLE);
                                        } else if (isAdminSite) {
                                            cancelJoiningButton.setVisibility(View.GONE);
                                            joinAsDonorButton.setVisibility(View.VISIBLE);
                                            joinAsVolunteerButton.setVisibility(View.VISIBLE);
                                        } else {
                                            joinAsDonorButton.setVisibility(View.VISIBLE);
                                            joinAsVolunteerButton.setVisibility(View.GONE);
                                        }

                                        // Set up button click listeners
                                        joinAsDonorButton.setOnClickListener(v -> showConfirmationDialog(event, userId, "donor"));
                                        joinAsVolunteerButton.setOnClickListener(v -> showConfirmationDialog(event, userId, "volunteer"));
                                        cancelJoiningButton.setOnClickListener(v -> showConfirmationDialog(event, userId, "cancel"));
                                    }
                                }
                            });
                }
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date and time", e);
        }

        // Schedule notifications for event start and end
        scheduleEventNotifications(event);

        eventListLayout.addView(eventView);
    }

    private void showConfirmationDialog(Event event, String userId, String action) {
        String message;
        if (action.equals("cancel")) {
            message = "Are you sure you want to cancel your participation?";
        } else {
            message = "Are you sure you want to join as " + action + "?";
        }

        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> handleUserAction(event, userId, action))
                .setNegativeButton("No", (dialog, which) -> onResume())
                .show();
    }private void handleUserAction(Event event, String userId, String action) {
        if (action.equals("donor")) {
            event.getUserIds().add(userId);
            db.collection("events").document(event.getId()).update("userIds", event.getUserIds())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "User joined as donor");
                        checkDonorMilestones(event);
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error joining as donor", e));
        } else if (action.equals("volunteer")) {
            event.getUserIdsVolunteer().add(userId);
            db.collection("events").document(event.getId()).update("userIdsVolunteer", event.getUserIdsVolunteer())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User joined as volunteer"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error joining as volunteer", e));
        } else if (action.equals("cancel")) {
            event.getUserIds().remove(userId);
            event.getUserIdsVolunteer().remove(userId);
            db.collection("events").document(event.getId()).update("userIds", event.getUserIds(), "userIdsVolunteer", event.getUserIdsVolunteer())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User cancelled participation"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error cancelling participation", e));
        }
        onResume();
    }

    private void checkDonorMilestones(Event event) {
        int donorCount = event.getUserIds().size();
        if (donorCount == 10 || donorCount == 20 || donorCount == 30 || donorCount == 40 || donorCount == 50) {
            sendNotification("Milestone Reached!", "Yay! The event has reached " + donorCount + " donors!");
        }
    }

    private void sendNotification(String title, String message) {
        String channelId = "donorhub_notifications";
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "DonorHub Notifications";
            String description = "Notifications for DonorHub app";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManagerSystem = getSystemService(NotificationManager.class);
            notificationManagerSystem.createNotificationChannel(channel);
        }

        // Check for notification permission only if API level is 33 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, handle accordingly
                Log.d(TAG, "Notification permission not granted");
                return;
            }
        }

        Intent intent = new Intent(this, DonationSiteHome.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }

    private void scheduleEventNotifications(Event event) {
        long startTime = event.getStartTime().getTime();
        long endTime = event.getEndTime().getTime();

        scheduleNotification("Event Starting", "The event " + event.getEventName() + " is starting now!", startTime);
        scheduleNotification("Event Ending", "The event " + event.getEventName() + " has ended.", endTime);
    }

    private void scheduleNotification(String title, String message, long time) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

    private void openMapsGuide() {
        Intent intent = new Intent(DonationSiteHome.this, MapsGuideActivity.class);
        intent.putExtra("siteLatitude", siteLatitude);
        intent.putExtra("siteLongitude", siteLongitude);
        startActivity(intent);
    }
}