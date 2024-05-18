//ConnectToServer.javaに対応
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
//テストブランチテスト

//import javax.xml.crypto.Data;

class ServerDataHolder{
    public static List<String> player_list = Collections.synchronizedList(new ArrayList<String>());
    
    public static int player_num = 0;
}

public class MultiClientServer{
    private static int SERVER_PORT = 8080;
    public static int[] players_x = new int[100];
    public static int player_num = 0;
    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        System.out.println("サーバ起動:serverSocket is " + serverSocket);

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

    public static void removeMatchingElements(List<String> list, int number) {
        synchronized (list) {
            Iterator<String> iterator = list.iterator();
            while (iterator.hasNext()) {
                String element = iterator.next();
                String[] parts = element.split(" ", 2);
                if (parts.length > 0) {
                    try {
                        int elementNumber = Integer.parseInt(parts[0]);
                        if (elementNumber == number) {
                            iterator.remove();
                        }
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            }
        }
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            String threadName = Thread.currentThread().getName();
            int thread_num = (threadName.charAt(threadName.length() - 1) - '0');

            System.out.println(thread_num);
            while (true) {
                Thread.sleep(10);

                String str_login_check = in.readLine();
                if (str_login_check == null || str_login_check.equals("END")) break;

                String message = thread_num + " " + in.readLine();
                removeMatchingElements(ServerDataHolder.player_list, thread_num);
                synchronized (ServerDataHolder.player_list) {
                    ServerDataHolder.player_list.add(message);
                }

                synchronized (ServerDataHolder.player_list) {
                    for (String str : ServerDataHolder.player_list) {
                        int match_num = -1;
                        String[] parts = str.split(" ", 2);
                        if (parts.length > 0) {
                            try {
                                match_num = Integer.parseInt(parts[0]);
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("先頭の部分が数字ではありません: " + str);
                            }
                        } else {
                            throw new IllegalArgumentException("文字列にスペースが含まれていません: " + str);
                        }
                        if (match_num != thread_num) {
                            out.println(str);
                        }
                    }
                }
                out.println("LOOPEND");
            }
            removeMatchingElements(ServerDataHolder.player_list, thread_num);
            System.out.println("Thread " + thread_num + "closing...");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("closing...");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}