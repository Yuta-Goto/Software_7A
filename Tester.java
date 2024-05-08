import java.io.IOException;

public class Tester {
    public static void main(String[] args) throws IOException{
        ClientMain connectToServer = new ClientMain();
        connectToServer.ConnectAndStart();
    }
}
