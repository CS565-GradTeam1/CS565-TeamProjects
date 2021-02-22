
import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws IOException {
        
        if (args.length != 2) {
            System.err.println(
                "Usage: java Client <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try (
            Socket echoSocket = new Socket(hostName, portNumber);
            DataOutputStream out = new DataOutputStream(echoSocket.getOutputStream());
            DataInputStream in = new DataInputStream(echoSocket.getInputStream());
        ) {
            
            BufferedReader stdIn =
                new BufferedReader(new InputStreamReader(System.in));
            Byte fromServer;
            String fromUser;
            int state=0;

            // keep running until the state machine reaches 4
            while(state!=4){
                // Read from the user and send the message to the server
                fromUser = stdIn.readLine();
                if (fromUser != null) {
                    out.writeInt(fromUser.length());
                    out.writeBytes(fromUser);
                    System.out.println("Client: " + fromUser);
                }

                // Receive the character from the server
                Thread.sleep(50);
                while (in.available()!=0) {
                    fromServer = in.readByte();
                    // Get the information from the server to terminates
                    if((char)(int)fromServer == '!'){
                        state=4;
                        break;
                    }

                    // Print out the character from the server
                    System.out.println("Server: " + (char)(int)fromServer);
                }
                //System.out.println("waiting for input!");
            }

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        } catch (InterruptedException e) {
            System.err.println("InterruptedException to " +
                hostName);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
