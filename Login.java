import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Login extends JFrame{
    private JTextField usernameField;

    public Login() {
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 350);



        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

       

        JLabel titlenameLabel = new JLabel("MOMIJ");
        JLabel emptyLabel1 = new JLabel(" ");
        JLabel usernameLabel = new JLabel("Enter Username");
        usernameField = new JTextField();
        JLabel emptyLabel2 = new JLabel(" ");
        JButton loginButton = new JButton("Login");
        JButton backButton = new JButton("Back");

        usernameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // テキストフィールドでEnterが押された時の処理
                loginbutton_pressed();
            }
        });

        titlenameLabel.setFont(new Font("Monospaced", Font.PLAIN, 36));
        emptyLabel1.setFont(new Font("Monospaced", Font.PLAIN, 36));
        emptyLabel2.setFont(new Font("Monospaced", Font.PLAIN, 24));
        usernameLabel.setFont(new Font("Monospaced", Font.PLAIN, 24));
        usernameField.setFont(new Font("Monospaced", Font.PLAIN, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        usernameField.setColumns(25);

        //
        JLabel logoLabel1 = new JLabel();
        JLabel logoLabel2 = new JLabel();
        ImageIcon logoImage1 = new ImageIcon("./datas/momij_left.png"); //各キャラクター画像のパスを指定
        ImageIcon logoImage2 = new ImageIcon("./datas/momij_right.png");
        logoLabel1.setIcon(logoImage1);
        logoLabel2.setIcon(logoImage2);
        logoLabel1.setBounds(10, 115, 256, 256);
        logoLabel2.setBounds(450,115,256,256);
        add(logoLabel1);
        add(logoLabel2);
        //

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 10;  //グリッドの幅を2に設定(2列にまたがる)
        gbc.anchor = GridBagConstraints.CENTER; //コンポーネントを中央に配置
        panel.add(titlenameLabel, gbc);

        gbc.gridy++; //次の行に移動
        gbc.gridwidth = 10;
        panel.add(emptyLabel1, gbc);

        gbc.gridy++;
        panel.add(usernameLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 10;
        panel.add(usernameField, gbc);

        gbc.gridy++; //次の行に移動
        gbc.gridwidth = 10;
        panel.add(emptyLabel2, gbc);

        //ログインボタンを配置
        gbc.gridy++; //次の行に移動
        gbc.gridwidth = 10;
        panel.add(loginButton, gbc);

        //タイトルに戻るボタンを配置
        gbc.gridy++; //次の行に移動
        gbc.gridwidth = 10;
        panel.add(backButton, gbc);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginbutton_pressed();
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                TitleScreen title = new TitleScreen();
                title.setVisible(true);
            }
        });
        add(panel);
        setVisible(true);
    }
    
    void loginbutton_pressed(){
        String username;
        Boolean validName = true;
        username = usernameField.getText();
        username = username.replace(" ", "_");
        // ログイン処理を実装する
        if(!GetStrLimitation(username)){
            JOptionPane.showMessageDialog(Login.this, "Username is too long","Invalid Username", JOptionPane.WARNING_MESSAGE);
            validName = false;
            usernameField.setText("");
            usernameField.requestFocusInWindow();
        } else {
            if(username.isEmpty() || username.matches("^_*$")){
                int option = JOptionPane.showConfirmDialog(null,"You'll connect the server as a Guest User","No Name",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (option == JOptionPane.YES_OPTION){
                    validName = true;
                }else if (option == JOptionPane.NO_OPTION){
                    validName = false;
                    usernameField.setText("");
                }
            } else {
                JOptionPane.showMessageDialog(Login.this, "Username: " + username,"Login Successful", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        //ログイン成功時にキャラクター選択画面に遷移
        if(validName){
            dispose(); //ログイン画面を閉じる
            CharacterSelect characterSelect = new CharacterSelect(username);
            characterSelect.setLocation(getLocation());
            characterSelect.setVisible(true);
        }
    }

    boolean GetStrLimitation(String str){ //文字列から日本語文字の数を取得する
        int count = 0;
        
        // 文字列内の各文字を調べる
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // Unicodeの範囲を利用して日本語文字かどうかを判別する
            // 日本語の範囲はUnicodeのU+3000からU+9FFFまでとU+FF65からU+FF9Fまで
            if ((c >= '\u3000' && c <= '\u9FFF') || (c >= '\uFF65' && c <= '\uFF9F')) {
                count++;
            }
        }
        if(count+str.length()<20){
            return true;
        } else {
            return false;
        }
    }
}
