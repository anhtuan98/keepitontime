package vn.eagleeye.keepitontime;

/**
 * Created by natuan on 11/16/16.
 */
public interface NetworkListener {
    public void onReceived(Connection connection, byte[] data);


    public void onConnected(String clientAddress, int id);

    public void onDisconnected(String clientAddress, int id);




}
