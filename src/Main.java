import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class Main {

    public static void main(String[] args) {
        int numProcesses = 3;

        Thread[] myThreads = new Thread[numProcesses];
        try{
            // Create Registry
            Registry registry = LocateRegistry.createRegistry(1099);

            for (int i = 0; i < numProcesses; i++)
            {
                Component c = new Component(i, numProcesses);
                MyProcess p = new MyProcess(c);
                myThreads[i] = new Thread(p);
            }
            for (int i = 0; i < numProcesses; i++)
            {
                myThreads[i].start();
            }
            
        } catch (Exception e) {
            System.err.println("Could not create registry exception: " + e.toString()); 
            e.printStackTrace(); 
        } 
    }

}

class MyProcess implements Runnable
{   
    private Component c;
    public MyProcess(Component c) {
        this.c = c;
    }

    public void run() {
        int wait = (int) (Math.random()*10000);
        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            c.sendRequest();
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
    
}