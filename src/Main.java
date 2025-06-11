import dao.questionDao;
import model.Question;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Tes Koneksi Database
        if (db.dbConnection.getConnection() != null) {
            System.out.println("✅ Koneksi ke database berhasil!");
        } else {
            System.out.println("❌ Gagal koneksi ke database.");
            return;  // Jika gagal, hentikan eksekusi program
        }

        // Ambil soal dari database
        List<Question> questions = questionDao.getQuestions();
        Scanner scanner = new Scanner(System.in);
        int score = 0;

        // Menampilkan soal satu per satu
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);

            // Menampilkan soal dan pilihan jawaban
            System.out.println("Soal " + (i + 1) + ": " + q.getQuestionText());
            System.out.println("A. " + q.getOptionA());
            System.out.println("B. " + q.getOptionB());
            System.out.println("C. " + q.getOptionC());
            System.out.println("D. " + q.getOptionD());

            // Meminta input jawaban
            System.out.print("Jawaban (A/B/C/D): ");
            String answer = scanner.nextLine().toUpperCase(); // Pastikan input huruf besar

            // Cek jawaban
            if (answer.equalsIgnoreCase(q.getCorrectOption())) {
                System.out.println("✅ Jawaban benar!");
                score++;
            } else {
                System.out.println("❌ Jawaban salah! Jawaban yang benar: " + q.getCorrectOption());
            }
            System.out.println();
        }

        // Tampilkan hasil akhir
        System.out.println("Kuis selesai! Skor akhir kamu: " + score + " dari " + questions.size());

    }
}
