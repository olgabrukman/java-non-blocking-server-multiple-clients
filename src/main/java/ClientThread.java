import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Observable;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientThread extends Observable implements Runnable {
    private final String LOGIN ="Login";
    private final String LOGOUT ="Logout";
    private final String SENDMESSAGETOCLIENT="SendMessageToClient";

    private final String BROADCASTNESSAGETOALLCLIENTS="BroadcastNessageToAllClients";
    private final String GETALLLOGGEDINCLIENTS="GetAllLoggedInClients";
    private final String GETDBDATA="GetDBData";

    private String userName;
    private boolean loggedin, loggedout;

    /** For reading input from socket */
    private BufferedReader br;

    /** For writing output to socket. */
    private PrintWriter pw;

    /** Socket object representing client connection */
    private Socket socket;
    private boolean running;

    private static final Logger log= Logger.getLogger( ClientThread.class.getName() );

    public ClientThread(Socket socket) throws IOException {
        this.socket = socket;


        running = false;
        log.log( Level.FINE, "get I/O from socket");
        try {
            br = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()
                    )
            );

            pw = new PrintWriter(socket.getOutputStream(), true);
            running = true;
            log.log( Level.FINE, "set status to true");
        }
        catch (IOException ioe) {
            throw ioe;
        }
    }

    /**
     *Stops clients connection
     */

    public void stopClient()
    {
        try {
            this.socket.close();
            log.log( Level.FINE, "stopped server side client thread");
        }catch(IOException ioe){ };
    }

    public void run() {
        String msg = "";

        log.log( Level.FINE, "started server thread for client");
        log.log( Level.FINE, "started start listening for message from client");

        try {
            while ((msg = br.readLine()) != null && running && !loggedout) {
                handle(msg);
            }
            running = false;
        }
        catch (IOException ioe) {
            running = false;
        }
        //it's time to close the socket
        try {
            this.socket.close();
            System.out.println("Closing connection");
            log.log( Level.FINE, "Closing connection");
        } catch (IOException ioe) { }

        //notify the observers for cleanup etc.
        this.setChanged();              //inherit from Observable
        this.notifyObservers(this);     //inherit from Observable
    }

    private void handle(String msg) throws FileNotFoundException {
        log.log( Level.FINE, "handling message {0}", msg);
        if (msg.startsWith(LOGIN)) {
            if (!loggedin) {
                userName = msg.split(" ")[1];
                pw.println(String.format("Hello %s. You are logged in.", userName));
                log.log( Level.FINE, String.format("User %s logged in.", userName));
                loggedin = true;
                synchronized (Server.clientsPrintWriters) {
                    Server.clientsPrintWriters.put(userName, pw);
                }
            }
            return;
        }
        if (msg.startsWith(LOGOUT)) {
            if (loggedin) {
                pw.println(String.format("Goodbye %s. You are logged out", userName));
                log.log( Level.FINE, String.format("User %s logged out.", userName));
                loggedin = false;
                loggedout = true;
                synchronized (Server.clientsPrintWriters) {
                    for (String name : Server.clientsPrintWriters.keySet()) {
                        if (!name.equals(userName)) {
                            System.out.println(String.format("%s: %s logged out", userName, name));
                            Server.clientsPrintWriters.get(name).println(String.format("%s has logged out", userName));
                            Server.clientsPrintWriters.get(name).flush();
                        }
                    }
                }
                Server.clientsPrintWriters.remove(userName);
            }
            return;
        }

        if (msg.startsWith(SENDMESSAGETOCLIENT)) {
            String to = msg.substring(SENDMESSAGETOCLIENT.length()+1, msg.indexOf("|"));
            String toMsg = msg.substring(msg.indexOf("|")+1);
            PrintWriter toPrintWriter;

            synchronized (Server.clientsPrintWriters) {
                toPrintWriter = Server.clientsPrintWriters.get(to);
                System.out.println(String.format("Client: %s sent you a message: %s", userName, toMsg));
                if (toPrintWriter != null) {
                    toPrintWriter.println(String.format("%s sent you a message: %s", userName, toMsg));
                    log.log( Level.FINE, String.format("User %s sends message [%s] to user %s.", userName, toMsg, to));
                    toPrintWriter.flush();
                }
            }
            return;
        }

        if (msg.startsWith(BROADCASTNESSAGETOALLCLIENTS)) {
            String broadcastMsg = msg.substring(BROADCASTNESSAGETOALLCLIENTS.length()+1);
            synchronized (Server.clientsPrintWriters) {
                for (String name : Server.clientsPrintWriters.keySet()) {
                    if (!name.equals(userName)) {
                        Server.clientsPrintWriters.get(name).println(String.format(broadcastMsg));
                        Server.clientsPrintWriters.get(name).flush();
                        log.log( Level.FINE, String.format("User %s broadcasts message [%s].", userName,msg));
                    }
                }
            }
            return;
        }
        if (msg.startsWith(GETALLLOGGEDINCLIENTS)) {
            Set<String> allUsers;
            synchronized (Server.clientsPrintWriters) {
                allUsers = Server.clientsPrintWriters.keySet();
            }
            pw.println(Arrays.toString(allUsers.toArray()));
            pw.flush();
            return;
        }
        if (msg.startsWith(GETDBDATA))
        {
            String format = msg.substring(GETDBDATA.length()+1);
            String data = getAndFormatData(format);
            pw.println(data);
            pw.flush();
            return;
        }
    }

    private String getAndFormatData(String format) throws FileNotFoundException {
        return new Scanner(new File("./src/main/resources/DB_data.json")).useDelimiter("\\Z").next();
    }

    public String getUserName() {
        return userName;
    }
}