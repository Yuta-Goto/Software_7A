import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CharacterSelect extends JFrame{
    public CharacterSelect(String username) {
        setTitle("Character Select");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(2,3));
        

        for(int i =1; i<=6;i++){
            JPanel characterPanel = new JPanel(); // キャラクターパネル
            characterPanel.setLayout(new BorderLayout());
            final int characterIndex = i;

            JLabel characterImageLabel = new JLabel();
            
            ImageIcon characterImage = new ImageIcon("./datas/Portraits/Character" + i + ".png"); //各キャラクター画像のパスを指定
            characterImageLabel.setIcon(characterImage);
            characterPanel.add(characterImageLabel, BorderLayout.CENTER);

            JButton selectButton = new JButton("Select");
            selectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e){
                    //キャラクターが選択されたときの処理をここに追加
                    JOptionPane.showMessageDialog(CharacterSelect.this, "Character" + characterIndex + "selected");
                    MainScreen mainscreen = new MainScreen();
                    mainscreen.SetMainScreen(characterIndex, username);
                }
            });
            JButton backButton = new JButton("Back to Login");
            backButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e){
                    dispose();
                    Login login = new Login();
                    login.setVisible(true);
                }
            });
            characterPanel.add(backButton, BorderLayout.NORTH);
            characterPanel.add(selectButton, BorderLayout.SOUTH);
            mainPanel.add(characterPanel); //メインパネルにキャラクターを追加

        }

        add(new JScrollPane(mainPanel)); //メインパネルをスクロール可能にする
        setVisible(true);    
    }
        
}
