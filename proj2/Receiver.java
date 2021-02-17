import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.Properties;

public class Receiver implements Runnable {
    private Thread t;
    
    String UserName;
    String KnownIP;
    int KnownPort;
    String MyIP;
    int MyPort;
    String DownStreamIP;
    int DownStreamPort;

    String Message;  
    String PropertiesPath;  
    

    Receiver( String PropertiesPath) {

      // Got values from the properties file
      try(InputStream input = new FileInputStream(PropertiesPath)){
         Properties prop = new Properties();

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
   }


   public void run() {
      //System.out.println("[Receiver]: Running " +  UserName );
      //System.out.println("[Receiver]: MyPort:" + MyPort +"  KnownPort:" + KnownPort);
      
      // Keep listening if there is connection
      try (ServerSocket serverSocket = new ServerSocket(MyPort)) { 
         while (true) {
             //new EchoThread(serverSocket.accept()).start();
             new EchoThread(serverSocket.accept(), PropertiesPath).start();
         }
      } catch (IOException e) {
            System.err.println("Could not listen on port " + MyPort);
            System.exit(-1);
      }

      //System.out.println("[Receiver]: " +  UserName + " exiting.");
    }
    
    // Create and start the thread
    public void start () {
       //System.out.println("[Receiver]: Starting " +  UserName );
       if (t == null) {
          t = new Thread (this, UserName);
          t.start ();
       }
    }
}
  