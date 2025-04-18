package com.example.donorhub;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.PolyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MapsGuideActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String TAG = "MapsGuideActivity";
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private LatLng userLocation;
    private LatLng siteLocation;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            // Get the site location from the intent
            double siteLatitude = getIntent().getDoubleExtra("siteLatitude", 0.0);
            double siteLongitude = getIntent().getDoubleExtra("siteLongitude", 0.0);
            siteLocation = new LatLng(siteLatitude, siteLongitude);

            // Log the site location
            Log.d(TAG, "Site Latitude: " + siteLatitude + ", Site Longitude: " + siteLongitude);

            // Add a marker at the site location
            mMap.addMarker(new MarkerOptions().position(siteLocation).title("Donation Site"));

            // Get the user's current location
            getUserLocation();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_guide);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        // Set up back button
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish();
        });

        // Set up home button
        ImageButton homeButton = findViewById(R.id.home_button);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MapsGuideActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserLocation();
    }

    private void getUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.clear(); // Clear existing markers and polylines
                    mMap.addMarker(new MarkerOptions()
                            .position(userLocation)
                            .title("Your Location")
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("user", 100, 100))));

                    // Add a marker at the site location again
                    mMap.addMarker(new MarkerOptions().position(siteLocation).title("Donation Site"));

                    // Fetch and draw the route from user location to site location
                    fetchRoute(userLocation, siteLocation);

                    // Adjust the zoom level to show both locations
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(userLocation);
                    builder.include(siteLocation);
                    LatLngBounds bounds = builder.build();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                } else {
                    Toast.makeText(MapsGuideActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private Bitmap resizeBitmap(String drawableName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(drawableName, "drawable", getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    private void fetchRoute(LatLng origin, LatLng destination) {
        String apiKey = getString(R.string.google_maps_key); // Your API key
        String urlString = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin.latitude + "," + origin.longitude + "&destination=" + destination.latitude + "," + destination.longitude + "&key=" + apiKey;

        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                Log.d(TAG, "Directions API response: " + jsonResponse.toString()); // Log the response
                JSONArray routes = jsonResponse.getJSONArray("routes");
                if (routes.length() > 0) {
                    JSONObject route = routes.getJSONObject(0);
                    JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                    String points = overviewPolyline.getString("points");
                    runOnUiThread(() -> drawRoute(points));
                } else {
                    Log.d(TAG, "No routes found");
                    runOnUiThread(() -> drawFallbackRoute());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching route: " + e.getMessage());
                runOnUiThread(() -> drawFallbackRoute());
            }
        }).start();
    }

    private void drawRoute(String encodedPolyline) {
        List<LatLng> points = PolyUtil.decode(encodedPolyline);
        if (points != null && !points.isEmpty()) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(points)
                    .color(ContextCompat.getColor(this, R.color.bold_pink))
                    .width(5);
            mMap.addPolyline(polylineOptions);
            Log.d(TAG, "Route drawn successfully");
        } else {
            Log.d(TAG, "No points to draw");
        }
    }

    private void drawFallbackRoute() {
        if (userLocation != null && siteLocation != null) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(userLocation, siteLocation)
                    .color(ContextCompat.getColor(this, R.color.bold_pink))
                    .width(5);
            mMap.addPolyline(polylineOptions);
            Log.d(TAG, "Fallback route drawn successfully");
        } else {
            Log.d(TAG, "User or site location is null, cannot draw fallback route");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}