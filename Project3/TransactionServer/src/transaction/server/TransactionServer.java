/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transaction.server;

import java.util.Properties;

import static transaction.client.TransactionClient.log;
import transaction.server.account.AccountManager;
import utils.PropertyHandler;
import java.net.*;
import java.io.*;
import transaction.server.transaction.TransactionManager;

/**
 *
 * @author Jiawei Gao, Yawen Peng
 */
public class TransactionServer extends Thread{
    
    // Declare static variables
    public static String host;
    public static int port;
    public static int numberAccounts;
    public static int initialBalance;
    public static boolean transactionView;
    public static int numberTransactions;
    public static AccountManager accountManager;
           
    public TransactionServer(String clientPropertiesFile, String serverPropertiesFile){
        try{
            // Read from the properties files
            Properties serverProperties = new PropertyHandler(serverPropertiesFile);
            host = serverProperties.getProperty("HOST");
            port = Integer.parseInt(serverProperties.getProperty("PORT"));
            numberAccounts = Integer.parseInt(serverProperties.getProperty("NUMBER_ACCOUNTS"));
            initialBalance = Integer.parseInt(serverProperties.getProperty("INITIAL_BALANCE"));
            transactionView = Boolean.parseBoolean(serverProperties.getProperty("TRANSACTION_VIEW"));
            
            Properties clientProperties = new PropertyHandler(clientPropertiesFile);
            numberTransactions = Integer.parseInt(clientProperties.getProperty("NUMBER_TRANSACTION"));     
            
            // Create accountManager
            accountManager = new AccountManager(numberAccounts,initialBalance);
            
        }catch (Exception ex){
            ex.printStackTrace();
        }
        
        log = new StringBuffer("");
    }
    
    @Override
    public void run()
    {
        System.out.println("transaction.server.TransactionServer.main(): "+port);
        
        // Keep the server in accept mode
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                new TransactionManager(serverSocket.accept()).start();
	    }
	} catch (IOException e) {
            System.err.println("Could not listen on port " + port);
            System.exit(-1);
        }
    }
    
    public static void main(String[] args) {
        // Get the root path of the project file
        String currentPath = System.getProperty("user.dir");
        
        // Start the Transaction Server
        (new TransactionServer(currentPath+"\\config\\TransactionClient.properties",currentPath+"\\config\\TransactionServer.properties")).start();
    
    }

        
}
