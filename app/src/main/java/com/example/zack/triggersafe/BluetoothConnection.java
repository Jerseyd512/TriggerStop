package com.example.zack.triggersafe;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Zack on 8/9/2016.
 */
public class BluetoothConnection {
    private static final int REQUEST_ENABLE_BT = 1;

    BluetoothAdapter bluetoothAdapter;

    ArrayList<BluetoothDevice> pairedDeviceArrayList;
    private String adapterString;

    ArrayAdapter<BluetoothDevice> pairedDeviceAdapter;
    private UUID myUUID;
    final String UUID_STRING_WELL_KNOWN_SPP =
            "00001101-0000-1000-8000-00805F9B34FB";
    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;
    private BTListener listener;
    private static final String TAG = "BLUETOOTH";
    private String ACK;


    public BluetoothConnection(){
        //using the well-known SPP UUID
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            adapterString = bluetoothAdapter.getName() + "\n" + bluetoothAdapter.getAddress();
        }
}

    public int setup(BTListener newListener) {
        if (bluetoothAdapter == null) {
            return 0;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            pairedDeviceArrayList = new ArrayList<BluetoothDevice>();

            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceArrayList.add(device);
                Log.d("BT_DEVICE", device.getName());
                //nameList.add(device.getName());
                if(device.getName().equals("TRIG_SAFE")){
                    Log.d("BT", "Found system!------------ \n \n \n");
                    //textStatus.setText("start ThreadConnectBTdevice"); //------
                    myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
                    myThreadConnectBTdevice.start();
                    listener = newListener;
                    ACK = "";
                    return 1;
                }
            }

//            pairedDeviceAdapter = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1,android.R.id.text1, pairedDeviceArrayList);
//            ArrayAdapter<String> objectArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, nameList);
//            listViewPairedDevice.setAdapter(objectArrayAdapter);
//            listViewPairedDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    BluetoothDevice device = (BluetoothDevice) pairedDeviceArrayList.get(nameList.indexOf(parent.getItemAtPosition(position)));
//                    Toast.makeText(MainActivity.this,
//                            "Name: " + device.getName() + "\n"
//                                    + "Address: " + device.getAddress() + "\n"
//                                    + "BondState: " + device.getBondState() + "\n"
//                                    + "BluetoothClass: " + device.getBluetoothClass() + "\n"
//                                    + "Class: " + device.getClass(),
//                            Toast.LENGTH_LONG).show();
//                    textStatus.setText("start ThreadConnectBTdevice"); //---------------------------------------------------------------
//                    myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
//                    myThreadConnectBTdevice.start();
//                }
//            });
        }
        return -1;
    }
    public void cancel() {
        if(myThreadConnectBTdevice!=null){
            myThreadConnectBTdevice.cancel();
        }
    }
    public int updateOfficerList(OfficerList newList){
        sendString("?ver\n");
        try {
            Thread.sleep(500);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if(ACK.equals(newList.toString())){
            return 1;
        }
        sendString("?newList\n");
        try {
            Thread.sleep(500);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if(ACK.equals("ready")){
            String tagString = newList.getTagString();
            sendString(tagString+'\n');
            try {
                Thread.sleep(3000);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            if(ACK.equals(tagString)){
                Log.d("UPDATE", "SUCCESFUL!! \n\n\n");
                return 1;
            }
        }
        return -1;
    }
    public void sendString(String data){
        if(myThreadConnected!=null){
            byte[] bytesToSend = data.getBytes();
            myThreadConnected.write(bytesToSend);
        }
    }
    //Called in ThreadConnectBTdevice once connect successed
    //to start ThreadConnected
    private void startThreadConnected(BluetoothSocket socket){

        myThreadConnected = new ThreadConnected(socket);
        myThreadConnected.start();
    }

    private class ThreadConnectBTdevice extends Thread {

        private BluetoothSocket bluetoothSocket = null;
        private final BluetoothDevice bluetoothDevice;


        private ThreadConnectBTdevice(BluetoothDevice device) {
            bluetoothDevice = device;

            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
//                textStatus.setText("bluetoothSocket: \n" + bluetoothSocket); //------------------------------------------------------------------------------------
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            boolean success = false;
            try {
                bluetoothSocket.connect();
                success = true;
            } catch (IOException e) {
                e.printStackTrace();

//                final String eMessage = e.getMessage();
//                runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        textStatus.setText("something wrong bluetoothSocket.connect(): \n" + eMessage); //----------------------------------------------------------
//                    }
//                });

//                try {
//                    bluetoothSocket.close();
//                } catch (IOException e1) {
//                    // TODO Auto-generated catch block
//                    e1.printStackTrace();
//                }
                try {
                    Log.e("","trying fallback...");

                    bluetoothSocket =(BluetoothSocket) bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(bluetoothDevice,1);
                    bluetoothSocket.connect();

                    Log.e("","Connected");
                }
                catch (Exception e2) {
                    Log.e("BT", "Couldn't establish Bluetooth connection!");
                    final String eMessage = e.getMessage();
//                    runOnUiThread(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            textStatus.setText("something wrong bluetoothSocket.connect(): \n" + eMessage); //----------------------------------------------------------
//                        }
//                    });
                }
            }

            if(success){
                //connect successful
                final String msgconnected = "connect successful:\n" + "BluetoothSocket: " + bluetoothSocket + "\n" + "BluetoothDevice: " + bluetoothDevice;

//                runOnUiThread(new Runnable(){
//
//                    @Override
//                    public void run() {
//                        textStatus.setText(msgconnected);
//
//                        listViewPairedDevice.setVisibility(View.GONE);
//                        //inputPane.setVisibility(View.VISIBLE);
//                    }});

                startThreadConnected(bluetoothSocket);
            }else{
                //fail
            }
        }

        public void cancel() {

//            Toast.makeText(getApplicationContext(), "close bluetoothSocket",  Toast.LENGTH_LONG).show();

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }
    public enum EventType{
        DRAWN, RAISED, FIRED, ACK
    }
    public class BTEvent{
        private EventType type;
        private String message;
        private String officerID;

        public BTEvent(EventType newType, String newMsg, String oID){
            type = newType;
            message = newMsg;
            officerID = oID;
        }
        public EventType getType(){
            return type;
        }
        public String getMessage(){
            return message;
        }
        public String getOfficerID(){
            return officerID;
        }
    }
    interface BTListener{
        void btDataRecieved(BTEvent event);
    }

    /*
    ThreadConnected:
    Background Thread to handle Bluetooth data communication
    after connected
     */
    private class ThreadConnected extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;
        private String input = "";

        public ThreadConnected(BluetoothSocket socket) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
//            showToast("Connection Successful!");
//            makeClickable();

            while (true) {
                try {
                    bytes = connectedInputStream.read(buffer);
                    if(buffer[bytes-1] == '\n') {
                        String strReceived = new String(buffer, 0, bytes);
                        input += strReceived;
                        Log.d("RECEIVED", input );
                        if(listener != null){
                            String[] split = input.split(":");
                            EventType type;
                            if(split.length == 3){
                                switch(split[0]){
                                    case "drawn":
                                        type = EventType.DRAWN;
                                        listener.btDataRecieved(new BTEvent(type, split[1], split[2]));
                                        break;
                                    case "raised":
                                        type = EventType.RAISED;
                                        listener.btDataRecieved(new BTEvent(type, split[1], split[2]));
                                        break;
                                    case "fired":
                                        type = EventType.FIRED;
                                        listener.btDataRecieved(new BTEvent(type, split[1], split[2]));
                                        break;
                                    case "ack":
                                        ACK = split[1];
                                        break;
                                    default:

                                }

                            }
                           // listener.btDataRecieved(input);
                        }
                        input = "";
                    }else{
                        input += new String(buffer, 0, bytes);
                    }
//                    Log.d("RECEIVED", strReceived );
                    Log.d("RECEIVED", "buffer"+ buffer.toString());
                    Log.d("RECEIVED", "bytes" + bytes);
                    //final String msgReceived = String.valueOf(bytes) + " bytes received:\n" + strReceived;

//                    runOnUiThread(new Runnable(){
//
//                        @Override
//                        public void run() {
//                            textStatus.setText(msgReceived);
//                        }});

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String msgConnectionLost = "Connection lost:\n"
                            + e.getMessage();
//                    runOnUiThread(new Runnable(){
//
//                        @Override
//                        public void run() {
//                            textStatus.setText(msgConnectionLost);
//                        }});
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
