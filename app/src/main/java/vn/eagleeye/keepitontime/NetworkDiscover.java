package vn.eagleeye.keepitontime;

/**
 * Created by natuan on 11/16/16.
 */

/*
Welcome to Ubiquitous Computing Research Group 

This code following the GNU License. 
Please feel free to use for academic and learning purposes.

Dr. Tuan A. Nguyen
Faculty of Computer Networks and Communications 
University of Information Technology
Vietnam National University
https://vn.linkedin.com/in/tuanubicom
 

*/

        import java.io.BufferedInputStream;
        import java.io.BufferedReader;
        import java.io.BufferedWriter;
        import java.io.ByteArrayOutputStream;
        import java.io.FileReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.io.OutputStream;
        import java.io.OutputStreamWriter;
        import java.net.InetAddress;
        import java.net.NetworkInterface;
        import java.net.UnknownHostException;
        import java.util.ArrayList;
        import java.util.List;

        import junit.framework.TestCase;

        import vn.eagleeye.keepitontime.SSH.ReadThread;
        import vn.eagleeye.keepitontime.SSH.ReadThread.ReadEventHandler;

        import ch.ethz.ssh2.Connection;
        import ch.ethz.ssh2.SCPClient;
        import ch.ethz.ssh2.Session;
        import ch.ethz.ssh2.StreamGobbler;

        import android.app.Activity;
        import android.app.Dialog;
        import android.content.Context;
        import android.util.Log;
        import android.view.View;
        import android.view.WindowManager;
        import android.view.inputmethod.InputMethodManager;
        import android.widget.*;
        import android.widget.CompoundButton.OnCheckedChangeListener;

public class NetworkDiscover extends MyThread {

    private ArrayList<String> listOfInterfaces;
    private ArrayList<String> listOfReachableAddress = new ArrayList<String>();
    private ArrayList<String> listOfPiAddress = new ArrayList<String>();
    private ArrayList<MyThread> listOfCreatedThreads = new ArrayList<MyThread>();
    static final int timeOut = 2000;// lower this may increase the speed of
    // scanning
    int countForEach = 5;// lower this value may increase the speed of scanning
    // (incease number of concurrent threads)
    EventHandler handler;
    Activity activity;
    boolean useSameUserAndPass;
    // SSHConfig testConfig;
    ArrayList<SSHConfig> listOfSSHConfigs = new ArrayList<SSHConfig>();

    private Dialog userpassDialog;
    private String currentTestAddress;
    public NetworkDiscover(ArrayList<String> listOfInterfaces, Activity context) {
        this.listOfInterfaces = listOfInterfaces;
        this.activity = context;
        createUserpassDialog();
    }


    public void setHandler(EventHandler handler) {
        this.handler = handler;
    }

    static int numOfFinishedSubThreads = 0;
    static int createdSubThreads = 0;

    public void run() {
        // this.isRunning = true;
        numOfFinishedSubThreads = 0;
        createdSubThreads = 0;

        final int numOfSubThreads = ((int) Math.ceil(255.0 / countForEach))
                * listOfInterfaces.size();

        for (String hostname : listOfInterfaces) {

            ArrayList<String> listOfAddresses = new ArrayList<String>();
            String firstPart = getThreePartsOfIp(hostname);
            for (int i = 1; i < 255; i++) {
                listOfAddresses.add(firstPart + "." + i);
            }

            for (int i = 0; i < 255; i += countForEach) {
                final SubThread sub = new SubThread(listOfAddresses, i, countForEach);
                createdSubThreads++;
                Log.d(MainActivity.TAG, "Subthread finished: "
                        + "created subthread " + createdSubThreads);
                listOfCreatedThreads.add(sub);

                sub.setHandler(new EventHandler() {

                    @Override
                    public synchronized void onFinished() {

						/*
						 * Log.d(MainActivity.TAG, "Subthread finished: " +
						 * numOfFinishedSubThreads() + "/" + numOfSubThreads);
						 */
                        if (sub.isRunning())
                        {
                            if (increaseNumOfFinishedSubThreads() >= numOfSubThreads) {
                                Log.d(MainActivity.TAG, "Discover finished! "
                                        + numOfFinishedSubThreads() + "/"
                                        + numOfSubThreads);

                                getAddressAndShowUserpassDialog();
								/*for (String address : listOfReachableAddress)
								{
									Log.d(MainActivity.TAG, "SHOW DIALOG!");
									showUsernamePasswordDialog(address);
								}*/

                            }
                        }

                    }

                    @Override
                    public void onFindReachableAddress(final String address) {
                        Log.d(MainActivity.TAG, "find reachable address "
                                + address);
                        handler.onFindReachableAddress(address);
                    }



                    @Override
                    public void onTestFindPICompleted(SSHConfig config) {
                        // TODO Auto-generated method stub

                    }
                });
                sub.start();
                Log.d(MainActivity.TAG, "start sub-thread discover fromr " + i
                        + ", count =" + countForEach);
            }
        }
    }

    public synchronized void addListOfReachableAddress(String address) {
        this.listOfReachableAddress.add(address);
    }

    public synchronized String removeListOfReachableAddress(int index) {
        return this.listOfReachableAddress.remove(index);
    }

    public synchronized ArrayList<String> getListOfReachableAddress() {
        return this.listOfReachableAddress;
    }

    public synchronized void addListOfPIAddress(String address) {
        this.listOfPiAddress.add(address);
    }

    public synchronized ArrayList<String> getListOfPIAddress() {
        return this.listOfPiAddress;
    }

    // This must be run in one operation
    private synchronized int increaseNumOfFinishedSubThreads() {
        return ++numOfFinishedSubThreads;
    }

    // don't use this to check
    private synchronized int numOfFinishedSubThreads() {
        return numOfFinishedSubThreads;
    }

    private void getAddressAndShowUserpassDialog() {
        if (getListOfReachableAddress().size() > 0)
            showUsernamePasswordDialog(removeListOfReachableAddress(0));
        else {
            // //////////////////FINISHED////////////////////////
            Log.d(MainActivity.TAG, "scan completed, sshconfigs: ");
            for (SSHConfig config : listOfSSHConfigs) {
                Log.d(MainActivity.TAG, config.username + ", "
                        + config.password);
            }

            if (handler != null)
                handler.onFinished();
        }
    }

    private void createUserpassDialog()
    {
        userpassDialog = new Dialog(activity);
        userpassDialog.setContentView(R.layout.dialog_userpassword);
        userpassDialog.setCancelable(false);




        /////////////TESTING//////////////////








        ////////////////////////////////////////
        //ListView listView = (ListView)userpassDialog.findViewById(R.id.listView);
        //listView.addI

        final EditText userText = (EditText) userpassDialog
                .findViewById(R.id.userText);
        userText.setText("pi");

        userText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    userpassDialog.getWindow()
                            .setSoftInputMode(
                                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        final EditText passText = (EditText) userpassDialog
                .findViewById(R.id.passText);
        passText.setText("raspberry");
        final CheckBox checkBox = (CheckBox) userpassDialog
                .findViewById(R.id.useSameUserPassCheckBox);
        useSameUserAndPass = checkBox.isChecked();
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                useSameUserAndPass = checkBox.isChecked();

            }
        });
        //SSHConfig testConfig = new SSHConfig();
        //testConfig.clientAdddress = address;
        Button okBtn = (Button) userpassDialog.findViewById(R.id.okBtn);
        //userpassDialog.setTitle("Machine " + address + " login:");
        //Log.d(MainActivity.TAG, "SHOW DIALOG!!!!!!");
        //dialog.show();

        okBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (userText.getText().toString().length() == 0)
                    return;

                userpassDialog.hide();
                userpassDialogClickOK(userText.getText().toString(), passText.getText().toString());


            }
        });
    }

    private void userpassDialogClickOK(String user, String pass)
    {
        SSHConfig testConfig = new SSHConfig();
        testConfig.username = user;
        testConfig.password = pass;
        testConfig.clientAdddress = currentTestAddress;
        //listOfSSHConfigs.add(testConfig);
        startSSHTest(testConfig);
        getAddressAndShowUserpassDialog();
    }


    //SHOW DIALOG FOR THE GIVEN ADDRESS
    private void showUsernamePasswordDialog(final String address) {
        currentTestAddress = address;
        if (useSameUserAndPass && listOfSSHConfigs.size() > 0)
        {
            SSHConfig testConfig = new SSHConfig();
            testConfig.username = listOfSSHConfigs.get(listOfSSHConfigs.size() - 1).username;
            testConfig.password = listOfSSHConfigs.get(listOfSSHConfigs.size() - 1).password;
            testConfig.clientAdddress = address;
            //listOfSSHConfigs.add(testConfig);
            startSSHTest(testConfig);
            getAddressAndShowUserpassDialog();

        }else
        {
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    if (activity.isFinishing())
                        return;
                    //if (!useSameUserAndPass)
                    userpassDialog.setTitle(address);
                    Log.d(MainActivity.TAG, "SHOW DIALOG!");
                    userpassDialog.show();
                }
            };
            activity.runOnUiThread(myRunnable);
        }

    }


    private void startSSHTest(final SSHConfig testConfig) {
        listOfSSHConfigs.add(testConfig);
        SSHThread sshThread = new SSHThread(true, testConfig);
        sshThread.setHandler(new SSHEventHandler() {

            @Override
            public void onConnected(String clientName) {

                // listOfPiAddress.add(clientName);
                addListOfPIAddress(clientName);
                Log.d(MainActivity.TAG, "Found PI at " + clientName);
                testConfig.canConnect = true;
                handler.onTestFindPICompleted(testConfig);

            }

            @Override
            public void onRunConfigCompleted(SSHThread sshThread) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onExceptionOccur(String clientName) {
                Log.d(MainActivity.TAG, "address " + clientName +" cannot connect SSH");
                testConfig.canConnect = false;
                handler.onTestFindPICompleted(testConfig);


            }

        });
        listOfCreatedThreads.add(sshThread);
        sshThread.start();
    }

    public void dispose() {
        Log.d(MainActivity.TAG, "dispose NetworkDiscover thread");

        for (MyThread thread : listOfCreatedThreads) {
            try {
                thread.dispose();// test this to see if thread is stop;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // example: 192.168.100.5 -> 192.168.100
    public String getThreePartsOfIp(String address) {
        String result = "";
        try {
            result = address.substring(0, address.lastIndexOf("."));
        } catch (Exception ex) {

        }
        return result;

    }

    // example: 192.168.100.5 -> 5
    public int getLastNumberOfIp(String address) {
        int num = 0;
        try {
            num = Integer
                    .parseInt(address.substring(address.lastIndexOf(".") + 1));
        } catch (Exception ex) {

        }
        return num;
    }

    class SubThread extends MyThread {
        ArrayList<String> listOfAddresses;
        int start;
        int count;
        EventHandler handler;

        public SubThread(ArrayList<String> listOfAddresses, int start, int count) {
            this.listOfAddresses = listOfAddresses;
            this.start = start;
            this.count = count;
        }

        public void setHandler(EventHandler handler) {
            this.handler = handler;
        }

        public void run() {

            Log.d(MainActivity.TAG, "sub thread run from " + start + ", "
                    + listOfAddresses.size());
            for (int i = start; i < start + count; i++) {
                if (isStop)
                    break;
                try {
                    if (listOfAddresses.size() > i) {
                        String address = listOfAddresses.get(i);
                        InetAddress addr = InetAddress.getByName(address);

                        boolean isReachable = addr.isReachable(timeOut);
                        Log.d(MainActivity.TAG, address + " is reachable "
                                + isReachable);
                        if (isReachable) {
                            // listOfReachableAddress.add(address);
                            addListOfReachableAddress(address);
                            if (this.handler != null)
                                this.handler.onFindReachableAddress(address);
                        }
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (this.handler != null)
                this.handler.onFinished();
        }

    }

    interface EventHandler {
        void onFinished();

        void onFindReachableAddress(String address);

        void onTestFindPICompleted(SSHConfig config);
    }

    // This Class uses SSH to copy .jar file, config wifi, .... for specific
    // address
    static class SSHThread extends MyThread {
        // String clientName;
        SSHConfig config;
        Session sess;
        Connection conn;
        ArrayList<String> listOfCommands = new ArrayList<String>();
        boolean isTest;
        SSHEventHandler handler;
        static byte[] appClientData;
        static byte[] hostapdData1;//for EW-7811 UN
        static byte[] debData1;//for running hostapd for EW-7811 UN
        static byte[] wifiAPConfigData1;//for EW-7811 UN
        static byte[] wifiConfig;//this is the same for all 'wlan0'
        static boolean resourceLoaded = false;
        String remainCommand = "";


        public SSHThread(boolean isTestConnection, SSHConfig config) {
            this.isTest = isTestConnection;
            // this.clientName = clientName;
            this.config = config;
            conn = new Connection(config.clientAdddress);

            if (!isTestConnection) {
                createListOfCommandsFromConfig(config);
            }
        }

        public void addRemainCommand(String command)
        {
            remainCommand = command;
        }
        private void createListOfCommandsFromConfig(SSHConfig config) {
            if (config.installJava) {
                //listOfCommands.add("ls");
            }

            listOfCommands.add("mv /usr/bin/raspi-config /usr/bin/raspi-config.bak");
            listOfCommands.add("sed -i.bak \"s|exec /usr/bin/X -nolisten tcp \"$@\"|exec /usr/bin/X -s 0 dpms -nolisten tcp \"$@\"|\" /etc/X11/xinit/xserverrc");

            if (config.configWifi) {
                if (config.isWifiAccessPoint)
                {
                    listOfCommands.add("apt-get -y purge isc-dhcp-server");
                    listOfCommands.add("copyHostapd");
                    listOfCommands.add("unzip -o hostapd.zip");
                    listOfCommands.add("mv hostapd /usr/sbin/hostapd");
                    listOfCommands.add("chown root.root /usr/sbin/hostapd");
                    listOfCommands.add("chmod 755 /usr/sbin/hostapd");


                    listOfCommands.add("copydeb");
                    listOfCommands.add("unzip -o deb.zip");
                    listOfCommands.add("dpkg -i deb/*.deb");

                    listOfCommands.add("copyWifiAPConfig");
                    listOfCommands.add("unzip -o wifi_ap_config.zip");
                    listOfCommands.add("mkdir /etc/hostapd");
                    listOfCommands.add("cp hostapd.conf /etc/hostapd/hostapd.conf");
                    listOfCommands.add("cp interfaces /etc/network/interfaces");
                    listOfCommands.add("cp dhcpd.conf /etc/dhcp/dhcpd.conf");

                    //change ssid & key of Access Point
                    listOfCommands.add("sed -i.bak 's/DaveConroyPi/"+config.accessPointSSID+"/' /etc/hostapd/hostapd.conf");
                    listOfCommands.add("sed -i.bak 's/thisislongakey/"+config.accessPointKey+"/' /etc/hostapd/hostapd.conf");

                    listOfCommands.add("echo 'DAEMON_CONF=\"/etc/hostapd/hostapd.conf\"' > /etc/default/hostapd");
                }
                else
                {
                    listOfCommands.add("copyWifiConfig");
                    listOfCommands.add("unzip -o wifi_config.zip");

                    listOfCommands.add("cp interfaces /etc/network/interfaces");
                    listOfCommands.add("cp wpa_supplicant.conf /etc/wpa_supplicant/wpa_supplicant.conf");

                    listOfCommands.add("sed -i.bak 's/DaveConroyPi/"+config.accessPointSSID+"/' /etc/hostapd/hostapd.conf");
                    listOfCommands.add("sed -i.bak 's/thisislongakey/"+config.accessPointKey+"/' /etc/hostapd/hostapd.conf");

                    //remove hosapd & dhcp server if exist
                    listOfCommands.add("rm /usr/sbin/hostapd");
                    //listOfCommands.add("rm /etc/init.d/isc-dhcp-server");
                    listOfCommands.add("apt-get -y purge isc-dhcp-server");



                }
            }
            if (config.runAtStartup)
            {
                //test remove raspi-config



                //listOfCommands.add("echo 'su -s /bin/bash -c startx your_user&' >> /etc/rc.local");
                listOfCommands.add("cat /etc/rc.local > /etc/temp.rc.local");
                listOfCommands.add("sed -i.bak 's/exit 0//' /etc/temp.rc.local");
                listOfCommands.add("sed -i.bak \"s|su -s /bin/bash -c startx "+config.username+"&||\" /etc/temp.rc.local");
                listOfCommands.add("sed -i.bak \"s|sudo hostapd -B /etc/hostapd/hostapd.conf||\" /etc/temp.rc.local");

                listOfCommands.add("echo 'su -s /bin/bash -c startx "+config.username+"&' >> /etc/temp.rc.local");
                listOfCommands.add("echo 'sudo hostapd -B /etc/hostapd/hostapd.conf' >> /etc/temp.rc.local");
                listOfCommands.add("echo 'exit 0' >> /etc/temp.rc.local");
                listOfCommands.add("cat /etc/temp.rc.local > /etc/rc.local");
                listOfCommands.add("echo 'allowed_users=anybody' > /etc/X11/Xwrapper.config");
                //run .jar file with passed username as argument
                listOfCommands.add("echo -e \"#~"+config.username+"/.xinitrc\n java -jar /home/"+config.username+"/count_down_client/EntryPoint.jar "+config.username+"\" > /home/"+config.username+"/.xinitrc");

            }else
            {
                //revert settings here:
            }
            if (config.copyJarFile) {
                listOfCommands.add("copyClientApp");
            }

            listOfCommands.add("echo configEnd");

        }

        public static void loadResourceFiles(Activity activity) throws IOException {

            InputStream stream = null;
            if (resourceLoaded)
                return;


            if (appClientData == null) {

                stream = activity.getResources().openRawResource(
                        activity.getResources().getIdentifier("count_down_client", "raw",
                                activity.getPackageName()));

                byte[] buffer = new byte[2048];
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                int len = 0;
                while ((len = stream.read(buffer)) > 0) {
                    byteOut.write(buffer, 0, len);
                }

                appClientData = byteOut.toByteArray();
            }
            if (hostapdData1 == null) {
                stream = activity.getResources().openRawResource(
                        activity.getResources().getIdentifier("hostapd", "raw",
                                activity.getPackageName()));

                byte[] buffer = new byte[2048];
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                int len = 0;
                while ((len = stream.read(buffer)) > 0) {
                    byteOut.write(buffer, 0, len);
                }

                hostapdData1 = byteOut.toByteArray();
            }

            if (debData1 == null) {
                stream = activity.getResources().openRawResource(
                        activity.getResources().getIdentifier("deb", "raw",
                                activity.getPackageName()));

                byte[] buffer = new byte[2048];
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                int len = 0;
                while ((len = stream.read(buffer)) > 0) {
                    byteOut.write(buffer, 0, len);
                }

                debData1 = byteOut.toByteArray();
            }

            if (wifiAPConfigData1 == null) {
                stream = activity.getResources().openRawResource(
                        activity.getResources().getIdentifier("wifi_ap_config", "raw",
                                activity.getPackageName()));

                byte[] buffer = new byte[2048];
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                int len = 0;
                while ((len = stream.read(buffer)) > 0) {
                    byteOut.write(buffer, 0, len);
                }

                wifiAPConfigData1 = byteOut.toByteArray();
            }

            if (wifiConfig == null) {
                stream = activity.getResources().openRawResource(
                        activity.getResources().getIdentifier("wifi_config", "raw",
                                activity.getPackageName()));

                byte[] buffer = new byte[2048];
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                int len = 0;
                while ((len = stream.read(buffer)) > 0) {
                    byteOut.write(buffer, 0, len);
                }

                wifiConfig = byteOut.toByteArray();
            }
            resourceLoaded = true;

        }

        public void setHandler(SSHEventHandler handler) {
            this.handler = handler;
        }

        public void run() {
            try {
                conn.connect(null, timeOut, timeOut);

				/*
				 * Authenticate. If you get an IOException saying something like
				 * "Authentication method password not supported by the server at this stage."
				 * then please check the FAQ.
				 */
                boolean isAuthenticated = conn.authenticateWithPassword(
                        config.username, config.password);

                if (isAuthenticated == false) {
                    throw new IOException("Authentication failed.");
                }

                if (handler != null)
                    handler.onConnected(config.clientAdddress);
                System.out.println("SSH connected to " + config.clientAdddress);

				/* Create a session */
                sess = conn.openSession();
                sess.startShell();
                InputStream stderr = sess.getStderr();
                OutputStream stdin = sess.getStdin();

                InputStream stdout = new StreamGobbler(sess.getStdout());
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        stdout));
                ReadThread reader = new ReadThread(stdout);
                reader.setHandler(new ReadEventHandler() {

                    @Override
                    public void onReceived(String line) {
                        if (line.contains("configEnd"))//configEnd will be printed out at the end
                        {
                            if (handler!=null)
                                handler.onRunConfigCompleted(SSHThread.this);
                        }
                    }
                });
                reader.start();

                BufferedReader ebr = new BufferedReader(new InputStreamReader(
                        stderr));
                ReadThread ereader = new ReadThread(stderr);
                ereader.setHandler(new ReadEventHandler() {

                    @Override
                    public void onReceived(String line) {


                    }
                });
                ereader.start();

                String sub_command = "sudo -s\n";
                stdin.write(sub_command.getBytes());

                for (String command : listOfCommands) {
                    if (command.equals("copyClientApp")) {
                        SCPClient scpc = conn.createSCPClient();
                        scpc.put(this.appClientData, "count_down_client.zip",
                                "/home/" + config.username + "/");
                        //unzip force overwrite
                        sub_command = "unzip -o /home/" + config.username
                                + "/count_down_client.zip\n";
                        stdin.write(sub_command.getBytes());

                    } else if (command.equals("copyHostapd"))
                    {
                        SCPClient scpc = conn.createSCPClient();
                        scpc.put(this.hostapdData1, "hostapd.zip",
                                "/home/" + config.username + "/");

                    } else if (command.equals("copydeb"))
                    {
                        SCPClient scpc = conn.createSCPClient();
                        scpc.put(this.debData1, "deb.zip",
                                "/home/" + config.username + "/");

                    } else if (command.equals("copyWifiAPConfig"))
                    {
                        SCPClient scpc = conn.createSCPClient();
                        scpc.put(this.wifiAPConfigData1, "wifi_ap_config.zip",
                                "/home/" + config.username + "/");

                    }else if (command.equals("copyWifiConfig"))
                    {
                        SCPClient scpc = conn.createSCPClient();
                        scpc.put(this.wifiConfig, "wifi_config.zip",
                                "/home/" + config.username + "/");

                    }
                    else {
                        command = command + "\n";
                        stdin.write(command.getBytes());
                    }
                }

                Log.d(MainActivity.TAG, "config done for " + config.clientAdddress);



                if (!this.isTest)
                {
                    synchronized (this)
                    {
                        while (remainCommand == "")
                        {
                            this.wait();
                        }
                        String command = remainCommand + "\n";
                        stdin.write(command.getBytes());
                        Log.d(MainActivity.TAG, "Notify inside ssh thread, continue work " + command);


                    }
                }

            } catch (Exception ex) {
                if (handler!=null)
                    handler.onExceptionOccur(config.clientAdddress);
                ex.printStackTrace();
            }

        }

        public void dispose() {
            super.dispose();
            if (conn != null) {
                sess.close();
                conn.close();
            }
        }
    }

    interface SSHEventHandler {
        void onExceptionOccur(String clientName);
        void onConnected(String clientName);
        //void onRunConfigCompleted();
        void onRunConfigCompleted(SSHThread sshThread);

    }

    class SSHConfig {
        public boolean copyJarFile = true;
        public boolean configWifi = false;
        public boolean isWifiAccessPoint = false;
        //public String accessPointSSID = "CountDownAccessPoint";
        //public String accessPointKey = "123456";

        public String accessPointSSID = "CountDownAccessPoint";
        public String accessPointKey = "123456789";
        public boolean runAtStartup = true;
        public boolean installJava = true;
        public String username = "pi";
        public String password = "raspberry";
        public String clientAdddress;
        public boolean canConnect = false;

    }
}

