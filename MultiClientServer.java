//ConnectToServer.javaに対応
import java.io.*;
import java.net.*;
//テストブランチテスト

//import javax.xml.crypto.Data;

class ServerDataHolder{
    public static int[] players_x = new int[100];
    public static int[] players_y = new int[100];
    public static String[] players_message = new String[100];
    
    public static int player_num = 0;
}

public class MultiClientServer{
    private static int SERVER_PORT = 8080;
    public static int[] players_x = new int[100];
    public static int player_num = 0;
 
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

            //ログイン時の1度きりの送信(あれば)   Ryosuke

            while(true){
                //1秒ごとに送信
                Thread.sleep(1000);

                //(スレッド番号の確認) 
                System.out.println(thread_num);

                //ログアウトしてるかどうかの文字列受信 Ryosuke
                String str_login_check = in.readLine();
                if(str_login_check.equals("END")) break;


                //Clientから受信 Yuta
                String message = in.readLine();
                ServerDataHolder.players_message[thread_num] = message;
                int x = Integer.valueOf(in.readLine());
                ServerDataHolder.players_x[thread_num] = x;

                int y = Integer.valueOf(in.readLine());
                ServerDataHolder.players_y[thread_num] = y;

                //Clientへ送信 Yuta
                for(int i=0;i<ServerDataHolder.player_num;i++){
                    System.out.println(message + "クライアント" + threadName + "　座標： (" + x + "," + y+ ")");
                    //out.println(str + " from SERVER!" + "　座標： (" + ServerDataHolder.players_x[i] + "," + y+ ")");
                    out.println(ServerDataHolder.players_message[i]);
                    out.println(ServerDataHolder.players_x[i]);
                    out.println(ServerDataHolder.players_y[i]);
                    if(i!= ServerDataHolder.player_num-1) out.println("LOOPNOW");// 最後以外はこの一文を送っておく(ループ中だよの合図)
                }
                out.println("LOOPEND");
            }
            //ログアウト時の適切な送信(あれば) Ryosuke
        
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
}