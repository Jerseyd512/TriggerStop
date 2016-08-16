package com.example.zack.triggersafe;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.Timer;
import java.util.TimerTask;

import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class MainActivity extends AppCompatActivity implements BluetoothConnection.BTListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    //String dialog = "bluetooth available";
    private static final int REQUEST_ENABLE_BT = 1;
    String CONSUMER_KEY = "QvTH9tzR7eswCxDhRuuvElKXL";
    String CONSUMER_SECRET = "2EC438Wyfm9txbkjds6OXA3frwv8j7LBTgjfqmDOEZQgt9T9zR";
    String ACCESS_SECRET = "JPftTKXthAXFla4gO8tHELIsKn6U0vtP8dgAZLsDboiur";
    String ACCESS_TOKEN = "758918404715663360-OwWixpErLXj6oj6oUR3dGX3IOjUJjgI";
    // Consumer
    Twitter twitter;

    // Access Token
    AccessToken accessToken;

    BluetoothAdapter bluetoothAdapter;

    //ArrayList<BluetoothDevice> pairedDeviceArrayList;
    BluetoothConnection BTconnection;
    TextView textInfo, textStatus;
    //ListView listViewPairedDevice;
    //LinearLayout inputPane;
    EditText inputField;
    Button btnUpdate;
    FloatingActionButton fab;
    //List<String> nameList = new ArrayList<String>();
    OfficerList officerList;
    GoogleApiClient mGoogleApiClient;
    GeoLocation loc;
    private static Timer timer;
    private static TimerTask timeout;
    int onboardOfficerVersion;
    boolean updateRunning;

    // ArrayAdapter<BluetoothDevice> pairedDeviceAdapter;
    //private UUID myUUID;
    //final String UUID_STRING_WELL_KNOWN_SPP =
    //"00001101-0000-1000-8000-00805F9B34FB";
    //ThreadConnectBTdevice myThreadConnectBTdevice;
    //ThreadConnected myThreadConnected;
    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
       if (mLastLocation != null) {
          loc = new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude());
       }
   }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void btDataReceived(BluetoothConnection.BTEvent event){
        if(event.getType() == BluetoothConnection.EventType.READY){
            if(event.getMessage().equals("1")){
                Log.d("SYSTEM", "SYSTEM INITIALIZED CORRECTLY");
                try {
                    onboardOfficerVersion = Integer.valueOf(event.getOfficerID());
                }catch(NumberFormatException n){

                }
                //textStatus.setText(Integer.toString(onboardOfficerVersion));
                showToast("Connected to TriggerStop!");
                if(onboardOfficerVersion < officerList.getVersionNumber()){
                    showUpdate("A new officer List is available, the current version is: " + Integer.toString(onboardOfficerVersion) + " and the latest update is: " + Integer.toString(officerList.getVersionNumber()),1);
                }else{
                    showUpdate("Successfully connected to system, officer list up to date", 2);
                }
            }else if(event.getMessage().equals("-1")){
                showToast("Something went wrong");
            }
        }else if(event.getType() == BluetoothConnection.EventType.ACK){
            if(updateRunning){
                Log.d("UPDATE", "still running");
                //timeout.cancel();
                timer.cancel();
                timer.purge();
            }
            timer = new Timer();
            updateRunning = true;

            timer.schedule(new TimerTask(){
                @Override
                public void run() {
                    updateRunning = false;
                    showUpdate("A new officer List is available, the current version is: " + Integer.toString(onboardOfficerVersion) + " and the latest update is: " + Integer.toString(officerList.getVersionNumber()),1);
                    showToast("Update timed out, please try again");
                    // Your database code here
                }
            },3*1000);
            try {
                Thread.sleep(500);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            int progress = BTconnection.updateOfficerAck(event.getOfficerID());
            if(progress == 0){
                showUpdate("Officer list update complete!", 2);
                timer.cancel();
                timer.purge();
                updateRunning = false;
                try {
                    Thread.sleep(1000);                 //1000 milliseconds is one second.
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                hideUpdate();
            }else if(progress == -1){
                showUpdate("A new officer List is available, the current version is: " + Integer.toString(onboardOfficerVersion) + " and the latest update is: " + Integer.toString(officerList.getVersionNumber()),1);
                showToast("Update failed, please try again");
            }else{
                showUpdate("Updating: " + Integer.toString(progress) + " of " + officerList.getCount() + " items sent!", 2);

                showUpdate("Officer list update complete!", 2);
                timer.cancel();
                timer.purge();
                updateRunning = false;
                try {
                    Thread.sleep(500);                 //1000 milliseconds is one second.
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                hideUpdate();
            }
        }
        else {

            StatusUpdate update = new StatusUpdate("Officer: " + officerList.getOfficerName(event.getOfficerID()) + " : " + event.getType() + "   OfficerID: " + event.getOfficerID() + "(" + + System.currentTimeMillis()+")");
            update.setLocation(loc);
            // Posting Status
            Status status = null;
            try {
                Log.d("LOCATION", loc.toString());
                status = twitter.updateStatus(update);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            showToast(event.getType().toString() + event.getMessage());
        }
    }
    private static final String TAG = "BLUETOOTH";

    AlertDialog.Builder builder1;
    AlertDialog alert11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
        accessToken = new AccessToken(ACCESS_TOKEN, ACCESS_SECRET);
        twitter.setOAuthAccessToken(accessToken);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        BTconnection = new BluetoothConnection();

        Officer[] officers = new Officer[6];
        officers[0] = new Officer("Bob", "2208127060");
        officers[1] = new Officer("Bill", "1774460217");
        officers[2] = new Officer("Jeb", "1110418396");
        officers[3] = new Officer("Tim", "1158641699");
        officers[4] = new Officer("Ryan", "2372790555");
        officers[5] = new Officer("Dave", "2372790555");
        officerList = new OfficerList(2,officers);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        textInfo = (TextView)findViewById(R.id.info);
        textStatus = (TextView)findViewById(R.id.status);
        //listViewPairedDevice = (ListView)findViewById(R.id.pairedlist);
        btnUpdate = (Button)findViewById(R.id.update);
        //inputPane = (LinearLayout)findViewById(R.id.inputpane);
        //inputField = (EditText)findViewById(R.id.input);
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
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    @Override
    protected void onStart() {
        super.onStart();

        if(BTconnection.setup(this) == -1){
            showToast("TriggerStop system not paired, please pair to device");
            Intent intentOpenBluetoothSettings = new Intent();
            intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(intentOpenBluetoothSettings);
        }
        mGoogleApiClient.connect();
        //makeClickable();
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
    public void showUpdate(final String msg, final int mode){
        //showToast("Current");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textStatus.setText(msg);
                if(mode == 1) {
                    btnUpdate.setVisibility(View.VISIBLE);
                    btnUpdate.setClickable(true);
                    btnUpdate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            updateList();
                        }
                    });
                }else if(mode == 2){
                    btnUpdate.setVisibility(View.INVISIBLE);
                    btnUpdate.setClickable(false);
                }
            }
        });
    }
    public void hideUpdate(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textStatus.setText("");
                btnUpdate.setVisibility(View.INVISIBLE);
                btnUpdate.setClickable(false);
            }
        });
    }
    public void updateList(){
        BTconnection.updateOfficerList(officerList);
    }
    public void makeClickable(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fab.setVisibility(View.VISIBLE);
                fab.setClickable(true);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(inputField.getText().toString().equals("")){
//
//
//                            StatusUpdate update = new StatusUpdate(inputField.getText().toString());
//                            update.setLocation(loc);
//                            // Posting Status
//                            Status status = null;
//                            try {
//                                Log.d("LOCATION", loc.toString());
//                                status = twitter.updateStatus(update);
//                            } catch (TwitterException e) {
//                                e.printStackTrace();
//                            }
//                            Log.d("TWEET","Successfully updated the status: "
//                                    + status.getText());
                            //BTconnection.updateOfficerList(officerList);
                        }else {
                            //BTconnection.sendString(inputField.getText().toString());
                        }
                    }
                });
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        BTconnection.cancel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode == Activity.RESULT_OK){
                BTconnection.setup(this);
            }else{
                Toast.makeText(this, "BlueTooth NOT enabled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showToast("Something failed with google play services");
    }

    /*
    ThreadConnectBTdevice:
    Background Thread to handle BlueTooth connecting
    */

}
