import java.rmi.Remote;

public interface Component_RMI extends Remote {
    public void receiveRequest(int id, int requestNumber);
    
    public void receiveToken(int[] grants);
}
