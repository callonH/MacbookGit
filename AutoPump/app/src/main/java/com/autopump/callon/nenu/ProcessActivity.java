package com.autopump.callon.nenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;




public class ProcessActivity extends Activity {
    private final static String TAG = ProcessActivity.class.getSimpleName();
    private static final int SIMPLEPROFILE_CHAR6_LEN = 16;
    public static final String EXTRAS_SYRINGE_VALUE = "SYRINGE_VALUE";
    public static final String EXTRAS_SPEED_VALUE = "SPEED_VALUE";
    public static final String EXTRAS_TUBE_VALUE = "TUBE_VALUE";

    private final int max_tvlength = 4;
    private final int max_dflength = 5;
    private final int max_sslength = 1;

    private boolean mWashstate = false;
    private boolean mRunstate = false;
    private boolean mDatastate = true;
    private boolean mForceStop = false;


    private int Syringe_value = 0;
    private int Speed_value = 0;
    private int Tube_value = 0;

    private int mRunway = 0;
    private SQLiteDatabase db = null;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private String editTV_forest = null;
    private String editDF_forest = null;
    private String editSS_forest = null;

    private EditText edit_TV = null;
    private EditText edit_DF = null;
    private EditText edit_SS = null;
    private EditText edit_AUTO = null;

    private TextView text_diluent = null;
    private TextView text_sample = null;
    private TextView text_injection = null;

    private int TV_value = 0;
    private float DF_value = 0;
    private int SS_value = 0;

    private int text_id = 0;
    private Handler handler = null;
    private Timer timer = null;
    private TimerTask task = null;
    private boolean change = false;

    private RoundProgressBarWidthNumber DiluentBar = null;
    private RoundProgressBarWidthNumber SampleBar = null;
    private RoundProgressBarWidthNumber InjectionBar = null;
    private static final int MSG_PROGRESS_UPDATE = 0x110;

    private byte[] getedit2;

    //private static int testprocess = 50;

    private ImageButton returnButton = null;
    private ImageButton startButton = null;
    private ImageButton washButton = null;
    private final String MyService = "0000fff0-0000-1000-8000-00805f9b34fb";

    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private ArrayList<BluetoothGattCharacteristic> charas =
            new ArrayList<BluetoothGattCharacteristic>();

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {

            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if(mBluetoothLeService != null){
                Log.d(TAG, "connect action");
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                startButton.setOnClickListener(clickListener);
                Log.d(TAG, "service action");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "disconnect action");
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                RunProcessBar(intent);
            }
        }
    };

    public OnClickListener clickListener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            Log.d(TAG, "click action");
            // TODO Auto-generated method stub
            if (mGattCharacteristics != null) {
                if(charas.size()>=6){
                    BluetoothGattCharacteristic characteristic =
                            charas.get(0);
                    BluetoothGattCharacteristic characteristic_write = charas.get(5);
                    int charaProp_write = characteristic_write.getProperties();
                    int charaProp = characteristic.getProperties();
                    //String getUUID = characteristic.getUuid().toString();
                    if (((charaProp_write | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0)
                            &&
                            (characteristic_write != null)
                            &&
                            (mRunstate == true)
                            &&
                            (mDatastate == true)){
                        Log.d(TAG, "send s");
                        if (mNotifyCharacteristic != null) {
                            mBluetoothLeService.setCharacteristicNotification(
                                    mNotifyCharacteristic, false);
                            mNotifyCharacteristic = null;
                        }
                        byte[] writev = new byte[SIMPLEPROFILE_CHAR6_LEN];
                        Arrays.fill(writev, (byte)'0');
                        byte[] getedit1 = edit_TV.getText().toString().getBytes();//edit1.getText().toString().getBytes();
                        getedit2 = edit_DF.getText().toString().getBytes();
                        byte[] getedit3 = edit_SS.getText().toString().getBytes();

                        TV_value = Integer.parseInt(edit_TV.getText().toString());
                        SS_value = Integer.parseInt(edit_SS.getText().toString());
                        if(edit_DF.getText().toString().matches("^((\\d+\\.\\d*[0-9]\\d*)|(\\d*[0-9]\\d*\\.\\d+)|(\\d*[0-9]\\d*))$"))
                            DF_value = Float.parseFloat(edit_DF.getText().toString());
                        else{
                            String subString = edit_DF.getText().toString().substring(1, edit_DF.length());
                            if(subString.matches("^((\\d+\\.\\d*[0-9]\\d*)|(\\d*[0-9]\\d*\\.\\d+)|(\\d*[0-9]\\d*))$")){
                                DF_value = Float.parseFloat(subString);
                            }
                            else{//////??????????
                                DF_value = 0;
                            }
                        }
                        //System.out.println(getedit1 + " " + getedit2 + " " + getedit3);

                        if ((getedit1.length + getedit2.length + getedit3.length) <= (max_tvlength+max_dflength+max_sslength)) {
                            writev[0] = (byte)'s';
                            writev[1] = (byte)(getedit1.length);
                            writev[2] = (byte)(getedit2.length);
                            writev[3] = (byte)(getedit3.length);
                            System.arraycopy(getedit1, 0, writev, 4, getedit1.length);
                            System.arraycopy(getedit2, 0, writev, 4+getedit1.length, getedit2.length);
                            System.arraycopy(getedit3, 0, writev, 4+getedit1.length+getedit2.length, getedit3.length);
                            if(edit_AUTO.getText().toString().equals("ON")){
                                writev[4+getedit1.length+getedit2.length+getedit3.length] = (byte)'1';
                            }
                            else{
                                writev[4+getedit1.length+getedit2.length+getedit3.length] = (byte)'0';
                            }
                            writev[5+getedit1.length+getedit2.length+getedit3.length] = '@';
                        }



                        mForceStop = true;
                        characteristic_write.setValue(writev);
                        characteristic_write.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                        mBluetoothLeService.writeCharacteristic(characteristic_write);
                        stopFlicker();
                        InjectionHandler.removeMessages(MSG_PROGRESS_UPDATE);
                        DiluentHandler.removeMessages(MSG_PROGRESS_UPDATE);
                        SampleHandler.removeMessages(MSG_PROGRESS_UPDATE);



                    }
                    if (((charaProp_write | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0)
                            &&
                            (characteristic_write != null)
                            &&
                            (mRunstate == false)
                            &&
                            (mDatastate == true)
                            ){
                        Log.d(TAG, "send 2");
                        if (mNotifyCharacteristic != null) {
                            mBluetoothLeService.setCharacteristicNotification(
                                    mNotifyCharacteristic, false);
                            mNotifyCharacteristic = null;
                        }
                        byte[] writev = new byte[SIMPLEPROFILE_CHAR6_LEN];
                        Arrays.fill(writev, (byte)'0');
                        byte[] getedit1 = edit_TV.getText().toString().getBytes();//edit1.getText().toString().getBytes();
                        getedit2 = edit_DF.getText().toString().getBytes();
                        byte[] getedit3 = edit_SS.getText().toString().getBytes();
                        //Log.d(TAG, "length:"+getedit2.length + "  "+getedit2[0]+ " "+ getedit2[1]);
                        TV_value = Integer.parseInt(edit_TV.getText().toString());
                        SS_value = Integer.parseInt(edit_SS.getText().toString());
                        if(edit_DF.getText().toString().matches("^((\\d+\\.\\d*[0-9]\\d*)|(\\d*[0-9]\\d*\\.\\d+)|(\\d*[0-9]\\d*))$")){
                            DF_value = Float.parseFloat(edit_DF.getText().toString());
                            Log.d(TAG, "right 0: ");
                        }
                        else{
                            String subString = edit_DF.getText().toString().substring(1, edit_DF.length());
                            Log.d(TAG, "subStrig : "+ subString);
                            if(subString.matches("^((\\d+\\.\\d*[0-9]\\d*)|(\\d*[0-9]\\d*\\.\\d+)|(\\d*[0-9]\\d*))$")){
                                DF_value = Float.parseFloat(subString);
                                Log.d(TAG, "right 1: ");
                            }
                            else{
                                DF_value = 0;
                            }
                        }
                        //System.out.println(getedit1 + " " + getedit2 + " " + getedit3);

                        if ((getedit1.length + getedit2.length + getedit3.length) <= (max_tvlength+max_dflength+max_sslength)) {
                            writev[0] = (byte)'2';
                            writev[1] = (byte)(getedit1.length);
                            writev[2] = (byte)(getedit2.length);
                            writev[3] = (byte)(getedit3.length);
                            System.arraycopy(getedit1, 0, writev, 4, getedit1.length);
                            System.arraycopy(getedit2, 0, writev, 4+getedit1.length, getedit2.length);
                            System.arraycopy(getedit3, 0, writev, 4+getedit1.length+getedit2.length, getedit3.length);
                            if(edit_AUTO.getText().toString().equals("ON")){
                                writev[4+getedit1.length+getedit2.length+getedit3.length] = (byte)'1';
                            }
                            else{
                                writev[4+getedit1.length+getedit2.length+getedit3.length] = (byte)'0';
                            }
                            writev[5+getedit1.length+getedit2.length+getedit3.length] = '@';
                        }


		                	/*
		                	for(int i = 0;i<15;i++){
		                		Log.d(TAG, "is:" + writev[i]);
		                	}
		                	*/

                        characteristic_write.setValue(writev);
                        characteristic_write.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                        mBluetoothLeService.writeCharacteristic(characteristic_write);

                        if(getedit2[0] != '/' && DF_value == 0){
                            mRunway = 4;
                            DiluentBar.setProgress(0);
                            SampleBar.setProgress(0);
                            InjectionBar.setProgress(0);
                            stopFlicker();
                            SampleHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
                            mRunstate = true;
                        }
                        else if(getedit2[0] == '/'){
                            if(DF_value >= 1 || DF_value == 0){
                                mRunway = 5;
                                DiluentBar.setProgress(0);
                                SampleBar.setProgress(0);
                                InjectionBar.setProgress(0);
                                stopFlicker();
                                DiluentHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
                                mRunstate = true;
                            }
                            else if(DF_value < 1 && DF_value > 0){
                                mRunway = 2;
                                DiluentBar.setProgress(0);
                                SampleBar.setProgress(0);
                                InjectionBar.setProgress(0);
                                stopFlicker();
                                DiluentHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
                                mRunstate = true;
                            }
                        }
                        else{
                            if(DF_value >= 1){
                                mRunway = 1;
                                DiluentBar.setProgress(0);
                                SampleBar.setProgress(0);
                                InjectionBar.setProgress(0);
                                stopFlicker();
                                DiluentHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
                                mRunstate = true;
                            }
                            else if (DF_value < 1 && DF_value > 0) {
                                mRunway = 3;
                                DiluentBar.setProgress(0);
                                SampleBar.setProgress(0);
                                InjectionBar.setProgress(0);
                                stopFlicker();
                                SampleHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
                                mRunstate = true;
                            }
                        }




                        Log.d(TAG, "mRunway : "+ mRunway);

                        startButton.setImageResource(R.mipmap.button_stop);
                        mForceStop = false;


                    }


                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mNotifyCharacteristic = characteristic;
                        mBluetoothLeService.setCharacteristicNotification(
                                characteristic, true);
                    }

                }
            }
        }
    };

    private long GetTimeDelay(int speedvalue){
        long delayMillis = 55;
        switch(speedvalue){
            case 0:
                delayMillis = 25;
                break;
            case 1:
                delayMillis = 25;
                break;
            case 2:
                delayMillis = 40;
                break;
            case 3:
                delayMillis = 55;
                break;
            case 4:
                delayMillis = 110;
                break;
            case 5:
                delayMillis = 310;
                break;
            case 6:
                delayMillis = 310;
                break;
            case 7:
                delayMillis = 310;
                break;
            case 8:
                delayMillis = 310;
                break;
            case 9:
                delayMillis = 310;
                break;
            default:
                delayMillis = 55;
                break;
        }
        delayMillis = delayMillis * Integer.parseInt(edit_TV.getText().toString()) / Syringe_value;
        if(mWashstate){
            delayMillis = 25;
        }
        return delayMillis;
    }


    private Handler DiluentHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int progress = DiluentBar.getProgress();

            if (progress >= 100) {
                DiluentHandler.removeMessages(MSG_PROGRESS_UPDATE);

            }
            else{
                long delayMillis = 55;
                delayMillis = GetTimeDelay(Speed_value);

                DiluentBar.setProgress(++progress);
                DiluentHandler.sendEmptyMessageDelayed(MSG_PROGRESS_UPDATE, delayMillis);
            }
        };
    };

    private Handler SampleHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int progress = SampleBar.getProgress();
            if (progress >= 100) {
                SampleHandler.removeMessages(MSG_PROGRESS_UPDATE);

            }
            else{
                long delayMillis = 55;
                delayMillis = GetTimeDelay(SS_value);

                SampleBar.setProgress(++progress);
                SampleHandler.sendEmptyMessageDelayed(MSG_PROGRESS_UPDATE, delayMillis);
            }
        };
    };

    private Handler InjectionHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int progress = InjectionBar.getProgress();

            if (progress >= 100) {
                InjectionHandler.removeMessages(MSG_PROGRESS_UPDATE);

            }
            else{
                long delayMillis = 55;
                delayMillis = GetTimeDelay(Speed_value);

                InjectionBar.setProgress(++progress);
                InjectionHandler.sendEmptyMessageDelayed(MSG_PROGRESS_UPDATE, delayMillis);
            }
        };
    };
    private void RunProcessBar(Intent intent) {
        if(mForceStop == true){
            if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 0){
                DiluentBar.setProgress(100);
                SampleBar.setProgress(100);
                InjectionBar.setProgress(100);
                mForceStop = false;
                startButton.setImageResource(R.mipmap.button_start);
                mRunstate = false;
                if(getedit2[0] == '0' && getedit2.length == 1)
                    text_id = 1;
                else
                    text_id = 0;
                startFlicker();
            }
            InjectionHandler.removeMessages(MSG_PROGRESS_UPDATE);
            DiluentHandler.removeMessages(MSG_PROGRESS_UPDATE);
            SampleHandler.removeMessages(MSG_PROGRESS_UPDATE);
        }
        else{
            if(mRunway == 1){
                Log.d(TAG, "Received Run Command1");
                if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 1){
                    DiluentBar.setProgress(100);
                    text_id = 1;
                    startFlicker();
                    Log.d(TAG, "100%");
                }
                else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 2){
                    SampleHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
                    stopFlicker();
                }
                else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 3){
                    SampleBar.setProgress(100);
                    text_id = 2;
                    startFlicker();
                }
                else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 4){
                    InjectionHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
                    stopFlicker();
                }
                else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 5){
                    InjectionBar.setProgress(100);
                    startButton.setImageResource(R.mipmap.button_start);
                    mRunstate = false;
                    text_id = 0;
                    startFlicker();
                }
                else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 240){
                    DiluentBar.setProgress(0);
                    SampleBar.setProgress(0);
                    InjectionBar.setProgress(0);
                    DiluentHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
                    startButton.setImageResource(R.mipmap.button_stop);
                    stopFlicker();
                    mRunstate = true;
                }
            }
            else if (mRunway == 2) {
                Log.d(TAG, "Received Run Command2"+" "+intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0));
                float increase_num = 0;
                String subString = edit_DF.getText().toString().substring(1, edit_DF.length());
                if(subString.matches("^((\\d+\\.\\d*[0-9]\\d*)|(\\d*[0-9]\\d*\\.\\d+)|(\\d*[0-9]\\d*))$"))
                    increase_num = Float.parseFloat(subString);
                if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 1){
                    DiluentBar.setProgress(100);
                    text_id = 2;
                    startFlicker();
                }
                else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 255){
                    InjectionBar.setProgress(100);
                    DF_value = increase_num;
                    startButton.setImageResource(R.mipmap.button_start);
                    mRunstate = false;
                    text_id = 0;
                    startFlicker();
                }
                else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 240){
                    Log.d(TAG, "Received Run Command2 Restart");
                    DiluentBar.setProgress(0);
                    SampleBar.setProgress(0);
                    InjectionBar.setProgress(0);
                    DiluentHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
                    startButton.setImageResource(R.mipmap.button_stop);
                    stopFlicker();
                    mRunstate = true;
                }
                else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) % 2 == 0){
                    InjectionHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
                    stopFlicker();
                }
                else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) % 2 == 1){
                    Log.d(TAG, "DF is "+DF_value);
                    InjectionBar.setProgress((int)(DF_value*100));
                    DF_value += increase_num;
                    InjectionHandler.removeMessages(MSG_PROGRESS_UPDATE);
                }

            }
            else if (mRunway == 3) {
                Log.d(TAG, "Received Run Command3");
                float increase_num = 0;
                if(edit_DF.getText().toString().matches("^((\\d+\\.\\d*[0-9]\\d*)|(\\d*[0-9]\\d*\\.\\d+)|(\\d*[0-9]\\d*))$"))
                    increase_num = Float.parseFloat(edit_DF.getText().toString());
                if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 1){
                    SampleBar.setProgress(100);
                    text_id = 2;
                    startFlicker();
                }
                else if (intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 255) {
                    InjectionBar.setProgress(100);
                    DF_value = increase_num;
                    startButton.setImageResource(R.mipmap.button_start);
                    mRunstate = false;
                    text_id = 1;
                    startFlicker();
                }
                else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 240){//???????????2==0???2==1???????????????????????
                    DiluentBar.setProgress(0);
                    SampleBar.setProgress(0);
                    InjectionBar.setProgress(0);
                    SampleHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
                    startButton.setImageResource(R.mipmap.button_stop);
                    stopFlicker();
                    mRunstate = true;
                }
                else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) % 2 == 0){
                    InjectionHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
                    stopFlicker();
                }
                else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) % 2 == 1){
                    InjectionBar.setProgress((int)(DF_value*100));
                    DF_value += increase_num;
                    InjectionHandler.removeMessages(MSG_PROGRESS_UPDATE);
                }

            }
            else if(mRunway == 4){
                if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 1){
                    SampleBar.setProgress(100);
                    text_id = 2;
                    startFlicker();
                }
                else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 2){
                    InjectionHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
                    stopFlicker();
                }
                else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 3){
                    InjectionBar.setProgress(100);
                    startButton.setImageResource(R.mipmap.button_start);
                    mRunstate = false;
                    text_id = 1;
                    startFlicker();
                }
                if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 240){
                    DiluentBar.setProgress(0);
                    SampleBar.setProgress(0);
                    InjectionBar.setProgress(0);
                    SampleHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
                    startButton.setImageResource(R.mipmap.button_stop);
                    stopFlicker();
                    mRunstate = true;
                }
            }
            else if(mRunway == 5){
                if(DF_value == 0){
                    if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 1){
                        DiluentBar.setProgress(100);
                        text_id = 2;
                        startFlicker();
                    }
                    else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 2){
                        InjectionHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
                        stopFlicker();
                    }
                    else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 3){
                        InjectionBar.setProgress(100);
                        startButton.setImageResource(R.mipmap.button_start);
                        mRunstate = false;
                        mWashstate = false;
                        text_id = 0;
                        startFlicker();
                    }
                    if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 240){
                        DiluentBar.setProgress(0);
                        SampleBar.setProgress(0);
                        InjectionBar.setProgress(0);
                        DiluentHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
                        startButton.setImageResource(R.mipmap.button_stop);
                        stopFlicker();
                        mRunstate = true;
                    }
                }
                else {
                    if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 1){
                        DiluentBar.setProgress(100);
                        text_id = 1;
                        startFlicker();
                    }
                    else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 2){
                        SampleHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
                        stopFlicker();
                    }
                    else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 3){
                        SampleBar.setProgress(100);
                        text_id = 2;
                        startFlicker();
                    }
                    else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 4){
                        InjectionHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
                        stopFlicker();
                    }
                    else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 5){
                        InjectionBar.setProgress(100);
                        startButton.setImageResource(R.mipmap.button_start);
                        mRunstate = false;
                        text_id = 0;
                        startFlicker();
                    }
                    if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 240){
                        DiluentBar.setProgress(0);
                        SampleBar.setProgress(0);
                        InjectionBar.setProgress(0);
                        DiluentHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
                        startButton.setImageResource(R.mipmap.button_stop);
                        stopFlicker();
                        mRunstate = true;
                    }
                }
            }
            else{//mRunway incorrect
                DiluentBar.setProgress(0);
                SampleBar.setProgress(0);
                InjectionBar.setProgress(0);
            }
        }
    }

    private OnClickListener washClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (mGattCharacteristics != null) {
                if(charas.size()>=6){
                    BluetoothGattCharacteristic characteristic =
                            charas.get(0);
                    BluetoothGattCharacteristic characteristic_write = charas.get(5);
                    int charaProp_write = characteristic_write.getProperties();
                    int charaProp = characteristic.getProperties();
                    //String getUUID = characteristic.getUuid().toString();
                    if (((charaProp_write | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0)
                            &&
                            (characteristic_write != null)
                            &&
                            (mRunstate == false)){

                        byte[] writev = new byte[SIMPLEPROFILE_CHAR6_LEN];
                        Arrays.fill(writev, (byte)'0');

                        writev[0] = (byte)'w';
                        writev[1] = (byte)0x04;//important
                        writev[2] = (byte)0x02;//important
                        writev[3] = (byte)0x01;//important

                        writev[4] = (byte)'5';
                        writev[5] = (byte)'0';
                        writev[6] = (byte)'0';
                        writev[7] = (byte)'0';
                        writev[8] = (byte)'/';
                        writev[9] = (byte)'0';
                        writev[10] = (byte)'1';
                        writev[11] = (byte)'1';
                        writev[12] = (byte)'@';
                        //System.out.println(getedit1 + " " + getedit2 + " " + getedit3);

	                	/*
	                	for(int i = 0;i<15;i++){
	                		Log.d(TAG, "is:" + writev[i]);
	                	}
	                	*/

                        characteristic_write.setValue(writev);
                        characteristic_write.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                        mBluetoothLeService.writeCharacteristic(characteristic_write);

                        DF_value = 0;


                        mWashstate = true;
                        mRunway = 5;
                        DiluentBar.setProgress(0);
                        SampleBar.setProgress(0);
                        InjectionBar.setProgress(0);
                        stopFlicker();
                        DiluentHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
                        mRunstate = true;


                        startButton.setImageResource(R.mipmap.button_stop);
                        mForceStop = false;

                    }

                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mNotifyCharacteristic = characteristic;
                        mBluetoothLeService.setCharacteristicNotification(
                                characteristic, true);
                    }

                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.run_syringe);

        final Intent intent = getIntent();
        Syringe_value = intent.getIntExtra(EXTRAS_SYRINGE_VALUE, 5000);
        Speed_value = intent.getIntExtra(EXTRAS_SPEED_VALUE, 6);
        Tube_value = intent.getIntExtra(EXTRAS_TUBE_VALUE, 1000);

        edit_TV = (EditText)findViewById(R.id.edit_TV);
        edit_DF = (EditText)findViewById(R.id.edit_DF);
        edit_SS = (EditText)findViewById(R.id.edit_SS);
        edit_AUTO = (EditText)findViewById(R.id.edit_AUTO);

        text_diluent = (TextView)findViewById(R.id.textView1);
        text_sample = (TextView)findViewById(R.id.textView2);
        text_injection = (TextView)findViewById(R.id.textView3);


        DiluentBar = (RoundProgressBarWidthNumber)findViewById(R.id.RoundProgressBar01);
        SampleBar = (RoundProgressBarWidthNumber)findViewById(R.id.RoundProgressBar02);
        InjectionBar = (RoundProgressBarWidthNumber)findViewById(R.id.RoundProgressBar03);



        edit_TV.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        edit_DF.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        edit_SS.setInputType(EditorInfo.TYPE_CLASS_PHONE);



        //edit_TV.addTextChangedListener(watcher);
        //edit_DF.addTextChangedListener(watcher);
        //edit_SS.addTextChangedListener(watcher);
        edit_TV.setOnClickListener(edit_clicked);
        edit_TV.setOnEditorActionListener(edit_action);
        edit_DF.setOnClickListener(edit_clicked);
        edit_DF.setOnEditorActionListener(edit_action);
        edit_SS.setOnClickListener(edit_clicked);
        edit_SS.setOnEditorActionListener(edit_action);


        Log.d(TAG, "create action");
        edit_AUTO.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if(edit_AUTO.getText().toString().equals("ON")){
                    edit_AUTO.setText("OFF");
                }
                else {
                    edit_AUTO.setText("ON");
                }

            }
        });

        returnButton = (ImageButton)findViewById(R.id.Button_return);
        startButton = (ImageButton)findViewById(R.id.Button_start);
        washButton = (ImageButton)findViewById(R.id.Button_wash);

        washButton.setOnClickListener(washClickListener);

        returnButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                //ReturntoMenu();
                finish();
            }
        });
        db = openOrCreateDatabase("callon_menu2_db", Context.MODE_PRIVATE, null);
        //db.execSQL("DROP TABLE IF EXISTS table1");

        if(tabbleIsExist("table2")){
            DatabaseHelper dbHelper = new DatabaseHelper(ProcessActivity.this, "callon_menu2_db");
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query("table2", new String[]{"id","name"}, "id=?", new String[]{"1"}, null, null, null);
            while(cursor.moveToNext()){
                String name = cursor.getString(cursor.getColumnIndex("name"));
                edit_TV.setText(name);
            }
            cursor = db.query("table2", new String[]{"id","name"}, "id=?", new String[]{"2"}, null, null, null);
            while(cursor.moveToNext()){
                String name = cursor.getString(cursor.getColumnIndex("name"));
                edit_DF.setText(name);
            }
            cursor = db.query("table2", new String[]{"id","name"}, "id=?", new String[]{"3"}, null, null, null);
            while(cursor.moveToNext()){
                String name = cursor.getString(cursor.getColumnIndex("name"));
                edit_SS.setText(name);
            }
            cursor = db.query("table2", new String[]{"id","name"}, "id=?", new String[]{"4"}, null, null, null);
            while(cursor.moveToNext()){
                String name = cursor.getString(cursor.getColumnIndex("name"));
                edit_AUTO.setText(name);
            }
        }
        else {
            db.execSQL("CREATE TABLE table2(id int,name varchar(20))");
            ContentValues values = new ContentValues();
            values.put("id", 1);
            values.put("name", "5000");
            db.insert("table2", null, values);
            values = new ContentValues();
            values.put("id", 2);
            values.put("name", "50");
            db.insert("table2", null, values);
            values = new ContentValues();
            values.put("id", 3);
            values.put("name", "5");
            db.insert("table2", null, values);
            values = new ContentValues();
            values.put("id", 4);
            values.put("name", "OFF");
            db.insert("table2", null, values);
            edit_TV.setText("5000");
            edit_DF.setText("50");
            edit_SS.setText("5");
            edit_AUTO.setText("OFF");
        }


        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);//startService(gattServiceIntent);
        getedit2 = edit_DF.getText().toString().getBytes();
        //DF_value = Float.parseFloat(edit_DF.getText().toString());
        if(getedit2[0] == '0')
            text_id = 1;
        else
            text_id = 0;
        startFlicker();

    }

    private void stopFlicker(){
        if(task != null)
        {
            task.cancel();
            task = null;
        }
        if(timer != null)
        {
            timer.cancel();
            timer = null;

        }
        text_diluent.setTextColor(0xff2903FC);
        text_sample.setTextColor(0xffF53B03);
        text_injection.setTextColor(Color.GREEN);
    }

    private void startFlicker() {
        if(handler == null){
            handler = new Handler(){
                @Override
                public void dispatchMessage(Message msg){
                    if(change){
                        change = false;
                        if(text_id == 0){
                            text_diluent.setTextColor(Color.TRANSPARENT);
                        }
                        else if(text_id == 1){
                            text_sample.setTextColor(Color.TRANSPARENT);
                        }
                        else if(text_id == 2){
                            text_injection.setTextColor(Color.TRANSPARENT);
                        }
                    }
                    else{
                        change = true;
                        if(text_id == 0)
                            text_diluent.setTextColor(0xff2903FC);
                        else if(text_id == 1)
                            text_sample.setTextColor(0xffF53B03);
                        else if(text_id == 2)
                            text_injection.setTextColor(Color.GREEN);
                    }

                }
            };
        }
        if(timer == null){
            timer = new Timer();
        }
        if(task == null){
            task = new TimerTask() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Message msg = new Message();
                    handler.sendMessage(msg);
                }
            };
        }
        timer.schedule(task, 1, 300);

    }
    /******************************/
    private OnClickListener edit_clicked = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Log.d(TAG, "OnEditorclickListener");
            editTV_forest = edit_TV.getText().toString();
            editDF_forest = edit_DF.getText().toString();
            editSS_forest = edit_SS.getText().toString();
        }
    };
    private boolean tabbleIsExist(String tableName){
        boolean result = false;
        if(tableName == null){
            return false;
        }
        SQLiteDatabase db = null;
        DatabaseHelper dbHelper = new DatabaseHelper(ProcessActivity.this, "callon_menu2_db");
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            String sql = "select count(*) as c from Sqlite_master  where type ='table' and name ='"+tableName.trim()+"' ";
            cursor = db.rawQuery(sql, null);
            if(cursor.moveToNext()){
                int count = cursor.getInt(0);
                if(count>0){
                    result = true;
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
        }
        return result;
    }

    private OnEditorActionListener edit_action = new OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // TODO Auto-generated method stub
            Log.d(TAG, "OnEditorActionListener");
            String subString = edit_DF.getText().toString().substring(1, edit_DF.length());
            if((edit_TV.getText().length()>max_tvlength || edit_TV.getText().length()<1 || Syringe_value < Integer.parseInt(edit_TV.getText().toString()))
                    || (edit_DF.getText().length()>max_dflength || edit_DF.getText().length()<1)
                    ||(!((edit_DF.getText().toString().matches("^((\\d+\\.\\d*[0-9]\\d*)|(\\d*[0-9]\\d*\\.\\d+)|(\\d*[0-9]\\d*))$") || subString.matches("^((\\d+\\.\\d*[0-9]\\d*)|(\\d*[0-9]\\d*\\.\\d+)|(\\d*[0-9]\\d*))$"))))
                    || (edit_SS.getText().length()!=max_sslength || edit_SS.getText().toString().equals("."))
                    ){
                AlertDialog.Builder builder = new AlertDialog.Builder(ProcessActivity.this);
                builder.setIcon(R.mipmap.ic_launcher);
                builder.setTitle("Input Error");
                builder.setMessage("Please check your data!");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                    }
                });
                builder.show();
                mDatastate = false;
            }
            else{
                mDatastate = true;
            }

            if(edit_TV.getText().toString().equals(editTV_forest) == false ||
                    edit_DF.getText().toString().equals(editDF_forest) == false ||
                    edit_SS.getText().toString().equals(editSS_forest) == false){
                Log.d(TAG, "do not equal");
                if (mGattCharacteristics != null) {
                    if(charas.size()>=6){
                        BluetoothGattCharacteristic characteristic =
                                charas.get(0);
                        BluetoothGattCharacteristic characteristic_write = charas.get(5);
                        int charaProp_write = characteristic_write.getProperties();
                        int charaProp = characteristic.getProperties();
                        //String getUUID = characteristic.getUuid().toString();
                        if (((charaProp_write | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0)
                                &&
                                (characteristic_write != null)
                                &&
                                (mRunstate == false)
                                &&
                                (mDatastate == true)
                                ){

                            byte[] writev = new byte[SIMPLEPROFILE_CHAR6_LEN];
                            Arrays.fill(writev, (byte)'0');
                            byte[] getedit1 = edit_TV.getText().toString().getBytes();//edit1.getText().toString().getBytes();
                            getedit2 = edit_DF.getText().toString().getBytes();
                            byte[] getedit3 = edit_SS.getText().toString().getBytes();

                            //System.out.println(getedit1 + " " + getedit2 + " " + getedit3);

                            if ((getedit1.length + getedit2.length + getedit3.length) <= (max_tvlength+max_dflength+max_sslength)) {
                                writev[0] = (byte)'l';
                                writev[1] = (byte)(getedit1.length);
                                writev[2] = (byte)(getedit2.length);
                                writev[3] = (byte)(getedit3.length);
                                System.arraycopy(getedit1, 0, writev, 4, getedit1.length);
                                System.arraycopy(getedit2, 0, writev, 4+getedit1.length, getedit2.length);
                                System.arraycopy(getedit3, 0, writev, 4+getedit1.length+getedit2.length, getedit3.length);
                                if(edit_AUTO.getText().toString().equals("ON")){
                                    writev[4+getedit1.length+getedit2.length+getedit3.length] = (byte)'1';
                                }
                                else{
                                    writev[4+getedit1.length+getedit2.length+getedit3.length] = (byte)'0';
                                }
                                writev[5+getedit1.length+getedit2.length+getedit3.length] = '@';
                            }

		                	/*
		                	for(int i = 0;i<15;i++){
		                		Log.d(TAG, "is:" + writev[i]);
		                	}
		                	*/

                            characteristic_write.setValue(writev);
                            characteristic_write.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                            mBluetoothLeService.writeCharacteristic(characteristic_write);


		                    /*
		                    writev[0] = (byte)1;
		                    characteristic_write.setValue(writev);
		                    mBluetoothLeService.writeCharacteristic(characteristic_write);

		                    writev[0] = (byte)2;
		                    characteristic_write.setValue(writev);
		                    mBluetoothLeService.writeCharacteristic(characteristic_write);*/
                        }
                        if(getedit2[0] == '0')
                            text_id = 1;
                        else
                            text_id = 0;

                        text_diluent.setTextColor(0xff2903FC);
                        text_sample.setTextColor(0xffF53B03);
                        text_injection.setTextColor(Color.GREEN);

                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }

                    }
                }
            }
            return false;
        }
    };


    /*************
     **********************
     private TextWatcher watcher = new TextWatcher() {

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    // TODO Auto-generated method stub
    Log.d(TAG, "onTextChanged");
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
    int after) {
    // TODO Auto-generated method stub
    Log.d(TAG, "beforeTextChanged");
    }

    @Override
    public void afterTextChanged(Editable s) {
    // TODO Auto-generated method stub
    Log.d(TAG, "afterTextChanged");

    }
    };
     ********************
     ***********/
    private void ReturntoMenu() {
        //Intent intent = new Intent(this, DeviceControlActivity.class);
        //startActivity(intent);
        if (mGattCharacteristics != null) {
            if(charas.size()>=6){
                BluetoothGattCharacteristic characteristic =
                        charas.get(0);
                BluetoothGattCharacteristic characteristic_write = charas.get(5);
                int charaProp_write = characteristic_write.getProperties();
                int charaProp = characteristic.getProperties();
                //String getUUID = characteristic.getUuid().toString();
                if (((charaProp_write | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0)
                        &&
                        (characteristic_write != null)
                        &&
                        (mRunstate == false)){

                    byte[] writev = new byte[SIMPLEPROFILE_CHAR6_LEN];
                    Arrays.fill(writev, (byte)'0');
                    byte[] getedit1 = ProcessActivity.EXTRAS_SYRINGE_VALUE.getBytes();//edit1.getText().toString().getBytes();
                    byte[] getedit2 = ProcessActivity.EXTRAS_SPEED_VALUE.getBytes();
                    byte[] getedit3 = ProcessActivity.EXTRAS_TUBE_VALUE.getBytes();
                    if ((getedit1.length + getedit2.length + getedit3.length) <= (max_tvlength+max_dflength+max_sslength)) {
                        writev[0] = (byte)'1';
                        writev[1] = (byte)(getedit1.length);
                        writev[2] = (byte)(getedit2.length);
                        writev[3] = (byte)(getedit3.length);
                        System.arraycopy(getedit1, 0, writev, 4, getedit1.length);
                        System.arraycopy(getedit2, 0, writev, 4 + getedit1.length, getedit2.length);
                        System.arraycopy(getedit3, 0, writev, 4 + getedit1.length + getedit2.length, getedit3.length);
                        writev[4 + getedit1.length + getedit2.length + getedit3.length] = '@';
                    }

                    //System.out.println(getedit1 + " " + getedit2 + " " + getedit3);

                	/*
                	for(int i = 0;i<15;i++){
                		Log.d(TAG, "is:" + writev[i]);
                	}
                	*/

                    characteristic_write.setValue(writev);
                    characteristic_write.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                    mBluetoothLeService.writeCharacteristic(characteristic_write);
                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    mNotifyCharacteristic = characteristic;
                    mBluetoothLeService.setCharacteristicNotification(
                            characteristic, true);
                }

            }
        }


        finish();
    }

    @Override
    protected void onResume() {
        /**
         *
         */
        if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        // TODO Auto-generated method stub
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        DatabaseHelper dbHelper = new DatabaseHelper(ProcessActivity.this, "callon_menu2_db");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String name = edit_TV.getText().toString();
        values.put("name", name);
        db.update("table2", values, "id=?", new String[]{"1"});

        values = new ContentValues();
        name = edit_DF.getText().toString();
        values.put("name", name);
        db.update("table2", values, "id=?", new String[]{"2"});

        values = new ContentValues();
        name = edit_SS.getText().toString();
        values.put("name", name);
        db.update("table2", values, "id=?", new String[]{"3"});

        values = new ContentValues();
        name = edit_AUTO.getText().toString();
        values.put("name", name);
        db.update("table2", values, "id=?", new String[]{"4"});

        db.close();
        stopFlicker();
        unbindService(mServiceConnection);
        super.onDestroy();

    }
    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;

        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            if(uuid.equals(MyService)){
                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    charas.add(gattCharacteristic);
                }
            }
            mGattCharacteristics.add(charas);
            // gattCharacteristicData.add(gattCharacteristicGroupData);

        }
        //  edit2.setText(""+charas.size());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

}