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

}
