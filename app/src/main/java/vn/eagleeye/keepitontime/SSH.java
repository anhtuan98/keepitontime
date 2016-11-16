package vn.eagleeye.keepitontime;

/**
 * Created by natuan on 11/16/16.
 */

/*
Welcome to Ubiquitous Computing Research Group 

This code following the GNU License. 
Please feel free to use for academic and learning purposes.

Dr. Tuan Nguyen 
Faculty of Computer Networks and Communications 
University of Information Technology
Vietnam National University
https://vn.linkedin.com/in/tuanubicom

*/

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;

        import android.util.Log;
        import ch.ethz.ssh2.ChannelCondition;
        import ch.ethz.ssh2.Connection;
        import ch.ethz.ssh2.Session;
        import ch.ethz.ssh2.StreamGobbler;
        import java.io.BufferedWriter;
        import java.io.DataInputStream;
        import java.io.FileInputStream;
        import java.io.FileReader;
        import java.io.OutputStream;
        import java.io.OutputStreamWriter;
        import java.util.ArrayList;
        import java.util.Arrays;

public class SSH extends Thread {

    String username = "pi";
    String password = "raspberry";
    String hostname;
    private ArrayList<String> commands = new ArrayList<String>();
    private ArrayList<String> preAddCommands = new ArrayList<String>();
    Connection conn;
    private boolean isConnected = false;
    Session sess;
    //BufferedWriter bw;
    SSHHandler handler;
    ReadThread reader;

    public SSH(String hostname) {
        this.hostname = hostname;
        conn = new Connection(hostname);
        this.commands.add("ls");
        this.commands.add("omxplayer adasdasd");
    }

    public void setSSHHandler(SSHHandler handler)
    {
        this.handler = handler;
    }
    public void addCommand(String command) {
        this.preAddCommands.add(command);

    }

    public void run() {
        while(true)
        {
            try {
                if (!isConnected) {
                    conn.connect();

					/*
					 * Authenticate. If you get an IOException saying something like
					 * "Authentication method password not supported by the server at this stage."
					 * then please check the FAQ.
					 */
                    boolean isAuthenticated = conn.authenticateWithPassword(
                            username, password);

                    if (isAuthenticated == false) {
                        throw new IOException("Authentication failed.");
                    }
                    if (handler!=null)
                    {
                        handler.onConnected();
                    }
                    System.out.println("SSH connected to " + hostname);
                    isConnected = true;
					/* Create a session */
                    sess = conn.openSession();
                    //sess.wait(condition_set, timeout)

                    // sess.execCommand("sudo -i");
                    // sess.execCommand("159874632");
                    sess.startShell();
                    //sess.wait(condition_set, timeout)
                    // sess.execCommand("startx");
                    OutputStream stdin = sess.getStdin();

                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(stdin));

                    bw.write("ls");
                    //bw.flush();
                    //bw.close();
                    InputStream stdout = new StreamGobbler(sess.getStdout());

                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            stdout));

                    reader = new ReadThread(stdout);
                    reader.setTag("stdout");
                    reader.start();


                    InputStream stderr = new StreamGobbler(sess.getStderr());

                    BufferedReader br2 = new BufferedReader(new InputStreamReader(
                            stderr));

                    ReadThread reader2 = new ReadThread(stderr);
                    reader2.setTag("stderr");
                    reader2.start();


					/*while (true) {
						String line = br.readLine();
						if (line == null) {
							break;
						}
						System.out.println(line);
					}*/


                } else {
                    for (String command : this.preAddCommands) {
                        this.commands.add(command);
                    }
                    this.preAddCommands.clear();
                    for (String command : this.commands) {
                        OutputStream out = sess.getStdin();
                        //command = command +"\n";
                        if (command.equals("seek"))
                        {
                            out.write(39);
                        }else
                        {
                            out.write(command.getBytes());
                        }

                        //BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(stdin));
                        //bw.write(command+"\n");

                        //bw.flush();
                        //bw.close();
                        //sess.waitForCondition(ChannelCondition.EOF, 0);
                        Log.d(MainActivity.TAG, "Send SSH command " + command);

                    }
                    commands.clear();

                }

            } catch (Exception e) {
                e.printStackTrace(System.err);
                //System.exit(2);
            }
        }
    }

    public void dispose() {
        try {
            if (isConnected)
            {
                if (reader!=null)
                    reader.dispose();
                //bw.close();
                sess.close();
                conn.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    public static class ReadThread extends Thread
    {
        //BufferedReader br;
        boolean isStop = false;
        ReadEventHandler handler;
        String tag;
        InputStream in;
        public ReadThread(InputStream in)
        {
            this.in = in;
        }

        public void setHandler(ReadEventHandler handler) {
            this.handler = handler;
        }
        public void setTag(String tag)
        {
            this.tag = tag;
        }

        public void run()
        {
            byte[] buff = new byte[8192];
            while (!isStop) {
                //String line;
                try {
                    int len  = in.read(buff);
                    if (len == -1)
                    {
                        Log.d(MainActivity.TAG, "len = -1");
                        return;

                    }
					/*if (line == null) {
						break;
					}*/
                    //System.out.println(line);
                    String line = new String(buff, 0, len);
                    if (handler!=null)
                        handler.onReceived(line);
                    Log.d(MainActivity.TAG +" " + tag +" ", line);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        }
        public void dispose()
        {
            this.isStop = true;
        }
        public static interface ReadEventHandler
        {
            void onReceived(String line);
        }
    }

    interface SSHHandler
    {
        void onConnected();
    }
}
