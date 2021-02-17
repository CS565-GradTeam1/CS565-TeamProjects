
import java.net.*;
import java.util.Properties;
import java.io.*;

public class EchoThread extends Thread {
    // Variables
    private Socket socket = null;

    String DownStreamIP;
    int DownStreamPort;
    String message;
    String PropertiesPath;
    Properties prop;

    public EchoThread(Socket socket, String PropertiesPath) {
        super("EchoThread");
        this.socket = socket;

        // Get values from the properties file
        try(InputStream input = new FileInputStream(PropertiesPath)){
            prop = new Properties();

            prop.load(input);

            DownStreamIP=prop.getProperty("DownStreamIP");
            DownStreamPort=Integer.parseInt(prop.getProperty("DownStreamPort"));
            this.PropertiesPath = PropertiesPath;
        }catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    public void run() {

        // Communication via socket
        try (
            DataOutputStream toClient = new DataOutputStream(socket.getOutputStream());
            //DataInputStream fromClient = new DataInputStream(socket.getInputStream());
            BufferedReader fromC = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            
            // the server print out a control message whenever a client connected
            //System.out.println("Here is a new client connected!");

            // Get the request title
            String requestTitle = fromC.readLine();
            //System.out.println("[Receiver]:" + requestTitle);

            // Receive a join request
            if(requestTitle.equals("Join")){
                // Reply DownStreamIP and DownStreamPort to the joining node
                toClient.writeBytes(DownStreamIP + "\n" + DownStreamPort);

                // Got the new DownStreamIP and DownStreamPort
                DownStreamIP = fromC.readLine();
                DownStreamPort = Integer.parseInt(fromC.readLine());

                // Update data in file     
                setProper("DownStreamIP", DownStreamIP);
                setProper("DownStreamPort", Integer.toString(DownStreamPort));
                //System.out.println("[Sender Updated]DownStream IP: " + DownStreamIP +" DownStream Port: " + DownStreamPort);
            }
            
            // Receive a note request
            else if(requestTitle.equals("Note")){
                // Get the IP and portNum in the request
                String fromIP = fromC.readLine();
                String fromPort = fromC.readLine();
                message = fromC.readLine();

                // Pass the note request to the downstream node if necessary
                if(!(fromIP.equals(prop.getProperty("MyIP")) 
                    && fromPort.equals(prop.getProperty("MyPort")))){

                        // Print out the message
                        System.out.println(message);
                        // Pass the note request
                        try (
                            Socket echoSocket = new Socket(prop.getProperty("DownStreamIP"), Integer.parseInt(prop.getProperty("DownStreamPort")));
                            DataOutputStream out = new DataOutputStream(echoSocket.getOutputStream());
                            //DataInputStream in = new DataInputStream(echoSocket.getInputStream());
                            BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()))
                         ) {
                            out.writeBytes("Note" + "\n" + fromIP + "\n" + fromPort + "\n" + message + "\n");
                            //System.out.println("Message passed!");
                            echoSocket.close();

                         } catch (UnknownHostException e) {
                            System.err.println("Don't know about " + DownStreamPort);
                            System.exit(1);
                         } catch (IOException e) {
                            System.err.println("Couldn't get I/O for the connection to " +
                            DownStreamPort);
                            System.exit(1);
                         } 
                         
                }

            }

            // Receive a leave request
            else if(requestTitle.equals("Leave")){
                //System.out.println("[Receiver]: Got leaving request");

                // Get values in the request
                String fromIP = fromC.readLine();
                String fromPort = fromC.readLine();
                
                String nodeDownIP = fromC.readLine();
                String nodeDownPort = fromC.readLine();

                // Changing the downstream node into the leaving node's downstream node
                // If the leaving node is the current downstream node
                if(fromIP.equals(prop.getProperty("DownStreamIP")) &&
                    fromPort.equals(prop.getProperty("DownStreamPort"))){
                    
                    // Updates the downstream node info
                    setProper("DownStreamIP", nodeDownIP);
                    setProper("DownStreamPort", nodeDownPort);

                    //System.out.println("[Sender Updated]DownStream IP: " 
                    //+ prop.getProperty("DownStreamIP") +" DownStream Port: " + prop.getProperty("DownStreamPort"));
                
                }
                // Pass the leaving request to the downstream node
                // Otherwise
                else{
                    try (
                            Socket echoSocket = new Socket(prop.getProperty("DownStreamIP"), Integer.parseInt(prop.getProperty("DownStreamPort")));
                            DataOutputStream out = new DataOutputStream(echoSocket.getOutputStream());
                            //DataInputStream in = new DataInputStream(echoSocket.getInputStream());
                            BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()))
                         ) {

                            // Pass the leaving request
                            out.writeBytes("Leave" + "\n" + fromIP + "\n" + fromPort + "\n" + nodeDownIP + "\n" + nodeDownPort + "\n");
                            //out.writeBytes("Leave"+"\n"+fromIP+"\n"+fromPort+"\n");
                            System.out.println("Leave request passed!");
                            echoSocket.close();

                    } catch (UnknownHostException e) {
                            System.err.println("Don't know about " + DownStreamPort);
                            System.exit(1);
                    } catch (IOException e) {
                            System.err.println("Couldn't get I/O for the connection to " +
                            DownStreamPort);
                            System.exit(1);
                    } 
                }
            
            // Print out error if the request title is unvalid
            }else{
                System.out.println("[Receiver]: Wrong requestTitle :" +requestTitle);
            }

            // Close the socket
            //socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Update the value in the properties file
    public void setProper(String key,String value){
        try {
           prop.setProperty(key, value);
           FileOutputStream out2file = new FileOutputStream(PropertiesPath);
           prop.store(out2file, null);
           out2file.close();
        } catch (FileNotFoundException e) {
           e.printStackTrace();
        } catch (IOException e) {
           e.printStackTrace();
        }
     }
}
