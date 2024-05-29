import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//ユーザーネーム入力画面の処理を行う
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

        //テキストフィールド(ユーザーネームを入力する場所)
        usernameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Enterが押された場合次の処理に進む
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

        JLabel logoLabel1 = new JLabel();
        JLabel logoLabel2 = new JLabel();
        ImageIcon logoImage1 = new ImageIcon("./datas/momij_left.png");
        ImageIcon logoImage2 = new ImageIcon("./datas/momij_right.png");
        logoLabel1.setIcon(logoImage1);
        logoLabel2.setIcon(logoImage2);
        logoLabel1.setBounds(10, 115, 256, 256);
        logoLabel2.setBounds(450,115,256,256);
        add(logoLabel1);
        add(logoLabel2);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 10;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(titlenameLabel, gbc);

        gbc.gridy++; //次の行に移動
        gbc.gridwidth = 10;
        panel.add(emptyLabel1, gbc);

        gbc.gridy++; //次の行に移動
        panel.add(usernameLabel, gbc);

        gbc.gridy++; //次の行に移動
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

        //押すと次の処理に移行するボタン
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginbutton_pressed();
            }
        });

        //押すとタイトル画面に戻るボタン
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
    
    //loginButtonが押されるかEnterキーが押された場合の処理
    void loginbutton_pressed(){
        String username;
        Boolean validName = true;
        username = usernameField.getText();
        // 入力されたユーザーネーム内のスペース" "をアンダーバー"_"に置換
        username = username.replace(" ", "_");
        // 入力されたユーザーネームを判別する
        if(!GetStrLimitation(username)){ //ユーザーネームが長すぎる場合警告を表示し画面遷移を行わない
            JOptionPane.showMessageDialog(Login.this, "Username is too long","Invalid Username", JOptionPane.WARNING_MESSAGE);
            validName = false;
            usernameField.setText("");
            usernameField.requestFocusInWindow();
        } else {
            if(username.isEmpty() || username.matches("^_*$")){ //ユーザーネームが空あるいはアンダーバーのみで構成される場合名前をGuest_Userとし次の画面に進むか聞く
                int option = JOptionPane.showConfirmDialog(null,"You'll connect the server as a Guest User","No Name",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (option == JOptionPane.YES_OPTION){
                    validName = true;
                }else if (option == JOptionPane.NO_OPTION){
                    validName = false;
                    usernameField.setText("");
                }
            } else { //ユーザーネームが適切な長さの場合、メッセージを表示しそのまま次に進む
                JOptionPane.showMessageDialog(Login.this, "Username: " + username,"Login Successful", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        //キャラクター選択画面に遷移
        if(validName){
            dispose(); //画面を閉じる
            CharacterSelect characterSelect = new CharacterSelect(username);
            characterSelect.setLocation(getLocation());
            characterSelect.setVisible(true);
        }
    }

    //文字列の長さを判定する。アルファベットを1文字、日本語を2文字とし20文字以内の場合trueを、超えた場合falseを返す
    boolean GetStrLimitation(String str){
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
