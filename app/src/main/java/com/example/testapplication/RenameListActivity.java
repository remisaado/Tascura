package com.example.testapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class RenameListActivity extends AppCompatActivity {

    EditText listEditText;
    View renameListButton;
    View backButton;
    FirebaseAuth mAuth;

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
        String userId = mAuth.getCurrentUser().getUid();

        String categoryId = categories.get(spinnerPosition).getCategoryId();

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(userId).child(categoryId);

        final String text = listEditText.getText().toString();

        HashMap newValue = new HashMap();
        newValue.put(categoryId, text);

        if (text.trim().length() > 0)
        {
            categories.get(spinnerPosition).setCategoryName(text);
            databaseReference.updateChildren(newValue);

            Toast.makeText(this, "List name updated", Toast.LENGTH_SHORT).show();

            finish();
        } else
        {
            Toast.makeText(this, "You did not enter any text", Toast.LENGTH_SHORT).show();
        }

    }
}