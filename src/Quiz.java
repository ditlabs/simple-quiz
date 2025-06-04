import javax.swing.*;
import java.awt.event.*;

public class Quiz {
    static int index = 0;
    static int score = 0;

    static String[] questions = {
            "Apa ibu kota Indonesia?",
            "Siapa presiden pertama Indonesia?",
            "Berapakah hasil dari 5 + 3?",
            "Pulau terbesar di Indonesia adalah?",
            "Bahasa pemrograman yang diawali oleh huruf J?"
    };

    static String[][] options = {
            {"Bandung", "Jakarta", "Surabaya"},
            {"Sukarno", "Soedirman", "Hatta"},
            {"6", "8", "10"},
            {"Sumatra", "Jawa", "Kalimantan"},
            {"Javascript", "Python", "Ruby"}
    };

    static int[] correctAnswers = {1, 0, 1, 2, 0};

    public static void main(String[] args) {
        JFrame frame = new JFrame("Quiz 5 Soal");
        frame.setSize(450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        JLabel questionLabel = new JLabel(questions[index]);
        questionLabel.setBounds(30, 20, 400, 25);
        frame.add(questionLabel);

        JRadioButton opt1 = new JRadioButton();
        JRadioButton opt2 = new JRadioButton();
        JRadioButton opt3 = new JRadioButton();
        opt1.setBounds(30, 60, 300, 25);
        opt2.setBounds(30, 90, 300, 25);
        opt3.setBounds(30, 120, 300, 25);

        ButtonGroup group = new ButtonGroup();
        group.add(opt1);
        group.add(opt2);
        group.add(opt3);

        frame.add(opt1);
        frame.add(opt2);
        frame.add(opt3);

        JButton nextButton = new JButton("Next");
        nextButton.setBounds(30, 160, 100, 30);
        frame.add(nextButton);

        JLabel resultLabel = new JLabel("");
        resultLabel.setBounds(30, 200, 400, 25);
        frame.add(resultLabel);

        // Tampilkan soal pertama
        updateSoal(questionLabel, opt1, opt2, opt3);

        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Cek jawaban
                int selected = -1;
                if (opt1.isSelected()) selected = 0;
                else if (opt2.isSelected()) selected = 1;
                else if (opt3.isSelected()) selected = 2;

                if (selected == correctAnswers[index]) {
                    score++;
                }

                index++;

                // Lanjut ke soal berikutnya atau tampilkan hasil
                if (index < questions.length) {
                    group.clearSelection();
                    updateSoal(questionLabel, opt1, opt2, opt3);
                } else {
                    // Quiz selesai
                    questionLabel.setText("Quiz selesai!");
                    opt1.setVisible(false);
                    opt2.setVisible(false);
                    opt3.setVisible(false);
                    nextButton.setEnabled(false);
                    resultLabel.setText("Skor akhir kamu: " + score + " dari " + questions.length);
                }
            }
        });

        frame.setVisible(true);
    }

    // Fungsi untuk menampilkan soal dan pilihan
    static void updateSoal(JLabel label, JRadioButton o1, JRadioButton o2, JRadioButton o3) {
        label.setText(questions[index]);
        o1.setText(options[index][0]);
        o2.setText(options[index][1]);
        o3.setText(options[index][2]);
    }
}
