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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

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
    public static final String CONSUMER_KEY = "5EdbsvlOQFR5O16p8jduyKFpX ";
    public static final String CONSUMER_SECRET = "CpED6f6dKKiKgUUWWREjELUdlPvOOPZU63M4n49VgtAfX3Fjic";

    public static final String REQUEST_URL = "http://api.twitter.com/oauth/request_token";
    public static final String ACCESS_URL = "http://api.twitter.com/oauth/access_token";
    public static final String AUTHORIZE_URL = "http://api.twitter.com/oauth/authorize";

    final public static String CALLBACK_SCHEME = "x-latify-oauth-twitter";
    final public static String CALLBACK_URL = CALLBACK_SCHEME + "://callback";
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
    OfficerList officerList;
    GoogleApiClient mGoogleApiClient;
    GeoLocation loc;

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

    public void btDataRecieved(BluetoothConnection.BTEvent event){
        showToast(event.getType().toString() + event.getMessage());
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        BTconnection = new BluetoothConnection();
        Officer[] officers = new Officer[6];
        officers[0] = new Officer("Bob", "0123");
        officers[1] = new Officer("Bill", "0124");
        officers[2] = new Officer("Jeb", "0125");
        officers[3] = new Officer("Tim", "0126");
        officers[4] = new Officer("Ryan", "0127");
        officers[5] = new Officer("Dave", "0128");
        officerList = new OfficerList(1,officers);

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


//        String CONSUMER_KEY = "QvTH9tzR7eswCxDhRuuvElKXL";
//        String CONSUMER_SECRET = "2EC438Wyfm9txbkjds6OXA3frwv8j7LBTgjfqmDOEZQgt9T9zR";
//        String ACCESS_SECRET = "JPftTKXthAXFla4gO8tHELIsKn6U0vtP8dgAZLsDboiur";
//        String ACCESS_TOKEN = "758918404715663360-OwWixpErLXj6oj6oUR3dGX3IOjUJjgI";
//
//        // Consumer
//        Twitter twitter = new TwitterFactory().getInstance();
//        twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
//
//        // Access Token
//        AccessToken accessToken = null;
//        accessToken = new AccessToken(ACCESS_TOKEN, ACCESS_SECRET);
//        twitter.setOAuthAccessToken(accessToken);
//        StatusUpdate update = new StatusUpdate("Testing Location!");
//        update.setLocation(loc);
//        // Posting Status
//        Status status = null;
//        try {
//            status = twitter.updateStatus("Testing!");
//        } catch (TwitterException e) {
//            e.printStackTrace();
//        }
//        Log.d("TWEET","Successfully updated the status: "
//                + status.getText());
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

//        //Turn ON BlueTooth if it is OFF
//        if (!bluetoothAdapter.isEnabled()) {
//            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
//        }

        if(BTconnection.setup(this) == -1){
            showToast("TriggerStop system not paired, please pair to device");
            Intent intentOpenBluetoothSettings = new Intent();
            intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(intentOpenBluetoothSettings);
        }
        mGoogleApiClient.connect();
        makeClickable();
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
    public void makeClickable(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fab.setVisibility(View.VISIBLE);
                fab.setClickable(true);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!(inputField.getText().toString().equals(""))){
                            String CONSUMER_KEY = "QvTH9tzR7eswCxDhRuuvElKXL";
                            String CONSUMER_SECRET = "2EC438Wyfm9txbkjds6OXA3frwv8j7LBTgjfqmDOEZQgt9T9zR";
                            String ACCESS_SECRET = "JPftTKXthAXFla4gO8tHELIsKn6U0vtP8dgAZLsDboiur";
                            String ACCESS_TOKEN = "758918404715663360-OwWixpErLXj6oj6oUR3dGX3IOjUJjgI";

                            // Consumer
                            Twitter twitter = new TwitterFactory().getInstance();
                            twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);

                            // Access Token
                            AccessToken accessToken = null;
                            accessToken = new AccessToken(ACCESS_TOKEN, ACCESS_SECRET);
                            twitter.setOAuthAccessToken(accessToken);
                            StatusUpdate update = new StatusUpdate(inputField.getText().toString());
                            update.setLocation(loc);
                            // Posting Status
                            Status status = null;
                            try {
                                Log.d("LOCATION", loc.toString());
                                status = twitter.updateStatus(update);
                            } catch (TwitterException e) {
                                e.printStackTrace();
                            }
//                            Log.d("TWEET","Successfully updated the status: "
//                                    + status.getText());
                            //BTconnection.updateOfficerList(officerList);
                        }
                        BTconnection.sendString(inputField.getText().toString());
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
