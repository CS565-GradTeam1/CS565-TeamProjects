package appserver.job.impl;

/**
 * Class [FibonacciAux] Helper class for PlusOne to demonstrate that dependent classes are loaded automatically
 when a class is loaded (in this case PlusOne)
 * 
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public class FibonacciAux {
    
    Integer number = null;
    
    public FibonacciAux(Integer number) {
        this.number = number;
    }
    
    public Long getResult() {
        return Fab(this.number);
    }
    
    public static Long Fab(Integer num){
        
        if(num==1 || num==2){
            return Long.valueOf(1);
        }else{
            return Fab(num-1) + Fab(num-2);
        }
    }
}
