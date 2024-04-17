//未完成なので悪しからず。
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class Map{
    Image MapImage = Toolkit.getDefaultToolkit().getImage("Map.png");

    public void DrawMap(Graphics g){
        
    }
}

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

    int GetDistance(int posiX, int posiY){
        return (posiX - x)*(posiX - x)+(posiY - y)*(posiY - y);
    }  

    public void draw(Graphics g){
        g.drawImage(img, x-Size/2, y-Size/2, Size, Size, null);
    }
}

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

//-------------未実装-------------
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
//------------------------------

class Haijin{

    private int    x, y;     //位置座標
    private static final int    SIZE      = 200;//大きさ
    private static final double THRESHOLD = 40000;//排除半径
    private int stride;
    
    Haijin(int xx, int yy){
        x = xx;
        y = yy;
    }

    void CheckDistanceToObject(Object[] ob){
        for(int i = 0; i < ob.length; i++){
            if(ob[i].GetDistance(x,y) < THRESHOLD){
                stride = -Math.abs(stride);
                return;
            }
        }
        stride = Math.abs(stride);
    }

    void CheckDistanceToWall(Wall[] wall){
        for(int i = 0; i < wall.length; i++){
            if(wall[i].GetDistance(x,y) < THRESHOLD){
                stride = -Math.abs(stride);
                return;
            }
        }
        stride = Math.abs(stride);
    }

    void Walk(boolean L, boolean U, boolean R, boolean D){
        if(L){
            x-= stride;
        }
        if(U){
            y-= stride;
        }
        if(R){
            x+= stride;
        }
        if(D){
            y+= stride;
        }
    }

    void draw(Graphics g){
        g.fillOval(x,y,SIZE,SIZE);
    }
}


abstract class MyButton extends Button implements ActionListener{
    Simulator2 simulator2;
    MyButton(Simulator2 sim,String name){
        super(name);
        simulator2 = sim;
        simulator2.addMyComponent(this, BorderLayout.EAST);
        addActionListener(this);
    }
    public abstract void actionPerformed(ActionEvent e);
}
    
class GoButton extends MyButton{
    GoButton(Simulator2 sim){
        super(sim,"Go");
    }
    public void actionPerformed(ActionEvent e){
        simulator2.go();
    }
}


class MyTextField extends TextField{
    MyTextField(Simulator2 sim,String str){
        super(str,10);
        sim.addMyComponent(this, BorderLayout.WEST);
    }
}



public class Simulator2 extends JFrame implements Runnable{

    private Haijin haijin;
    private Object[] object;
    private Wall[] wall;
    private FireFly[] fireflies;
    private int NumFireFlies = 500;
    private Thread thread;
    private Image offscreen = null;
    private MyTextField textField;
    private MyButton button;
    private Container container = null;

    private boolean left = false;
    private boolean up = false;
    private boolean right = false;
    private boolean down = false;

        
    final static private int SIZE    = 500;  // 動画を描画する領域の縦横サイズ
    final static private int XOFFSET = 20;   // 左右の縁の余裕
    final static private int YOFFSET = 80;   // MyTextFieldやMyButton分の高さ
    final static private int YOYU    = 10;   // 下の縁の余裕

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

    void addMyComponent(Component x, String position){
        if (container == null) {
            container = new JPanel();
            getContentPane().add(container, BorderLayout.NORTH);
        }
        container.add(x, position);
    }
    
    void go(){
        int num  = Integer.parseInt(textField.getText());
        System.out.println("change num="+num);
        //initFireFlies(num);
        System.out.println(" done.");
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

        currentg.drawImage(offscreen, XOFFSET, YOFFSET, this);
        //作ったコマを一気に表示用Graphicsにコピー
    }

    //排他処理が必要なメソッド。
    synchronized
        private void ChangeCoordinate(){
            haijin.CheckDistanceToObject(object);
            haijin.CheckDistanceToWall(wall);
            haijin.Walk(left,up,right,down);
        }

    synchronized 
        private void draw(Graphics g){
            for(int i = 0; i < NumFireFlies; i++){
                object[i].draw(g);
            }
            haijin.draw(g);
        }

    //////////// 初期化
    public void init(){
        setTitle("Test");
        setBounds(0, 0, XOFFSET+SIZE+XOFFSET, YOFFSET+SIZE+YOYU);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.black);

        textField = new MyTextField(this, ""+NumFireFlies); //this:インスタンス自身を参照
        button = new GoButton(this);   
        //initFireFlies(NumFireFlies);

        setVisible(true); // proceedOne()でcreateImage()を実行する前にvisibleにする必要あり
        
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    //////////// 並行処理
    public void run() {
        while(true) {
            try{
                Thread.sleep(10);
            } 
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            ChangeCoordinate();
            //俳人の次の状態を計算
            proceedOne();
            //つぎのコマを描く
        }
    }

    public static void main(String[] args) {
        Simulator2 sim = new Simulator2();
        sim.init();
    }
}