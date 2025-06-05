package ui;

import dao.questionDao;
import model.Question;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class mainMenu extends JFrame {  // Ganti dengan mainMenu jika nama kelas sesuai

    private JTextArea questionArea;
    private JRadioButton optionA, optionB, optionC, optionD;
    private JButton nextButton;
    private ButtonGroup optionsGroup;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;

    public mainMenu() {  // Sesuaikan konstruktor dengan nama kelas mainMenu
        // Set frame properties
        setTitle("Simple Quiz");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create UI components
        questionArea = new JTextArea(5, 40);
        questionArea.setEditable(false);

        optionA = new JRadioButton();
        optionB = new JRadioButton();
        optionC = new JRadioButton();
        optionD = new JRadioButton();

        nextButton = new JButton("Next");
        optionsGroup = new ButtonGroup();
        optionsGroup.add(optionA);
        optionsGroup.add(optionB);
        optionsGroup.add(optionC);
        optionsGroup.add(optionD);

        // Layout the components
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JScrollPane(questionArea));
        panel.add(optionA);
        panel.add(optionB);
        panel.add(optionC);
        panel.add(optionD);
        panel.add(nextButton);

        add(panel);

        // Fetch questions from database
        questions = questionDao.getQuestions();

        // Display the first question
        displayQuestion(currentQuestionIndex);

        // Add next button action listener
        nextButton.addActionListener(e -> {
            String selectedOption = getSelectedOption();
            if (selectedOption != null) {
                if (selectedOption.equalsIgnoreCase(questions.get(currentQuestionIndex).getCorrectOption())) {
                    score++;
                }
                currentQuestionIndex++;
                if (currentQuestionIndex < questions.size()) {
                    displayQuestion(currentQuestionIndex);
                } else {
                    JOptionPane.showMessageDialog(this, "Quiz completed! Your score: " + score);
                }
            }
        });
    }

    private void displayQuestion(int index) {
        Question q = questions.get(index);
        questionArea.setText(q.getQuestionText());
        optionA.setText(q.getOptionA());
        optionB.setText(q.getOptionB());
        optionC.setText(q.getOptionC());
        optionD.setText(q.getOptionD());
    }

    private String getSelectedOption() {
        if (optionA.isSelected()) {
            return "A";
        } else if (optionB.isSelected()) {
            return "B";
        } else if (optionC.isSelected()) {
            return "C";
        } else if (optionD.isSelected()) {
            return "D";
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new mainMenu().setVisible(true);  // Sesuaikan dengan nama kelas mainMenu
        });
    }
}
