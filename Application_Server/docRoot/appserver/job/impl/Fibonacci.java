package appserver.job.impl;

import appserver.job.Tool;

/**
 * Class [Fibonacci] Simple POC class that implements the Tool interface
 * 
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public class Fibonacci implements Tool{

    FibonacciAux helper = null;
    
    @Override
    public Object go(Object parameters) {
        
        helper = new FibonacciAux((Integer) parameters);
        return helper.getResult();
    }
}
