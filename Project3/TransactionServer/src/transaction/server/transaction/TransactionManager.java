package transaction.server.transaction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import transaction.comm.Message;
import transaction.comm.MessageTypes;
import transaction.server.TransactionServer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import static transaction.comm.MessageTypes.READ_REQUEST_RESPONSE;
import static transaction.comm.MessageTypes.TRANSACTION_COMMITTED;

/**
 *
 * @author Jiawei Gao, Yawen Peng
 */
public class TransactionManager extends Thread implements MessageTypes {

    //Declare and initialize variables
    private static int transactionIdCounter = 0;
    private static final ArrayList<Transaction> transactions = new ArrayList<>();
    private static final HashMap<Integer, Transaction> committedTransactions = new HashMap<>();
    private static int transactionNumberCounter = 0;
    private Socket socket = null;
    private ObjectOutputStream writeToNet = null;
    private ObjectInputStream readFromNet = null;
    Message message;

    // Create a Transaction type variable for saving the state 
    Transaction transaction = new Transaction(++transactionIdCounter, committedTransactions.size());

    public TransactionManager(Socket socket) {
        super("TransactionManager");
        this.socket = socket;

        // Get the socket
        try {
            writeToNet = new ObjectOutputStream(socket.getOutputStream());
            readFromNet = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            System.out.println("[TransactionManagerWorker.run] Error occurred when opening a socket");
            ex.printStackTrace();
        }
    }

    public void run() {
        // local variable for keeping the response loop
        boolean notClosed = true;

        while (notClosed) {
            // Get the message type
            try {
                message = (Message) readFromNet.readObject();
            } catch (IOException | ClassNotFoundException | NullPointerException ex) {
                System.out.println("[TransactionManagerWorker.run] Error when reading message type");
                ex.printStackTrace();
            }

            // Handle the message based on the message type
            switch (message.MessageType) {
                case OPEN_TRANSACTION:
                    try {
                        writeToNet.writeObject(transaction.transactionID);

                        System.out.println("[TransactionManagerWorker.run] OPEN_TRANSACTION #" + transaction.transactionID);
                    } catch (IOException ex) {
                        System.out.println("[TransactionManagerWorker.run] Error occurred when sending transaction ID: " + transaction.transactionID);
                        ex.printStackTrace();
                    }
                    break;

                case READ_REQUEST:
                    try {
                        int accountNum = Integer.parseInt(message.object.toString());
                        System.out.println("[TransactionManagerWorker.run] READ_REQUEST >>>>>>>>>>>>>>> account #" + accountNum);
                        
                        int balance = transaction.read(accountNum);
                        writeToNet.writeObject(new Message(READ_REQUEST_RESPONSE, balance));
                        System.out.println("[TransactionManagerWorker.run] READ_REQUEST <<<<<<<<<<<<<<< account #" + accountNum + ", balance $" + balance);
                    } catch (IOException ex) {
                        System.out.println("[TransactionManagerWorker.run] Error occurred when sending transaction ID");
                        ex.printStackTrace();
                    }
                    break;

                case WRITE_REQUEST:
                    try {
                    int account = Integer.parseInt(message.objects[0].toString());
                    int balance = Integer.parseInt(message.objects[1].toString());
                    System.out.println("[TransactionManagerWorker.run] WRITE_REQUEST >>>>>>>>>>>>>>> account #" + account + ", new balance $" + balance);
                    
                    transaction.write(account, balance);
                    writeToNet.writeObject(new Message(WRITE_REQUEST_RESPONSE, balance));
                    System.out.println("[TransactionManagerWorker.run] WRITE_REQUEST <<<<<<<<<<<<<<< account #" + account + ", new balance $" + balance);

                    //System.out.println("WRITE_REQUEST succues!!! " + account + ": " + balence);
                } catch (IOException ex) {
                    System.out.println("[TransactionManagerWorker.run] Error occurred when sending transaction ID");
                    ex.printStackTrace();
                }
                break;

                case CLOSE_TRANSACTION:
                    notClosed = false;

                    synchronized (transactions) {
                        transactions.remove(transaction);

                        if (validateTransaction(transaction)) {
                            //add this transaction to the committed transaction
                            committedTransactions.put(transaction.getTransactionNumber(), transaction);

                            //tell client transaction committed
                            try {
                                writeToNet.writeObject((Integer) TRANSACTION_COMMITTED);
                                System.out.println("[TransactionManagerWorker.run] CLOSE_TRANSACTION #" + transaction.transactionID + "- COMMITTED\n");

                                readFromNet.close();
                                writeToNet.close();
                                socket.close();
                            } catch (IOException e) {
                                System.out.println("[TransactionManagerWorker.run] CLOSE_TRANSACTION #");
                            }

                            transaction.log("[TransactionManagerWorker.run] CLOSE_TRANSACTION #" + transaction);

                            //write data to operational data
                            writeTransaction(transaction);
                        } else {
                            //validation failed, abort this transaction and tell client
                            try {
                                writeToNet.writeObject((Integer) TRANSACTION_ABORTED);
                                System.out.println("[TransactionManagerWorker.run] CLOSE_TRANSACTION #" + transaction.transactionID + "- ABORTION\n");

                            } catch (IOException e) {
                                System.out.println("[TransactionManagerWorker.run] ABORD_TRANSACTION #");
                            }

                            transaction.log("[TransactionManagerWorker.run] ABORD_TRANSACTION #" + transaction);
                        }
                    }

            }
        }

    }

    /* 
    Validating Transaction
    Return true/false
     */
    private boolean validateTransaction(Transaction transaction) {
        // Set transaction number
        transaction.setTransactionNumber(++transactionNumberCounter);

        // Get lastCommitedTransactionNumber and transactionNumber of the transaction
        int lastCommitedTransactionNumber = transaction.lastCommittedTransactionNumber;
        int transactionNumber = transaction.getTransactionNumber();

        // loop transactions with the transaction number from lastCommitedTransactionNumber+1 to transactionNumber-1
        for (int i = lastCommitedTransactionNumber + 1; i < transactionNumber; i++) {
            //System.out.println("comparing " + transactionNumber + "and " + i);

            // comparing the read set of the current transaction with the write set of these temp_transaction
            Transaction temp_transaction = committedTransactions.get(i);
            Set<Integer> keys = temp_transaction.writeSet.keySet();
            Iterator<Integer> keysIterator = keys.iterator();
            while (keysIterator.hasNext()) {
                int keyTemp = keysIterator.next();

                // Validation fail if there is a overlap
                if (transaction.readSet.size() == 1) {
                    if (keyTemp == transaction.readSet.get(0)) {
                        transactionNumberCounter--;
                        return false;
                    }
                } else {
                    if (keyTemp == transaction.readSet.get(0) || keyTemp == transaction.readSet.get(1)) {
                        transactionNumberCounter--;
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void writeTransaction(Transaction transaction) {
        // Write Transaction to the global memory (permanent memory)
        Set<Integer> keys = transaction.writeSet.keySet();
        Iterator<Integer> keysIterator = keys.iterator();
        while (keysIterator.hasNext()) {
            int keyTemp = keysIterator.next();
            TransactionServer.accountManager.write(keyTemp, transaction.writeSet.get(keyTemp));
        }

        // Close the server when transactions done 
        // and print out the branch total
        if (transaction.transactionNumber == TransactionServer.numberTransactions) {
            System.out.println("===========================BRANCH TOTAL===========================");
            System.out.println("---> $" + TransactionServer.accountManager.totalBalance());
            System.exit(0);
        }
    }
}
