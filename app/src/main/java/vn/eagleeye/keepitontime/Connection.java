package vn.eagleeye.keepitontime;

/**
 * Created by natuan on 11/16/16.
 */

/*


This code following the GNU License. 
Please feel free to use for academic and learning purposes. 

Dr. Tuan Nguyen
Faculty of Computer Networks and Communications
University of Information Technology
Vietnam National University

*/

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Connection extends MyThread {

    // Thread t;

    InputStream inFromSocket;
    OutputStream outToSocket;
    NetworkListener listener;
    Socket socket;
    NetworkListener beginListener;
    int useCompression = 1;// 0 means not use
    public int id;// 0: server
    private String address;

    // boolean begin = true;// receive first message
    public Connection(String address, int id, Socket socket) throws IOException {
        this.address = address;
        this.id = id;
        this.socket = socket;

        inFromSocket = new DataInputStream(socket.getInputStream());
        outToSocket = new DataOutputStream(socket.getOutputStream());

    }

    /*void readFirst() throws IOException
     {
     byte[] data = new byte[inFromSocket.readInt()];
     if (data.length > 0)
     {
     inFromSocket.readFully(data);
     byte[] decoded = data;
     if (useCompression == 1)
     {
     decoded = UneroCompression1.decompressByte(decoded);
     }

     this.beginListener.onReceived(this, decoded);

     }
     }*/

    public void dispose()
    {
        super.dispose();

        try {
            socket.close();
            inFromSocket.close();
            outToSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    int read() throws IOException {
        byte[] data = new byte[100];
        int length = inFromSocket.read(data);
        System.out.println("read from socket" + length);
        return length;
    }

    public void run() {
        //ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        while (!isStop) {
            if (this.socket.isClosed()) {
                break;
            }

            try {
                byte[] buffer = new byte[10000];
                int length = inFromSocket.read(buffer);

                if (this.listener != null) {
                    this.listener.onReceived(this, buffer);
                }

                if (length == -1) {
                    break;
                }
            } catch (IOException e) {

                break;// client is disconnected
            }

        }
        // disconnect
        if (this.listener != null) {
            this.listener.onDisconnected(this.address, id);
        }
    }

 /*   public void start() {
        if (t == null) {
            t = new Thread(this, "UneroConnection");
            t.start();
        }
    }*/

    private void sendData(byte[] data, int length) {
        try {
            outToSocket.write(data, 0, length);
            Thread.sleep(100);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void writeEnding() throws IOException {
        String end = "ending";
        byte[] data = end.getBytes();
        //outToClient.write(end.getBytes());
        sendData(data, data.length);
    }

    void writeBinaryEnding() throws IOException {
        String end = "bnding";
        byte[] data = end.getBytes();
        //outToClient.write(end.getBytes());
        sendData(data, data.length);
    }

    private void sendChunkData(byte[] data) {
        int remain = data.length;
        byte[] chunk = new byte[1024 * 1024];
        int i = 0;
        int count = 0;
        try {
            while (remain > 0) {

                int len = remain > chunk.length ? chunk.length : remain;
                //copy to chunk
                for (i = 0; i < len; i++) {
                    chunk[i] = data[count++];
                }
                //send chunk
                //outToClient.write(chunk, 0, len);
                sendData(chunk, len);
                //usleep(1000 * 100);//100 milliseconds
                remain -= chunk.length;
            }
            writeBinaryEnding();
        } catch (Exception ex) {

        }
    }

    public void sendByteArrayData(String name, byte[] data) {

        try {
            String fileDescriptor = "Descriptor|" + name;
            byte[] descriptor = fileDescriptor.getBytes();
            sendData(descriptor, descriptor.length);
            //outToClient.write(fileDescriptor.getBytes());
            writeEnding();
            sendChunkData(data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendStringData(String string) {
        try {
            byte[] data = string.getBytes();
            sendData(data, data.length);
            // writeEnding();
        } catch (Exception ex) {

        }
    }
}
