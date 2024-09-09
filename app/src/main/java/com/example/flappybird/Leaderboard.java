package com.example.flappybird;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Leaderboard extends AppCompatActivity {

    TextView player1, player2, player3;
    Button playAgainButton, exitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard); // Set the correct layout file

        // Initialize views
        player1 = findViewById(R.id.player_1);
        player2 = findViewById(R.id.player_2);
        player3 = findViewById(R.id.player_3);
        playAgainButton = findViewById(R.id.btn_play_again);
        exitButton = findViewById(R.id.btn_exit);

        // Load leaderboard data from Firebase
        loadLeaderboardData();

        // Set up Play Again button to restart the game
        playAgainButton.setOnClickListener(v -> {
            Intent intent = new Intent(Leaderboard.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Set up Exit button to close the app
        exitButton.setOnClickListener(v -> finishAffinity());
    }

    private void loadLeaderboardData() {
        // Get a reference to the Firebase Realtime Database
        DatabaseReference scoresRef = FirebaseDatabase.getInstance().getReference("scores");

        // Fetch data from Firebase
        scoresRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot snapshot = task.getResult();
                List<Map.Entry<String, Long>> leaderboard = new ArrayList<>();

                // Iterate through the scores and add to the list
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String username = userSnapshot.child("username").getValue(String.class);
                    Long score = userSnapshot.child("score").getValue(Long.class);

                    if (username != null && score != null) {
                        leaderboard.add(Map.entry(username, score));
                    }
                }

                if (!leaderboard.isEmpty()) {
                    // Sort the leaderboard by score in descending order
                    Collections.sort(leaderboard, (a, b) -> b.getValue().compareTo(a.getValue()));
                    // Update the UI for the top 3 players
                    updateLeaderboardUI(leaderboard);
                } else {
                    Toast.makeText(Leaderboard.this, "No leaderboard data available.", Toast.LENGTH_SHORT).show();
                }

            } else {
                // Handle any failure in fetching the data
                Log.e("Leaderboard", "Error loading data from Firebase", task.getException());
                Toast.makeText(Leaderboard.this, "Failed to load leaderboard data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLeaderboardUI(List<Map.Entry<String, Long>> leaderboard) {
        // Update the UI for the top 3 players or show placeholders
        if (leaderboard.size() > 0) {
            player1.setText("1. " + leaderboard.get(0).getKey() + ": " + leaderboard.get(0).getValue());
        } else {
            player1.setText("1. No data");
        }
        if (leaderboard.size() > 1) {
            player2.setText("2. " + leaderboard.get(1).getKey() + ": " + leaderboard.get(1).getValue());
        } else {
            player2.setText("2. No data");
        }
        if (leaderboard.size() > 2) {
            player3.setText("3. " + leaderboard.get(2).getKey() + ": " + leaderboard.get(2).getValue());
        } else {
            player3.setText("3. No data");
        }
    }
}
