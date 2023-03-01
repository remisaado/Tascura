package com.task.tascura;

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
        // Get method for the Firebase database reference initialization to prevent duplication.
        if (mAuth.getCurrentUser() != null)
        {
            String userId = mAuth.getCurrentUser().getUid();
            return FirebaseDatabase.getInstance().getReference(DatabaseNodes.USERS).child(userId);
        }
        return null;
    }
}
