package com.example.donorhub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.donorhub.Models.DonationSite;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;

public class RegisterDonationSiteActivity extends AppCompatActivity {

    private EditText eventNameEditText, addressEditText, bloodTypesEditText, latitudeEditText, longitudeEditText;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.donationsite_regist);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        eventNameEditText = findViewById(R.id.eventNameEditText);
        addressEditText = findViewById(R.id.addressEditText);
        bloodTypesEditText = findViewById(R.id.bloodTypesEditText);
        latitudeEditText = findViewById(R.id.latitudeEditText);
        longitudeEditText = findViewById(R.id.longitudeEditText);
        registerButton = findViewById(R.id.registerButton);

        // Back button logic
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Register button logic
        registerButton.setOnClickListener(v -> handleRegister());
    }

    private void handleRegister() {
        String eventName = eventNameEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String bloodTypes = bloodTypesEditText.getText().toString().trim();
        String latitudeStr = latitudeEditText.getText().toString().trim();
        String longitudeStr = longitudeEditText.getText().toString().trim();

        if (eventName.isEmpty() || address.isEmpty() || bloodTypes.isEmpty() || latitudeStr.isEmpty() || longitudeStr.isEmpty()) {
            Toast.makeText(RegisterDonationSiteActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double latitude = Double.parseDouble(latitudeStr);
        double longitude = Double.parseDouble(longitudeStr);
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String adminId = firebaseUser != null ? firebaseUser.getUid() : null;

        // Create a new document reference with an auto-generated ID
        db.collection("donation_sites").add(new DonationSite(
                null,
                eventName,
                address,
                Arrays.asList(bloodTypes.split(",")),
                latitude,
                longitude,
                adminId
        )).addOnSuccessListener(documentReference -> {
            // Get the auto-generated ID
            String id = documentReference.getId();
            // Update the donation site with the ID
            documentReference.update("id", id)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(RegisterDonationSiteActivity.this, "Donation site registered successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity
                    })
                    .addOnFailureListener(e -> Toast.makeText(RegisterDonationSiteActivity.this, "Error updating donation site ID", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(RegisterDonationSiteActivity.this, "Error registering donation site", Toast.LENGTH_SHORT).show());
    }
}