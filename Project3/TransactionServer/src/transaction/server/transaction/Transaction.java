/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transaction.server.transaction;

import transaction.server.TransactionServer;
import java.util.ArrayList;
import java.util.HashMap;


/**
 *
 * @author Jiawei Gao, Yawen Peng
 */
public class Transaction {
    
    // Declare variables
    int transactionID;
    int transactionNumber;
    int lastCommittedTransactionNumber;
    
    // the sets of tentative data
    ArrayList<Integer>         readSet = new ArrayList<>();
    HashMap<Integer, Integer> writeSet = new HashMap<>();
    
    StringBuffer log = new StringBuffer("");
    
    Transaction(int transactionID, int lastCommittedTransactionNumber){
        this.transactionID = transactionID;
        this.lastCommittedTransactionNumber = lastCommittedTransactionNumber;
    }
    
    public int read (int accountNumber)
    {
        Integer balance;
        
        //check if value to be read was written by this transaction
        balance = writeSet.get(accountNumber);
        
        //if not, read the committed version of it
        if(balance == null)
        {
            balance = TransactionServer.accountManager.read(accountNumber);
        }
        
        if(!readSet.contains(accountNumber))
        {
            readSet.add(accountNumber);
        }
        
        return balance;
    }
    
    public int write(int accountNumber, int balance)
    {
        int priorBalance = read(accountNumber);
        
        if(!writeSet.containsKey(accountNumber))
        {
            
        }
        
        writeSet.put(accountNumber, balance);
        
        return accountNumber;
    }
    
    public ArrayList getReadSet()
    {
        return readSet;
    }
    
    public HashMap getWriteSet()
    {
        return writeSet;
    }
    
    public int getTransactionID()
    {
        return transactionID;
    }
    
    public int getTransactionNumber()
    {
        return transactionNumber;
    }
    
    public void setTransactionNumber(int transactionNumber)
    {
        this.transactionNumber = transactionNumber;
    }
    
    public int getLastCommittedTransactionNumber()
    {
        return lastCommittedTransactionNumber;
    }
    
    public void log(String logString)
    {
        log.append("\n").append(logString);
        
        if(!TransactionServer.transactionView)
        {
            System.out.println("Transaction #" + transactionID + ((transactionID < 10)?" ":"")+logString);
        }
    }
            
    public String getLog()
    {
        return log.toString();
    }
    
}
