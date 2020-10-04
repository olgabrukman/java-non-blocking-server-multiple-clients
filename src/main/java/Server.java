import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.io.IOException;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Observer {
    private Socket socket;
    public static Map<String, PrintWriter> clientsPrintWriters = new HashMap<String, PrintWriter>();

    private List<ClientThread> clients;
    private ServerSocket ssocket;  //Server Socket
    private StartServerThread sst; //inner class

    private static final Logger log= Logger.getLogger( Server.class.getName() );
    private ClientThread clientThread;

    /** Port number of Server. */
    private int port = 5555;
    private boolean listening; //status for listening

    public Server() {
        this.clients = new ArrayList<ClientThread>();

        this.listening = false;
    }

    public void startServer() {
        if (!listening) {
            this.sst = new StartServerThread();
            this.sst.start();
            this.listening = true;
        }
    }

    public void stopServer() {
        if (this.listening) {
            this.sst.stopServerThread();
            log.log( Level.FINE, "close all connected clients");
            for(ClientThread ct: clients)
            {

                ct.stopClient();
            }
            this.listening = false;
        }
    }

    //observer interface//
    public void update(Observable observable, Object object) {
        //notified by observables, do cleanup here//
        this.clients.remove(observable);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }



    /** This inner class will keep listening to incoming connections,
     *  and initiating a ClientThread object for each connection. */

    private class StartServerThread extends Thread {
        private boolean listen;

        public StartServerThread() {
            this.listen = false;
        }

        public void run() {
            this.listen = true;
            try {
                Server.this.ssocket =
                        new ServerSocket(Server.this.port);


                while (this.listen) {
                    //wait for client to connect//

                    Server.this.socket = Server.this.ssocket.accept();
                    log.log( Level.FINE, "Client connected");
                    System.out.println("Client connected");
                    try {
                        Server.this.clientThread =
                                new ClientThread(Server.this.socket);
                        Thread t =
                                new Thread(Server.this.clientThread);
                        Server.this.clientThread.addObserver(Server.this);
                        Server.this.clients.add(Server.this.clientThread);
                        t.start();
                    } catch (IOException ioe) {
                        clientsPrintWriters.remove(clientThread.getUserName());
                        clientThread.stopClient();
                    }
                }
            } catch (IOException ioe) {
                log.log( Level.FINE, "I/O error in ServerSocket");
                this.stopServerThread();
            }
        }

        public void stopServerThread() {
            try {
                Server.this.ssocket.close();
            }
            catch (IOException ioe) {
                log.log( Level.FINE, "Unable to close server socket");
                System.exit(1);
            }

            this.listen = false;
        }
    }

    public static void main(String[] argv)throws IOException {
        Server s = new Server();
        s.startServer();
    }
}
