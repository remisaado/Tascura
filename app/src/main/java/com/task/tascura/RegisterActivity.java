package com.task.tascura;

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

        buttonDefault();

        registerButton.setOnClickListener(view -> createUser());

        logInHereTextView.setOnClickListener(view -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }

    void buttonDefault()
    {
        registerButton.setText(R.string.register);
        registerButton.setEnabled(true);
        registerButton.setAlpha(1);
    }

    void buttonLoading()
    {
        registerButton.setText(R.string.registering);
        registerButton.setEnabled(false);
        registerButton.setAlpha(0.5f);
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
            buttonLoading();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                buttonDefault();

                if (!task.isSuccessful())
                {
                    String errorCode = ((FirebaseAuthException) Objects.requireNonNull(task.getException())).getErrorCode();

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