/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transaction.server.account;

import java.util.HashMap;
import transaction.comm.MessageTypes;
import transaction.server.transaction.Transaction;

/**
 *
 * @author Jiawei Gao, Yawen Peng
 */


public class AccountManager{
    int numberAccounts;
    int initialBalance;
    private static final HashMap<Integer, Account> accounts = new HashMap<>();
    
    // Initialize the accounts
    public AccountManager(int numberAccounts, int initialBalance)
    {
        this.numberAccounts = numberAccounts;
        this.initialBalance = initialBalance;
        
        for(int i=0; i<numberAccounts; i++){
            accounts.put(i, new Account(i, initialBalance));
        }
    }
    
    // Read the balance from the memory
    public int read (int accountNumber)
    {
        return accounts.get(accountNumber).balance;
    }
    
    // Write the new balance to the account
    public void write (int accountNumber, int amount)
    {
        accounts.get(accountNumber).balance = amount;
    }
    
    // Return the branch total
    public int totalBalance ()
    {
        int sum=0;
        for(int i=0; i<numberAccounts; i++){
            sum += accounts.get(i).balance;
        }
        return sum;
    }
    
}
