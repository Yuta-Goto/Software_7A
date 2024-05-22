//メイン画面を実装
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

//地図
class Map{
    Image MapImage;
    private int Width;
    private int Height;
    public static final int MapTile = 48;

    Map(int width, int height, String Mapfile){
        MapImage = Toolkit.getDefaultToolkit().getImage(Mapfile);
        Width = width;
        Height = height;
    }

    public void draw(Graphics g){
        g.drawImage(MapImage, 0, 0, Width, Height, null);
    }
}

//当たり判定のある壁
class Wall{
    private int x1, x2, y1, y2;

    Wall(int X1, int Y1, int X2, int Y2){
        x1 = X1;
        y1 = Y1;
        x2 = X2;
        y2 = Y2;
    }

    //座標(posiX,posiY)と壁の距離を返す
    int GetDistance(int posiX, int posiY){
        return Math.max(Math.max(posiX - x2, x1 - posiX), Math.max(posiY - y2, y1 - posiY));
    }

}

//当たり判定のある障害物
class Object {
    Image img;
    private int  x1, x2, y1, y2;

    Object(int X1, int Y1, int X2, int Y2, String FileName){
        x1 = X1;
        y1 = Y1;
        x2 = X2;
        y2 = Y2;
        img = Toolkit.getDefaultToolkit().getImage(FileName);
    }

    //座標(posiX,posiY)と壁の距離を返す
    int GetDistance(int posiX, int posiY){
        return Math.max(Math.max(posiX - x2, x1 - posiX), Math.max(posiY - y2, y1 - posiY));
    }

    public void draw(Graphics g){
        g.drawImage(img, x1, y1, x2-x1, y2-y1, null);
    }
}

//------自身も含めた参加者全員を描画するためのクラス------
class Person{
    public int uniqueValue;   //インスタンスを識別する固有値。サーバーのスレッド番号を格納できるといいかな...
    public int x, y;          //位置座標
    public int characterSelect;
    public int direction = 0; //アバターの向きを表す変数
    public int anim = 0;      //アバターのアニメーション状態を表す変数
    public int usernamelength = 0; //ユーザーネームの長さ
    public String UserName = ""; //ユーザーネーム
    public String Comment = "";  //ユーザーのコメント
    public int commentlength = 0;
    public int connectionTimer = 10;
    private static final int Size = 48; //大きさ
    private static final int AlphabetSize = 7; //標準フォント(Monospaced,サイズ12)での1文字あたりの文字幅
    private static final int JapaneseSize = 5; //日本語文字とアルファベットの文字幅の差分

    Image img, portrait;
    Font graphicFont = new Font("Monospaced", Font.PLAIN, 12);
    Font WindowFont = new Font("Monospaced", Font.PLAIN, 30);

    Person(String Name, int character, int integer){
        UserName = Name;
        characterSelect = character;
        if(Name.isEmpty()){ //ユーザーネームはデフォルトでGuest User
            UserName = "Guest_User";
        }
        usernamelength = UserName.length()*AlphabetSize+CharacterCount(UserName)*JapaneseSize;
        img = Toolkit.getDefaultToolkit().getImage("./datas/Characters/character"+characterSelect+".png");
        portrait = Toolkit.getDefaultToolkit().getImage("./datas/Portraits/character"+characterSelect+".png");
        uniqueValue = integer;
    }

    int CharacterCount(String str){ //文字列から日本語文字の数を取得する
        int japaneseCharCount = 0;
        
        // 文字列内の各文字を調べる
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // Unicodeの範囲を利用して日本語文字かどうかを判別する
            // 日本語の範囲はUnicodeのU+3000からU+9FFFまでとU+FF65からU+FF9Fまで
            if ((c >= '\u3000' && c <= '\u9FFF') || (c >= '\uFF65' && c <= '\uFF9F')) {
                japaneseCharCount++;
            }
        }
        return japaneseCharCount;
    }

    //インスタンスの状態(座標・向き・アニメーション状態・コメント)を設定
    void SetPersonState(int xx, int yy, int d, int t, String comment){
        x = xx;
        y = yy;
        direction = d;
        anim = t;
        Comment = comment;
        commentlength = Comment.length()*AlphabetSize+CharacterCount(Comment)*JapaneseSize;
        connectionTimer = 100;//10;
    }

    //参加者と吹き出しを座標を起点に描画
    void draw(Graphics g){
        Color UserBG = new Color(255, 255, 255, 100);
        //g.setFont(graphicFont);
        g.drawImage(img, x-Size/2, y-Size/2, x+Size/2, y+Size/2, 48*anim, 48*direction, 48*(anim+1), 48*(direction+1),null);
        g.setColor(UserBG);
        g.fillRect(x-usernamelength/2-2, y-Size/2-12-1, usernamelength+4, 12+2);
        g.setColor(Color.BLACK);
        g.drawString(UserName, x-usernamelength/2, y-Size/2);
    }

    void drawComment(Graphics g){
        if(!Comment.isEmpty()){
            g.setColor(Color.WHITE);
            g.fillRoundRect(x+Size, y-21, commentlength+12, 24, 24, 24);
            g.fillOval(x+Size-12, y, 16, 8);
            g.fillOval(x+Size-24, y+10, 8, 4);
            g.setColor(Color.BLACK);
            g.drawRoundRect(x+Size, y-21, commentlength+12, 24, 24, 24);
            g.drawOval(x+Size-12, y, 16, 8);
            g.drawOval(x+Size-24, y+10, 8, 4);
            g.drawString(Comment, x+Size+6, y-5);
        }
        connectionTimer--;
    }

    void drawlist(Graphics g, int i){
        g.setFont(WindowFont);
        g.drawImage(portrait, 15, 15+120*i, 90, 90, null);
        g.drawRect(15, 15+120*i, 90, 90);
        g.drawLine(0,120*(i+1),600,120*(i+1));
        g.drawString(UserName, 140, 75+120*i);

    }
}
//---------------------------

//アバター。クライアントが操作する仮想体。
class Avatar{
    private int    x, y;         //位置座標
    private int screenCenterX, screenCenterY;
    private int    nextx, nexty; //入力を受けた後の当たり判定前仮位置座標
    private int characterselect;
    private int    direction = 0;//アバターの向きを表す変数
    private int    anim = 3;
    private int commentTimer = 0;
    private String UserName = "";     //ユーザーネーム
    private String Comment = "";      //ユーザーのコメント
    private static final int Size = 48;           //大きさ
    private static final int Threshold = Size/2;  //排除半径
    private static final int Stride = 3;          //アバターの歩幅
    private static final int AnimationClock = 10; //歩行アニメーションを何クロックおきに切り替えるか

    Image IconImage;
    //MainScreen gui;
    
    Avatar(int xx, int yy, int character, String Name){
        x = xx;
        y = yy;
        characterselect = character;
        UserName = Name;
        if(Name.isEmpty()){ //ユーザーネームはデフォルトでGuest User
            UserName = "Guest_User";
        }
        IconImage = Toolkit.getDefaultToolkit().getImage("./datas/Characters/character"+characterselect+".png");   
    }

    //コメントが消えるまでの時間を設定
    void SaySth(String str){
        if(!str.isEmpty()){
            Comment = str;
            commentTimer = 300;
        }
    }

    //自分の現在地、向き、アニメーション変数、コメントを送信用文字列に加工して返す。
    String GetData(){
        return UserName+" "+characterselect+" "+x+" "+y+" "+direction+" "+anim+" "+Comment;
    }

    
    //ウィンドウで描画する中心の座標を取得
    int GetStandardPointX(int MapSizeX, int Sight, int X){
        screenCenterX = Math.min(Math.max(x, Sight), MapSizeX - Sight);
        return screenCenterX;
    }

    int GetStandardPointY(int MapSizeY, int Sight, int Y){
        screenCenterY = Math.min(Math.max(y, Sight), MapSizeY - Sight);
        return screenCenterY;
    }

    //入力を受けて次の仮の座標を計算する
    void CalcNextCoordinate(boolean L, boolean U, boolean R, boolean D){
        nextx = x;
        nexty = y;
        if(!U||!D){
            if(U){
                nexty -= Stride;
                direction = 3;
            }
            if(D){
                nexty += Stride;
                direction = 0;
            }
        }
        if(!R||!L){
            if(R){
                nextx += Stride;
                direction = 2;
            }
            if(L){
                nextx -= Stride;
                direction = 1;
            }
        }
    }

    //ある物体に近ければfalse、どの物体とも遠ければtrueを返す
    boolean CheckDistanceToObject(Object[] ob){
        if(ob == null) {
            return true;
        }
        for(int i = 0; i < ob.length; i++){
            if(ob[i].GetDistance(nextx,nexty) < Threshold){
                return false;
            }
        }
        return true;
    }

    //ある壁に近ければfalse、どの壁とも遠ければtrueを返す
    boolean CheckDistanceToWall(Wall[] wall){
        if(wall == null) {
            return true;
        }
        for(int i = 0; i < wall.length; i++){
            if(wall[i].GetDistance(nextx,nexty) < Threshold){
                return false;
            }
        }
        return true;
    }

    //壁とオブジェクトの当たり判定を行い、衝突がなければ仮座標を実座標に代入する
    Person CheckCollision(Wall[] wall, Object[] object, int Timer){
        anim = 3;
        if(CheckDistanceToWall(wall) && CheckDistanceToObject(object)){
                if(x != nextx || y != nexty){
                    x = nextx;
                    y = nexty;
                    anim = (3+(Timer+AnimationClock-1)/AnimationClock) % 4;
                }
        }
        Person avatarPerson = new Person(UserName, characterselect,-1);
        avatarPerson.SetPersonState(x,y,direction,anim,Comment);
        return avatarPerson;
    }

    //コメントの表示時間の管理
    void draw(Graphics g, int Timer, boolean pause){
        if(commentTimer != 0){
            commentTimer--;
        } else {
            Comment = "";
        }
        if(pause){
            drawPauseWindow(g, screenCenterX, screenCenterY);
        }
    }

    //ポーズ画面の描画
    void drawPauseWindow(Graphics g, int X, int Y){
        Color PauseBG = new Color(0,0,0,100);
        Color WindowColor = new Color(237,237,237);
        g.setColor(PauseBG);
        g.fillRect(X-300, Y-300, 600, 600);
        g.setColor(WindowColor);
        g.fillRoundRect(X-100, Y-150, 200, 330,30,30);
        g.setColor(Color.BLACK);
        g.drawRoundRect(X-100, Y-150, 200, 330,30,30);

        g.drawRoundRect(X-60, Y-120, 120, 30,15,15);
        g.drawRoundRect(X-60, Y-60, 120, 30,15,15);
        g.drawRoundRect(X-60, Y, 120, 30,15,15);
        g.drawRoundRect(X-60, Y+60, 120, 30,15,15);
        g.drawRoundRect(X-60, Y+120, 120, 30,15,15);

        g.drawString("Return", X-21, Y-100);
        g.drawString("Room Members", X-42, Y-40);
        g.drawString("Send Message", X-42, Y+20);
        g.drawString("Keyboard", X-28, Y+80);
        g.drawString("Log out", X-24, Y+140);
    }
}

public class MainScreen extends JFrame implements Runnable{

    private JPanel SimulationPanel;
    private JButton returnbutton;
    private JButton personsbutton;
    private JButton commentbutton;
    private JButton keyboardbutton;
    private JButton logoutbutton;
    private Avatar avatar;
    private Map map;
    private int NumOfObject, NumOfWall, NumOfPoint;
    private Object[] object;
    private Wall[] wall;
    private Thread thread;
    private Image offscreen = null;
    Image memberList = null;
    private JTextField textField;

    private boolean left = false;
    private boolean up = false;
    private boolean right = false;
    private boolean down = false;
    private boolean chatting = false;

    private boolean pause = false;

    private int Timer = 0;
    private int SightX;
    private int SightY;
    
    private boolean activated = false;
    
    final static private int WindowSize = 700;   // 動画を描画する領域のサイズ
    final static private int GraphicRange = 300; // アバターの視界範囲(描画範囲)
    //final static private float MagRate = ((float)WindowSize)/((float)GraphicRange*2);

    final static public int MapSizeX = 984*2; // マップ全体の幅
    final static public int MapSizeY = 960*2; // マップ全体の高さ

    public static List<Person> RoomMember = Collections.synchronizedList(new ArrayList<Person>());

    private Client_connection client_connection;

    //ポーズ画面のボタンを設定
    void SetMainScrrenComponents() {
        SimulationPanel = new JPanel();
        SimulationPanel.setLayout(null);
        SimulationPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyRelease(e);
            }
        });
        SimulationPanel.setFocusable(true);
        this.setContentPane(SimulationPanel);
        SetPauseScreenButtons();

        // テキストフィールドの追加
        textField = new JTextField("");
        textField.setBounds(0, 0, 0, 0); // 適切な位置とサイズを設定
        SimulationPanel.add(textField);

        // エンターキーのリスナーを設定
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // テキストフィールドでEnterが押された時の処理
                speak();
            }
        });
    }

    //-----キーの入力を取得-----
    private void handleKeyPress(KeyEvent e) {
        if(!pause){
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    left = true;
                    break;
                case KeyEvent.VK_UP:
                    up = true;
                    break;
                case KeyEvent.VK_RIGHT:
                    right = true;
                    break;
                case KeyEvent.VK_DOWN:
                    down = true;
                    break;
                case KeyEvent.VK_ESCAPE:
                    left = false;
                    up = false;
                    right = false;
                    down = false;
                    pause = true;
                    SetButtonsState();
                    break;
                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_T:
                    left = false;
                    up = false;
                    right = false;
                    down = false;
                    chatting = true;
                    textField.setText("");
                    textField.requestFocusInWindow();
                    break;
            }
        } else {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_ESCAPE:
                    pause = false;
                    SetButtonsState();
                    break;
                
                default:
                    break;
            }
        }

    }

    private void handleKeyRelease(KeyEvent e) {
        switch (e.getKeyCode()) {
            case 37:
                left = false;
                break;
            case 38:
                up = false;
                break;
            case 39:
                right = false;
                break;
            case 40:
                down = false;
                break;
        }
    }
    //----------------------------

    //ポーズ画面で用いるボタンを設定し、表示する。
    void SetPauseScreenButtons(){
        returnbutton = new JButton("");
        personsbutton = new JButton("");
        commentbutton = new JButton("");
        keyboardbutton = new JButton("");
        logoutbutton = new JButton("");

        returnbutton.setBounds(WindowSize/2-70, 180, 140, 40); 
        SetButtonInvisible(returnbutton);
        returnbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //クリック時ポーズ画面を閉じる
                pause = false;
                SetButtonsState();
                SimulationPanel.requestFocusInWindow();
            }
        });

        personsbutton.setBounds(WindowSize/2-70, 250, 140, 40);
        SetButtonInvisible(personsbutton);
        personsbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showRoomMember();
                personsbutton.setEnabled(false);
            }
        });

        commentbutton.setBounds(WindowSize/2-70, 320, 140, 40);
        SetButtonInvisible(commentbutton);
        commentbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //クリック時ポーズ画面を開き、チャットモードに設定する
                pause = false;
                chatting = true;
                SetButtonsState();
                textField.requestFocusInWindow();
            }
        });

        keyboardbutton.setBounds(WindowSize/2-70, 390, 140, 40);
        SetButtonInvisible(keyboardbutton);
        keyboardbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //クリック時操作方法の画面を開く
                showKeyboardOperation();
            }
        });

        logoutbutton.setBounds(WindowSize/2-70, 460, 140, 40);
        SetButtonInvisible(logoutbutton);
        logoutbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // クリック時ウィンドウを閉じてログイン画面に遷移する
                CloseWindow();
                TitleScreen title = new TitleScreen();
                title.setVisible(true);
            }
        });

        SimulationPanel.add(returnbutton);
        SimulationPanel.add(personsbutton);
        SimulationPanel.add(commentbutton);
        SimulationPanel.add(keyboardbutton);
        SimulationPanel.add(logoutbutton);
        SetButtonsState();
    }

    //ボタンを透明にする
    void SetButtonInvisible(JButton button){
        button.setOpaque(false); // 透明に設定
        button.setContentAreaFilled(false); // コンテンツ領域も透明にする
        button.setBorderPainted(false); // ボーダーを非表示にする
        button.setFocusPainted(false);
    }

    //ポーズ画面の時のみボタンを押せるようにする
    void SetButtonsState(){
        returnbutton.setEnabled(pause);
        personsbutton.setEnabled(pause);
        commentbutton.setEnabled(pause);
        keyboardbutton.setEnabled(pause);
        logoutbutton.setEnabled(pause);
    }

    //コメント送信時、アバターのインスタンスにコメントを引き渡し、チャット画面を閉じる
    void speak(){
        String str = textField.getText();
        if(!str.isEmpty()){
            //ChangeMonologue(str);
            avatar.SaySth(str);
        }
        textField.setText("");
        chatting = false;
        SimulationPanel.requestFocusInWindow();
    }

    //メッセージ入力ウィンドウを表示
    void drawChatWindow(Graphics g, int X, int Y){
        Color ChatWindow = new Color(0,0,0,100);
        g.setColor(ChatWindow);
        g.fillRect(X-250, Y+275, 500, 16);
        g.setColor(Color.WHITE);
        g.drawString(textField.getText(), X-250, Y+274+12);
    }

    //参加者一覧を表示
    private void showRoomMember() {
        JFrame memberWindow = new JFrame("Room Members");
        memberWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        memberWindow.setLayout(new BorderLayout());

        JPanel imagePanel = new JPanel() {
            List<Person> MemberGraphic = RoomMember;
            //MemberGraphic = RoomMember;
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                List<Person> MemberGraphic;
                synchronized(RoomMember) {
                    MemberGraphic = new ArrayList<>(RoomMember); // スナップショットを作成
                }

                g.setColor(Color.WHITE);
                g.fillRect(0, 0, 600, 120 * MemberGraphic.size());
                g.setColor(Color.YELLOW);
                g.fillRect(0, 0, 600, 120);
                g.setColor(Color.BLACK);

                Collections.sort(MemberGraphic, new Comparator<Person>() {
                    @Override
                    public int compare(Person c1, Person c2) {
                        return Integer.compare(c1.uniqueValue, c2.uniqueValue);
                    }
                });
                int i = 0;
                for (Person p : MemberGraphic) {
                    p.drawlist(g, i);
                    System.out.println(p.UserName + "," + p.characterSelect);
                    i++;
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(600,Math.max(MemberGraphic.size()*120,550));
            }
        };

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton closeButton = new JButton("Return to Menu");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                memberWindow.dispose();
                personsbutton.setEnabled(true);
            }
        });

        buttonPanel.add(closeButton);

        memberWindow.add(imagePanel, BorderLayout.NORTH);
        memberWindow.add(buttonPanel, BorderLayout.SOUTH);

        memberWindow.pack();

        int x = getX() + (getWidth() - memberWindow.getWidth()) / 2;
        int y = getY() + (getHeight() - memberWindow.getHeight()) / 2;
        memberWindow.setLocation(x, y);

        memberWindow.setVisible(true);

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
                g.drawImage(image, 0, 0, WindowSize, WindowSize-50, null);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(WindowSize, WindowSize-50);
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

    private void proceedOne(){
        if(offscreen == null) {
            offscreen = this.createImage(MapSizeX, MapSizeY);
        }
            
        Graphics g = offscreen.getGraphics(); 
        //ユーザには見えないoffscreenを準備

        g.clearRect(0, 0, MapSizeX, MapSizeY); 
        //以前のコマを全部消す。ユーザには見えないのでちらちらしない

        draw(g); 
        //つぎのコマをoffscreenに描く。ユーザにはこの絵はまだ見えていない

        Graphics currentg = this.getGraphics(); 
        //表示用Graphicsを得る

        currentg.drawImage(offscreen, 0, 0, WindowSize, WindowSize, SightX - GraphicRange, SightY - GraphicRange, SightX + GraphicRange, SightY + GraphicRange, this);
        //作ったコマを一気に表示用Graphicsにコピー
    }

    //排他処理が必要なメソッド。
    synchronized
        private void MoveAvatar(){ //入力と現在の座標に応じてアバターの座標を計算する
            if(up||left||right||down){
                Timer++;
            } else {
                Timer = 0;
            }
            avatar.CalcNextCoordinate(left,up,right,down);
            //avatar.CheckCollision(wall,object,Timer);
            updateRoomMember(avatar.CheckCollision(wall,object,Timer));
            SightX = avatar.GetStandardPointX(MapSizeX, GraphicRange, SightX);
            SightY = avatar.GetStandardPointY(MapSizeY, GraphicRange, SightY);
        }

    synchronized
        public static void updateRoomMember(Person person){ //参加者のリストを更新
            boolean exist = false;
            for(Person p : RoomMember){
                if(p.uniqueValue == person.uniqueValue){
                    p.SetPersonState(person.x, person.y, person.direction, person.anim, person.Comment);
                    exist = true;
                    break;
                }
            }
            if(!exist){
                RoomMember.add(person);
            }
        }

        synchronized public static void removeUnconnectMembers() {
            Iterator<Person> iterator = RoomMember.iterator();
            while (iterator.hasNext()) {
                Person person = iterator.next();
                if (person.connectionTimer < 0) {
                    iterator.remove();
                }
            }
        }

    synchronized 
        private void draw(Graphics g){ //マップ上にオブジェクトと参加者を描画する
            List<Person> roomMemberCopy;
            synchronized(RoomMember) {
                removeUnconnectMembers();
                roomMemberCopy = new ArrayList<>(RoomMember);
            }
            map.draw(g);
            for(int i = 0; i < object.length; i++){
                object[i].draw(g);
            }
            Collections.sort(roomMemberCopy, new Comparator<Person>() {
                @Override
                public int compare(Person c1, Person c2) {
                    return Integer.compare(c1.y, c2.y);
                }
            });
            for(Person p : roomMemberCopy){
                p.draw(g);
            }
            for(Person p : roomMemberCopy){
                p.drawComment(g);
            }
            if(chatting){
                drawChatWindow(g, SightX, SightY);
            }
            avatar.draw(g,Timer,pause);
        }

    // メイン画面の初期設定
    public void SetMainScreen(int characterSelect, String username){
        setTitle("Online Meeting");
        setBounds(0, 0, WindowSize, WindowSize);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(Color.black);
        SetMainScrrenComponents();

        Font defaultFont = new Font("Monospaced", Font.PLAIN, 12); // 文字のフォントを設定
        setFont(defaultFont);

        ArrayList<Integer> spawnX = new ArrayList<>();
        ArrayList<Integer> spawnY = new ArrayList<>();
        map = new Map(MapSizeX,MapSizeY,"./datas/Map.png");
        LoadObject("./datas/Object.txt");
        LoadWall("./datas/Wall.txt");
        int random = LoadSpawnPoint("./datas/InitialPoints.txt", spawnX, spawnY);

        RoomMember = new ArrayList<Person>();

        avatar = new Avatar(spawnX.get(random),spawnY.get(random),characterSelect,username);
        Person avatargraphic = new Person(username, characterSelect, -1);
        avatargraphic.SetPersonState(spawnX.get(random),spawnY.get(random),0,3,"");
        RoomMember.add(avatargraphic);

        setVisible(true); // proceedOne()でcreateImage()を実行する前にvisibleにする。
        
        activated = true;
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }

        try {
            client_connection = new Client_connection();
            client_connection.ConnectAndStart(avatar);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //スレッドを終了し、ウィンドウを閉じる
    public void CloseWindow(){
        activated = false;
        //setVisible(false);
        if (client_connection != null) {
            client_connection.CloseConnection();;
        }
    }

    public void LoadObject(String ObjectDatafile){
        try {
            // オブジェクトのデータファイルの読み込み
            BufferedReader br = new BufferedReader(new FileReader(ObjectDatafile));
            NumOfObject = Integer.parseInt(br.readLine());
            object = new Object[NumOfObject];
            for (int i = 0; i < NumOfObject; i++) {
                String line = br.readLine();
                String[] parts = line.split(" ");
                int X1 = Integer.parseInt(parts[0]) * Map.MapTile;
                int Y1 = Integer.parseInt(parts[1]) * Map.MapTile;
                int X2 = Integer.parseInt(parts[2]) * Map.MapTile;
                int Y2 = Integer.parseInt(parts[3]) * Map.MapTile;
                String filename = parts[4];
                object[i] = new Object(X1,Y1,X2,Y2, "./datas/objects/"+filename);
            }
            br.close();
            System.out.println("ObjectData Load Succeeded!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void LoadWall(String WallDatafile){
        try {
            // 壁のデータファイルの読み込み
            BufferedReader br = new BufferedReader(new FileReader(WallDatafile));
            NumOfWall = Integer.parseInt(br.readLine());
            wall = new Wall[NumOfWall];
            for (int i = 0; i < NumOfWall; i++) {
                String line = br.readLine();
                String[] parts = line.split(" ");
                int Xposititon1 = Integer.parseInt(parts[0]) * Map.MapTile;
                int Yposititon1 = Integer.parseInt(parts[1]) * Map.MapTile;
                int Xposititon2 = Integer.parseInt(parts[2]) * Map.MapTile;
                int Yposititon2 = Integer.parseInt(parts[3]) * Map.MapTile;
                wall[i] = new Wall(Xposititon1, Yposititon1, Xposititon2, Yposititon2);
            }
            br.close();
            System.out.println("WallData Load Succeeded!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int LoadSpawnPoint(String Datafile, ArrayList<Integer> X, ArrayList<Integer> Y){
        try {
            // 初期地点の座標データファイルの読み込み
            BufferedReader br = new BufferedReader(new FileReader(Datafile));
            NumOfPoint = Integer.parseInt(br.readLine());
            for (int i = 0; i < NumOfPoint; i++) {
                String line = br.readLine();
                String[] parts = line.split(" ");
                X.add(Integer.parseInt(parts[0]) * Map.MapTile);
                Y.add(Integer.parseInt(parts[1]) * Map.MapTile);
            }
            br.close();
            System.out.println("SpawnPoint Load Succeeded!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Random rand = new Random();
        return rand.nextInt(NumOfPoint);
    }

    //並行処理
    public void run() {
        while(activated) {
            try{
                Thread.sleep(5);
            } 
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            MoveAvatar();
            //アバターの次の座標を計算
            proceedOne();
            //つぎのコマを描く
        }
        dispose();
    }

}