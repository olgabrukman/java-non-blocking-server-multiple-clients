import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class ClientTest {

    private static final String TEST_HOSTNAME = Client.hostName;
    private static final int TEST_PORT = Client.port;
    Client client1 = null;
    Client client2 = null;


    @Test
    public void testServerResponse() {
        ServerSocket testServerSocket = null;

        try {
            testServerSocket = new ServerSocket(TEST_PORT);
            testServerSocket.setSoTimeout(100);
            client1 = new Client();
            client2 = new Client();
            Socket testSocket = testServerSocket.accept();
            testSocket = testServerSocket.accept();

            client1.getPw().println("Login A");
            String msg = client1.getBr().readLine();
            assertEquals("Hello A. You are logged in.", msg);

            client1.getPw().println("GetAllLoggedInClients");
             msg = client1.getBr().readLine();
            assertEquals("[A]", msg);

            client2.getPw().println("Login B");
            msg = client2.getBr().readLine();
            assertEquals("Hello B. You are logged in.", msg);

            client1.getPw().println("GetAllLoggedInClients");
            assertEquals("[A, B]", client1.getBr().readLine());

            client2.getPw().println("SendMessageToClient A|Hello, my name is B");
            assertEquals("A sent you a message: Hello, my name is B”", client1.getBr().readLine());

            client2.getPw().println("BroadcastNessageToAllClients  B is broadcasting");
            assertEquals( "B is broadcasting", client1.getBr().readLine());

            client1.getPw().println("Logout");
            assertEquals("Goodbye A. You are logged out.", client1.getBr().readLine());
            assertEquals("A has logged out.", client2.getBr().readLine());
        } catch (Exception e) {
        }

        try {
            testServerSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        client1.disconnect();
    }


}
