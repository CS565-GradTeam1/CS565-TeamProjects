package appserver.satellite;

import appserver.job.Job;
import appserver.comm.ConnectivityInfo;
import appserver.job.UnknownToolException;
import appserver.comm.Message;
import static appserver.comm.MessageTypes.JOB_REQUEST;
import static appserver.comm.MessageTypes.REGISTER_SATELLITE;
import appserver.job.Tool;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.PropertyHandler;

/**
 * Class [Satellite] Instances of this class represent computing nodes that
 * execute jobs by calling the callback method of tool a implementation, loading
 * the tool's code dynamically over a network or locally from the cache, if a
 * tool got executed before.
 *
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public class Satellite extends Thread {

    private ConnectivityInfo satelliteInfo = new ConnectivityInfo();
    private ConnectivityInfo serverInfo = new ConnectivityInfo();
    private HTTPClassLoader classLoader = null;
    //private Hashtable toolsCache = null;
    private Hashtable<String, Tool> toolsCache = null;

    public Satellite(String satellitePropertiesFile, String classLoaderPropertiesFile, String serverPropertiesFile) {

        // read this satellite's properties and populate satelliteInfo object,
        // which later on will be sent to the server
        // ...
        try {
            // get satelliteInfo with properties read 
            Properties properties;
            properties = new PropertyHandler(satellitePropertiesFile);
            satelliteInfo.setName(properties.getProperty("NAME"));
            satelliteInfo.setPort(Integer.parseInt(properties.getProperty("PORT")));

        } catch (Exception e) {
            System.err.println("Properties file " + satellitePropertiesFile + " not found, exiting ...");
            System.exit(1);
        }

        // read properties of the application server and populate serverInfo object
        // other than satellites, the as doesn't have a human-readable name, so leave it out
        // ...
        try {
            // get serverInfo with properties read 
            Properties properties;
            properties = new PropertyHandler(serverPropertiesFile);
            serverInfo.setHost(properties.getProperty("HOST"));
            serverInfo.setPort(Integer.parseInt(properties.getProperty("PORT")));

        } catch (Exception e) {
            System.err.println("Properties file " + serverPropertiesFile + " not found, exiting ...");
            System.exit(1);
        }

        // read properties of the code server and create class loader
        // -------------------
        // ...
        Properties configuration = null;
        try {
            configuration = new PropertyHandler(classLoaderPropertiesFile);
        } catch (IOException e) {
            // no use carrying on, so bailing out ...
            System.err.println("No config file found, bailing out ...");
            System.exit(1);
        }

        String host = configuration.getProperty("HOST");
        String portString = configuration.getProperty("PORT");

        if ((host != null) && (portString != null)) {
            try {
                classLoader = new HTTPClassLoader(host, Integer.parseInt(portString));
            } catch (NumberFormatException nfe) {
                System.err.println("Wrong Portnumber, using Defaults");
            }
        } else {
            System.err.println("configuration data incomplete, using Defaults");
        }

        if (classLoader == null) {
            System.err.println("Could not create HTTPClassLoader, exiting ...");
            System.exit(1);
        }

        // create tools cache
        // -------------------
        // ...
        toolsCache = new Hashtable<String, Tool>();
    }

    @Override
    public void run() {
        System.out.println("SatelliteServer.appserver.satellite.run(): " + satelliteInfo.getPort());

        // register this satellite with the SatelliteManager on the server
        // ---------------------------------------------------------------
        // ...
        Socket toServer = null;
        try {
            toServer = new Socket(serverInfo.getHost(), serverInfo.getPort());
        } catch (IOException e) {
            System.err.println("Could not connect to the server " + serverInfo.getPort());
            System.exit(-1);
        }
        try {
            // register this satellite
            ObjectOutputStream write = new ObjectOutputStream(toServer.getOutputStream());
            write.writeObject(new Message(REGISTER_SATELLITE,satelliteInfo));

            // receive result from satellite
            // for simplicity, the result is not encapsulated in a message
            //ObjectInputStream read = new ObjectInputStream(toServer.getInputStream());
            //Integer result = (Integer) read.readObject();

            // write result back to client
            //writeToNet.writeObject(result);

            System.out.println("SatelliteServer.appserver.satellite.run(): " + satelliteInfo.getPort() + " Registed request sent out");            
            toServer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        // create server socket
        // ---------------------------------------------------------------
        // ...

        // Keep the server in accept mode
        // start taking job requests in a server loop
        // ---------------------------------------------------------------
        // ...
        try ( ServerSocket jobRequest = new ServerSocket(satelliteInfo.getPort())) {
            while (true) {
                new SatelliteThread(jobRequest.accept(), this).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + satelliteInfo.getPort());
            System.exit(-1);
        }

    }

    // inner helper class that is instanciated in above server loop and processes single job requests
    private class SatelliteThread extends Thread {

        Satellite satellite = null;
        Socket jobRequest = null;
        ObjectInputStream readFromNet = null;
        ObjectOutputStream writeToNet = null;
        Message message = null;

        SatelliteThread(Socket jobRequest, Satellite satellite) {
            this.jobRequest = jobRequest;
            this.satellite = satellite;
        }

        @Override
        public void run() {
            // setting up object streams
            // ...
            // Get the socket
            try {
                writeToNet = new ObjectOutputStream(jobRequest.getOutputStream());
                readFromNet = new ObjectInputStream(jobRequest.getInputStream());
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

            // Parse the request type
            switch (message.getType()) {
                case JOB_REQUEST:
                    // processing job request
                    // ...
                    try {
                    // Get the tool object
                    
                    Job job = (Job) message.getContent();
                    Tool tool = getToolObject(job.getToolName());
                    //System.out.println("SatelliteServer.appserver.satellite.SatelliteThread.run(): " + satelliteInfo.getPort() + " get the tool");    
                    
                    // Calculate and return the result
                    writeToNet.writeObject(tool.go(job.getParameters()));
                    //System.out.println("SatelliteServer.appserver.satellite.SatelliteThread.run(): " + satelliteInfo.getPort() + " returned the result");     
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                break;

                default:
                    System.err.println("[SatelliteThread.run] Warning: Message type not implemented");
            }
        }
    }

    /**
     * Aux method to get a tool object, given the fully qualified class string
     * If the tool has been used before, it is returned immediately out of the
     * cache, otherwise it is loaded dynamically
     */
    public Tool getToolObject(String toolClassString) throws UnknownToolException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        Tool toolObject = null;

        // If there the tool is not in the toolsCache
        if ((toolObject = toolsCache.get(toolClassString)) == null) {
            // Load the tool from the web server 
            Class<?> toolClass = classLoader.loadClass(toolClassString);
            try {
                toolObject = (Tool) toolClass.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                Logger.getLogger(Satellite.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("[Satellite] getToolObject() - Exception");
            }

            // Put the tool into the toolsCache
            toolsCache.put(toolClassString, toolObject);
        } // Otherwise
        // Showing the tool is already in the toolsCache
        else {
            System.out.println("Tool: \"" + toolClassString + "\" already in Cache");
        }

        // Return toolObject
        return toolObject;
    }

    public static void main(String[] args) {
        // start the satellite
        Satellite satellite = new Satellite(args[0], args[1], args[2]);
        satellite.run();
    }
}
