package vn.eagleeye.keepitontime;

/**
 * Created by natuan on 11/16/16.
 */

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends MyThread
{
    public static final int MAX_CONNECTIONS_SIZE = 8;
    //private Thread t;
    private ServerSocket server;

    private NetworkListener listener;
    private ArrayList<Connection> connectionArrayList;
    private ArrayList<Connection> readyAddConnectionArrayList;
    private boolean error = false;
    public Server(int port)
    {
        connectionArrayList = new ArrayList<Connection>();
        readyAddConnectionArrayList = new ArrayList<Connection>();
        try
        {
            //server = new ServerSocket(0);
            //int port = server.getLocalPort();

            server = new ServerSocket(port);

        } catch (IOException e)
        {
            e.printStackTrace();
            //Debug.Log("error port " + port + " is already in use");
            error = true;
        }

    }

    public void dispose()
    {
        super.dispose();
        this.close();
        for(Connection con : this.connectionArrayList)
        {
            con.dispose();
        }
    }
    //close server
    public void close()
    {
        try
        {

            System.out.println("close socket server");

            this.server.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void run()
    {
        if (error)
            return;
        //System.out.println("connectToMachine server");
        // checkHosts("192.168.100");
        //getIp();
        // getLocalIpAddress();
        //System.out.println(getIpAddress());
        while (!isStop)
        {
            try
            {
                if (this.server.isClosed())
                    break;
                Socket socket = this.server.accept();
                int clientId = connectionArrayList.size();


                Connection connection = new Connection(socket.getInetAddress().getHostAddress(),clientId, socket);

                connection.listener = this.listener;
                connection.start();
                connection.sendStringData(NetworkCommands.writeClientIdCommand(clientId));
                //send clientId to client
                //UneroByteArrayData ubad = new UneroByteArrayData();
                //ubad.addUnsignedInt(clientId, MAX_CONNECTIONS_SIZE);
                //connection.sendData(ubad.getByteArray());

                readyAddConnectionArrayList.add(connection);
                //connectionArrayList.add(connection);
                if (this.listener != null)
                {
                    this.listener.onConnected(socket.getInetAddress().getHostAddress(), clientId);
                }

            } catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    }

    public void checkHosts(String subnet){
        int timeout=1000;
        for (int i=1;i<254;i++){
            String host=subnet + "." + i;
            try
            {
                if (InetAddress.getByName(host).isReachable(timeout)){
                    System.out.println(host + " is reachable");
                }else
                {
                    System.out.println(host +" is unreachable");
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        System.out.println("end check");
    }



    // connectToMachine server
	/*public void start()
	{
		if (t == null)
		{
			t = new Thread(this, "ServerThread");
			t.start();
		}
	}*/

    /*public void sendDataToAll(byte[] data)
    {
        //Debug.Log("send length = " + data.length);
        for (Connection con : this.connectionArrayList)
            try
            {
                con.sendByteArrayData(data);
            } catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }*/
    public void sendStringDataToAll(String string)
    {

        for (Connection con : this.readyAddConnectionArrayList)
        {
            connectionArrayList.add(con);
        }
        this.readyAddConnectionArrayList.clear();
        for (Connection con : this.connectionArrayList)
        {
            con.sendStringData(string);
        }
    }
    public void sendStringDataTo(int id, String string)
    {
        for (Connection con : this.connectionArrayList)
        {
            if (con.id == id)
            {
                con.sendStringData(string);
                break;
            }
        }
    }

    public void setListener(NetworkListener listener)
    {
        this.listener = listener;
    }

    public int getNumberOfConnections()
    {
        return this.connectionArrayList.size();
    }



}
