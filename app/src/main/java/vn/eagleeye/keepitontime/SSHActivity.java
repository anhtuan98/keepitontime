package vn.eagleeye.keepitontime;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import vn.eagleeye.keepitontime.SSH.SSHHandler;

import android.widget.*;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class SSHActivity extends AppCompatActivity {

    private static String fontSize ="Size 200";
    private SSH ssh;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ssh);
        final TextView sshStatusText =(TextView)findViewById(R.id.sshStatusText);
        //if (MainActivity.clientAddress!=null)
        //{
        ssh = new SSH("192.168.100.9");
        ssh.setSSHHandler(new SSH.SSHHandler() {

            @Override
            public void onConnected() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        sshStatusText.setText("connected");

                    }
                });

            }
        });
        ssh.start();
        // }
        final EditText inputText=(EditText)findViewById(R.id.inputText);
        Button runBtn = (Button)findViewById(R.id.runBtn);
        runBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (ssh!=null)
                {
                    ssh.addCommand(inputText.getText().toString());
                }
            }
        });

        Button omxPlayBtn = (Button)findViewById(R.id.omxPlayBtn);

        omxPlayBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ssh!=null)
                {
                    ssh.addCommand("omxplayer -r -p -o hdmi ~/Desktop/chacaidoseve.mp4\n");
                }

            }
        });

        Button omxBtn = (Button)findViewById(R.id.testOmxBtn);

        omxBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ssh!=null)
                {
                    ssh.addCommand("p");
                }

            }
        });

        Button seekBtn = (Button)findViewById(R.id.seekOmxBtn);

        seekBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ssh!=null)
                {
                    ssh.addCommand("\\027[C");
                }

            }
        });

        Button rebootBtn = (Button)findViewById(R.id.rebootBtn);

        rebootBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ssh!=null)
                {
                    ssh.addCommand("sudo reboot");
                }

            }
        });



    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ssh!=null)
            ssh.dispose();
    }


}

