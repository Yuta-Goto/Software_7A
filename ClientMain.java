//サーバー接続とデータの送受信をするクラス群
//MultiClientServer.javaに対応

import java.io.*;
import java.net.*;

//サーバーへの接続まで担当。
class Client_connection{
    private static int SERVER_PORT = 8080;

    private Client client;

    public Client_connection(){
        
    }

    public void ConnectAndStart(Avatar avatar) throws IOException{
        InetAddress addr = InetAddress.getByName("localhost");

        //スレッド処理の開始
        Socket socket = new Socket(addr,SERVER_PORT);
        client = new Client(socket,avatar);
        client.start();
    }

    public void CloseConnection(){
        if(client != null) client.stopRunning();
    }
}

//サーバ接続後の処理。socketを受け取ってその後の処理を行う。
class Client extends Thread{

    private Socket socket;
    private Avatar avatar;
    private Boolean running = true;
    private int x, y, d, t, effectNum,characterSelect, thread_num;
    private String userName;

    public Client(Socket socket, Avatar avatar) {
        this.socket = socket;
        this.avatar = avatar;
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

            while(running){
                //50分の1秒ごとに処理を行う。適宜値は変更する
                Thread.sleep(5);

                if (avatar == null) { // ログアウト条件
                    break;
                }
                out.println("CONTINUE"); //"ENDとの整合性を取るために、whileが続く場合はとりあえず送っとく。

                //サーバへ送信
                out.println(avatar.GetData());
                
                //サーバから受信した文字列を情報に分解する
                String str,comment;
                do {
                    str = in.readLine();//ユーザ情報兼、ループ判定
                    if (str == null || str.equals("LOOPEND")) break;

                    String[] parts = str.split(" ");
                    thread_num = Integer.parseInt(parts[0]);
                    userName = parts[1];
                    characterSelect = Integer.parseInt(parts[2]);
                    x = Integer.parseInt(parts[3]);         //X座標
                    y = Integer.parseInt(parts[4]);         //Y座標
                    d = Integer.parseInt(parts[5]);         //方向情報
                    t = Integer.parseInt(parts[6]);         //歩行時アニメーション情報
                    effectNum = Integer.parseInt(parts[7]); //effectの追加
                    comment = "";                           //ユーザーのコメント
                    for(int i=8;i<parts.length;i++){
                        comment += (parts[i] + " ");
                    }
                    Person person = new Person(userName, characterSelect, thread_num);
                    person.SetPersonState(x, y, d, t, comment,effectNum);
                    //できたPersonのインスタンスをMainScreen内のリストRoomMemberに照合
                    MainScreen.updateRoomMember(person);
                } while (true);
                
            }
            //ログアウト時の適切な送信
            out.println("END");

        } catch(IOException e){
            e.printStackTrace();
        } catch(InterruptedException e){
            e.getStackTrace();
        } finally{
            System.out.println("closing...");
            try{
                socket.close();//ソケットを閉じる
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public void stopRunning() {
        running = false;
    }
}