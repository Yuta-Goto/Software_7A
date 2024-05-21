//ひな形最終形
//MultiClientServer.javaに対応
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

class LocalDataHolder{
    //接続人数 
    public static int player_num = 0;

    
    public static List<Person> persons = new ArrayList<Person>();
    public static Avatar clientAvatar = new Avatar(0, 0, 0, "a");

    public static boolean[] players_here = new boolean[100];//true:プレイヤーは接続中, false:プレイヤーは接続してない

    public static boolean login_check = true;
    public static boolean spawned_from_client = false;

    public static int my_thread_num = 0;
}


//サーバーへの接続まで担当。
class ClientMain{
    
    private static int SERVER_PORT = 8080;
    private Avatar avatar;

    public ClientMain(Avatar avatar){
        this.avatar = avatar;
    }

    public void ConnectAndStart() throws IOException{
        InetAddress addr = InetAddress.getByName("192.168.56.1");
        
        //スレッド処理の開始
        Socket socket = new Socket(addr,SERVER_PORT);
        new Client(socket,avatar).start();
    }
}



//サーバ接続後の処理。socketを受け取ってその後の処理を行う。
class Client extends Thread{

    private Socket socket;//ソケット
    private Avatar avatar;//自分のAvatar情報

    private static int x,y;
    private static int direction,anim;
    String comment;

    

    //private static int known_max_p=-1;//クライアント側で検知している最大接続人数 始めは必ず0(1)人なので、-1からスタートして初期化してもらう

    

    public Client(Socket socket,Avatar avatar){
        this.socket = socket;
        this.avatar = avatar;
        
    }

    private void initializePlayersHere(){
        for(int i=0;i<100;i++){
            LocalDataHolder.players_here[i] = true;
        }
    }
    
    public void run(){
        try{
            //ソケット通信の前処理
            System.out.println("socket = " + socket);
            BufferedReader in =
                new BufferedReader(
                    new InputStreamReader(
                        socket.getInputStream()));//データ受信用バッファの設定
            PrintWriter out =
                new PrintWriter(
                    new BufferedWriter(
                        new OutputStreamWriter(
                            socket.getOutputStream())),true);//送信バッファ設定

            initializePlayersHere();

            //ログインしたときの1度きりのデータを送信    Ryosuke
            out.println(avatar.getName());
            out.println(avatar.getChara());
            out.println(avatar.getX());
            out.println(avatar.getY());

            //ログイン時の1度きりの受信(あれば)   Ryosuke

            //臨時変数 i (ログアウト処理が完成したら消す)=======
            int i=0;
            //===============

            Scanner scanner = new Scanner(System.in);

            while(true){
                //50分の1秒ごとに処理を行う。適宜値は変更する
                Thread.sleep(1000);

                /* 
                //System.out.print("メッセージを入力してください（ログアウトする場合は'LOGOUT'と入力）: ");
                String message = "a";//scanner.nextLine();
                //ログアウト時にはwhileを抜ける処理  Ryosuke
                
                if(message.equals("LOGOUT")){
                    out.println("LOGOUT");
                    break;
                }
                out.println("CONTINUE");//"ENDとの整合性を取るために、whileが続く場合はとりあえず送っとく。
                */
                out.println(LocalDataHolder.login_check);
                if(!LocalDataHolder.login_check){
                    break;
                }

                //フロントエンドから今のデータを持ってくる Yuta(Avatarの情報) & Ryosuke(ログアウト情報)
                x = LocalDataHolder.clientAvatar.getX();
                y = LocalDataHolder.clientAvatar.getY();
                direction = LocalDataHolder.clientAvatar.getDirection();
                anim = LocalDataHolder.clientAvatar.getAnim();
                comment = LocalDataHolder.clientAvatar.getComment();

                //サーバへ送信 Yuta
                out.println(comment);
                out.println(x);
                out.println(y);
                out.println(direction);
                out.println(anim);

                //サーバから受信 Yuta
                String str_loop_check;
                int p = 0;
                
                while(true){
                    
                    //LocalDataHolder.players_here[p] = true;
                    str_loop_check = in.readLine();
                    if(str_loop_check.equals("LOOPEND")) break;//サーバでループ抜けてるならこちらも抜ける。

                    if(LocalDataHolder.players_here[p]) System.out.println(p);

                    if(str_loop_check.equals("LOOPNOW_ITSME") || str_loop_check.equals("LOOPNOW_SKIP")) {//自分か、接続の切れた人の場合更新しない勢になる。
                        if(str_loop_check.equals("LOOPNOW_SKIP")) LocalDataHolder.players_here[p] = false;//スキップしてるという事はそこにいないという事。
                        if(str_loop_check.equals("LOOPNOW_ITSME")){
                            boolean first_loop = Boolean.valueOf(in.readLine());
                            if(first_loop){//新しいメンバー（自分）が接続された時1度きり//フロントで既にあるのでは、、？
                                String name = in.readLine();
                                int chara = Integer.valueOf(in.readLine());
                                int unique = Integer.valueOf(in.readLine());
                                LocalDataHolder.persons.add(new Person(name, chara, unique));
                                System.out.println("add me " + p + " " + unique);
                                LocalDataHolder.my_thread_num = unique;
                                LocalDataHolder.spawned_from_client = true;
                                //known_max_p = p;
                            }
                        }
                        //if(str_loop_check.equals("LOOPNOW_SKIP"))
                        
                        p++;
                        continue;//自分もしくは既に抜けた人ならcontinue;
                    }
                    



                    boolean first_loop = Boolean.valueOf(in.readLine());
                    if(first_loop){//新しいメンバーが接続された時1度きり
                        String name = in.readLine();
                        int chara = Integer.valueOf(in.readLine());
                        int unique = Integer.valueOf(in.readLine());
                        LocalDataHolder.persons.add(new Person(name, chara, unique));
                        System.out.println("add other " + p + " " + unique);
                        //known_max_p = p;
                    }
                    //自分のデータはサーバから受け取らない。それはサーバが何かしら教えてくれるのでその合図でスキップ。
                    //ログアウトしたメンバーのデータはサーバから受け取らない。ログアウトしたthread_numの時はサーバが合図を出してくれるのでその合図でスキップ。(ログアウトに際するインスタンスの削除は現時点ではない。)

                    String messager = in.readLine();
                    int player_x = Integer.valueOf(in.readLine());
                    int player_y = Integer.valueOf(in.readLine());
                    int player_direction = Integer.valueOf(in.readLine());
                    int player_anim = Integer.valueOf(in.readLine());
                    LocalDataHolder.persons.get(p).SetPersonState(player_x,player_y,player_direction,player_anim,messager);//SKIP案件じゃなければ変更を反映
                    //MainScreenへ、こちらで作った仮インスタンスをそのまま渡す。(ServerSideからClientSideへインスタンスごと渡す)
                    //このプログラムはMainScreen自体で実行
                    //MainScreen.updateRoomMember(LocalDataHolder.persons.get(p));

                    p++;
                }
                //ログ表示
                //for(int k=0;k<p;k++){
                    //System.out.println(k + " message:" + LocalDataHolder.persons.get(p).Comment + " x:" + LocalDataHolder.players_x[k] + " y:" + LocalDataHolder.players_y[k]+ " ");
                //}
            }
            
            //ログアウト時の適切な送信  Ryosuke
            //out.println("END");ブールで代用できる。

            //ログアウト時の適切な受信(あれば)  Ryosuke
            /* 
            System.out.println("ログアウトしました。再ログインしますか？（Y/N）");
            String answer = scanner.nextLine();
            if(answer.equalsIgnoreCase("Y")){
                new ClientMain(avatar).ConnectAndStart();
            }else{
                System.out.println("プログラムを終了します。");
            }
            */

        } catch(IOException e){
            e.printStackTrace();
        } catch(InterruptedException e){
            //例外の時
            e.getStackTrace();
        } finally{
            System.out.println("closing...");
            try{
                socket.close();//抜けたクライアントに関するソケットを閉じる
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    

}