package com.example.testapplication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {

    private final FirebaseAuth mAuth;

    public FirebaseHelper()
    {
        mAuth = FirebaseAuth.getInstance();
    }

    public DatabaseReference getDatabaseReference()
    {
        if (mAuth.getCurrentUser() != null)
        {
            String userId = mAuth.getCurrentUser().getUid();
            return FirebaseDatabase.getInstance().getReference("Users").child(userId);
        }
        return null;
    }
}
