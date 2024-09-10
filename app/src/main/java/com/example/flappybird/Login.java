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
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        loginUsername = findViewById(R.id.username_input);
        loginPassword = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        signupButton = findViewById(R.id.signup_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = loginUsername.getText().toString().trim();
                String password = loginPassword.getText().toString().trim();

                if (username.isEmpty()) {
                    loginUsername.setError("You must enter a username.");
                } else if (password.isEmpty()) {
                    loginPassword.setError("You must enter a password.");
                } else {
                    auth.signInWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Login.this, MainActivity.class));
                            } else {
                                Toast.makeText(Login.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });
    }
}
