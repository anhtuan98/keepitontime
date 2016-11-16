package vn.eagleeye.keepitontime;

/**
 * Created by natuan on 11/16/16.
 */

        import java.net.*;
        import java.util.ArrayList;
        import java.util.Enumeration;

        import android.app.Activity;
        import android.app.ProgressDialog;
        import android.util.Log;


public class NetworkHandler
{
    int port = 5222;
    Server server;
    BroadcastSender sender;
    NetworkListener listener;
    Clock clockThread = new Clock();
    boolean firstConnect = false;
    Activity activity;

    public NetworkHandler(Activity activity)
    {
        this.activity = activity;
        sender = new BroadcastSender(getIpAddress(),port);
        sender.start();

        server = new Server(port);
        server.setListener(new NetworkListener()
        {
            @Override
            public void onReceived(Connection connection, byte[] data)
            {

                listener.onReceived(null, data);
                if (firstConnect)
                {
                    String command = new String(data).trim();
                    if (command.contains("clockIsCounting"))
                    {
                        String isCounting = command.substring(command.lastIndexOf("=") + 1);
                        if (isCounting.equals("true"))
                        {
                            //client is counting
                            clockThread.setPause(false);
                        }else
                        {
                            clockThread.setPause(true);
                        }
                    }else if (command.contains("second=")) {
                        //get time from one alive client
                        String totalSecondstr = command.substring(command.lastIndexOf("=") + 1);
                        int totalSecond = Integer.parseInt(totalSecondstr);
                        clockThread.setTotalSecond(totalSecond);
                        firstConnect = false;

                    }
                }
            }

            @Override
            public void onConnected(String clientAddress, int id)
            {
                firstConnect = true;
                //sender.dispose();
                sender.setPausing(true);
                listener.onConnected(clientAddress, id);
            }

            @Override
            public void onDisconnected(String clientAddress, int id)
            {
                sender.setPausing(false);
                listener.onDisconnected(clientAddress, id);
            }
        });
        server.start();

        clockThread.setHandler(new Clock.ClockEventHandler() {

            @Override
            public void tick(int totalSecond) {

                Log.d(MainActivity.TAG, "total second = " + totalSecond);
                updateNewTime(totalSecond);
            }
        });
        clockThread.start();
        clockThread.setPause(true);
    }


    public void setListener(NetworkListener listener) {
        this.listener = listener;
    }


    public void turnOff()
    {
        server.sendStringDataToAll("turnOff");
    }
    public void setNewTime(int hour, int minute)
    {
        int totalSecond = hour * 60 * 60 + minute * 60;
        clockThread.setTotalSecond(totalSecond);
        //clockThread.start();
        clockThread.setPause(true);
        server.sendStringDataToAll("setTimeSecond="+totalSecond);

    }
    public void updateNewTime(int totalSecond)
    {
        server.sendStringDataToAll("updateTimeSecond="+totalSecond);
    }
    public void setNewFontSize(int size)
    {
        server.sendStringDataToAll("setFontSize="+size);
    }
    public void startClock()
    {
        server.sendStringDataToAll("start");
        clockThread.setPause(false);

    }
    public void pauseClock()
    {
        server.sendStringDataToAll("pause");
        clockThread.setPause(true);
    }
    public void dispose()
    {
        clockThread.dispose();
        server.dispose();
        sender.dispose();
    }

    private ArrayList<String> getIpAddress() {
        String ip = "";
        ArrayList<String> listOfHostAddress = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                        listOfHostAddress.add(inetAddress.getHostAddress());
                    }else
                    {
                        //  System.out.println("what is this: " + inetAddress.getHostAddress());
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return listOfHostAddress;
    }
}


class Clock extends MyThread
{
    private int totalSecond;
    private boolean isPause;

    ClockEventHandler handler;


    public boolean isPause() {
        return isPause;
    }
    public void setPause(boolean isPause) {
        this.isPause = isPause;
    }
    public void setHandler(ClockEventHandler handler) {
        this.handler = handler;
    }
    public void setTotalSecond(int totalSecond)
    {
        this.totalSecond = totalSecond;
    }
    public void run()
    {
        while(!isStop)
        {
            if (!isPause)
            {
                this.totalSecond--;
                if (handler!=null)
                    handler.tick(totalSecond);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    interface ClockEventHandler
    {
        void tick(int totalSecond);
    }
}
class BroadcastSender extends Thread
{
    private int port;
    //private Thread t;
    private ReceiveHandler mReceiveHandler;
    private ArrayList<String> ipInterfaces;
    private boolean isStop = false;
    private boolean isPausing = false;

    public BroadcastSender(ArrayList<String> ipInterfaces, int port)
    {
        this.ipInterfaces = ipInterfaces;
        this.port = port;
    }

    public void setReceiveHandler(ReceiveHandler receiveHandler)
    {
        mReceiveHandler = receiveHandler;
    }



    public boolean isPausing() {
        return isPausing;
    }

    public void setPausing(boolean isPausing) {
        this.isPausing = isPausing;
    }

    @Override
    public void run()
    {
        try
        {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            while(!isStop)
            {
                if (isPausing)
                    continue;


                for (String ip : ipInterfaces)//list of this app's ips
                {
                    String classIP = ip.substring(0, ip.lastIndexOf('.')) + ".255";

                    byte[] sendData = ("CountDownServer,ip=" + ip).getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(classIP), port);
                    socket.send(sendPacket);
                    System.out.println("Request packet sent to: " + classIP);





                }
                Thread.sleep(3000);
            }
            socket.close();


        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    public void dispose()
    {
        this.isStop = true;
    }

    interface ReceiveHandler
    {
        void onReceived(String ip, String data);
    }
}
