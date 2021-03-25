/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transaction.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import transaction.comm.Message;
import transaction.comm.MessageTypes;
/**
 *
 * @author Jiawei Gao, Yawen Peng
 */


public class TransactionServerProxy implements MessageTypes {
    
    String host = null;
    int port;
    
    private Socket dbConnection = null;
    private ObjectOutputStream writeToNet = null;
    private ObjectInputStream readFromNet = null;
    private Integer transactionID = 0;
    
    TransactionServerProxy(String host, int port)
    {
        this.host = host;
        this.port = port;
    }
    
    public int openTransaction(){
    
        try{
            dbConnection = new Socket(host, port);
            writeToNet = new ObjectOutputStream(dbConnection.getOutputStream());
            readFromNet = new ObjectInputStream(dbConnection.getInputStream());
        } catch (IOException ex){
            System.out.println("[TransactionServerProxy.openTransaction] Error occurred when opening a socket");
            ex.printStackTrace();
        }
        
        try{
            writeToNet.writeObject(new Message(OPEN_TRANSACTION, null));
            transactionID = (Integer) readFromNet.readObject();
        }catch (IOException | ClassNotFoundException | NullPointerException ex){
            System.out.println("[TransactionServerProxy.openTransaction] Error when writing/reading message");
            ex.printStackTrace();
        }
        return transactionID;
    }
    
    
    public int closeTransaction(){
        int returnStatus = TRANSACTION_ABORTED;
        
        try{
            writeToNet.writeObject(new Message(CLOSE_TRANSACTION, null));
            returnStatus = (int) readFromNet.readObject();
            
            readFromNet.close();
            writeToNet.close();
            dbConnection.close();
        } catch (IOException | ClassNotFoundException ex){
            System.out.println("[TransactionServerProxy.closeTransaction] Error occured");
            ex.printStackTrace();
        }
        return returnStatus;
    }
    
    public int read(int accountNumber)
    {
        Message response = null;
        
        try{
            writeToNet.writeObject(new Message(READ_REQUEST, accountNumber));
            response= (Message) readFromNet.readObject();
            
        } catch (IOException | ClassNotFoundException ex){
            System.out.println("[TransactionServerProxy.read] Error occurred when reading from account ");
            ex.printStackTrace();
        }
        
        return Integer.parseInt(response.object.toString());    
    }
    
    public int write(int accountNumber, int amount)
    {
        Message response = null;
  
        try{
            writeToNet.writeObject(new Message(WRITE_REQUEST, new Object[]{accountNumber,amount}));
            response = (Message) readFromNet.readObject();
        } catch (IOException | ClassNotFoundException ex){
            System.out.println("[TransactionServerProxy.write] Error occurred when writing to account");
            ex.printStackTrace();
        }
        
        return Integer.parseInt(response.object.toString());
    }
}
