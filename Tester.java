import java.io.IOException;

public class Tester {

    private static Avatar avatar = new Avatar(0, 0, 0, "tester");
    public static void main(String[] args) throws IOException{
        ClientMain connectToServer = new ClientMain(avatar);
        connectToServer.ConnectAndStart();
    }
}
