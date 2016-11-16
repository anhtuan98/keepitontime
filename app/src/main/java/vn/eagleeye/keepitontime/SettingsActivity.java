package vn.eagleeye.keepitontime;


        import java.io.IOException;
        import java.net.InetAddress;
        import java.net.NetworkInterface;
        import java.net.SocketException;
        import java.net.UnknownHostException;
        import java.util.ArrayList;
        import java.util.Enumeration;
        import java.util.List;

        import vn.eagleeye.keepitontime.NetworkDiscover.SSHConfig;
        import vn.eagleeye.keepitontime.NetworkDiscover.SSHThread;

        import android.support.v7.app.AppCompatActivity;
        import android.widget.*;
        import android.support.v7.app.ActionBarActivity;
        import android.support.v7.app.ActionBar;
        import android.support.v4.app.Fragment;
        import android.app.AlertDialog;
        import android.app.Dialog;
        import android.app.ProgressDialog;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.net.NetworkInfo;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
        import android.os.Build;

public class SettingsActivity extends AppCompatActivity {

    ArrayList<String> listOfHostAddress = new ArrayList<String>();
    NetworkDiscover nd;
    TextView statusText;
    Button startScanBtn;
    Button applyDefaultConfigBtn;
    Button applyWifiConfigBtn;
    boolean networkDiscoverFinished = true;

    private int countApplySSHConfig = 0;
    private int countTestThread = 0;

    ProgressDialog progressDialog;
    ArrayList<SSHThread> listOfSSHThreads = new ArrayList<SSHThread>();
    ArrayList<SSHConfig> listOfPiConfigs = new ArrayList<SSHConfig>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        countApplySSHConfig = 0;

        final NetworkDiscover.EventHandler networkHandler = new NetworkDiscover.EventHandler() {

            @Override
            public void onFinished() {

                // This is just finished find reachable address

				/*
				 * Log.d(MainActivity.TAG,
				 * "network discover finished at Settings");
				 * networkDiscoverFinished = true; nd.dispose();
				 * runOnUiThread(new Runnable() {
				 *
				 * @Override public void run() { if
				 * (nd.getListOfPIAddress().size() == 0) { String text =
				 * "Scan finished"; text += ", found " +
				 * nd.getListOfPIAddress().size() + " available PI in network!";
				 * statusText.setText(text); }
				 *
				 * } });
				 */
            }

            @Override
            public void onFindReachableAddress(String address) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        String message = "found "
                                + nd.getListOfReachableAddress().size()
                                + " possible machines";
                        statusText.setText(message);
                        progressDialog.setMessage(message);
                    }

                });
            }


            @Override
            public void onTestFindPICompleted(SSHConfig config) {
                if (config.canConnect)
                    listOfPiConfigs.add(config);
                if (increaseCountTestThread() >= nd.listOfSSHConfigs.size()) {
                    //SCANNING COMPLETED
                    networkDiscoverFinished = true;
                    nd.dispose();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            String text = "Scan finished";
                            text += ", found " + nd.getListOfPIAddress().size()
                                    + " available PI(s) in network!";
                            statusText.setText(text);
                            progressDialog.setMessage(text);
                            progressDialog.dismiss();

                        }
                    });
                }

            }
        };

        statusText = (TextView) findViewById(R.id.statusText);
        startScanBtn = (Button) findViewById(R.id.startScanBtn);
        startScanBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(MainActivity.TAG, "nd is finished "
                        + networkDiscoverFinished);
                if (networkDiscoverFinished) {
                    getIpAddress();
                    nd = new NetworkDiscover(listOfHostAddress,
                            SettingsActivity.this);
                    nd.setHandler(networkHandler);
                    nd.start();
                    networkDiscoverFinished = false;
                    listOfPiConfigs.clear();
                    progressDialog = ProgressDialog.show(SettingsActivity.this,
                            "Scanning PI...", "", true);
                    progressDialog.setCancelable(true);
                    progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Log.d(MainActivity.TAG, "CANCEL PROGRESS DIALOG");
                            statusText.setText("Scanning aborted!");
                            networkDiscoverFinished = true;
                            nd.dispose();

                        }
                    });

                }

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        statusText.setText("scanning");
                    }
                });
            }
        });

        applyDefaultConfigBtn = (Button) findViewById(R.id.defaultConfigBtn);
        applyDefaultConfigBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (nd != null) {

                    if (nd.listOfSSHConfigs.size() > 0)
                        progressDialog = ProgressDialog.show(SettingsActivity.this,
                                "Applying default config...", "", true);



                    for (SSHConfig config : nd.listOfSSHConfigs) {
                        if (!config.canConnect)
                            continue;
                        progressDialog.setMessage("applying config for "
                                + config.clientAdddress);
                        applySSHConfigFor(config,
                                new NetworkDiscover.SSHEventHandler() {

                                    @Override
                                    public void onRunConfigCompleted(
                                            final SSHThread ssh) {
                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                progressDialog
                                                        .setMessage("apply config successfully for "
                                                                + ssh.config.clientAdddress);

                                            }
                                        });

                                        if (increaseCountApplySSHConfig() >= nd
                                                .getListOfPIAddress().size()) {
                                            runOnUiThread(new Runnable() {

                                                @Override
                                                public void run() {
                                                    progressDialog.dismiss();
                                                }
                                            });
                                            // config completed for all machines
                                            Log.d(MainActivity.TAG,
                                                    "config completed for all "
                                                            + nd.getListOfPIAddress()
                                                            .size()
                                                            + " machine");
                                            showAlertDialog(
                                                    "Reboot PIs",
                                                    "Reboot PIs for the changes to take effect?",
                                                    "OK",
                                                    "Cancel",
                                                    new DialogInterface.OnClickListener() {

                                                        @Override
                                                        public void onClick(
                                                                DialogInterface dialog,
                                                                int which) {
                                                            try {

                                                                synchronized (ssh) {
                                                                    ssh.addRemainCommand("reboot");
                                                                    ssh.notify();
                                                                }
                                                            } catch (Exception ex) {

                                                            }

                                                        }
                                                    },
                                                    new DialogInterface.OnClickListener() {

                                                        @Override
                                                        public void onClick(
                                                                DialogInterface dialog,
                                                                int which) {
                                                            // TODO
                                                            // Auto-generated
                                                            // method stub

                                                        }
                                                    });
                                        }

                                    }

                                    @Override
                                    public void onConnected(String clientName) {
                                        // TODO Auto-generated method stub

                                    }

                                    @Override
                                    public void onExceptionOccur(
                                            String clientName) {
                                        // TODO Auto-generated method stub

                                    }
                                });
                    }
                }

            }
        });



        applyWifiConfigBtn = (Button) findViewById(R.id.wifiConfigBtn);
        applyWifiConfigBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (nd != null) {


                    //Show the list to choose the address that become an Access Point
                    //The others will connect to that Access Point
                    //if (nd.listOfSSHConfigs.size() > 0)
                    showChooseAccessPointDialog();
                }

            }
        });


        Button backToClockBtn = (Button) findViewById(R.id.backToClockBtn);
        backToClockBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (nd != null) {
                    finish();
                }

            }
        });

		/*
		 * TestThread t = new TestThread(); t.start();
		 */
        //getIpAddress();

        nd = new NetworkDiscover(listOfHostAddress, this);

        nd.setHandler(networkHandler);

    }

    void showChooseAccessPointDialog()
    {
        final Dialog dialog = new Dialog(this);

        dialog.setTitle("Choose a hotspot");
        dialog.setContentView(R.layout.dialog_choose_access_point);

        Spinner spinner = (Spinner)dialog.findViewById(R.id.ipChooseSpinner);
        List<String> list = new ArrayList<String>();
        for (SSHConfig config : this.listOfPiConfigs)
        {
            config.configWifi = true;
            config.isWifiAccessPoint = false;
            list.add(config.clientAdddress);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                for (SSHConfig config : listOfPiConfigs)
                {
                    config.isWifiAccessPoint = false;
                }
                String address = parent.getItemAtPosition(position).toString();
                listOfPiConfigs.get(position).isWifiAccessPoint = true;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }

        });
        if (list.size()>0)
        {
            spinner.setSelection(0);
        }


        Button okBtn = (Button)dialog.findViewById(R.id.okBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //Start Wifi config here
                dialog.dismiss();
                startWifiConfig();
            }
        });

        dialog.show();

    }


    void startWifiConfig()
    {
        if (listOfPiConfigs.size()>0)
            progressDialog = ProgressDialog.show(SettingsActivity.this,
                    "Applying wifi config...", "", true);



        for (SSHConfig config : listOfPiConfigs) {

            progressDialog.setMessage("applying config for "
                    + config.clientAdddress);
            applySSHConfigFor(config,
                    new NetworkDiscover.SSHEventHandler() {

                        @Override
                        public void onRunConfigCompleted(
                                final SSHThread ssh) {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    progressDialog
                                            .setMessage("apply config successfully for "
                                                    + ssh.config.clientAdddress);

                                }
                            });

                            if (increaseCountApplySSHConfig() >= nd
                                    .getListOfPIAddress().size()) {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                    }
                                });
                                // config completed for all machines
                                Log.d(MainActivity.TAG,
                                        "config completed for all "
                                                + nd.getListOfPIAddress()
                                                .size()
                                                + " machine");
                                showAlertDialog(
                                        "Reboot PIs",
                                        "Reboot PIs for the changes to take effect?",
                                        "OK",
                                        "Cancel",
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                try {

                                                    synchronized (ssh) {
                                                        ssh.addRemainCommand("reboot");
                                                        ssh.notify();
                                                    }
                                                } catch (Exception ex) {

                                                }

                                            }
                                        },
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                // TODO
                                                // Auto-generated
                                                // method stub

                                            }
                                        });
                            }

                        }

                        @Override
                        public void onConnected(String clientName) {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onExceptionOccur(
                                String clientName) {
                            // TODO Auto-generated method stub

                        }
                    });
        }
    }
	/*public void onBackPressed() {
		super.onBackPressed();
		try {
			progressDialog.dismiss();
			statusText.setText("operation aborted!");
			networkDiscoverFinished = true;
			nd.dispose();
		} catch (Exception ex) {
			ex.printStackTrace();

		}
	}*/

    private void showAlertDialog(final String title, final String message,
                                 final String leftBoxText, final String rightBoxText,
                                 final DialogInterface.OnClickListener leftClickListener,
                                 final DialogInterface.OnClickListener rightClickListener) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        SettingsActivity.this);

                // set title
                alertDialogBuilder.setTitle(title);

                // set dialog message
                alertDialogBuilder
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton(leftBoxText,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                        if (leftClickListener != null)
                                            leftClickListener.onClick(dialog,
                                                    id);

                                    }
                                })
                        .setNegativeButton(rightBoxText,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        // if this button is clicked, just close
                                        // the dialog box and do nothing
                                        dialog.cancel();
                                        if (rightClickListener != null)
                                            rightClickListener.onClick(dialog,
                                                    id);
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

            }
        });

    }

    private synchronized int increaseCountApplySSHConfig() {
        return ++countApplySSHConfig;
    }

    private synchronized int increaseCountTestThread() {
        return ++countTestThread;
    }

    // Run SSH to config the PI for specific SSHConfig
    private void applySSHConfigFor(final SSHConfig config,
                                   final NetworkDiscover.SSHEventHandler handler) {



        Runnable runnable = new Runnable() {

            @Override
            public void run() {

                try {
                    SSHThread.loadResourceFiles(SettingsActivity.this);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                SSHThread ssh = new SSHThread(false, config);
                listOfSSHThreads.add(ssh);

                ssh.setHandler(handler);
                ssh.start();

            }
        };
        Thread trun = new Thread(runnable);
        trun.start();

    }

    private String getIpAddress() {
        listOfHostAddress.clear();
        String ip = "";
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
                    } else {
                        // System.out.println("what is this: " +
                        // inetAddress.getHostAddress());
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(SettingsActivity.this,
                    ChangeFontActivity.class);

            // i.putExtra(CheatActivity.EXTRA_ANSWER_IS_TRUE, answerIsTrue);
            startActivity(i);
            return true;
        }
        if (id == R.id.action_settings2) {

            return true;
        }
        if (id == R.id.action_settings3) {

            Intent i = new Intent(SettingsActivity.this, SSHActivity.class);

            startActivity(i);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        nd.dispose();
    }

    class TestThread extends Thread {
        int count = 0;

        public void run() {

            for (int i = 1; i < 255; i++) {
                InetAddress addr;
                try {
                    addr = InetAddress.getByName("192.168.100." + i);
                    boolean ok = addr.isReachable(2000);
                    Log.d(MainActivity.TAG, "address " + addr.getHostName()
                            + " is reachable " + ok);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
			/*
			 * while(true) { Log.d(MainActivity.TAG, "Thread is running " +
			 * count ); count++; try { Thread.sleep(1000); } catch
			 * (InterruptedException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); } }
			 */
        }
    }

}
