package com.example.testapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        final String text = listEditText.getText().toString();
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("CategoryList");

        myRef.orderByValue().equalTo(text).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    Toast.makeText(AddListActivity.this, "This list already exists", Toast.LENGTH_SHORT).show();
                } else
                    {
                        if (text.trim().length() > 0)
                        {
                            myRef.push().setValue(text);
                            listEditText.getText().clear();

                            Toast.makeText(AddListActivity.this, "New list added", Toast.LENGTH_SHORT).show();
                            finish();
                        } else
                        {
                            Toast.makeText(AddListActivity.this, "You did not enter any text", Toast.LENGTH_SHORT).show();
                        }
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
