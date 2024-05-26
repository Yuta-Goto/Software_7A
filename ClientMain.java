//サーバー接続とデータの送受信をするクラス群
//MultiClientServer.javaに対応

import java.io.*;
import java.net.*;

class LocalDataHolder{
    
}

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
    private String userName, comment;

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

            //ログインしたときの1度きりのデータを送信    Ryosuke

            //ログイン時の1度きりの受信(あれば)   Ryosuke

            //臨時変数 i (ログアウト処理が完成したら消す)=======
            //===============

            while(running){
                //50分の1秒ごとに処理を行う。適宜値は変更する
                Thread.sleep(5);

                //フロントエンドから今のデータを持ってくる Yuta(Avatorの情報) & Ryosuke(ログアウト情報)

                //ログアウト時にはwhileを抜ける処理  Ryosuke
                //ログアウト処理ができるまで一時的に5回でwhileを抜けるようにしてる(何かしら抜ける処理がないとerror)
                if (avatar == null) { // 本当はここにログアウト条件
                    break;
                }
                out.println("CONTINUE"); //"ENDとの整合性を取るために、whileが続く場合はとりあえず送っとく。

                //サーバへ送信 Yuta
                out.println(avatar.GetData());
                //out.println(avatar.getComment());//追加
                
                //サーバから受信 Yuta
                String str,comment;
                do {
                    str = in.readLine();//ユーザ情報兼、ループ判定
                    if (str == null || str.equals("LOOPEND")) break;
                    /* 
                    String s;
                    if((s = in.readLine()) != null){
                        comment = s;
                    }else{
                        comment = s;
                    }
                    */

                    String[] parts = str.split(" ");
                    thread_num = Integer.parseInt(parts[0]);
                    userName = parts[1];
                    characterSelect = Integer.parseInt(parts[2]);
                    x = Integer.parseInt(parts[3]);
                    y = Integer.parseInt(parts[4]);
                    d = Integer.parseInt(parts[5]);
                    t = Integer.parseInt(parts[6]);
                    effectNum = Integer.parseInt(parts[7]);//effectの追加
                    /* 
                    if(parts.length > 7){
                        comment = parts[7];
                    } else {
                        comment = "";
                    }
                    */
                    comment = "";
                    for(int i=8;i<parts.length;i++){
                        comment += (parts[i] + " ");
                    }
                    Person person = new Person(userName, characterSelect, thread_num);
                    person.SetPersonState(x, y, d, t, comment,effectNum);
                    MainScreen.updateRoomMember(person);
                } while (true);

                //フロントエンドに、受信した全プレイヤーのデータを渡す Yuta
                
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

    public void stopRunning() {
        running = false;
    }
}