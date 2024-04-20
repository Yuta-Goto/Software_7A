//座標の移動とか描画とかのみを実装
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

//地図は絶対座標で実装
class Map{
    Image MapImage;
    private int Width;
    private int Height;

    Map(int width, int height, String Mapfile){
        MapImage = Toolkit.getDefaultToolkit().getImage(Mapfile);
        Width = width;
        Height = height;
    }

    public void draw(Graphics g){
        g.drawImage(MapImage, 0, 0, Width, Height, null);
    }
}

//当たり判定のある障害物
class Object{
    Image img;
    private int x, y;
    private int Width;//大きさ
    private int Height;

    Object(int xx, int yy, int width, int height, String FileName){
        x = xx;
        y = yy;
        Width = width;
        Height = height;
        img = Toolkit.getDefaultToolkit().getImage(FileName);
    }

    //座標(posiX,posiY)とオブジェクトの距離を返す    
    int GetDistance(int posiX, int posiY){
        return Math.max(Math.abs(posiX - x) - Width/2, Math.abs(posiY - y) - Height/2);
    }  

    public void draw(Graphics g){
        g.drawImage(img, x - Width/2, y - Height/2, Width, Height, null);
    }
}

//当たり判定のある壁
class Wall{
    private int x, y;
    private int Length;
    private boolean XorY;
    private int EndDistance;

    Wall(int xx, int yy, int l, boolean TF){
        x = xx;
        y = yy;
        Length = l;
        XorY = TF;
    }

    //座標(posiX,posiY)と壁の距離を返す
    int GetDistance(int posiX, int posiY){
        if(XorY){
            EndDistance = Math.min((posiX - x + Length)*(posiX - x + Length)+(posiY - y)*(posiY - y),(posiX - x - Length)*(posiX - x - Length)+(posiY - y)*(posiY - y));
            return Math.max((posiY-y)*(posiY-y),EndDistance);
        } else {
            EndDistance = Math.min((posiX - x)*(posiX - x)+(posiY - y + Length)*(posiY - y + Length),(posiX - x)*(posiX - x)+(posiY - y - Length)*(posiY - y - Length));
            return Math.max((posiX-x)*(posiX-x),EndDistance);
        }
    }
}

//------他の参加者。未実装------
class Person{
    Image img;
    private int x, y;

    Person(int xx, int yy){
        x = xx;
        y = yy;
    }

    public void draw(Graphics g){
    }
}
//---------------------------

//アバター。クライアントが操作する仮想体。
class Avatar{
    private int    x, y;         //位置座標
    private int    nextx, nexty; //入力を受けた後の当たり判定前仮位置座標
    private int    direction = 0;    //アバターの向きを表す変数
    private String UserName;     //ユーザーネーム
    private String Comment;      //ユーザーのコメント
    private static final int Size = 48;           //大きさ
    private static final int Threshold = Size/2;  //排除半径
    private static final int Stride = 3;          //アバターの歩幅
    private static final int AnimationClock = 10; //歩行アニメーションを何クロックおきに切り替えるか

    Image IconImage = Toolkit.getDefaultToolkit().getImage("./datas/Characters/Horse.png");
    
    Avatar(int xx, int yy, String Name){
        x = xx;
        y = yy;
        UserName = Name;
        if(Name == null){ //ユーザーネームはデフォルトでGuest User
            UserName = "Guest User";
        }
    }

    //マップ内にいればtrue、そうでなければfalseを返す
    boolean CheckDistanceToWorldEnd(int WorldSizeX, int WorldSizeY){
        if(nextx > Size/2 && nexty > Size/2 && nextx < WorldSizeX - Size/2 && nexty < WorldSizeY - Size/2){
            return true;
        } else {
            return false;
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

    //入力を受けて次の仮の座標を計算する
    void CalcNextCoordinate(boolean L, boolean U, boolean R, boolean D){
        nextx = x;
        nexty = y;
        if(U){
            nexty -= Stride;
            direction = 3;
        }
        if(R){
            nextx += Stride;
            direction = 2;
        }
        if(L){
            nextx -= Stride;
            direction = 1;
        }
        if(D){
            nexty += Stride;
            direction = 0;
        }
    }

    //当たり判定がないことが確認された場合に実行される。仮の座標を実座標に代入する
    void ConfirmNoCollision(){
        x = nextx;
        y = nexty;
    }

    void draw(Graphics g, int Timer){
        int t = (int)(1 - Math.sin((int)(Timer/AnimationClock)*Math.PI/2));
        g.drawRect(x-Size/2, y-Size/2, Size, Size);
        g.drawImage(IconImage, x-Size/2, y-Size/2, x+Size/2, y+Size/2, Size*t, Size*direction, Size*(t+1), Size*(direction+1),null);
        g.drawString(UserName, x-Size/2, y-Size/2);
    }
}

//ボタン周りはほとんどやっていない
abstract class CustomButton extends Button implements ActionListener{
    Simulator simulator;
    CustomButton(Simulator sim,String name){
        super(name);
        simulator = sim;
        simulator.addMyComponent(this, BorderLayout.EAST);
        addActionListener(this);
    }
    public abstract void actionPerformed(ActionEvent e);
}
    
class SendButton extends CustomButton{
    SendButton(Simulator sim){
        super(sim,"Speak");
    }
    public void actionPerformed(ActionEvent e){
        simulator.speak();
    }
}

class CustomTextField extends TextField{
    CustomTextField(Simulator sim,String str){
        super(str,10);
        sim.addMyComponent(this, BorderLayout.WEST);
    }
}

public class Simulator extends JFrame implements Runnable{

    private JPanel SimulationPanel;
    private Avatar avatar;
    private Map map;
    private int NumOfObject, NumOfWall;
    private Object[] object;
    private Wall[] wall;
    private Thread thread;
    private Image offscreen = null;
    private CustomTextField textField;
    private CustomButton button;
    private Container container = null;

    private boolean left = false;
    private boolean up = false;
    private boolean right = false;
    private boolean down = false;

    private int Timer = 0;
        
    final static private int MapSizeX = 1000; // 動画を描画する領域の横サイズ
    final static private int MapSizeY = 700;  // 動画を描画する領域の縦サイズ
    final static private int XMARGIN = 20;    // 左右の縁の余裕
    final static private int YMARGIN = 80;    // MyTextFieldやMyButton分の高さ
    final static private int YOYU    = 10;    // 下の縁の余裕


    public Simulator() {
        SimulationPanel = new JPanel();
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
    }

    //-----キーの入力を取得-----
    private void handleKeyPress(KeyEvent e) {
        switch (e.getKeyCode()) {
            case 37:
                left = true;
                break;
            case 38:
                up = true;
                break;
            case 39:
                right = true;
                break;
            case 40:
                down = true;
                break;
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

    void addMyComponent(Component x, String position){
        if (container == null) {
            container = new JPanel();
            getContentPane().add(container, BorderLayout.NORTH);
        }
        container.add(x, position);
    }

    //ボタンを押した時に実行。未実装
    void speak(){
        String str = textField.getText();
        System.out.println("You said '"+str+"'.");
        SimulationPanel.requestFocusInWindow();
    }

    private void proceedOne(){
        if(offscreen == null) {
            offscreen = this.createImage(MapSizeX,MapSizeY);
        }
            
        Graphics g = offscreen.getGraphics(); 
        //ユーザには見えないoffscreenを準備

        g.clearRect(0, 0, MapSizeX, MapSizeY); 
        //以前のコマを全部消す。ユーザには見えないのでちらちらしない

        draw(g); 
        //つぎのコマをoffscreenに描く。ユーザにはこの絵はまだ見えていない

        Graphics currentg = this.getGraphics(); 
        //表示用Graphicsを得る

        currentg.drawImage(offscreen, XMARGIN, YMARGIN, this);
        //作ったコマを一気に表示用Graphicsにコピー
    }

    //排他処理が必要なメソッド。
    synchronized
        private void ChangeMonologue(String monologue){
            //テキストメッセージ関係、未実装
        }

    synchronized
        private void MoveAvatar(){ //入力と現在の座標に応じてアバターの座標を計算する
            if(up||left||right||down){
                Timer++;
            } else {
                Timer = 0;
            }
            avatar.CalcNextCoordinate(left,up,right,down);
            if(avatar.CheckDistanceToWorldEnd(MapSizeX,MapSizeY)){
                if(avatar.CheckDistanceToWall(wall)){
                    if(avatar.CheckDistanceToObject(object)){
                        avatar.ConfirmNoCollision();
                    }
                }
            }
        }

    synchronized 
        private void draw(Graphics g){
            map.draw(g);
            for(int i = 0; i < object.length; i++){
                object[i].draw(g);
            }
            avatar.draw(g,Timer);
        }

    // 初期化
    public void SetWindow(){
        setTitle("Online Meeting");
        setBounds(0, 0, XMARGIN+MapSizeX+XMARGIN, YMARGIN+MapSizeY+YOYU);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.black);

        map = new Map(MapSizeX,MapSizeY,"./datas/SampleMap.png");
        LoadObject("./datas/Object.txt");
        LoadWall("./datas/Wall.txt");

        avatar = new Avatar(MapSizeX/2,MapSizeY/2,null);

        textField = new CustomTextField(this, "");
        button = new SendButton(this);

        setVisible(true); // proceedOne()でcreateImage()を実行する前にvisibleにする。
        
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
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
                int Xposiiton = Integer.parseInt(parts[0]);
                int Yposiiton = Integer.parseInt(parts[1]);
                int width = Integer.parseInt(parts[2]);
                int height = Integer.parseInt(parts[3]);
                String filename = parts[4];
                object[i] = new Object(Xposiiton, Yposiiton, width, height, "./datas/"+filename);
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
                int Xposiiton = Integer.parseInt(parts[0]);
                int Yposiiton = Integer.parseInt(parts[1]);
                int ObjectSize = Integer.parseInt(parts[2]);
                String XorY = parts[3];
                wall[i] = new Wall(Xposiiton, Yposiiton, ObjectSize, "X".equals(XorY));
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //並行処理
    public void run() {
        while(true) {
            try{
                Thread.sleep(10);
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
        Simulator sim = new Simulator();
        sim.SetWindow();
    }
}