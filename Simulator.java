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
    int MapSize;

    Map(int size, String Mapfile){
        MapImage = Toolkit.getDefaultToolkit().getImage(Mapfile);
        MapSize = size;
    }

    public void draw(Graphics g){
        g.drawImage(MapImage, 0, 0, MapSize, MapSize, null);
    }
}

//当たり判定のある障害物
class Object{
    Image img;
    private int x, y;
    private int Size;//大きさ

    Object(int xx, int yy, int ObjectSize, String FileName){
        x = xx;
        y = yy;
        Size = ObjectSize;
        img = Toolkit.getDefaultToolkit().getImage(FileName);
    }

    //座標(posiX,posiY)とオブジェクトの距離を返す    
    int GetDistance(int posiX, int posiY){
        return (posiX - x)*(posiX - x)+(posiY - y)*(posiY - y);
    }  

    public void draw(Graphics g){
        g.drawImage(img, x-Size/2, y-Size/2, Size, Size, null);
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
    private int    x, y;        //位置座標
    private int    nextx, nexty;//入力を受けた後の当たり判定前仮位置座標
    private String UserName;    //ユーザーネーム
    private String Comment;     //ユーザーのコメント
    private static final int    SIZE      = 50;//大きさ
    private static final double THRESHOLD = SIZE*SIZE/4;//排除半径
    private static final int Stride = 5;

    Image IconImage = Toolkit.getDefaultToolkit().getImage("./datas/SampleIcon.png");
    
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
        if(nextx > SIZE/2 && nexty > SIZE/2 && nextx < WorldSizeX - SIZE/2 && nexty < WorldSizeY - SIZE/2){
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
            if(ob[i].GetDistance(nextx,nexty) < THRESHOLD){
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
            if(wall[i].GetDistance(nextx,nexty) < THRESHOLD){
                return false;
            }
        }
        return true;
    }

    //入力を受けて次の仮の座標を計算する
    void CalcNextCoordinate(boolean L, boolean U, boolean R, boolean D){
        nextx = x;
        nexty = y;
        if(L){
            nextx -= Stride;
        }
        if(U){
            nexty -= Stride;
        }
        if(R){
            nextx += Stride;
        }
        if(D){
            nexty += Stride;
        }
    }

    //当たり判定がないことが確認された場合に実行される。仮の座標を実座標に代入する
    void ConfirmNoCollision(){
        x = nextx;
        y = nexty;
    }

    void draw(Graphics g){
        g.drawImage(IconImage, x-SIZE/2, y-SIZE/2, SIZE, SIZE, null);
        g.drawString(UserName, x-SIZE/2, y-SIZE/2);
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

public class Simulator extends JFrame implements Runnable, KeyListener{

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

        
    final static private int SIZE    = 700;  // 動画を描画する領域の縦横サイズ
    final static private int XMARGIN = 20;   // 左右の縁の余裕
    final static private int YMARGIN = 80;   // MyTextFieldやMyButton分の高さ
    final static private int YOYU    = 10;   // 下の縁の余裕

    public Simulator() {
        this.addKeyListener(this);         // キーボードリスナーとして自分自身を登録
        this.setFocusable(true); // フォーカスを取得できるようにする
    }

    //-----キーの入力を取得-----
    @Override
    public void keyPressed(KeyEvent e) {
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

    @Override
    public void keyReleased(KeyEvent e) {
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

    @Override
    public void keyTyped(KeyEvent e) {
        // keyTyped メソッドは実装していない
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
    }

    private void proceedOne(){
        if(offscreen == null) {
            offscreen = this.createImage(SIZE,SIZE);
        }
            
        Graphics g = offscreen.getGraphics(); 
        //ユーザには見えないoffscreenを準備

        g.clearRect(0, 0, SIZE, SIZE); 
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
        private void ChangeCoordinate(){
            avatar.CalcNextCoordinate(left,up,right,down);
            if(avatar.CheckDistanceToWorldEnd(SIZE, SIZE)){
                if(avatar.CheckDistanceToObject(object)){
                    if(avatar.CheckDistanceToWall(wall)){
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
            avatar.draw(g);
        }

    // 初期化
    public void SetWindow(){
        setTitle("Online Meeting");
        setBounds(0, 0, XMARGIN+SIZE+XMARGIN, YMARGIN+SIZE+YOYU);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.black);

        map = new Map(SIZE,"./datas/SampleMap.png");
        LoadObject("./datas/Object.txt");
        LoadWall("./datas/Wall.txt");

        avatar = new Avatar(SIZE/2,SIZE/2,null);

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
                int ObjectSize = Integer.parseInt(parts[2]);
                String filename = parts[3];
                object[i] = new Object(Xposiiton, Yposiiton, ObjectSize, "./datas/"+filename);
            }
            br.close();
            System.out.println("ObjectData Loaded!");
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
            ChangeCoordinate();
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