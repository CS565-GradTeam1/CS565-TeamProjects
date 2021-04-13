package appserver.server;

import appserver.comm.Message;
import static appserver.comm.MessageTypes.JOB_REQUEST;
import static appserver.comm.MessageTypes.REGISTER_SATELLITE;
import appserver.comm.ConnectivityInfo;
import appserver.job.Job;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import utils.PropertyHandler;
import web.SimpleWebServer;

/**
 *
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public class Server {

    // Singleton objects - there is only one of them. For simplicity, this is not enforced though ...
    static SatelliteManager satelliteManager = null;
    static LoadManager loadManager = null;
    static ServerSocket serverSocket = null;
    String host;
    int port;

    public Server(String serverPropertiesFile) {

        // create satellite manager and load manager
        // ...
        satelliteManager = new SatelliteManager();
        loadManager = new LoadManager();
        
        // read server properties and create server socket
        // ...
        try {
            Properties properties;
            properties = new PropertyHandler(serverPropertiesFile);
            host = properties.getProperty("HOST");
            port = Integer.parseInt(properties.getProperty("PORT"));
            serverSocket = new ServerSocket(port);
            
        } catch (Exception e) {
            System.err.println("Properties file " + serverPropertiesFile + " not found, exiting ...");
            System.exit(1);
        }
    }

    public void run() {
    // serve clients in server loop ...
    // when a request comes in, a ServerThread object is spawned
    // ...
        System.out.println("SatelliteServer.appserver.Server.run(): " + port);
        try{
            while (true) {
                (new ServerThread(serverSocket.accept())).start();
            }
        } catch (Exception e) {
            System.err.println("server loop error");
            System.exit(1);
        }
            
    }

    // objects of this helper class communicate with satellites or clients
    private class ServerThread extends Thread {

        Socket client = null;
        ObjectInputStream readFromNet = null;
        ObjectOutputStream writeToNet = null;
        Message message = null;

        private ServerThread(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            // set up object streams and read message
            // ...
        
            try {
                writeToNet = new ObjectOutputStream(client.getOutputStream());
                readFromNet = new ObjectInputStream(client.getInputStream());
            } catch (IOException ex) {
                System.out.println("[TransactionManagerWorker.run] Error occurred when opening a socket");
                ex.printStackTrace();
            }
        
            // reading message
            // ...
            try {
                message = (Message) readFromNet.readObject();
            } catch (IOException | ClassNotFoundException | NullPointerException ex) {
                System.out.println("[TransactionManagerWorker.run] Error when reading message type");
                ex.printStackTrace();
            }

            
            // process message
            switch (message.getType()) {
                case REGISTER_SATELLITE:
                    System.err.println("\n[ServerThread.run] Received register satellite request");
                    // read satellite info
                    // ...
                    ConnectivityInfo con = (ConnectivityInfo)message.getContent();
                    
                    // register satellite
                    synchronized (Server.satelliteManager) {
                        // ...
                        Server.satelliteManager.registerSatellite(con);
                    }

                    // add satellite to loadManager
                    synchronized (Server.loadManager) {
                        // ...
                        Server.loadManager.satelliteAdded(con.getName());
                    }

                    System.out.println("SatelliteServer.appserver.Server.ServerThread.run() Client: "+ con.getName() + "[" + con.getPort() + "] has registed");
                    
                    break;

                case JOB_REQUEST:
                    System.err.println("\n[ServerThread.run] Received job request");

                    String satelliteName = null;
                    ConnectivityInfo satInfo = null;
                    
                    Job job = (Job)message.getContent();
                        
                    synchronized (Server.loadManager) {
                        // get next satellite from load manager
                        // ...
                        try{
                            satelliteName = Server.loadManager.nextSatellite();
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }
                        
                        
                        // get connectivity info for next satellite from satellite manager
                        // ...
                        satInfo = Server.satelliteManager.getSatelliteForName(satelliteName);
                    }
                    
                    //System.out.println("SatelliteServer.appserver.Server.ServerThread.run(): Satellite "+ satelliteName + " to handle the request");
                    
                    Socket satellite = null;
                    // connect to satellite
                    // ...
                    try {
                        
                        satellite = new Socket(satInfo.getHost(),satInfo.getPort());

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    // open object streams,
                    // forward message (as is) to satellite,
                    // receive result from satellite and
                    // write result back to client
                    // ...
                    try {
                        // forward message (as is) to satellite,
                        ObjectOutputStream write = new ObjectOutputStream(satellite.getOutputStream());
                        write.writeObject(message);

                        // receive result from satellite
                        // for simplicity, the result is not encapsulated in a message
                        ObjectInputStream read = new ObjectInputStream(satellite.getInputStream());
                        Long result = (Long) read.readObject();
                        
                        // write result back to client
                        writeToNet.writeObject(result);

                    
                        //System.out.println("SatelliteServer.appserver.Server.ServerThread.run(): Result has been sent to client. Result is " + result);
                    }catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    break;

                default:
                    System.err.println("[ServerThread.run] Warning: Message type not implemented");
            }
        }
    }

    // main()
    public static void main(String[] args) {
        // start the application server
        Server server = null;
        if(args.length == 1) {
            server = new Server(args[0]);
        } else {
            server = new Server("../../config/Server.properties");
        }
        server.run();
    }
}
