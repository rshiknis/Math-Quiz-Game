package com.example.flappybird;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button loginButton, signupButton;
    private EditText loginUsername, loginPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // This should be the name of your XML file if different

        // Initialize Firebase authentication instance
        auth = FirebaseAuth.getInstance();

        // Link to XML layout elements
        loginUsername = findViewById(R.id.username_input); // Username input from XML
        loginPassword = findViewById(R.id.password_input); // Password input from XML
        loginButton = findViewById(R.id.login_button); // Login button from XML
        signupButton = findViewById(R.id.signup_button); // Signup button from XML

        // Login button logic
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = loginUsername.getText().toString().trim();
                String password = loginPassword.getText().toString().trim();

                // Check if username and password are not empty
                if (username.isEmpty()) {
                    loginUsername.setError("You must enter a username.");
                } else if (password.isEmpty()) {
                    loginPassword.setError("You must enter a password.");
                } else {
                    // Authenticate the user with Firebase
                    auth.signInWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // If login is successful, show a success message and transition to MainActivity
                                Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Login.this, MainActivity.class));
                            } else {
                                // Show an error message if login fails
                                Toast.makeText(Login.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        // Signup button logic (redirect to the registration page)
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });
    }
}
