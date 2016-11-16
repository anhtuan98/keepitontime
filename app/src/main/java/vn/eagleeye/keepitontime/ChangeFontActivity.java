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

public class ChangeFontActivity extends AppCompatActivity {

    private static String fontSize ="Size 300";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_font);

        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        List<String> list = new ArrayList<String>();
        for(int i =10;i< 800;i+=5)
        {
            list.add("Size " + i);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String str = parent.getItemAtPosition(position).toString();
                fontSize = str;
                System.out.println(str);
                int size = Integer.parseInt(str.substring(5));

                if (MainActivity.handler!=null)
                    MainActivity.handler.setNewFontSize(size);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }

        });
        spinner.setSelection(list.indexOf(fontSize));
        //spinner.set
    }


}

