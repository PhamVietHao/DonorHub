package com.example.donorhub;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DonationSiteDetailActivity extends AppCompatActivity {

    private TextView siteNameTextView;
    private TextView siteAddressTextView;
    private TextView siteBloodTypesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_site_detail);

        // Initialize UI elements
        siteNameTextView = findViewById(R.id.site_name);
        siteAddressTextView = findViewById(R.id.site_address);
        siteBloodTypesTextView = findViewById(R.id.site_blood_types);

        // Get data from intent
        String siteName = getIntent().getStringExtra("siteName");
        String siteAddress = getIntent().getStringExtra("siteAddress");
        String siteBloodTypes = getIntent().getStringExtra("siteBloodTypes");

        // Set data to UI elements
        siteNameTextView.setText(siteName);
        siteAddressTextView.setText(siteAddress);
        siteBloodTypesTextView.setText(siteBloodTypes);
    }
}
