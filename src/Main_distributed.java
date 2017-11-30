import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;


public class Main_distributed {

    public static void main(String[] args) {
        
        int local_ip_idx = 0;
        String[] ip = new String[args.length/2];
        int[] np = new int[args.length/2];
        int numProcesses = 0;
        
        for(int i = 0; i < args.length/2; i++)
        {            
            ip[i] = args[i];
            np[i] = Integer.parseInt(args[args.length/2+i]);
            numProcesses += np[i];
            if (args[i].equals("localhost"))
                local_ip_idx = i;
        }
               
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }

        // TODO 
        String[] dests = new String[numProcesses];
        int counter = 0;
        for(int i = 0; i < ip.length; i++) {
            for(int j = 0; j < np[i]; j++) {
                dests[counter] = ip[i];
                counter++;
            }
        }
        
        
        Thread[] myThreads = new Thread[np[local_ip_idx]];
        try{
            // Create Registry
            Registry registry = LocateRegistry.createRegistry(1099);
        } catch (Exception e) {
            System.err.println("Could not create registry exception: " + e.toString()); 
            e.printStackTrace(); 
        } 
        
        // Create processes
        int counterLocal = 0;
        for (int i = 0; i < numProcesses; i++)
        {
            if(dests[i].equals("localhost")) {
                Component c;
                try {
                    c = new Component(i, numProcesses, dests);
                    MyProcess p = new MyProcess(c);
                    myThreads[counterLocal++] = new Thread(p);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Press enter to continue");
        Scanner scan = new Scanner(System.in);
        scan.nextLine();

        // Make processes send random requests
        for (int i = 0; i < np[local_ip_idx]; i++)
        {
            myThreads[i].start();
        }        
    }

}