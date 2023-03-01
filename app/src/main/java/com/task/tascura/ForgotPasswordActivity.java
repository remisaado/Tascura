package com.task.tascura;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText emailEditText;
    Button sendButton;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        View arrowBackView = findViewById(R.id.arrowBackView);
        emailEditText = findViewById(R.id.emailEditText);
        sendButton = findViewById(R.id.sendButton);

        mAuth = FirebaseAuth.getInstance();

        buttonDefault();

        arrowBackView.setOnClickListener(view -> finish());

        sendButton.setOnClickListener(view -> sendPasswordResetEmail());
    }

    void buttonDefault()
    {
        // Sets the send button to default mode.
        sendButton.setText(R.string.send_new_password);
        sendButton.setEnabled(true);
        sendButton.setAlpha(1);
    }

    void buttonLoading()
    {
        // Sets the send button to loading mode.
        sendButton.setText(R.string.sending);
        sendButton.setEnabled(false);
        sendButton.setAlpha(0.5f);
    }

    private void sendPasswordResetEmail()
    {
        // Method for sending a password reset email.

        String email = emailEditText.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            emailEditText.setError("Email cannot be empty");
            emailEditText.requestFocus();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            emailEditText.setError("Please provide a valid email");
            emailEditText.requestFocus();
        }
        else
        {
            // Sets the button to loading mode while attempting to send email.
            buttonLoading();

            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task ->
            {
                // Attempts to send the password reset email to the provided email.

                // Sets the button to default mode when attempt is finished.
                buttonDefault();

                if (!task.isSuccessful())
                {
                    Toast.makeText(this, getString(R.string.toast_something_went_wrong), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(this, getString(R.string.toast_email_sent_successfully), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }
}