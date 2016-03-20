/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bluetooth.le;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.bluetooth.le.R;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public final String MyUUID = "0000fff6-0000-1000-8000-00805f9b34fb";
    private final String MyService = "0000fff0-0000-1000-8000-00805f9b34fb";
    private TextView mConnectionState;
    private TextView mServiceState;
    //private TextView mData;
    
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    //private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
 //   private ImageButton Syringe;
 //   private ImageButton Speed;
 //   private ImageButton Tube;
 //   private ImageButton Program;
    private Button edit1;//Syringe
    private Button edit2;//Speed
    private Button edit3;//Tube
    private Button edit4;//Program
    
    private static final String[] Syringe_value = new String[]{"500", "1000",
    	"2500" , "5000" , "4000"}; 
    private static final String[] Speed_value = new String[]{"1", "2",
    	"3" , "5" , "4"}; 
    private static final String[] Tube_value = new String[]{"100", "200",
    	"500" , "1000" , "1500"}; 
    
    //private ExpandableListView mGattServicesList;//0225
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    //private BluetoothGattCharacteristic mNotifyCharacteristic;

    //private final String LIST_NAME = "NAME";
    //private final String LIST_UUID = "UUID";
    private ArrayList<BluetoothGattCharacteristic> charas =
            new ArrayList<BluetoothGattCharacteristic>();
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                mServiceState.setText(R.string.NotReady);
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                mServiceState.setText(R.string.Ready);
                edit4.setOnClickListener(clickListener); 
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                ;//displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };
    

    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);//0225
        //mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.

        
        //((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        //mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);//0225
        //mGattServicesList.setOnChildClickListener(servicesListClickListner);//0225
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mServiceState = (TextView)findViewById(R.id.service_state);
        
        //mData = (TextView)findViewById(R.id.DataField);
        
        //mDataField = (TextView) findViewById(R.id.data_value);
 /*
        Syringe = (ImageButton)findViewById(R.id.imageButton1);
        Speed = (ImageButton)findViewById(R.id.imageButton2);
        Tube = (ImageButton)findViewById(R.id.imageButton3);
        Program = (ImageButton)findViewById(R.id.imageButton4);
*/        
        edit1 =  (Button)findViewById(R.id.imageButton5);//Syringe
        edit2 =  (Button)findViewById(R.id.imageButton6);//Speed
        edit3 =  (Button)findViewById(R.id.imageButton7);//Tube
        edit4 =  (Button)findViewById(R.id.imageButton8);//Program
        
        mServiceState.setText(R.string.NotReady);
        edit1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	View outerView = LayoutInflater.from(DeviceControlActivity.this).inflate(R.layout.wheel_view, null);
                WheelView wv = (WheelView) outerView.findViewById(R.id.wheel_view_wv);
                wv.setOffset(2);
                wv.setItems(Arrays.asList(Syringe_value));
                wv.setSeletion(3);
                wv.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                    @Override
                    public void onSelected(int selectedIndex, String item) {
                    	edit1.setText(item);
                    }
                });

                new AlertDialog.Builder(DeviceControlActivity.this)
                        .setTitle("Your selection is")
                        .setView(outerView)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
        edit2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	View outerView = LayoutInflater.from(DeviceControlActivity.this).inflate(R.layout.wheel_view, null);
                WheelView wv = (WheelView) outerView.findViewById(R.id.wheel_view_wv);
                wv.setOffset(2);
                wv.setItems(Arrays.asList(Speed_value));
                wv.setSeletion(3);
                wv.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                    @Override
                    public void onSelected(int selectedIndex, String item) {
                    	edit2.setText(item);
                    }
                });

                new AlertDialog.Builder(DeviceControlActivity.this)
                        .setTitle("Your selection is")
                        .setView(outerView)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
        edit3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	View outerView = LayoutInflater.from(DeviceControlActivity.this).inflate(R.layout.wheel_view, null);
                WheelView wv = (WheelView) outerView.findViewById(R.id.wheel_view_wv);
                wv.setOffset(2);
                wv.setItems(Arrays.asList(Tube_value));
                wv.setSeletion(3);
                wv.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                    @Override
                    public void onSelected(int selectedIndex, String item) {
                    	edit3.setText(item);
                    }
                });

                new AlertDialog.Builder(DeviceControlActivity.this)
                        .setTitle("Your selection is")
                        .setView(outerView)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
        
        
        edit1.setText("5000");
        edit2.setText("5");
        edit3.setText("1000");
        edit4.setText("快捷模式");
    
        
        
        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        //startService(gattServiceIntent);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        
   }

    @Override
    protected void onResume() {
    	/**
    	  * 设置为横屏
    	  */
    	 if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
    	  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    	 }
    	 super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    public OnClickListener clickListener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
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
	                		(characteristic_write != null)){
	                	
	                	if (mNotifyCharacteristic != null) {
                            mBluetoothLeService.setCharacteristicNotification(
                                    mNotifyCharacteristic, false);
                            mNotifyCharacteristic = null;
                        }	
	                	byte[] writev = new byte[15];
	                	Arrays.fill(writev, (byte)'0');
	                	byte[] getedit1 = edit1.getText().toString().getBytes();//edit1.getText().toString().getBytes();
	                	byte[] getedit2 = edit2.getText().toString().getBytes();
	                	byte[] getedit3 = edit3.getText().toString().getBytes();
	                	
	                	
	                	//System.out.println(getedit1 + " " + getedit2 + " " + getedit3);
	                	
	                	if ((getedit1.length + getedit2.length + getedit3.length) <= 9) {
	                		writev[0] = (byte)'1';
		                	writev[1] = (byte)(getedit1.length);
		                	writev[2] = (byte)(getedit2.length);
		                	writev[3] = (byte)(getedit3.length);
							System.arraycopy(getedit1, 0, writev, 4, getedit1.length);
							System.arraycopy(getedit2, 0, writev, 4 + getedit1.length, getedit2.length);
							System.arraycopy(getedit3, 0, writev, 4 + getedit1.length + getedit2.length, getedit3.length);
							writev[4 + getedit1.length + getedit2.length + getedit3.length] = '@';
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
	                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        // If there is an active notification on a characteristic, clear
                        // it first so it doesn't update the data field on the user interface.
                        if (mNotifyCharacteristic != null) {
                            mBluetoothLeService.setCharacteristicNotification(
                                    mNotifyCharacteristic, false);
                            mNotifyCharacteristic = null;
                        }
                        mBluetoothLeService.readCharacteristic(characteristic);
                        
                    }       
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mNotifyCharacteristic = characteristic;
                        mBluetoothLeService.setCharacteristicNotification(
                                characteristic, true);
                    }

				}
			}
			
			GotoMyActivity();
			
		}
	};
    	
    
    
    
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void GotoMyActivity() {
    	/**/final Intent intent = new Intent(this,ProcessActivity.class);
    	intent.putExtra(ProcessActivity.EXTRAS_SYRINGE_VALUE, Integer.parseInt(edit1.getText().toString()));
    	intent.putExtra(ProcessActivity.EXTRAS_SPEED_VALUE, Integer.parseInt(edit2.getText().toString()));
    	intent.putExtra(ProcessActivity.EXTRAS_TUBE_VALUE, Integer.parseInt(edit3.getText().toString()));
    	startActivityForResult(intent, 0);
        
    }
    
    private void displayData(String data) {
        if (data != null) {
        	//mData.setText(data);//mDataField.setText(data);
        }
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
