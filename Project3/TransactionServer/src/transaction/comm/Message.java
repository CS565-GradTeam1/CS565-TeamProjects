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
public class Message implements java.io.Serializable{

    public int MessageType;
    public Object object;

    public Object objects[];
    
    public Message(int MessageType, Object[] objects) {
        this.MessageType = MessageType;
        this.objects = objects;
    }
    
    public Message(int MessageType, Object object) {
        this.MessageType = MessageType;
        this.object = object;
    }
    

    
}
