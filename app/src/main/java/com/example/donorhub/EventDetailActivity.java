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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;

public class EventDetailActivity extends AppCompatActivity {

    private TextView eventNameTextView;
    private TextView eventDateTextView;
    private TextView eventTimeTextView;
    private LinearLayout participantListLayout;
    private Button generateReportButton;
    private ImageButton backbutton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_detail_activity);

        // Initialize UI elements
        eventNameTextView = findViewById(R.id.event_name);
        eventDateTextView = findViewById(R.id.event_date);
        eventTimeTextView = findViewById(R.id.event_time);
        participantListLayout = findViewById(R.id.participant_list);
        generateReportButton = findViewById(R.id.generate_report_button);
        backbutton = findViewById(R.id.back_button);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get data from intent
        String eventId = getIntent().getStringExtra("eventId");
        String siteId = getIntent().getStringExtra("siteId");
        String eventName = getIntent().getStringExtra("eventName");
        String startDate = getIntent().getStringExtra("startDate");
        String startTime = getIntent().getStringExtra("startTime");
        String endTime = getIntent().getStringExtra("endTime");
        ArrayList<String> userIds = getIntent().getStringArrayListExtra("userIds");
        ArrayList<String> bloodTypes = getIntent().getStringArrayListExtra("bloodTypes");
        ArrayList<String> userIdsVolunteer = getIntent().getStringArrayListExtra("userIdsVolunteer");

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
        loadParticipants(userIds);

        backbutton.setOnClickListener(v -> finish());

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

    private void loadParticipants(ArrayList<String> userIds) {
        for (String userId : userIds) {
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                addParticipantToLayout(user.getName(), user.getBloodType());
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors
                    });
        }
    }

    private void addParticipantToLayout(String name, String bloodType) {
        View participantView = LayoutInflater.from(this).inflate(R.layout.participant_item, participantListLayout, false);

        TextView participantNameTextView = participantView.findViewById(R.id.participant_name);
        TextView participantBloodTypeTextView = participantView.findViewById(R.id.participant_bloodtype);
        Button actionButton = participantView.findViewById(R.id.action_button);

        participantNameTextView.setText(name);
        participantBloodTypeTextView.setText(bloodType);

        actionButton.setOnClickListener(v -> {
            // Implement action button functionality here
        });

        participantListLayout.addView(participantView);
    }
}