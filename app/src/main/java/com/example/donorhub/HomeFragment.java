package com.example.donorhub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
public class HomeFragment extends Fragment {

    private TextView greetingText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        greetingText = view.findViewById(R.id.greeting_text);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String name = documentSnapshot.getString("name");
                                greetingText.setText("Hello " + name);
                            } else {
                                greetingText.setText("Hello User");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            greetingText.setText("Hello User");
                        }
                    });
        } else {
            greetingText.setText("Hello User");
        }

        return view;
    }
}