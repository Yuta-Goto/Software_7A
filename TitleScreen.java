import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TitleScreen extends JFrame {

    public TitleScreen() {
        // フレームの設定
        setTitle("Online Meeting in Java");
        setSize(600, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // "Title" ラベルの設定
        JLabel titleLabel = new JLabel("Online Meeting in Java");
        titleLabel.setFont(new Font("Monospaced", Font.PLAIN, 36));
        titleLabel.setBounds(50, 50, 600, 50); // 座標 (250, 50) に表示
        add(titleLabel);

        // "Quit" ボタンの設定
        JButton quitButton = new JButton("Quit");
        quitButton.setFont(new Font("Monospaced", Font.PLAIN, 24));
        quitButton.setBounds(350, 150, 150, 100);
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0); // プログラムを終了する
            }
        });
        add(quitButton);

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Monospaced", Font.PLAIN, 24));
        loginButton.setBounds(100, 150, 150, 100);
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Login loginScreen = new Login();
                loginScreen.setLocation(getLocation());
                dispose();
            }
        });
        add(loginButton);
    }

    public static void main(String[] args) {
        // イベントディスパッチスレッドでGUIを作成
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TitleScreen frame = new TitleScreen();
                frame.setVisible(true);
            }
        });
    }
}