import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

public class Component extends UnicastRemoteObject implements Component_RMI {
    
    private static final String naming = "Component-";
    private boolean token_present;
    private boolean critical;
    private int[] requests;
    private int[] grants;
    private int id;
    private int numberOfProcesses;
    private String[] ips;

    /**
     * Component with id 0 is initialized with the token.
     */
    public Component(int id, int numberOfProcesses, String[] ips) throws RemoteException {
        super();
        this.token_present = (id == 0);
        this.critical = false;
        this.requests = new int[numberOfProcesses];
        this.grants = new int[numberOfProcesses];
        this.id = id;
        this.numberOfProcesses = numberOfProcesses;
        this.ips = ips;
        
        bind();
    }
    
    /**
     * Binding the remote object (stub) in the local registry
     */
    private void bind() {
        try{
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(naming + id, this);
            System.err.println("Process " + id + " ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void receiveRequest(int requesting_id, int requestNumber) {
        // Update requests
        requests[requesting_id] = requestNumber;
        
        // Check if token present and not in critical section or
        // that request has not been granted already
        if(token_present && !critical && requestNumber > grants[requesting_id]) {
            sendToken(requesting_id);
        }
        
    }

    @Override
    public void receiveToken(int[] grants) {
        // Grant current process
        grants[id] = requests[id];
        // Update local granted array
        this.grants = grants;
        
        println("Token received");
        token_present = true;
        criticalSection();
        
        // Once done check if another process needs it
        checkRequests();
    }
    
    /**
     * Broadcast request to all other components.
     */
    public void sendRequest() throws MalformedURLException, RemoteException, NotBoundException {
        // There is a new request
        requests[id]++;
        
        println("Broadcasting request number " + Arrays.toString(requests));
        
        // Broadcast request to all processes including current process
        // to check if they have the token
        for(int i = 0; i < numberOfProcesses; i++) {
            String destName = "//" + ips[i] + ":1099/" + naming + i;
            Component_RMI dest = (Component_RMI) Naming.lookup(destName);
            dest.receiveRequest(id, requests[id]);
        }
    }
    
    /**
     * Send token to component with send_id.
     */
    private void sendToken(int send_id) {
        try {
            String destName = "//" + ips[send_id] + ":1099/" + naming + send_id;
            Component_RMI dest = (Component_RMI) Naming.lookup(destName);
            token_present = false;
            println("Sending token to " + send_id);
            dest.receiveToken(grants);
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Check the request array for requests that can be fulfilled.
     */
    private void checkRequests() {
        for(int i = 1; i < numberOfProcesses+1; i++) {
            
            // Cyclic check
            int checkReq = (id + i)%numberOfProcesses;
            
            // Send to first process with outstanding request
            if(requests[checkReq] > grants[checkReq]) {
                sendToken(checkReq);
                break;
            }
        }
    }
    
    private void criticalSection() {
        critical = true;
        println("Entering critical section");
        int wait = (int) (Math.random()*5000);
        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            println("Critical section interrupted");
        }
        println("Leaving critical section");
        critical = false;
    }
    
    private void println(String message)
    {
        String pidStr = "(" + this.id + ") ";
        System.err.println(pidStr + message);
    }

}
