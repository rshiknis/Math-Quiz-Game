package com.example.flappybird;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView totalQuestionsTextView;
    TextView questionTextView;
    TextView timerTextView;
    Button ansA, ansB, ansC;

    int score = 0;
    int totalQuestion = QuestionChoicePairings.questionChoices.length;
    int currentQuestionIndex = 0;
    String selectedAnswer = "";
    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        totalQuestionsTextView = findViewById(R.id.total_question);
        questionTextView = findViewById(R.id.question);
        timerTextView = findViewById(R.id.timer_text);
        ansA = findViewById(R.id.ans_A);
        ansB = findViewById(R.id.ans_B);
        ansC = findViewById(R.id.ans_C);

        ansA.setOnClickListener(this);
        ansB.setOnClickListener(this);
        ansC.setOnClickListener(this);

        totalQuestionsTextView.setText("Total questions : " + totalQuestion);

        loadNewQuestion();
    }

    @Override
    public void onClick(View view) {
        // Reset the button colors
        ansA.setBackgroundColor(Color.WHITE);
        ansB.setBackgroundColor(Color.WHITE);
        ansC.setBackgroundColor(Color.WHITE);

        // Mark the selected button
        Button clickedButton = (Button) view;
        selectedAnswer = clickedButton.getText().toString();
        clickedButton.setBackgroundColor(Color.MAGENTA); // Highlight selected answer

        // No need to check correctness immediately; the timer will handle it
    }

    void loadNewQuestion() {
        if (currentQuestionIndex == totalQuestion) {
            finishQuiz();
            return;
        }

        // Reset the button colors to default (e.g., white)
        ansA.setBackgroundColor(Color.WHITE);
        ansB.setBackgroundColor(Color.WHITE);
        ansC.setBackgroundColor(Color.WHITE);

        // Set the new question and answers
        questionTextView.setText(QuestionChoicePairings.questionChoices[currentQuestionIndex]);
        ansA.setText(QuestionChoicePairings.answerChoices[currentQuestionIndex][0]);
        ansB.setText(QuestionChoicePairings.answerChoices[currentQuestionIndex][1]);
        ansC.setText(QuestionChoicePairings.answerChoices[currentQuestionIndex][2]);

        // Reset answer selection
        selectedAnswer = "";

        // Start the 10-second countdown for the new question
        startTimer();
    }

    void startTimer() {
        countDownTimer = new CountDownTimer(10000, 1000) { // 10 seconds
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the timer text every second
                timerTextView.setText("Time left: " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                // Check if the selected answer was correct when the timer finishes
                if (selectedAnswer.equals(QuestionChoicePairings.correctAnswerChoices[currentQuestionIndex])) {
                    score++; // Increment score only if the answer was correct
                }

                // Move to the next question regardless of the answer
                currentQuestionIndex++;
                loadNewQuestion();
            }
        };
        countDownTimer.start();
    }

    void finishQuiz() {
        String passStatus = "";
        if (score > totalQuestion * 0.60) {
            passStatus = "Passed";
        } else {
            passStatus = "Failed";
        }

        new AlertDialog.Builder(this)
                .setTitle(passStatus)
                .setMessage("Score is " + score + " out of " + totalQuestion)
                .setPositiveButton("Restart", (dialogInterface, i) -> restartQuiz())
                .setCancelable(false)
                .show();
    }

    void restartQuiz() {
        score = 0;
        currentQuestionIndex = 0;
        loadNewQuestion();
    }
}
