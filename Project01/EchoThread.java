
import java.net.*;
import java.io.*;

public class EchoThread extends Thread {
    private Socket socket = null;

    public EchoThread(Socket socket) {
        super("EchoThread");
        this.socket = socket;
    }
    
    public void run() {

        try (
            DataOutputStream toClient = new DataOutputStream(socket.getOutputStream());
            DataInputStream fromClient = new DataInputStream(socket.getInputStream());
        ) {
            int len; 
            char charFromClient;
            int state = 0;

            // the server print out a control message whenever a client connected
            System.out.println("Here is a new client connected!");

            // Keep thread running until the state is 4 (which means the quit condition is met)
            while(state!=4){
                // Read the length of the string from client
                len = fromClient.readInt();

                for(int i=0; i<len; i++){
                    // Update charFromClient
                    charFromClient = (char)fromClient.readByte();
                    System.out.println(charFromClient);

                    // Send it back to client if the charFromClient is a letter
                    if(Character.isLetter(charFromClient)){
                        toClient.writeByte(charFromClient);
                    }

                    // State machine
                    if(state==3 && charFromClient=='t'){
                        state=4;
                    }else if(state==2 && charFromClient=='i'){
                        state = 3;
                    }else if(state==1 && charFromClient=='u'){
                        state = 2;
                    }else if(state==0 && charFromClient=='q'){
                        state = 1;
                    }else{
                        if(Character.isLetter(charFromClient)){
                            state = 0;
                        }
                    }

                }
            }
            // Telling the client to end the socket
            toClient.writeByte('!');

            // Close the socket
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
