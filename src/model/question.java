package model;

public class Question {
    private int id;
    private String questionText;
    private String imagePath; // Field untuk path gambar
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption;

    // Constructor lengkap: untuk mengambil data dari database
    public Question(int id, String questionText, String imagePath, String optionA, String optionB, String optionC, String optionD, String correctOption) {
        this.id = id;
        this.questionText = questionText;
        this.imagePath = imagePath;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
    }

    // Constructor untuk membuat soal baru dari form
    public Question(String questionText, String imagePath, String optionA, String optionB, String optionC, String optionD, String correctOption) {
        this.questionText = questionText;
        this.imagePath = imagePath;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
    }

    // Membaca data soal
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }

    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }

    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }

    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }

    public String getCorrectOption() { return correctOption; }
    public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }
}