//ConnectToServer.javaに対応
import java.io.*;
import java.net.*;
//テストブランチテスト

//import javax.xml.crypto.Data;

class ServerDataHolder{
    //プレイヤーの変数を保存しておく配列
    public static String[] players_name = new String[100];
    public static int[] players_chara = new int[100];
    public static int[] players_uniqueVal = new int[100];
    public static int[] players_x = new int[100];
    public static int[] players_y = new int[100];
    public static int[] players_direction = new int[100];
    public static int[] players_anim = new int[100];
    public static String[] players_message = new String[100];

    //サーバ内で使用するもの
    public static boolean[] players_here = new boolean[100];//true:プレイヤーは接続中, false:プレイヤーは接続してない
    
    public static int player_num = 0;
}

public class MultiClientServer{
    private static int SERVER_PORT = 8080;
    //public static int[] players_x = new int[100];
    //public static int player_num = 0;
 
    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        System.out.println("サーバ起動：serverSocket is "+serverSocket);

        try{
            while(true){//新しいクライアントが来るたびにループが1周する
                Socket socket = serverSocket.accept();//新しいクライアントが来るまでここで待機
                new ClientDealer(socket).start();//来たら、新しく並列処理開始。
            }
        } finally{
            serverSocket.close();//サーバのソケットを閉じる
        }
    }
}

class ClientDealer extends Thread{
    private Socket socket;

    private int prev_playernum = 0;
    public ClientDealer(Socket socket){
        this.socket = socket;
    }

    private int my_max(int a,int b){
        if(a>b) return a;
        return b;
    }

    public void run(){
        try{
            //送受信設定
            BufferedReader in = 
                    new BufferedReader(
                        new InputStreamReader(
                            socket.getInputStream()));//データ受信用バッファの設定
            PrintWriter out = 
                    new PrintWriter(
                        new BufferedWriter(
                            new OutputStreamWriter(
                                socket.getOutputStream())),true);//送信バッファの設定    
            String threadName = Thread.currentThread().getName();
            int thread_num = (threadName.charAt(threadName.length()-1)-'0');

            ServerDataHolder.player_num = my_max(ServerDataHolder.player_num, thread_num+1);




            //ログインしたときの1度きりのデータを受信    Ryosuke
            ServerDataHolder.players_name[thread_num] = in.readLine();
            ServerDataHolder.players_chara[thread_num] = Integer.valueOf(in.readLine());
            ServerDataHolder.players_x[thread_num] = Integer.valueOf(in.readLine());
            ServerDataHolder.players_y[thread_num] = Integer.valueOf(in.readLine());

            //ログイン時の1度きりの送信(あれば)   Ryosuke

            ServerDataHolder.players_here[thread_num] = true;//このプレイヤーの接続開始
            boolean first_loop = true;//1止めのループだけtrue
            while(true){
                //1秒ごとに送信
                Thread.sleep(1000);

                //(スレッド番号の確認) 
                //System.out.println(thread_num);

                /* 
                //ログアウトしてるかどうかの文字列受信 Ryosuke
                String str_login_check = in.readLine();
                if(str_login_check.equals("LOGOUT")) {
                    //ログアウトしたら、ログアウトしたよマークを付ける。
                    
                    break;
                }
                */
                boolean server_login_check = Boolean.valueOf(in.readLine());
                if(!server_login_check){
                    break;
                }

                

                //Clientから受信 Yuta
                ServerDataHolder.players_message[thread_num] = in.readLine();
                ServerDataHolder.players_x[thread_num] = Integer.valueOf(in.readLine());
                ServerDataHolder.players_y[thread_num] = Integer.valueOf(in.readLine());
                ServerDataHolder.players_direction[thread_num] = Integer.valueOf(in.readLine());
                ServerDataHolder.players_anim[thread_num] = Integer.valueOf(in.readLine());

                //Clientへ送信 Yuta
                for(int i=0;i<ServerDataHolder.player_num;i++){
                    if(ServerDataHolder.players_here[i]) System.out.println(i);
                    //if(i!= ServerDataHolder.player_num-1){
                        if(i==thread_num){//自分自身なら送信しない
                            out.println("LOOPNOW_ITSME");//ループ中ではあるのでcontinueする前に送っとく。 SKIPが合図
                            out.println(first_loop);
                            if(first_loop){
                                out.println(ServerDataHolder.players_name[i]);
                                out.println(ServerDataHolder.players_chara[i]);
                                out.println(i);
                                prev_playernum++;
                            }
                            continue;
                        }else if(!ServerDataHolder.players_here[i]){//既に抜けた人なら送信しない。
                            out.println("LOOPNOW_SKIP");
                            continue;
                        }else{
                            out.println("LOOPNOW");// 最後以外はこの一文を送っておく(ループ中だよの合図)
                        }
                    //}
                    //初回のみ
                    //
                    if(prev_playernum < ServerDataHolder.player_num && i==prev_playernum){//新しい人が入ってきたら、その人の時に新しいインスタンス生成用のデータを送る。
                        out.println(true);//trueが送られたら向こうでも受け取り準備する
                        out.println(ServerDataHolder.players_name[i]);
                        out.println(ServerDataHolder.players_chara[i]);
                        out.println(i);
                        prev_playernum++;
                    }else{
                        out.println(false);
                    }
                    //System.out.println(message + "クライアント" + threadName + "　座標： (" + x + "," + y+ ")");

                    out.println(ServerDataHolder.players_message[i]);
                    out.println(ServerDataHolder.players_x[i]);
                    out.println(ServerDataHolder.players_y[i]);
                    out.println(ServerDataHolder.players_direction[i]);
                    out.println(ServerDataHolder.players_anim[i]);
                }
                out.println("LOOPEND");

                first_loop = false;//2度目のループに入るのでfalse
                
            }
            //ログアウト時の適切な送信(あれば) Ryosuke

            ServerDataHolder.players_here[thread_num] = false;//このプレイヤーの接続終わり
        } catch(IOException e){
            e.printStackTrace();
        } catch(InterruptedException e){
            e.printStackTrace();
        } finally {//(例外の発生有無に寄らず実行される)
                System.out.println("closing...");
                try{
                    socket.close();//抜けたクライアントに関するソケットを閉じる
                }catch(IOException e){
                    e.printStackTrace();
                }
               
        }
    }

    private void GetData(){
        
    }
}