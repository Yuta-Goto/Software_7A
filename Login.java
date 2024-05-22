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
        JLabel titlenameLabel = new JLabel("Online Meeting In Java");
        JLabel emptyLabel1 = new JLabel(" ");
        JLabel usernameLabel = new JLabel("Enter Username");
        usernameField = new JTextField();
        JLabel emptyLabel2 = new JLabel(" ");
        JButton loginButton = new JButton("Login");
        JButton backButton = new JButton("Back");

        titlenameLabel.setFont(new Font("Monospaced", Font.PLAIN, 36));
        emptyLabel1.setFont(new Font("Monospaced", Font.PLAIN, 36));
        emptyLabel2.setFont(new Font("Monospaced", Font.PLAIN, 24));
        usernameLabel.setFont(new Font("Monospaced", Font.PLAIN, 24));
        usernameField.setFont(new Font("Monospaced", Font.PLAIN, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        usernameField.setColumns(25);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;  //グリッドの幅を2に設定(2列にまたがる)
        gbc.anchor = GridBagConstraints.CENTER; //コンポーネントを中央に配置
        panel.add(titlenameLabel, gbc);

        gbc.gridy++; //次の行に移動
        gbc.gridwidth = 4;
        panel.add(emptyLabel1, gbc);

        gbc.gridy++;
        panel.add(usernameLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 10;
        panel.add(usernameField, gbc);

        gbc.gridy++; //次の行に移動
        gbc.gridwidth = 4;
        panel.add(emptyLabel2, gbc);

        //ログインボタンを配置
        gbc.gridy++; //次の行に移動
        gbc.gridwidth = 4;
        panel.add(loginButton, gbc);

        //タイトルに戻るボタンを配置
        gbc.gridy++; //次の行に移動
        gbc.gridwidth = 4;
        panel.add(backButton, gbc);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username;
                Boolean validName = true;
                username = usernameField.getText();
                username = username.replace(" ", "_");
                // ログイン処理を実装する（仮の例として表示
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
                //ログイン成功時にキャラクター選択画面に遷移
                if(validName){
                    dispose(); //ログイン画面を閉じる
                    CharacterSelect characterSelect = new CharacterSelect(username);
                    characterSelect.setLocation(getLocation());
                    characterSelect.setVisible(true);
                }
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
    
}
