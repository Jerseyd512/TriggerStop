package com.example.zack.triggersafe;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //String dialog = "bluetooth available";
    private static final int REQUEST_ENABLE_BT = 1;

    BluetoothAdapter bluetoothAdapter;

    //ArrayList<BluetoothDevice> pairedDeviceArrayList;
    BluetoothConnection BTconnection;
    TextView textInfo, textStatus;
    ListView listViewPairedDevice;
    LinearLayout inputPane;
    EditText inputField;
    Button btnSend;
    FloatingActionButton fab;
    List<String> nameList = new ArrayList<String>();

   // ArrayAdapter<BluetoothDevice> pairedDeviceAdapter;
    //private UUID myUUID;
    //final String UUID_STRING_WELL_KNOWN_SPP =
            //"00001101-0000-1000-8000-00805F9B34FB";
    //ThreadConnectBTdevice myThreadConnectBTdevice;
    //ThreadConnected myThreadConnected;

    private static final String TAG = "BLUETOOTH";

    AlertDialog.Builder builder1;
    AlertDialog alert11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        BTconnection = new BluetoothConnection();

        fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(myThreadConnected!=null){
//                    byte[] bytesToSend = inputField.getText().toString().getBytes();
//                    myThreadConnected.write(bytesToSend);
//                }
////                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
////                    .setAction("Action", null).show();
//        }
//        });
        textInfo = (TextView)findViewById(R.id.info);
        textStatus = (TextView)findViewById(R.id.status);
        listViewPairedDevice = (ListView)findViewById(R.id.pairedlist);

        //inputPane = (LinearLayout)findViewById(R.id.inputpane);
        inputField = (EditText)findViewById(R.id.input);
                    //btnSend = (Button)findViewById(R.id.send);
                    //btnSend.setOnClickListener(new View.OnClickListener(){

                    //            @Override
                    //            public void onClick(View v) {
                    //                if(myThreadConnected!=null){
                    //                    byte[] bytesToSend = inputField.getText().toString().getBytes();
                    //                    myThreadConnected.write(bytesToSend);
                    //                }
                    //            }});



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStart() {
        super.onStart();

        //Turn ON BlueTooth if it is OFF
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        BTconnection.setup();
    }

    public void showToast(final String message)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
//    public void makeClickable(){
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                fab.setVisibility(View.VISIBLE);
//                fab.setClickable(true);
//                fab.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if(myThreadConnected!=null){
//                            byte[] bytesToSend = inputField.getText().toString().getBytes();
//                            myThreadConnected.write(bytesToSend);
//                        }
//                    }
//                });
//            }
//        });
//    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        BTconnection.cancel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode == Activity.RESULT_OK){
                BTconnection.setup();
            }else{
                Toast.makeText(this, "BlueTooth NOT enabled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /*
    ThreadConnectBTdevice:
    Background Thread to handle BlueTooth connecting
    */

}
