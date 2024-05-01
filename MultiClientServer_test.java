import java.io.*;
import java.net.*;
//テストブランチテスト

public class MultiClientServer_test{
    private static int SERVER_PORT = 8080;

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
            //実際の送受信
            while(true){
                String str = in.readLine();
                out.println(str);
                
                if(str.equals("END")) break;
                int x = in.read();
                out.println(x);
                
                int y = in.read();
                out.println(y);
                
                System.out.println(str + "クライアント" + threadName + "　座標： (" + x + "," + y+ ")");
                out.println(str + " from SERVER!" + "　座標： (" + x + "," + y+ ")");
            }
        
        } catch(IOException e){
            e.printStackTrace();
        }finally {//(例外の発生有無に寄らず実行される)
                System.out.println("closing...");
                try{
                    socket.close();//抜けたクライアントに関するソケットを閉じる
                }catch(IOException e){
                    e.printStackTrace();
                }
               
        }
    }
}