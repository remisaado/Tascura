package com.example.testapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;

public class RenameListActivity extends AppCompatActivity {

    EditText listEditText;
    View renameListButton;
    View backButton;
    FirebaseAuth mAuth;
    private FirebaseHelper firebaseHelper;

    ArrayList<Category> categories = new ArrayList<>();
    int spinnerPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rename_list);

        listEditText = findViewById(R.id.listEditText);
        renameListButton = findViewById(R.id.renameListButton);
        backButton = findViewById(R.id.backButton);

        mAuth = FirebaseAuth.getInstance();

        firebaseHelper = new FirebaseHelper();

        Intent intent = getIntent();
        categories = intent.getParcelableArrayListExtra(MainActivity.KEY_NAME);
        spinnerPosition = intent.getIntExtra(MainActivity.KEY_NAME_TWO, 0);

        String spinnerValue = categories.get(spinnerPosition).getCategoryName();
        listEditText.setText(spinnerValue);

        listEditText.setOnEditorActionListener(editorActionListener);

        renameListButton.setOnClickListener(v -> onRenameListClick());

        backButton.setOnClickListener(v -> finish());
    }

    private final TextView.OnEditorActionListener editorActionListener = (v, actionId, event) -> {
        onRenameListClick();

        return true;
    };

    private void onRenameListClick()
    {
        String categoryId = categories.get(spinnerPosition).getCategoryId();

        DatabaseReference databaseReference = firebaseHelper.getDatabaseReference()
                .child(categoryId);

        final String text = listEditText.getText().toString();

        HashMap<String, Object> newValue = new HashMap<>();
        newValue.put(categoryId, text);

        if (text.trim().length() > 0)
        {
            categories.get(spinnerPosition).setCategoryName(text);
            databaseReference.updateChildren(newValue);

            finish();
        }
        else
        {
            listEditText.setError(getString(R.string.error_no_text_entered));
            listEditText.requestFocus();
        }
    }
}