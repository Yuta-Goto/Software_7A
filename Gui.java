//メイン画面を実装
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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

//------他の参加者。未実装------
class Person{
    private int    x, y;          //位置座標
    private int    direction = 0; //アバターの向きを表す変数
    private String UserName;      //ユーザーネーム
    private String Comment = "";  //ユーザーのコメント
    private static final int Size = 48;           //大きさ
    private static final int AnimationClock = 10; //歩行アニメーションを何クロックおきに切り替えるか

    Image img;

    Person(String Name, int character){
        UserName = Name;
        if(Name.isEmpty()){ //ユーザーネームはデフォルトでGuest User
            UserName = "Guest User";
        }
        img = Toolkit.getDefaultToolkit().getImage("./datas/Characters/character"+character+".png");
    }

    void SetCoordinate(int xx, int yy){
        x = xx;
        y = yy;
    }

    void SaySth(String str){
        if(str.isEmpty()){
            Comment = str;
        }
    }

    void draw(Graphics g, int Timer){
        int t = (int)((Timer-1)/AnimationClock) % 4;
        //g.drawRect(x-Size/2, y-Size/2, Size, Size); //当たり判定の視覚化用
        g.drawImage(img, x-Size/2, y-Size/2, x+Size/2, y+Size/2, 48*t, 48*direction, 48*(t+1), 48*(direction+1),null);
        g.drawString(UserName, x-Size/2, y-Size/2);
        if(Comment.isEmpty()){
            Comment = "";
        } else {
            g.drawString(Comment, x+Size, y-Size/2);
        }
    }
}
//---------------------------

//アバター。クライアントが操作する仮想体。
class Avatar{
    private int    x, y;         //位置座標
    private int screenCenterX, screenCenterY;
    private int    nextx, nexty; //入力を受けた後の当たり判定前仮位置座標
    private int    direction = 0;//アバターの向きを表す変数
    private int commentTimer = 0;
    private int commentlength = 0;
    private int usernamelength = 0;
    private String UserName;     //ユーザーネーム
    private String Comment = null;      //ユーザーのコメント
    private static final int Size = 48;           //大きさ
    private static final int Threshold = Size/2;  //排除半径
    private static final int Stride = 3;          //アバターの歩幅
    private static final int AnimationClock = 10; //歩行アニメーションを何クロックおきに切り替えるか

    Image IconImage;
    Gui gui;
    
    Avatar(int xx, int yy, int character, String Name, int length, Gui Gui){
        x = xx;
        y = yy;
        UserName = Name;
        usernamelength = length;
        if(Name.isEmpty()){ //ユーザーネームはデフォルトでGuest User
            UserName = "Guest User";
            usernamelength = 61;
        }
        IconImage = Toolkit.getDefaultToolkit().getImage("./datas/Characters/character"+character+".png");   
    }

    void SaySth(String str, int textlength){
        if(!str.isEmpty()){
            Comment = str;
            commentlength = textlength;
            commentTimer = 200;
        }
    }

    int GetStandardPointX(int MapSizeX, int Sight, int X){
        screenCenterX = Math.min(Math.max(x, Sight), MapSizeX - Sight);
        return screenCenterX;
    }

    int GetStandardPointY(int MapSizeY, int Sight, int Y){
        screenCenterY = Math.min(Math.max(y, Sight), MapSizeY - Sight);
        return screenCenterY;
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

    //入力を受けて次の仮の座標を計算する
    void CalcNextCoordinate(boolean L, boolean U, boolean R, boolean D){
        nextx = x;
        nexty = y;
        if(U){
            nexty -= Stride;
            direction = 3;
        }
        if(D){
            nexty += Stride;
            direction = 0;
        }
        if(R){
            nextx += Stride;
            direction = 2;
        }
        if(L){
            nextx -= Stride;
            direction = 1;
        }
    }

    //当たり判定がないことが確認された場合に実行される。仮の座標を実座標に代入する
    void ConfirmNoCollision(){
        x = nextx;
        y = nexty;
    }

    void draw(Graphics g, int Timer, boolean pause){
        int t = (3+(Timer+AnimationClock-1)/AnimationClock) % 4;
        g.drawImage(IconImage, x-Size/2, y-Size/2, x+Size/2, y+Size/2, 48*t, 48*direction, 48*(t+1), 48*(direction+1),null);
        g.setColor(Color.WHITE);
        g.fillRect(x-usernamelength/2, y-Size/2-12, usernamelength, 12);
        g.setColor(Color.BLACK);
        g.drawRect(x-usernamelength/2, y-Size/2-12, usernamelength, 12);
        g.drawString(UserName, x-usernamelength/2, y-Size/2);
        if(commentTimer != 0){
            g.setColor(Color.WHITE);
            g.fillOval(x+Size, y-21, commentlength+12, 24);
            g.fillOval(x+Size-12, y, 16, 8);
            g.fillOval(x+Size-24, y+10, 8, 4);
            g.setColor(Color.BLACK);
            g.drawOval(x+Size, y-21, commentlength+12, 24);
            g.drawOval(x+Size-12, y, 16, 8);
            g.drawOval(x+Size-24, y+10, 8, 4);
            g.drawString(Comment, x+Size+6, y-5);
            commentTimer--;
        } else {
            Comment = "";
            commentlength = 0;
        }
        if(pause){
            drawPauseWindow(g, screenCenterX, screenCenterY);
        }
    }

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

        g.drawString("RETURN", X-25, Y-100);
        g.drawString("ROOM MEMBERS", X-50, Y-40);
        g.drawString("SEND MESSAGE", X-50, Y+20);
        g.drawString("KEYBOARD", X-30, Y+80);
        g.drawString("LOG OUT", X-27, Y+140);
    }
}

public class Gui extends JFrame implements Runnable{

    private JPanel SimulationPanel;
    private JButton returnbutton;
    private JButton personsbutton;
    private JButton commentbutton;
    private JButton keyboardbutton;
    private JButton logoutbutton;
    private Avatar avatar;
    private Map map;
    private int NumOfObject, NumOfWall;
    private Object[] object;
    private Wall[] wall;
    private Thread thread;
    private Image offscreen = null;
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
    final static private float MagRate = ((float)WindowSize)/((float)GraphicRange*2);

    final static public int MapSizeX = 984*2; // マップ全体の幅
    final static public int MapSizeY = 960*2; // マップ全体の高さ

    public Gui() {
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

    void SetPauseScreenButtons(){
        returnbutton = new JButton("");
        personsbutton = new JButton("");
        commentbutton = new JButton("");
        keyboardbutton = new JButton("");
        logoutbutton = new JButton("");

        returnbutton.setBounds(WindowSize/2-70, 180, 140, 40); // 適切な位置とサイズを設定
        //SetButtonInvisible(returnbutton);
        returnbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // ボタンがクリックされたときの処理
                pause = false;
                SetButtonsState();
                SimulationPanel.requestFocusInWindow();
            }
        });

        personsbutton.setBounds(WindowSize/2-70, 250, 140, 40); // 適切な位置とサイズを設定
        //SetButtonInvisible(personsbutton);
        personsbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // ボタンがクリックされたときの処理
                SimulationPanel.requestFocusInWindow();
            }
        });

        commentbutton.setBounds(WindowSize/2-70, 320, 140, 40); // 適切な位置とサイズを設定
        //SetButtonInvisible(commentbutton);
        commentbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // ボタンがクリックされたときの処理
                pause = false;
                chatting = true;
                SetButtonsState();
                textField.requestFocusInWindow();
            }
        });

        keyboardbutton.setBounds(WindowSize/2-70, 390, 140, 40); // 適切な位置とサイズを設定
        //SetButtonInvisible(keyboardbutton);
        keyboardbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // ボタンがクリックされたときの処理
                SimulationPanel.requestFocusInWindow();
            }
        });

        logoutbutton.setBounds(WindowSize/2-70, 460, 140, 40); // 適切な位置とサイズを設定
        //SetButtonInvisible(logoutbutton);
        logoutbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // ボタンがクリックされたときの処理
                CloseWindow();
            }
        });

        SimulationPanel.add(returnbutton);
        SimulationPanel.add(personsbutton);
        SimulationPanel.add(commentbutton);
        SimulationPanel.add(keyboardbutton);
        SimulationPanel.add(logoutbutton);
    }

    void SetButtonInvisible(JButton button){
        button.setOpaque(false); // 透明に設定
        button.setContentAreaFilled(false); // コンテンツ領域も透明にする
        button.setBorderPainted(false); // ボーダーを非表示にする
        button.setFocusPainted(false);
    }

    void SetButtonsState(){
        returnbutton.setEnabled(pause);
        personsbutton.setEnabled(pause);
        commentbutton.setEnabled(pause);
        keyboardbutton.setEnabled(pause);
        logoutbutton.setEnabled(pause);
    }

    void speak(){
        String str = textField.getText();
        int textlength = 0;
        if(!str.isEmpty()){
            FontMetrics fm = getFontMetrics(getFont());
            textlength = fm.stringWidth(str);
            ChangeMonologue(str, textlength);
        }
        textField.setText("");
        chatting = false;
        SimulationPanel.requestFocusInWindow();
    }

    void drawChatWindow(Graphics g, int X, int Y){
        Color ChatWindow = new Color(0,0,0,100);
        g.setColor(ChatWindow);
        g.fillRect(X-250, Y+275, 500, 16);
        g.setColor(Color.WHITE);
        g.drawString(textField.getText(), X-250, Y+274+12);
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
        private void ChangeMonologue(String monologue, int textlength){
            avatar.SaySth(monologue, textlength);
        }

    synchronized
        private void MoveAvatar(){ //入力と現在の座標に応じてアバターの座標を計算する
            if(up||left||right||down){
                Timer++;
            } else {
                Timer = 0;
            }
            avatar.CalcNextCoordinate(left,up,right,down);
            if(avatar.CheckDistanceToWall(wall)){
                if(avatar.CheckDistanceToObject(object)){
                    avatar.ConfirmNoCollision();
                }
            }
            SightX = avatar.GetStandardPointX(MapSizeX, GraphicRange, SightX);
            SightY = avatar.GetStandardPointY(MapSizeY, GraphicRange, SightY);
        }

    synchronized 
        private void draw(Graphics g){
            map.draw(g);
            for(int i = 0; i < object.length; i++){
                object[i].draw(g);
            }
            if(chatting){
                drawChatWindow(g, SightX, SightY);
            }
            avatar.draw(g,Timer,pause);
        }

    // 初期化
    public void SetMainScreen(int characterSelect){
        setTitle("Online Meeting");
        setBounds(0, 0, WindowSize, WindowSize);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.black);

        Font defaultFont = new Font("Arial", Font.PLAIN, 12); // 文字のフォントを設定
        setFont(defaultFont);

        String username = "Sample User Name";
        int namelength = 0;

        map = new Map(MapSizeX,MapSizeY,"./datas/Map.png");
        LoadObject("./datas/Object.txt");
        LoadWall("./datas/Wall.txt");

        FontMetrics fm = getFontMetrics(getFont());
        namelength = fm.stringWidth(username);
        avatar = new Avatar(MapSizeX/2,MapSizeY/2,characterSelect,username,namelength,this);

        setVisible(true); // proceedOne()でcreateImage()を実行する前にvisibleにする。
        
        activated = true;
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void CloseWindow(){
        activated = false;
        setVisible(false);
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
    }

    public static void main(String[] args) {
        Gui sim = new Gui();
        sim.SetMainScreen(3);
    }
}