
public class ChatNode {

        public static void main(String args[]) {
                
                // Parameters
                String PropertiesPath;
                // Get the current path
                String currentPath = System.getProperty("user.dir");

                // Get the name of the properties file
                // Using the default path if necessary
                try{
                        PropertiesPath = currentPath+ "/" +args[0] +".properties";
                }catch(ArrayIndexOutOfBoundsException ex){
                        PropertiesPath = currentPath + "/node1.properties";
                }

                // Starting two Threads (Sender and Receiver)
                Sender R1 = new Sender(PropertiesPath);
                R1.start();
                
                Receiver R2 = new Receiver(PropertiesPath);
                R2.start();
        }     
    
}
