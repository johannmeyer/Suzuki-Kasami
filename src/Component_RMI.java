import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Component_RMI extends Remote {
    
    public void receiveRequest(int id, int requestNumber) throws RemoteException;
    public void receiveToken(int[] grants) throws RemoteException;
}
