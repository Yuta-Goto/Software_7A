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
        JLabel titlenameLabel = new JLabel("application name");
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();
        JButton loginButton = new JButton("Login");
        GridBagConstraints gbc = new GridBagConstraints();
        usernameField.setColumns(10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;  //グリッドの幅を2に設定(2列にまたがる)
        gbc.anchor = GridBagConstraints.CENTER; //コンポーネントを中央に配置
        panel.add(titlenameLabel, gbc);
        //  空白のラベルを追加して間隔を調整
        gbc.gridy++;
        panel.add( new JLabel());
        //ユーザー名とパスワード入力フィールドを配置
        gbc.gridy++;
        panel.add(usernameLabel, gbc);
        gbc.gridy++;
        gbc.gridwidth = 10;
        panel.add(usernameField, gbc);

        //ログインボタンを配置
        gbc.gridy++; //次の行に移動
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username;
                username = usernameField.getText();
                // ログイン処理を実装する（仮の例として表示
                if(username.isEmpty()){
                    JOptionPane.showMessageDialog(Login.this, "Login as a Guest User","Login Successful", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(Login.this, "Username: " + username,"Login Successful", JOptionPane.INFORMATION_MESSAGE);
                }
                //ログイン成功時にキャラクター選択画面に遷移
                dispose(); //ログイン画面を閉じる
                CharacterSelect characterSelect = new CharacterSelect(username);
                characterSelect.setLocation(getLocation());
                characterSelect.setVisible(true);
            }
        });
        add(panel);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Login();
            }
        });
    }
    
}
