/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appserver.client;

import appserver.comm.Message;
import static appserver.comm.MessageTypes.JOB_REQUEST;
import appserver.job.Job;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Properties;
import utils.PropertyHandler;

/**
 *
 * @author G
 */
public class FibonacciClient extends Thread{
        
    String host = null;
    int port;
    int num;

    Properties properties;

    public FibonacciClient(String serverPropertiesFile, int i) {
        try {
            properties = new PropertyHandler(serverPropertiesFile);
            host = properties.getProperty("HOST");
            System.out.println("[FibonacciClient] Host: " + host);
            port = Integer.parseInt(properties.getProperty("PORT"));
            System.out.println("[FibonacciClient] Port: " + port);
            
            this.num = i;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void run() {
        try { 
            // connect to application server
            System.out.println("[FibonacciClient] Connect to application server: " + port);
            Socket server = new Socket(host, port);
            
            // hard-coded string of class, aka tool name ... plus one argument
            String classString = "appserver.job.impl.Fibonacci";
            //Integer number = new Integer(42);
            
            // create job and job request message
            Job job = new Job(classString, this.num);
            Message message = new Message(JOB_REQUEST, job);
            
            // sending job out to the application server in a message
            ObjectOutputStream writeToNet = new ObjectOutputStream(server.getOutputStream());
            writeToNet.writeObject(message);
            
            // reading result back in from application server
            // for simplicity, the result is not encapsulated in a message
            ObjectInputStream readFromNet = new ObjectInputStream(server.getInputStream());
            Long result = (Long) readFromNet.readObject();
            System.out.println("Fibonacci of " + this.num + ": "+ result+ "\n");
        } catch (Exception ex) {
            System.err.println("[FibonacciClient.run] Error occurred");
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {

        if(args.length == 1) {
            for(int i=1; i<48; i++){
                (new FibonacciClient(args[0], i)).start();
            }
        } else {
            for(int i=1; i<48; i++){
                (new FibonacciClient("../../config/Server.properties", i)).start();
            }
        }
    }  
}
