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

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText logInEmailEditText;
    EditText logInPasswordEditText;
    Button logInButton;
    TextView registerHereTextView;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        logInEmailEditText = findViewById(R.id.logInEmailEditText);
        logInPasswordEditText = findViewById(R.id.logInPasswordEditText);
        logInButton = findViewById(R.id.logInButton);
        registerHereTextView = findViewById(R.id.registerHereTextView);

        mAuth = FirebaseAuth.getInstance();

        buttonDefault();

        logInButton.setOnClickListener(view -> logInUser());

        registerHereTextView.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    void buttonDefault()
    {
        logInButton.setText(R.string.log_in);
        logInButton.setEnabled(true);
        logInButton.setAlpha(1);
    }

    void buttonLoading()
    {
        logInButton.setText(R.string.logging_in);
        logInButton.setEnabled(false);
        logInButton.setAlpha(0.5f);
    }

    private void logInUser()
    {
        String email = logInEmailEditText.getText().toString();
        String password = logInPasswordEditText.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            logInEmailEditText.setError("Email cannot be empty");
            logInEmailEditText.requestFocus();
        }
        else if (TextUtils.isEmpty(password))
        {
            logInPasswordEditText.setError("Password cannot be empty");
            logInPasswordEditText.requestFocus();
        }
        else
        {
            buttonLoading();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                buttonDefault();

                if (!task.isSuccessful())
                {
                    String errorCode = ((FirebaseAuthException) Objects.requireNonNull(task.getException())).getErrorCode();

                    switch (errorCode)
                    {
                        case "ERROR_INVALID_EMAIL":
                        case "ERROR_EMAIL_ALREADY_IN_USE":
                            logInEmailEditText.setError(task.getException().getMessage());
                            logInEmailEditText.requestFocus();
                            break;
                        case "ERROR_WRONG_PASSWORD":
                            logInPasswordEditText.setError(task.getException().getMessage());
                            logInPasswordEditText.requestFocus();
                            logInPasswordEditText.setText("");
                            break;
                        case "ERROR_WEAK_PASSWORD":
                            logInPasswordEditText.setError(task.getException().getMessage());
                            logInPasswordEditText.requestFocus();
                            break;
                        default:
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finishAffinity();
    }
}