package com.example.donorhub;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import com.example.donorhub.Models.DonationSite;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.widget.ImageButton;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private List<DonationSite> donationSites = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private float currentZoomLevel = 15.0f; // Initial zoom level
    private LocationCallback locationCallback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Request location permissions
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Set up the zoom out button
        ImageButton zoomOutButton = view.findViewById(R.id.zoom_out_button);
        zoomOutButton.setOnClickListener(v -> zoomOutMap());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch the current location every time the fragment is resumed
        getCurrentLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Remove location updates when the fragment is paused
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                // Handle permission denial
                Log.e("MapFragment", "Location permission denied");
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Enable location layer if permissions are granted
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            Log.d("MapFragment", "Current location: " + currentLocation.toString());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, currentZoomLevel));

                            // Add a custom marker for the user's current location with scaled bitmap
                            mMap.addMarker(new MarkerOptions()
                                    .position(currentLocation)
                                    .title("Your Location")
                                    .icon(bitmapDescriptorFromVector(getContext(), R.drawable.user, 100, 100))); // Adjust width and height as needed

                            loadDonationSites(currentLocation);
                        } else {
                            Log.e("MapFragment", "Current location is null, requesting new location");
                            requestNewLocation();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("MapFragment", "Error getting current location", e));
        } else {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void requestNewLocation() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    Log.d("MapFragment", "New location: " + currentLocation.toString());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, currentZoomLevel));

                    // Add a custom marker for the user's current location with scaled bitmap
                    mMap.addMarker(new MarkerOptions()
                            .position(currentLocation)
                            .title("Your Location")
                            .icon(bitmapDescriptorFromVector(getContext(), R.drawable.user, 100, 100))); // Adjust width and height as needed

                    loadDonationSites(currentLocation);
                    fusedLocationClient.removeLocationUpdates(this);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId, int width, int height) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap);
    }

    private void loadDonationSites(LatLng currentLocation) {
        db.collection("donation_sites")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            DonationSite site = document.toObject(DonationSite.class);
                            LatLng siteLocation = new LatLng(site.getLatitude(), site.getLongitude());

                            // Calculate distance between current location and donation site
                            float[] results = new float[1];
                            Location.distanceBetween(currentLocation.latitude, currentLocation.longitude, site.getLatitude(), site.getLongitude(), results);
                            float distanceInMeters = results[0];

                            // Check if the site is within 10km
                            if (distanceInMeters <= 10000) {
                                donationSites.add(site);
                                mMap.addMarker(new MarkerOptions().position(siteLocation).title(site.getName()).snippet(site.getAddress()));
                            }
                        }
                    } else {
                        Log.e("MapFragment", "Error getting donation sites: ", task.getException());
                    }
                });
    }

    private void zoomOutMap() {
        if (mMap != null) {
            currentZoomLevel -= 1.0f; // Decrease zoom level by 1 each time
            mMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoomLevel));
        }
    }
}