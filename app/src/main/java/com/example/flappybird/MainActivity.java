package com.example.flappybird;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView totalQuestionsTextView;
    TextView questionTextView;
    TextView timerTextView;
    TextView currentAnswerTextView;
    Button[] digitButtons = new Button[10];
    Button clearButton;
    Button submitButton;

    int score = 0;
    int totalQuestion = 0;
    StringBuilder selectedAnswer = new StringBuilder();
    CountDownTimer quizTimer;
    int correctAnswer;
    private FirebaseAuth auth;

    Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        totalQuestionsTextView = findViewById(R.id.total_question);
        questionTextView = findViewById(R.id.question);
        timerTextView = findViewById(R.id.timer_text);
        currentAnswerTextView = findViewById(R.id.current_answer);

        digitButtons[0] = findViewById(R.id.btn_0);
        digitButtons[1] = findViewById(R.id.btn_1);
        digitButtons[2] = findViewById(R.id.btn_2);
        digitButtons[3] = findViewById(R.id.btn_3);
        digitButtons[4] = findViewById(R.id.btn_4);
        digitButtons[5] = findViewById(R.id.btn_5);
        digitButtons[6] = findViewById(R.id.btn_6);
        digitButtons[7] = findViewById(R.id.btn_7);
        digitButtons[8] = findViewById(R.id.btn_8);
        digitButtons[9] = findViewById(R.id.btn_9);
        clearButton = findViewById(R.id.btn_clear);
        submitButton = findViewById(R.id.btn_submit);

        for (int i = 0; i < 10; i++) {
            digitButtons[i].setOnClickListener(this);
            digitButtons[i].setTextColor(Color.BLACK);
        }

        clearButton.setOnClickListener(v -> clearAnswer());
        submitButton.setOnClickListener(v -> checkAnswer());

        totalQuestionsTextView.setText("Total questions answered: " + totalQuestion);
        startQuiz();
    }

    @Override
    public void onClick(View view) {
        Button clickedButton = (Button) view;

        if (clickedButton.getCurrentTextColor() == Color.MAGENTA) {
            clickedButton.setBackgroundColor(Color.WHITE);
        } else {
            clickedButton.setBackgroundColor(Color.MAGENTA);
        }

        String digit = clickedButton.getText().toString();
        selectedAnswer.append(digit);
        currentAnswerTextView.setText(selectedAnswer.toString());
    }

    void clearAnswer() {
        selectedAnswer.setLength(0);
        currentAnswerTextView.setText("");
        for (Button btn : digitButtons) {
            btn.setBackgroundColor(Color.WHITE);
        }
    }

    void loadNewQuestion() {
        selectedAnswer.setLength(0);
        currentAnswerTextView.setText("");
        for (Button btn : digitButtons) {
            btn.setBackgroundColor(Color.WHITE);
        }

        int num1, num2;
        char operation;
        do {
            num1 = random.nextInt(10);
            num2 = random.nextInt(10) + 1;
            operation = getRandomOperation();
            correctAnswer = generateQuestion(num1, num2, operation);
        } while (correctAnswer < 0);

        questionTextView.setText(String.format("What is %d %c %d?", num1, operation, num2));
    }

    char getRandomOperation() {
        char[] operations = {'+', '-', '*', '/'};
        return operations[random.nextInt(4)];
    }

    int generateQuestion(int num1, int num2, char operation) {
        switch (operation) {
            case '+':
                return num1 + num2;
            case '-':
                return num1 - num2;
            case '*':
                return num1 * num2;
            case '/':
                if (num1 % num2 == 0) {
                    return num1 / num2;
                } else {
                    return -1;
                }
        }
        return -1;
    }

    void startQuiz() {
        quizTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerTextView.setText("Time left: " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                finishQuiz();
            }
        };
        quizTimer.start();

        loadNewQuestion();
    }

    void checkAnswer() {
        try {
            int userAnswer = Integer.parseInt(selectedAnswer.toString());
            if (userAnswer == correctAnswer) {
                score++;
            }
        } catch (NumberFormatException e) {
        }

        totalQuestion++;
        totalQuestionsTextView.setText("Total questions answered: " + totalQuestion);

        loadNewQuestion();
    }

    void finishQuiz() {
        quizTimer.cancel();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        final String[] username = {""};
        String email = user.getEmail();
        if (email != null && email.contains("@")) {
            username[0] = email.split("@")[0];
        }

        String userId = user.getUid();
        FirebaseDatabase.getInstance().getReference().child("scores").child(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Object scoreObj = task.getResult().child("score").getValue();
                        int previousScore = 0;
                        if (scoreObj != null) {
                            previousScore = Integer.parseInt(scoreObj.toString());
                        }

                        if (score > previousScore) {
                            Map<String, Object> result = new HashMap<>();
                            result.put("username", username[0]);
                            result.put("score", score);

                            FirebaseDatabase.getInstance().getReference().child("scores").child(userId).setValue(result);
                        }

                        new AlertDialog.Builder(this)
                                .setTitle("Quiz Finished")
                                .setMessage("Your score: " + score)
                                .setPositiveButton("Go to Leaderboard", (dialogInterface, i) -> moveToLeaderboard())
                                .setCancelable(false)
                                .show();
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("Error")
                                .setMessage("Failed to retrieve previous score.")
                                .setPositiveButton("Retry", (dialogInterface, i) -> finishQuiz())
                                .setCancelable(false)
                                .show();
                    }
                });
    }

    void moveToLeaderboard() {
        Intent intent = new Intent(MainActivity.this, Leaderboard.class);
        startActivity(intent);
        finish();
    }
}
