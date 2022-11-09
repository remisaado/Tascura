package com.example.testapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class RegisterActivity extends AppCompatActivity {

    EditText registerEmailEditText;
    EditText registerPasswordEditText;
    Button registerButton;
    TextView logInHereTextView;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerEmailEditText = findViewById(R.id.registerEmailEditText);
        registerPasswordEditText = findViewById(R.id.registerPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        logInHereTextView = findViewById(R.id.logInHereTextView);

        mAuth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(view -> createUser());

        logInHereTextView.setOnClickListener(view -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }

    private void createUser()
    {
        String email = registerEmailEditText.getText().toString();
        String password = registerPasswordEditText.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            registerEmailEditText.setError("Email cannot be empty");
            registerEmailEditText.requestFocus();
        }
        else if (TextUtils.isEmpty(password))
        {
            registerPasswordEditText.setError("Password cannot be empty");
            registerPasswordEditText.requestFocus();
        }
        else
        {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (!task.isSuccessful())
                {
                    @SuppressWarnings("ThrowableNotThrown")
                    String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                    switch (errorCode)
                    {
                        case "ERROR_INVALID_EMAIL":
                        case "ERROR_EMAIL_ALREADY_IN_USE":
                            registerEmailEditText.setError(task.getException().getMessage());
                            registerEmailEditText.requestFocus();
                            break;
                        case "ERROR_WRONG_PASSWORD":
                            registerPasswordEditText.setError(task.getException().getMessage());
                            registerPasswordEditText.requestFocus();
                            registerPasswordEditText.setText("");
                            break;
                        case "ERROR_WEAK_PASSWORD":
                            registerPasswordEditText.setError(task.getException().getMessage());
                            registerPasswordEditText.requestFocus();
                            break;
                        default:
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(RegisterActivity.this, this.getString(R.string.toast_user_registered_successfully), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                }
            });
        }
    }
}