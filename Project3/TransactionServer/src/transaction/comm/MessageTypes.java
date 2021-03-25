/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transaction.comm;

/**
 *
 * @author Jiawei Gao, Yawen Peng
 */
public interface MessageTypes{
    
    // coordinator interface
    public static final int OPEN_TRANSACTION = 1;
    public static final int CLOSE_TRANSACTION= 2; //return TRANSACTION_COMMITTED or TRANSACTION_ABORTED,
    public static final int ABORT_TRANSACTION = 3;//not implemented
            
    public static final int READ_REQUEST = 4;
    public static final int WRITE_REQUEST = 5;
    
    //message sent from the server in response to a client request
    public static final int READ_REQUEST_RESPONSE = 6;
    public static final int WRITE_REQUEST_RESPONSE = 7;
    
    //flags sent from server in response to a client's CLOSE_TRANSACTION
    public static final int  TRANSACTION_COMMITTED =8;
    public static final int  TRANSACTION_ABORTED = 9;
}
