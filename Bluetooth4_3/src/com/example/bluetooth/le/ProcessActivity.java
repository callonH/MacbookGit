package com.example.bluetooth.le;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.R.integer;
import android.app.Activity;
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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;

import com.zhy.view.RoundProgressBarWidthNumber;


public class ProcessActivity extends Activity {
	private final static String TAG = ProcessActivity.class.getSimpleName();
	
	public static final String EXTRAS_SYRINGE_VALUE = "SYRINGE_VALUE";
	public static final String EXTRAS_SPEED_VALUE = "SPEED_VALUE";
	public static final String EXTRAS_TUBE_VALUE = "TUBE_VALUE";
	
	private int Syringe_value = 0;
	private int Speed_value = 0;
	private int Tube_value = 0;
	
	private int mRunway;
	
	private EditText edit_TV = null; 
	private EditText edit_DF = null; 
	private EditText edit_SS = null; 
	private EditText edit_AUTO = null;
	
	private int TV_value = 0;
	private float DF_value = 0;
	private int SS_value = 0;
	
	private RoundProgressBarWidthNumber DiluentBar = null;
	private RoundProgressBarWidthNumber SampleBar = null;
	private RoundProgressBarWidthNumber InjectionBar = null;
	private static final int MSG_PROGRESS_UPDATE = 0x110;
	
	//private static int testprocess = 50;
	
	private ImageButton returnButton;
	private ImageButton startButton;
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
        /*之前都做过了*/
        	if(mBluetoothLeService != null){
        		Log.d(TAG, "connect action");
        		displayGattServices(mBluetoothLeService.getSupportedGattServices());
                startButton.setOnClickListener(clickListener); 
                Log.d(TAG, "service action");
        	}
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        	/*之前都做过了*/
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
	                //BluetoothGattCharacteristic characteristic =
	                //		charas.get(0);
	                BluetoothGattCharacteristic characteristic_write = charas.get(5);
	                int charaProp_write = characteristic_write.getProperties();
	                //int charaProp = characteristic.getProperties();
	                //String getUUID = characteristic.getUuid().toString();
	                if (((charaProp_write | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0)
	                		&&  
	                		(characteristic_write != null)){
	                	
	                	byte[] writev = new byte[15];
	                	Arrays.fill(writev, (byte)'0');
	                	byte[] getedit1 = edit_TV.getText().toString().getBytes();//edit1.getText().toString().getBytes();
	                	byte[] getedit2 = edit_DF.getText().toString().getBytes();
	                	byte[] getedit3 = edit_SS.getText().toString().getBytes();
	                	
	                	TV_value = Integer.parseInt(edit_TV.getText().toString());
	                	SS_value = Integer.parseInt(edit_SS.getText().toString());
	                	DF_value = Float.parseFloat(edit_DF.getText().toString());
	                	//System.out.println(getedit1 + " " + getedit2 + " " + getedit3);
	                	
	                	if ((getedit1.length + getedit2.length + getedit3.length) <= 9) {
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
	                    
	                    
	                    if(TV_value - Tube_value >= 0 && DF_value >= 1){
	                    	mRunway = 1;
	                    	DiluentBar.setProgress(0);
	                    	SampleBar.setProgress(0);
	                    	InjectionBar.setProgress(0);
	                    	DiluentHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
	                    }
	                    else if(TV_value - Tube_value >= 0 && DF_value < 1){
	                    	mRunway = 2;
	                    	DiluentBar.setProgress(0);
	                    	SampleBar.setProgress(0);
	                    	InjectionBar.setProgress(0);
	                    	DiluentHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
	                    }
	                    else if (TV_value - Tube_value < 0 && DF_value < 1) {
	                    	mRunway = 3;
	                    	DiluentBar.setProgress(0);
	                    	SampleBar.setProgress(0);
	                    	InjectionBar.setProgress(0);
	                    	DiluentHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
						}
	                    
	                    
	                    /*
	                    writev[0] = (byte)1;
	                    characteristic_write.setValue(writev);
	                    mBluetoothLeService.writeCharacteristic(characteristic_write);
	                    
	                    writev[0] = (byte)2;
	                    characteristic_write.setValue(writev);
	                    mBluetoothLeService.writeCharacteristic(characteristic_write);*/
	                }

				}
			}
		}
	};
    
	private Handler DiluentHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			int progress = DiluentBar.getProgress();
			
			if (progress >= 100) {
				DiluentHandler.removeMessages(MSG_PROGRESS_UPDATE);
			}
			else{
				DiluentBar.setProgress(++progress);
				DiluentHandler.sendEmptyMessageDelayed(MSG_PROGRESS_UPDATE, Speed_value*18);
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
				SampleBar.setProgress(++progress);
				SampleHandler.sendEmptyMessageDelayed(MSG_PROGRESS_UPDATE, SS_value*18);
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
				InjectionBar.setProgress(++progress);
				InjectionHandler.sendEmptyMessageDelayed(MSG_PROGRESS_UPDATE, Speed_value*18);
			}
		};
	};
	private void RunProcessBar(Intent intent) {
		if(mRunway == 1){
    		if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 1){
    			DiluentBar.setProgress(100);	
    		}
    		else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 2){
    			SampleHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
    		}
    		else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 3){
    			SampleBar.setProgress(100);
    			
    		}
    		else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 4){
    			InjectionHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
    		}
    		else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 5){
    			InjectionBar.setProgress(100);
    		}
    	}
		else if (mRunway == 2) {
			float increase_num = 0;
			increase_num = Float.parseFloat(edit_DF.getText().toString());
			if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 1){
    			DiluentBar.setProgress(100);
    		}
			else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 255){
				InjectionBar.setProgress(100);
			}
			else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) % 2 == 0){
				InjectionHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
			}
			else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) % 2 == 1){
				InjectionBar.setProgress((int)(DF_value*100));
				DF_value += increase_num;
				InjectionHandler.removeMessages(MSG_PROGRESS_UPDATE);
			}
		}
		else if (mRunway == 3) {
			float increase_num = 0;
			increase_num = Float.parseFloat(edit_DF.getText().toString());
			if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 1){
    			DiluentBar.setProgress(100);
    		}
			else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 2){
				SampleHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
			}
			else if (intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 3) {
				SampleBar.setProgress(100);
			}
			else if (intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) == 255) {
				InjectionBar.setProgress(100);
			}
			else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) % 2 == 0){
				InjectionHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
			}
			else if(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0) % 2 == 1){
				InjectionBar.setProgress((int)(DF_value*100));
				DF_value += increase_num;
				InjectionHandler.removeMessages(MSG_PROGRESS_UPDATE);
			}
		}
	}	
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
		
		DiluentBar = (RoundProgressBarWidthNumber)findViewById(R.id.RoundProgressBar01);
		SampleBar = (RoundProgressBarWidthNumber)findViewById(R.id.RoundProgressBar02);
		InjectionBar = (RoundProgressBarWidthNumber)findViewById(R.id.RoundProgressBar03);
		
		
		
		edit_TV.setInputType(EditorInfo.TYPE_CLASS_PHONE);
		edit_DF.setInputType(EditorInfo.TYPE_CLASS_PHONE);
		edit_SS.setInputType(EditorInfo.TYPE_CLASS_PHONE);

		edit_TV.setText("5000");
		edit_DF.setText("50");
		edit_SS.setText("5");
		
		edit_AUTO.setText("ON");
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
		
		returnButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ReturntoMenu();
				
			}
		});
		
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);//startService(gattServiceIntent);
		
	}
	
	private void ReturntoMenu() {
		//Intent intent = new Intent(this, DeviceControlActivity.class);
		//startActivity(intent);
		finish();
	}
	
	@Override
	protected void onResume() {
		/**
	   	  * 设置为横屏
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
		super.onDestroy();
		unbindService(mServiceConnection);
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