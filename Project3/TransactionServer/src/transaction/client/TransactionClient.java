/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transaction.client;

import java.util.Properties;
import static transaction.comm.MessageTypes.TRANSACTION_COMMITTED;
import static transaction.comm.MessageTypes.TRANSACTION_ABORTED;
import utils.PropertyHandler;

/**
 *
 * @author Jiawei Gao, Yawen Peng
 */
public class TransactionClient extends Thread {

    public static int numberTransactions;
    public static int numberAccounts;
    public static int initialBalance;

    public static String host;
    public static int port;

    public static StringBuffer log;

    public TransactionClient(String clientPropertiesFile, String serverPropertiesFile) {
        // Read from the properties files
        try {
            Properties serverProperties = new PropertyHandler(serverPropertiesFile);
            host = serverProperties.getProperty("HOST");
            port = Integer.parseInt(serverProperties.getProperty("PORT"));
            numberAccounts = Integer.parseInt(serverProperties.getProperty("NUMBER_ACCOUNTS"));
            initialBalance = Integer.parseInt(serverProperties.getProperty("INITIAL_BALANCE"));

            Properties clientProperties = new PropertyHandler(clientPropertiesFile);
            numberTransactions = Integer.parseInt(clientProperties.getProperty("NUMBER_TRANSACTION"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        log = new StringBuffer("");
    }

    @Override
    public void run() {
        int transactionCounter;

        for (transactionCounter = 0; transactionCounter < numberTransactions; transactionCounter++) {
            new Thread() {

                @Override
                public void run() {

                    int transactionID;
                    int priorTransactionID = 0;

                    int accountFrom;
                    int accountTo;

                    int amount;
                    int balance;

                    int returnStatus;

                    accountFrom = (int) Math.floor(Math.random() * numberAccounts);
                    accountTo = (int) Math.floor(Math.random() * numberAccounts);
                    amount = (int) Math.ceil(Math.random() * initialBalance);

                    try {
                        Thread.sleep((int) Math.floor(Math.random() * 1000));
                    } catch (InterruptedException ex) {
                        //Logger.getLogger(TransactionClient.class.getName()).log(level.SEVERE, null, ex);
                    }

                    do {
                        //open transaction
                        TransactionServerProxy transaction = new TransactionServerProxy(host, port);
                        transactionID = transaction.openTransaction();

                        if (priorTransactionID == 0) {
                            System.out.println("Transaction # " + transactionID + " started, transfer $" + amount + ": " + accountFrom + "->" + accountTo);
                        } else {
                            System.out.println("Prior transaction #" + priorTransactionID + " restart as transactions #" + transactionID + ", transfer $" + amount + ": " + accountFrom + "->" + accountTo);
                        }

                        //transfer money from accountFrom to accountTo
                        balance = transaction.read(accountFrom);
                        transaction.write(accountFrom, balance - amount);
                        balance = transaction.read(accountTo);
                        transaction.write(accountTo, balance + amount);

                        //close transaction
                        returnStatus = transaction.closeTransaction();

                        switch (returnStatus) {
                            case TRANSACTION_COMMITTED:
                                System.out.println("Transaction #" + transactionID + " COMMITTED");

                                break;
                            case TRANSACTION_ABORTED:
                                System.out.println("\tTransaction #" + transactionID + " ABORTED - restarting transaction ...");
                                priorTransactionID = transactionID;
                                break;
                            default:

                        }
                    } while (returnStatus == TRANSACTION_ABORTED); //get back and restart transaction

                }
            }.start();
        }
    }

    public static void main(String[] args) {
        // Get the root path of the project file
        String currentPath = System.getProperty("user.dir");

        // Start the TransactionClient
        (new TransactionClient(currentPath + "\\config\\TransactionClient.properties", currentPath + "\\config\\TransactionServer.properties")).start();

    }

}
