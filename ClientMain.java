//ひな形最終形
//MultiClientServer.javaに対応
import java.io.*;
import java.net.*;

class LocalDataHolder{
    //接続人数 
    public static int player_num = 0;

    //Avator(自分)の変数配列
    public static String[] players_message = new String[100];
    public static int[] players_x = new int[100];
    public static int[] players_y = new int[100];
    //PersonList(他の人たちのリスト一式)
    
}


//サーバーへの接続まで担当。
class ClientMain{
    private static int SERVER_PORT = 8080;

    public ClientMain(){
        
    }

    public void ConnectAndStart() throws IOException{
        InetAddress addr = InetAddress.getByName("192.168.56.1");

        //スレッド処理の開始
        Socket socket = new Socket(addr,SERVER_PORT);
        new Client(socket).start();
    }
}



//サーバ接続後の処理。socketを受け取ってその後の処理を行う。
class Client extends Thread{

    private Socket socket;

    private static int x,y;

    public Client(Socket socket){
        this.socket = socket;
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

            //ログインしたときの1度きりのデータを送信    Ryosuke

            //ログイン時の1度きりの受信(あれば)   Ryosuke

            //臨時変数 i (ログアウト処理が完成したら消す)=======
            int i=0;
            //===============

            while(true){
                //50分の1秒ごとに処理を行う。適宜値は変更する
                Thread.sleep(1000);

                //フロントエンドから今のデータを持ってくる Yuta(Avatorの情報) & Ryosuke(ログアウト情報)
                x = i*10;
                y = i;

                //ログアウト時にはwhileを抜ける処理  Ryosuke
                //ログアウト処理ができるまで一時的に5回でwhileを抜けるようにしてる(何かしら抜ける処理がないとerror)
                i++;
                if(i==5){//本当はここにログアウト条件
                    break;
                }
                out.println("CONTINUE");//"ENDとの整合性を取るために、whileが続く場合はとりあえず送っとく。


                //サーバへ送信 Yuta
                out.println(i+"回目の送信");
                out.println(x);
                out.println(y);

                //サーバから受信 Yuta
                // String str = in.readLine();
                // x = Integer.valueOf(in.readLine());
                // y = Integer.valueOf(in.readLine());
                // str = in.readLine();
                String str;
                int p = 0;
                do{
                    LocalDataHolder.players_message[p] = in.readLine();
                    LocalDataHolder.players_x[p] = Integer.valueOf(in.readLine());
                    LocalDataHolder.players_y[p] = Integer.valueOf(in.readLine());
                    str = in.readLine();
                    p++;
                }while(str.equals("LOOPNOW"));

                //フロントエンドに、受信した全プレイヤーのデータを渡す Yuta
                for(int k=0;k<p;k++){
                    System.out.println(k + " message:" + LocalDataHolder.players_message[k] + " x:" + LocalDataHolder.players_x[k] + " y:" + LocalDataHolder.players_y[k]+ " ");
                }
            }
            //ログアウト時の適切な送信  Ryosuke
            out.println("END");

            //ログアウト時の適切な受信(あれば)  Ryosuke

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