//ConnectToServer.javaに対応
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class ServerDataHolder{ //データを送受信する全スレッドがアクセスできるように各スレッドが受け取ったデータを保持する
    public static List<String> player_list = Collections.synchronizedList(new ArrayList<String>());
    //public static List<String> comment_list = Collections.synchronizedList(new ArrayList<String>());
}

public class MultiClientServer{
    private static int SERVER_PORT = 8080;
    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        System.out.println("サーバ起動:serverSocket is " + serverSocket);

        try{
            while(true){//新しいクライアントが来るたびにループが1周する
                Socket socket = serverSocket.accept();//新しいクライアントが来るまでここで待機
                new ClientDealer(socket).start();//来たら、新しく並列処理開始。
                System.out.println("start");
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

    //整数を入力しリストの要素の、先頭の数字が入力値と一致しているものをリストから削除する
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

            //2桁以上のスレッド番号を表示するプログラム
            String threadName = Thread.currentThread().getName();
            int i= threadName.length()-1;
            int thread_num = 0;
            int unit = 1;//10倍ずつされる。
            while(0<=(threadName.charAt(i)-'0') && (threadName.charAt(i)-'0')<=9){
                int digit = (threadName.charAt(threadName.length() - 1) - '0');
                thread_num += digit*unit;
                unit*=10;
                i--;
            }
            

            System.out.println(thread_num);
            
            while (true) {
                
                Thread.sleep(10);

                String str_login_check = in.readLine();
                //何も送られてこない、あるいは"END"が送られてきた場合、ループを終了
                if (str_login_check == null || str_login_check.equals("END")) break;

                //受信したデータを他のスレッドが送信するためのデータに加工(先頭にスレッド番号を追加)
                //前回データホルダーに格納したデータを削除し(どのスレッドが格納したかはデータの先頭の数字で識別)
                //加工したデータを新たにデータホルダーに加える。
                String message = thread_num + " " + in.readLine();
                //String comment = in.readLine();//追加
                removeMatchingElements(ServerDataHolder.player_list, thread_num);
                synchronized (ServerDataHolder.player_list) {
                    ServerDataHolder.player_list.add(message);
                    
                }
                //synchronized(ServerDataHolder.comment_list){ ServerDataHolder.comment_list.add(comment);}
                //自スレッドが受信したデータか判別し、他スレッドで受信されたデータのみをクライアントに送信する。
                synchronized (ServerDataHolder.player_list) {
                    
                    for (String str : ServerDataHolder.player_list) {
                        
                        int match_num = -1;
                        String[] parts = str.split(" ", 2);
                        if (parts.length > 0) {
                            try {
                                match_num = Integer.parseInt(parts[0]);
                            } catch (NumberFormatException e) {
                                //データが規格通りでないときの処理
                                throw new IllegalArgumentException("The first part of the string is not a number: " + str);
                            }
                        } else {
                            //データが規格通りでないときの処理
                            throw new IllegalArgumentException("String does not include space: " + str);
                        }
                        if (match_num != thread_num) {
                            out.println(str);
                            //synchronized(ServerDataHolder.comment_list){out.println(ServerDataHolder.comment_list.get(match_num));}
                        }
                    }
                }
                out.println("LOOPEND");//1つめに対して

                //Yuta追記
                
            }
            //ログアウト時、自スレッドの情報をデータホルダーから削除する
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