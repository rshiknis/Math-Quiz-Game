package com.example.flappybird;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
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
    TextView currentAnswerTextView; // Added this TextView for the current answer
    Button[] digitButtons = new Button[10]; // 0-9 digit buttons
    Button clearButton; // Clear button for answer reset

    int score = 0;
    int totalQuestion = 3; // Fixed total number of questions
    int currentQuestionIndex = 0;
    StringBuilder selectedAnswer = new StringBuilder(); // To hold multi-digit answers
    CountDownTimer countDownTimer;
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
        currentAnswerTextView = findViewById(R.id.current_answer); // To display the current answer

        // Set up digit buttons for 0-9
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
        clearButton = findViewById(R.id.btn_clear); // Clear button

        // Attach click listeners to digit buttons and set black text color
        for (int i = 0; i < 10; i++) {
            digitButtons[i].setOnClickListener(this);
            digitButtons[i].setTextColor(Color.BLACK); // Set text color to black
        }

        // Attach click listener for clear button
        clearButton.setOnClickListener(v -> clearAnswer());

        totalQuestionsTextView.setText("Total questions: " + totalQuestion);
        loadNewQuestion();
    }

    @Override
    public void onClick(View view) {
        Button clickedButton = (Button) view;

        // Toggle the button's color to magenta and append the digit to the answer
        if (clickedButton.getCurrentTextColor() == Color.MAGENTA) {
            clickedButton.setBackgroundColor(Color.WHITE); // Set back to white if unclicked
        } else {
            clickedButton.setBackgroundColor(Color.MAGENTA); // Set to magenta if clicked
        }

        String digit = clickedButton.getText().toString();
        selectedAnswer.append(digit); // Add the digit to the answer
        currentAnswerTextView.setText(selectedAnswer.toString()); // Update current answer display
    }

    void clearAnswer() {
        // Clear the current answer and reset button colors
        selectedAnswer.setLength(0); // Clear the selected answer
        currentAnswerTextView.setText(""); // Clear the displayed current answer
        for (Button btn : digitButtons) {
            btn.setBackgroundColor(Color.WHITE); // Reset all buttons to white
        }
    }

    void loadNewQuestion() {
        if (currentQuestionIndex == totalQuestion) {
            finishQuiz();
            return;
        }

        // Reset answer selection and button colors
        selectedAnswer.setLength(0); // Clear previous answer
        currentAnswerTextView.setText(""); // Clear current answer display
        for (Button btn : digitButtons) {
            btn.setBackgroundColor(Color.WHITE); // Reset buttons to white
        }

        // Generate random math question with positive whole number answers
        int num1, num2;
        char operation;
        do {
            num1 = random.nextInt(10);
            num2 = random.nextInt(10) + 1; // avoid zero for division
            operation = getRandomOperation(); // Randomly choose an operation
            correctAnswer = generateQuestion(num1, num2, operation);
        } while (correctAnswer < 0); // Ensure the answer is a positive whole number

        questionTextView.setText(String.format("What is %d %c %d?", num1, operation, num2));

        // Start the 10-second countdown for the new question
        startTimer();
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
                // Ensure division results in a whole number
                if (num1 % num2 == 0) {
                    return num1 / num2;
                } else {
                    return -1; // Invalid division, return -1 to regenerate the question
                }
        }
        return -1;
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
                try {
                    int userAnswer = Integer.parseInt(selectedAnswer.toString());
                    if (userAnswer == correctAnswer) {
                        score++; // Increment score only if the answer was correct
                    }
                } catch (NumberFormatException e) {
                    // Invalid answer (e.g., empty input), treat as incorrect
                }

                // Move to the next question
                currentQuestionIndex++;
                loadNewQuestion();
            }
        };
        countDownTimer.start();
    }

    void finishQuiz() {
        String passStatus = (score > totalQuestion * 0.60) ? "Passed" : "Failed";

        // Get the current Firebase user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Extract username from the email address
        final String[] username = {""}; // Wrap in array to allow modification
        String email = user.getEmail();
        if (email != null && email.contains("@")) {
            username[0] = email.split("@")[0]; // Get the part before the '@'
        }

        // Get a reference to the user's score in Firebase
        String userId = user.getUid();
        FirebaseDatabase.getInstance().getReference().child("scores").child(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Retrieve the current score from Firebase, if exists
                        Object scoreObj = task.getResult().child("score").getValue();
                        int previousScore = 0;
                        if (scoreObj != null) {
                            previousScore = Integer.parseInt(scoreObj.toString());
                        }

                        // Check if the new score is higher than the previous score
                        if (score > previousScore) {
                            // Create a map to store both the username and the new (higher) score
                            Map<String, Object> result = new HashMap<>();
                            result.put("username", username[0]);
                            result.put("score", score);

                            // Store the result in Firebase Realtime Database under the user's UID
                            FirebaseDatabase.getInstance().getReference().child("scores").child(userId).setValue(result);
                        }

                        // Show the result in an AlertDialog
                        new AlertDialog.Builder(this)
                                .setTitle(passStatus)
                                .setMessage("Score is " + score + " out of " + totalQuestion)
                                .setPositiveButton("Restart", (dialogInterface, i) -> restartQuiz())
                                .setCancelable(false)
                                .show();
                    } else {
                        // Handle potential failure to retrieve data from Firebase
                        new AlertDialog.Builder(this)
                                .setTitle("Error")
                                .setMessage("Failed to retrieve previous score.")
                                .setPositiveButton("Retry", (dialogInterface, i) -> finishQuiz())
                                .setCancelable(false)
                                .show();
                    }
                });
    }

    void restartQuiz() {
        score = 0;
        currentQuestionIndex = 0;
        loadNewQuestion();
    }
}
