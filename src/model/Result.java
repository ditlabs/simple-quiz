package model;

import java.sql.Timestamp;

public class Result {
    private int resultId;
    private int userId;
    private int score;
    private Timestamp quizDate;
    private String username; // Tambahan untuk join table

    // Constructor untuk mengambil data dari DB
    public Result(int resultId, int userId, String username, int score, Timestamp quizDate) {
        this.resultId = resultId;
        this.userId = userId;
        this.username = username;
        this.score = score;
        this.quizDate = quizDate;
    }

    // Getters
    public int getResultId() { return resultId; }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public int getScore() { return score; }
    public Timestamp getQuizDate() { return quizDate; }
}