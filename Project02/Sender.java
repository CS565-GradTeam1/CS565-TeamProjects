
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;


public class Sender implements Runnable {
      // Variables
      private Thread t;

      String UserName;
      String KnownIP;
      int KnownPort;
      String MyIP;
      int MyPort;
      String DownStreamIP;
      int DownStreamPort;

      String PropertiesPath;
      Properties prop;
      String Message;


      Sender( String PropertiesPath) {

         // Get values from the properties file
         try(InputStream input = new FileInputStream(PropertiesPath)){
            prop = new Properties();
            prop.load(input);

            KnownIP = prop.getProperty("KnownIP");
            KnownPort= Integer.parseInt(prop.getProperty("KnownPort"));
            DownStreamIP=prop.getProperty("DownStreamIP");
            DownStreamPort=Integer.parseInt(prop.getProperty("DownStreamPort"));
            UserName=prop.getProperty("UserName");
            MyIP=prop.getProperty("MyIP");
            MyPort=Integer.parseInt(prop.getProperty("MyPort"));
            this.PropertiesPath = PropertiesPath;
        }catch (IOException e) {
            e.printStackTrace();
        }
         //System.out.println("Creating sender\n" );
      }
        
      public void run() {
         //System.out.println("Running sender" );

         // Join the chat
         Join();

         // Send out messages
         // It will call leave() if the user enter "Leave"
         note();

         //System.out.println("Thread sender exiting.");
      }
        
      private void note() {
         // Keep reading from the user 
         while(true){
            // Get the latest values from the peoperties file
            try(InputStream input = new FileInputStream(PropertiesPath)){
               BufferedReader stdIn =
                  new BufferedReader(new InputStreamReader(System.in));
               Message = stdIn.readLine();

               prop.load(input);
               DownStreamIP=prop.getProperty("DownStreamIP");
               DownStreamPort=Integer.parseInt(prop.getProperty("DownStreamPort"));
               MyIP=prop.getProperty("MyIP");
               MyPort=Integer.parseInt(prop.getProperty("MyPort"));
            }catch (IOException e) {
               e.printStackTrace();
            }
            
            // Break the loop when the user want leave
            if(Message.equals("leave") || Message.equals("Leave")){
               break;
            }

            //System.out.println("[Sender]: D: " + DownStreamIP + " " + DownStreamPort);
            //System.out.println("[Sender]: M: " + MyIP+ " " + MyPort);

            // Send out message 
            // If get message from the user and the downstream node is not itself
            if(!(MyIP.equals(DownStreamIP) && (MyPort==DownStreamPort)) && Message!=null){

               //System.out.println("[Sender]: Connecting to "+ DownStreamPort);
               
               // Create the socket connection and send out the note request
               try (
               Socket echoSocket = new Socket(prop.getProperty("DownStreamIP"), Integer.parseInt(prop.getProperty("DownStreamPort")));
               DataOutputStream out = new DataOutputStream(echoSocket.getOutputStream());
               //DataInputStream in = new DataInputStream(echoSocket.getInputStream());
               BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()))
               ) {   
                  if(Message != null){

                     out.writeBytes("Note\n" +
                       MyIP + "\n" +
                       MyPort + "\n" + 
                        UserName +":" + Message + "\n");
                     System.out.println(UserName +": " + Message);
                  } 
                  
                  // Close the socket connection
                  //System.out.println("Message passed!");
                  echoSocket.close();

               }catch (UnknownHostException e) {
                  System.err.println("Don't know about " + DownStreamPort);
                  System.exit(1);
               } catch (IOException e) {
                  System.err.println("Couldn't get I/O for the connection to " +
                  DownStreamPort);
                  System.exit(1);
               } 

            }
         }

         // Call leave() because the user want leave the chat
         leave();
      }

      private void leave() {
         // Send out the leave requst 
         try (
               Socket echoSocket = new Socket(prop.getProperty("DownStreamIP"), Integer.parseInt(prop.getProperty("DownStreamPort")));
               DataOutputStream out = new DataOutputStream(echoSocket.getOutputStream());
               //DataInputStream in = new DataInputStream(echoSocket.getInputStream());
               BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()))
               ) {
               
               out.writeBytes("Leave\n" + MyIP + "\n" + MyPort +"\n");
               out.writeBytes(DownStreamIP +"\n" + DownStreamPort+"\n");
               
               System.out.println("Leave Request Sent out!");
   
               // Close connection
               echoSocket.close();
               
               // Back to the default value for DownStreamIP and DownStreamPort
               setProper("DownStreamIP", "localhost");
               setProper("DownStreamPort", "8001");

               System.exit(1);
            } catch (UnknownHostException e) {
               System.err.println("Don't know about host " + KnownIP);
               System.exit(1);
            } catch (IOException e) {
               System.err.println("Couldn't get I/O for the connection to " +
               KnownIP);
               System.exit(1);
            }
      }

      private void Join() {
         // if the node is the first node: do not need join
         if(MyIP.equals(KnownIP) && MyPort==KnownPort){
            System.out.println("[Sender]: " + "I am the first node!");
         }
         
         // otherwise: connect to the known node in the chat
         else{

            // Send out the join request
            try (
               Socket echoSocket = new Socket(KnownIP, KnownPort);
               DataOutputStream out = new DataOutputStream(echoSocket.getOutputStream());
               //DataInputStream in = new DataInputStream(echoSocket.getInputStream());
               BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()))
            ) {
               
               out.writeBytes("Join\n" + MyIP + "\n" + MyPort + "\n");
               System.out.println("[Sender]: Join Request Sent out!");
   
               // Receive the character from the server
               Thread.sleep(50);
               DownStreamIP = in.readLine();
               DownStreamPort = Integer.parseInt(in.readLine());

               // Update DownStreamIP and DownStreamPort in file     
               setProper("DownStreamIP", DownStreamIP);
               setProper("DownStreamPort", Integer.toString(DownStreamPort));
               //System.out.println("[Sender Updated]DownStream IP: " + DownStreamIP +" DownStream Port: " + DownStreamPort);
               
               // Close connection
               echoSocket.close();
            } catch (UnknownHostException e) {
               System.err.println("Don't know about host " + KnownIP);
               System.exit(1);
            } catch (IOException e) {
               System.err.println("Couldn't get I/O for the connection to " +
               KnownIP);
               System.exit(1);
            } catch (InterruptedException e) {
               System.err.println("InterruptedException to " +
               KnownIP);
               e.printStackTrace();
               System.exit(1);
            }
         }
      }

      // Create and start the thread
      public void start() {
           //System.out.println("Starting sender" );
           if (t == null) {
              t = new Thread (this,  "sender");
              t.start ();
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
      