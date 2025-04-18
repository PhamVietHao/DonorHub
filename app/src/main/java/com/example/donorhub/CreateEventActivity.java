package com.example.donorhub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.donorhub.Models.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateEventActivity extends AppCompatActivity {

    private EditText eventNameEditText, startDateEditText, startTimeEditText, endTimeEditText, bloodTypesEditText;
    private Button createEventButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String siteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_regist);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        eventNameEditText = findViewById(R.id.eventNameEditText);
        startDateEditText = findViewById(R.id.startDateEditText);
        startTimeEditText = findViewById(R.id.startTimeEditText);
        endTimeEditText = findViewById(R.id.endTimeEditText);
        bloodTypesEditText = findViewById(R.id.bloodTypesEditText);
        createEventButton = findViewById(R.id.createEventButton);

        // Back button logic
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Create event button logic
        createEventButton.setOnClickListener(v -> handleCreateEvent());

        // Get site ID from intent
        siteId = getIntent().getStringExtra("siteId");
    }

    private void handleCreateEvent() {
        String eventName = eventNameEditText.getText().toString().trim();
        String startDateStr = startDateEditText.getText().toString().trim();
        String startTimeStr = startTimeEditText.getText().toString().trim();
        String endTimeStr = endTimeEditText.getText().toString().trim();
        String bloodTypesStr = bloodTypesEditText.getText().toString().trim();

        if (eventName.isEmpty() || startDateStr.isEmpty() || startTimeStr.isEmpty() || endTimeStr.isEmpty() || bloodTypesStr.isEmpty()) {
            Toast.makeText(CreateEventActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date startDate, startTime, endTime;
        try {
            startDate = dateFormat.parse(startDateStr);
            startTime = timeFormat.parse(startTimeStr);
            endTime = timeFormat.parse(endTimeStr);

            // Combine date and time for start time
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(startDate);
            Calendar startTimeCalendar = Calendar.getInstance();
            startTimeCalendar.setTime(startTime);
            startCalendar.set(Calendar.HOUR_OF_DAY, startTimeCalendar.get(Calendar.HOUR_OF_DAY));
            startCalendar.set(Calendar.MINUTE, startTimeCalendar.get(Calendar.MINUTE));
            Date combinedStartTime = startCalendar.getTime();

            // Combine date and time for end time
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(startDate); // Assuming the event ends on the same day
            Calendar endTimeCalendar = Calendar.getInstance();
            endTimeCalendar.setTime(endTime);
            endCalendar.set(Calendar.HOUR_OF_DAY, endTimeCalendar.get(Calendar.HOUR_OF_DAY));
            endCalendar.set(Calendar.MINUTE, endTimeCalendar.get(Calendar.MINUTE));
            Date combinedEndTime = endCalendar.getTime();

            List<String> bloodTypes = Arrays.asList(bloodTypesStr.split(","));
            // Trim each blood type
            for (int i = 0; i < bloodTypes.size(); i++) {
                bloodTypes.set(i, bloodTypes.get(i).trim());
            }

            Event event = new Event(
                    null,
                    siteId,
                    eventName,
                    null, // No user IDs
                    startDate,
                    combinedStartTime,
                    combinedEndTime,
                    bloodTypes,
                    null
            );

            // Create a new document reference with an auto-generated ID
            db.collection("events").add(event)
                    .addOnSuccessListener(documentReference -> {
                        // Get the auto-generated ID
                        String id = documentReference.getId();
                        // Update the event with the ID
                        documentReference.update("id", id)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(CreateEventActivity.this, "Event created successfully", Toast.LENGTH_SHORT).show();
                                    finish(); // Close the activity
                                })
                                .addOnFailureListener(e -> Toast.makeText(CreateEventActivity.this, "Error updating event ID", Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(CreateEventActivity.this, "Error creating event", Toast.LENGTH_SHORT).show());

        } catch (ParseException e) {
            Toast.makeText(CreateEventActivity.this, "Invalid date or time format", Toast.LENGTH_SHORT).show();
        }
    }
}