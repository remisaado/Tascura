package com.example.testapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddListActivity extends AppCompatActivity {

    EditText listEditText;
    View addListButton;
    View backButton;
    FirebaseAuth mAuth;

    ArrayList<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_list);

        listEditText = findViewById(R.id.listEditText);
        addListButton = findViewById(R.id.addListButton);
        backButton = findViewById(R.id.backButton);

        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        categories = intent.getParcelableArrayListExtra(MainActivity.KEY_NAME);

        listEditText.setOnEditorActionListener(editorActionListener);

        addListButton.setOnClickListener(v -> onAddListClick());

        backButton.setOnClickListener(v -> finish());
    }

    private final TextView.OnEditorActionListener editorActionListener = (v, actionId, event) -> {
        onAddListClick();

        return true;
    };

    private void onAddListClick()
    {
        if (mAuth.getCurrentUser() != null)
        {
            String userId = mAuth.getCurrentUser().getUid();
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(userId);
            final String text = listEditText.getText().toString();

            databaseReference.orderByValue().equalTo(text).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                    {
                        listEditText.setError("This list already exists");
                        listEditText.requestFocus();
                    }
                    else
                    {
                        if (text.trim().length() > 0)
                        {
                            String autoGeneratedId = databaseReference.push().getKey();

                            if (autoGeneratedId != null)
                            {

                                categories.add(new Category(text, autoGeneratedId));

                                listEditText.getText().clear();

                                databaseReference.child(autoGeneratedId).child(autoGeneratedId).setValue(text);

                                SharedPreferences sharedPrefs = getSharedPreferences(MainActivity.SHARED_PREFS, 0);
                                SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();
                                sharedPrefsEditor.putInt(MainActivity.SPINNER_CHOICE, categories.size() - 1);
                                sharedPrefsEditor.apply();

                                finish();
                            }
                        }
                        else
                        {
                            listEditText.setError("You did not enter any text");
                            listEditText.requestFocus();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
