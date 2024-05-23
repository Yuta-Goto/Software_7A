import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class TitleScreen extends JFrame {

    public TitleScreen() {
        // フレームの設定
        setTitle("MOMIJ");
        setSize(650, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        ImageIcon icon = new ImageIcon("./datas/momij.png");
        setIconImage(icon.getImage());

        JLabel logoLabel = new JLabel();
        ImageIcon logoImage = new ImageIcon("./datas/momij_left.png"); //各キャラクター画像のパスを指定
        logoLabel.setIcon(logoImage);
        logoLabel.setBounds(96, 0, 144, 100);
        add(logoLabel);

        JLabel titleLabel = new JLabel("MOMiJ");
        titleLabel.setFont(new Font("Monospaced", Font.PLAIN, 50));
        titleLabel.setBounds(250, 50, 650, 50);
        add(titleLabel);

        // サブタイトルの設定
        JLabel subtitleLabel = new JLabel("Marvelous Online Meeting in Java");
        subtitleLabel.setFont(new Font("Monospaced", Font.PLAIN, 25));
        subtitleLabel.setBounds(80, 100, 650, 50);
        add(subtitleLabel);

        // "Quit" ボタンの設定
        JButton quitButton = new JButton("Quit");
        quitButton.setFont(new Font("Monospaced", Font.PLAIN, 16));
        quitButton.setBounds(200, 280, 250, 40);
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0); // プログラムを終了する
            }
        });
        add(quitButton);

        JButton keyboardButton = new JButton("Key Operation");
        keyboardButton.setFont(new Font("Monospaced", Font.PLAIN, 16));
        keyboardButton.setBounds(200, 230, 250, 40);
        keyboardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showKeyboardOperation();
            }
        });
        add(keyboardButton);

        JButton loginButton = new JButton("Join the Meeting");
        loginButton.setFont(new Font("Monospaced", Font.PLAIN, 16));
        loginButton.setBounds(200, 180, 250, 40);
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

        //キーボード割り当ての画面を表示
        private void showKeyboardOperation() {
            JFrame operationWindow = new JFrame("Keyboard");
            operationWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    
            operationWindow.setLayout(new BorderLayout());
    
            JPanel imagePanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    ImageIcon imageIcon = new ImageIcon("./datas/Keyboard.png");
                    Image image = imageIcon.getImage();
                    g.drawImage(image, 0, 0, 600, 550, null);
                }
    
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(600, 550);
                }
            };
    
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());
    
            JButton closeButton = new JButton("Return to Menu");
            closeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    operationWindow.dispose();
                }
            });
    
            buttonPanel.add(closeButton);
    
            operationWindow.add(imagePanel, BorderLayout.NORTH);
            operationWindow.add(buttonPanel, BorderLayout.SOUTH);
    
            operationWindow.pack();
    
            int x = getX() + (getWidth() - operationWindow.getWidth()) / 2;
            int y = getY() + (getHeight() - operationWindow.getHeight()) / 2;
            operationWindow.setLocation(x, y);
    
            operationWindow.setVisible(true);
        }

}

public class  MOMIJ{
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