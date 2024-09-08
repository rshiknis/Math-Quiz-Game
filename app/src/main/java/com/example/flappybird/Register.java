package com.example.flappybird;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText signupUsername, signupPassword;
    private Button signupButton, loginRedirect;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        signupUsername = findViewById(R.id.username_input);
        signupPassword = findViewById(R.id.password_input);
        signupButton = findViewById(R.id.signup_button);
        loginRedirect = findViewById(R.id.redirect_button);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = signupUsername.getText().toString().trim();
                String password = signupPassword.getText().toString().trim();
                if (username.isEmpty()) {
                    signupUsername.setError("You must enter a username.");
                } else if (password.isEmpty()) {
                    signupPassword.setError("You must enter a password.");
                } else {
                    auth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Register.this, "You have signed up successfully!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Register.this, Login.class));
                            } else {
                                Toast.makeText(Register.this, "Your sign in has failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });

        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });




    }
}
