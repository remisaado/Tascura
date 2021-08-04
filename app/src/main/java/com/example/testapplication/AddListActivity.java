package com.example.testapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddListActivity extends AppCompatActivity {

    EditText listEditText;
    View addListButton;
    View backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_list);

        listEditText = findViewById(R.id.listEditText);
        addListButton = findViewById(R.id.addListButton);
        backButton = findViewById(R.id.backButton);

        listEditText.setOnEditorActionListener(editorActionListener);

        addListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {onAddListClick();}
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish();}
        });
    }

    private TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            onAddListClick();

            return true;
        }
    };

    private void onAddListClick()
    {
        String text = listEditText.getText().toString();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("CategoryList");

        if (text.trim().length() > 0)
        {
            myRef.push().setValue(text);
            listEditText.getText().clear();

            Toast.makeText(this, "New list added", Toast.LENGTH_SHORT).show();
            finish();
        } else
            {
                Toast.makeText(this, "You did not enter any text", Toast.LENGTH_SHORT).show();
            }
    }
}
