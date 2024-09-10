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

    private TextView player1, player2, player3;
    private Button playAgainButton, exitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        player1 = findViewById(R.id.player_1);
        player2 = findViewById(R.id.player_2);
        player3 = findViewById(R.id.player_3);
        playAgainButton = findViewById(R.id.btn_play_again);
        exitButton = findViewById(R.id.btn_exit);

        loadLeaderboardData();

        playAgainButton.setOnClickListener(v -> {
            startActivity(new Intent(Leaderboard.this, MainActivity.class));
            finish();
        });

        exitButton.setOnClickListener(v -> finishAffinity());
    }

    private void loadLeaderboardData() {
        DatabaseReference scoresRef = FirebaseDatabase.getInstance().getReference("scores");

        scoresRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot snapshot = task.getResult();
                List<Map.Entry<String, Long>> leaderboard = new ArrayList<>();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String username = userSnapshot.child("username").getValue(String.class);
                    Long score = userSnapshot.child("score").getValue(Long.class);

                    if (username != null && score != null) {
                        leaderboard.add(Map.entry(username, score));
                    }
                }

                if (!leaderboard.isEmpty()) {
                    Collections.sort(leaderboard, (a, b) -> b.getValue().compareTo(a.getValue()));
                    updateLeaderboardUI(leaderboard);
                } else {
                    Toast.makeText(Leaderboard.this, "No leaderboard data available.", Toast.LENGTH_SHORT).show();
                }

            } else {
                Log.e("Leaderboard", "Error loading data from Firebase", task.getException());
                Toast.makeText(Leaderboard.this, "Failed to load leaderboard data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLeaderboardUI(List<Map.Entry<String, Long>> leaderboard) {
        player1.setText(leaderboard.size() > 0 ? "1. " + leaderboard.get(0).getKey() + ": " + leaderboard.get(0).getValue() : "1. No data");
        player2.setText(leaderboard.size() > 1 ? "2. " + leaderboard.get(1).getKey() + ": " + leaderboard.get(1).getValue() : "2. No data");
        player3.setText(leaderboard.size() > 2 ? "3. " + leaderboard.get(2).getKey() + ": " + leaderboard.get(2).getValue() : "3. No data");
    }
}
