package vn.eagleeye.keepitontime;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.widget.*;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class MainActivity extends AppCompatActivity {

    public static NetworkHandler handler;
    private EditText timeEditText;
    private Button setBtn;
    private Button startPauseBtn;
    private TextView statusText;
    private TimePicker timePicker;
    private boolean isStart = false;
    public static String clientAddress;
    public static final String TAG = "COUNTDOWNSERVER";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog = ProgressDialog.show(this, "Searching client...", "broadcasting local network");
        progressDialog.setCancelable(true);

        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(i);
        // this.finish();
        statusText = (TextView) findViewById(R.id.statusText);
        statusText.setText("disconnected");
        handler = new NetworkHandler(this);
        handler.setListener(new NetworkListener() {

            @Override
            public void onReceived(Connection connection, byte[] data) {
                // TODO Auto-generated method stub

                String str = new String(data).trim();
                System.out.println("received " + str);
                if (str.contains("second=")) {
                    String totalSecondstr = str.substring(str.lastIndexOf("=") + 1);
                    int totalSecond = Integer.parseInt(totalSecondstr);
                    final int hour = totalSecond / 3600;
                    int minute = totalSecond / 60;
                    final int fminute = minute % 60;
                    final int second = totalSecond % 60;
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            setEditText(hour, fminute, second);

                        }
                    });
                } else if (str.contains("clockIsCounting")) {

                    final String isCounting = str.substring(str
                            .lastIndexOf("=") + 1);
                    Log.d(MainActivity.TAG, "receive clockIsCounting "
                            + isCounting);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (isCounting.equals("true")) {
                                changeStartPauseState(true);

                            } else {
                                changeStartPauseState(false);
                            }
                        }
                    });
                }
            }

            @Override
            public void onDisconnected(final String clientAddress, int id) {

                // TODO Auto-generated method stub
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

						/*if (progressDialog!=null)
						{

						}*/
                        progressDialog = ProgressDialog.show(MainActivity.this, "Searching client...", "broadcasting local network");
                        progressDialog.setCancelable(true);
                        statusText
                                .setText("disconnected with " + clientAddress);
                    }
                });

            }

            @Override
            public void onConnected(String clientAddress, int id) {


                MainActivity.clientAddress = clientAddress;
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (progressDialog!=null)
                            progressDialog.dismiss();
                        // TODO Auto-generated method stub
                        statusText.setText("connected to "
                                + MainActivity.clientAddress);
                    }
                });

            }
        });
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setCurrentHour(0);
        timePicker.setCurrentMinute(15);
        setBtn = (Button) findViewById(R.id.setBtn);
        setBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
				/*setEditText(timePicker.getCurrentHour(),
						timePicker.getCurrentMinute(), 0);*/
                changeStartPauseState(false);
                handler.setNewTime(timePicker.getCurrentHour(),
                        timePicker.getCurrentMinute());

            }
        });
        startPauseBtn = (Button) findViewById(R.id.startPauseBtn);
        startPauseBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                changeStartPauseState();
            }
        });
        timeEditText = (EditText) findViewById(R.id.timeEditText);
        timePicker.setIs24HourView(true);
        timePicker
                .setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker timePicker, int i,
                                              int i2) {

                    }
                });
        setEditText(0, 0, 0);

    }

    void changeStartPauseState(boolean start) {
        isStart = start;
        if (isStart) {
            startPauseBtn.setText("Pause");
            handler.startClock();
        } else {
            startPauseBtn.setText("Start");
            handler.pauseClock();
        }
    }

    void changeStartPauseState() {
        isStart = !isStart;
        if (isStart) {
            startPauseBtn.setText("Pause");
            handler.startClock();
        } else {
            startPauseBtn.setText("Start");
            handler.pauseClock();
        }
    }

    private void setEditText(int hour, int minute, int second) {
        String hourStr = String.format("%02d", hour);
        String minuteStr = String.format("%02d", minute);
        String secondStr = String.format("%02d", second);
        timeEditText.setText(hourStr + ":" + minuteStr + ":" + secondStr);
        // handler.setNewTime(hour, minute);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(MainActivity.this, ChangeFontActivity.class);

            // i.putExtra(CheatActivity.EXTRA_ANSWER_IS_TRUE, answerIsTrue);
            startActivity(i);
            return true;
        }
        if (id == R.id.action_settings2) {
			/*
			 * Intent i = new Intent(MainActivity.this, SSHActivity.class);
			 *
			 * startActivity(i);
			 */

            turnOffPI();
            return true;
        }
        if (id == R.id.action_settings3) {

            Intent i = new Intent(MainActivity.this, SSHActivity.class);

            startActivity(i);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void turnOffPI() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to shutdown PI?");
        builder.setNegativeButton(getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.setPositiveButton(getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        handler.turnOff();

                    }
                });
        builder.create().show();
        return;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.dispose();
        // System.out.println("On App destroy");
        Log.d(MainActivity.TAG, "On App destroy");
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
