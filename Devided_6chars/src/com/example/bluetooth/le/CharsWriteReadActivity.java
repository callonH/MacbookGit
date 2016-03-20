package com.example.bluetooth.le;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.example.bluetooth.le.R;
//import com.example.bluetoothactivity.MainActivity.MyButtonClearListener;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class CharsWriteReadActivity extends Activity {
	public static final String EXTRAS_CHARS_NAME = "CHARS_NAME";
    public static final String EXTRAS_CHARS_UUID = "CHARS_UUID";
    private final int S_ACCELEROMETER = 0;
    private final int S_GYROSCOPE = 1;
    private final int S_MAGNETOMETER = 2;
    private final int S_PRESSURE = 3;
    private Spinner spinnerx;
    private Spinner spinnery;
    private String mCharsName;
    private int SelectPosition;
    private String mCharsUUID;
    SurfaceView surface = null;
  //��ͼʹ�ã����Կ���һ��SurfaceView
    private SurfaceHolder holder = null; 
	final int HEIGHT=320;   //���û�ͼ��Χ�߶�
    final int WIDTH=320;    //��ͼ��Χ���
    private Paint paint = null;      //����
    final int X_OFFSET = 5;  //x�ᣨԭ�㣩��ʼλ��ƫ�ƻ�ͼ��Χһ�� 
    private int cx = X_OFFSET;  //ʵʱx������
    //private int cy = 0;  //ʵʱy������
    int centerY = HEIGHT /2;  //y���λ��
 // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            /*֮ǰ��������*/
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        	/*֮ǰ��������*/
        }
    };
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                drawData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.read_and_write);
		final Intent intent = getIntent();
		mCharsName = intent.getStringExtra(EXTRAS_CHARS_NAME);
		mCharsUUID = intent.getStringExtra(EXTRAS_CHARS_UUID);
		SelectPosition = intent.getIntExtra("SELECT_POSITION", 0);
		((TextView) findViewById(R.id.chars_name)).setText(mCharsName);
		((TextView) findViewById(R.id.chars_uuid)).setText(mCharsUUID);
		((TextView) findViewById(R.id.getdata)).setText(R.string.label_data);
		
		spinnerx = (Spinner) findViewById(R.id.spinnerx);
		spinnery = (Spinner) findViewById(R.id.spinnery);
	    
        //����
		ArrayList<String> data_listx = new ArrayList<String>();
        data_listx.add("���ٶ�");
        data_listx.add("������");
        data_listx.add("�ش�");
        data_listx.add("�¶�");
      //����
      ArrayList<String> data_listy = new ArrayList<String>();
        data_listy.add("ʱ��");
        data_listy.add("���ٶ�");
        data_listy.add("������");
        data_listy.add("�ش�");
        data_listy.add("�¶�");
        //������
        ArrayAdapter<String> arr_adapterx= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data_listx);
      //������
        ArrayAdapter<String> arr_adaptery= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data_listy);
        //������ʽ
        arr_adapterx.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      //������ʽ
        arr_adaptery.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //����������
        spinnerx.setAdapter(arr_adapterx);
      //����������
        spinnery.setAdapter(arr_adaptery);
		//Button myButton=(Button)findViewById(R.id.senddataBtn);
		//myButton.setText("Send");//sin =(Button)findViewById(R.id.sin);
		//myButton.setOnClickListener(new MyButtonSendListener()); 
      //  myButton.setOnClickListener(new MyButtonListener());
		getActionBar().setTitle("BLE Sensor Data Curve");
		surface = (SurfaceView)findViewById(R.id.show);
		//��ʼ��SurfaceHolder����
		holder = surface.getHolder(); 
		//���û�����С��Ҫ��ʵ�ʵĻ�ͼλ�ô�һ�� 
        holder.setFixedSize(WIDTH+50, HEIGHT+100);  
        /*���ò��ε���ɫ�Ȳ���*/
        paint = new Paint();  
		paint.setColor(Color.GREEN);  //�����ε���ɫ����ɫ�ģ��������������ɫ
        paint.setStrokeWidth(3);
        holder.addCallback(new Callback() {  
            public void surfaceChanged(SurfaceHolder holder,int format,int width,int height){ 
                drawBack(holder); 
                //���û����仰����ʹ���ڿ�ʼ���г���������Ļû�а�ɫ�Ļ�������
                //ֱ�����°�������Ϊ�ڰ������ж�drawBack(SurfaceHolder holder)�ĵ���
            }
            @Override
			public void surfaceCreated(SurfaceHolder holder) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				// TODO Auto-generated method stub
				
			}
        });
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);//startService(gattServiceIntent);
	}
	@Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		//menu.findItem(R.id.menu_stop).setVisible(false);
        //menu.findItem(R.id.menu_scan).setVisible(true);
        //menu.findItem(R.id.menu_refresh).setActionView(null);
		return true;
	}
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        //finish();
    } 
    /*class MyButtonSendListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			
		}
		
	}*/
	
	//���û�������ɫ������XY���λ��
    private void drawBack(SurfaceHolder holder){ 
        Canvas canvas = holder.lockCanvas(); //��������
        //���ư�ɫ���� 
        canvas.drawColor(Color.WHITE); 
        Paint p = new Paint(); 
        p.setColor(Color.BLACK); 
        p.setStrokeWidth(2); 
         
        //���������� 
       canvas.drawLine(X_OFFSET, centerY, WIDTH, centerY, p); //����X�� ǰ�ĸ���������ʼ����
       canvas.drawLine(X_OFFSET, 20, X_OFFSET, HEIGHT, p); //����Y�� ǰ�ĸ���������ʼ����

        holder.unlockCanvasAndPost(canvas);  //�������� ��ʾ����Ļ��
        holder.lockCanvas(new Rect(0,0,0,0)); //�����ֲ���������ط������ı�
        holder.unlockCanvasAndPost(canvas); 
        
    }
    private static String[] stringAnalytical(String string, char c)  
    {  
        //�ַ����зָ����ĸ���  
        int count = 0;  
          
        //��������ָ���򷵻��ַ�����  
        if (string.indexOf(c) == -1)  
        {  
            return new String[]{string};  
        }  
          
        char[] cs = string.toCharArray();  
          
        //���˵���һ�������һ���Ƿָ��������  
        for (int i = 1; i < cs.length -1; i++)  
        {  
            if (cs[i] == c)  
            {  
                count++; //�õ��ָ����ĸ���  
            }  
        }  
          
        String[] strArray = new String[count + 1];  
        int k = 0, j = 0;  
        String str = string;  
          
        //ȥ����һ���ַ��Ƿָ��������  
        if ((k = str.indexOf(c)) == 0)  
        {  
            str = string.substring(k + 1);  
        }  
          
        //����Ƿ�����ָ������������򷵻��ַ���  
        if (str.indexOf(c) == -1)  
        {  
            return new String[]{str};  
        }  
          
        while ((k = str.indexOf(c)) != -1)  
        {  
            strArray[j++] = str.substring(0, k);  
            str = str.substring(k + 1);  
            if ((k = str.indexOf(c)) == -1 && str.length() > 0)  
            {  
                strArray[j++] = str.substring(0);  
            }  
        }  
          
        return strArray;  
    }  
    
	private void drawData(String data) {
		int cy[]= new int[3];
        if (data != null) {
        	final StringBuilder stringBuilder = new StringBuilder(data);
            String[] strArray = stringAnalytical(data,' ');//mDataField.setText(data);
        	((TextView) findViewById(R.id.getdataView)).setText(data);
        	if(S_ACCELEROMETER == SelectPosition){
        		cy[0] = Integer.parseInt(strArray[0]) + 
            			Integer.parseInt(strArray[1])*256;
        		cy[0] /= 164;
        		cy[0] = centerY - cy[0];
        		cy[1] = Integer.parseInt(strArray[2]) + 
            			Integer.parseInt(strArray[3])*256;
        		cy[1] /= 164;
        		cy[1] = centerY - cy[1];
        		cy[2] = Integer.parseInt(strArray[4]) + 
            			Integer.parseInt(strArray[5])*256;
        		cy[2] /= 164;
        		cy[2] = centerY - cy[2];
            	if(cx == 5){
            		drawBack(holder);  //����֮�����ԭ����ͼ�񣬴��¿�ʼ
            	}
            	for(int i=0;i<3;i++){
    	        	Canvas canvas = holder.lockCanvas(new Rect(cx,cy[i]-2,cx+2,cy[i]+2)); 
    	        	if(i==0)
    	            {	paint.setColor(Color.GREEN);//���ò�����ɫ
    	         	   canvas.drawPoint(cx, cy[i], paint); //���    
    	            }
    	            else if(i==1)
    	            { paint.setColor(Color.RED);
    	            canvas.drawPoint(cx, cy[i], paint); //���           
    	            }   
    	            else if(i==2)
    	            { paint.setColor(Color.BLUE);
    	            canvas.drawPoint(cx, cy[i], paint); //���           
    	            }   
    	     	   holder.unlockCanvasAndPost(canvas);  //��������
            	}
         	   cx++;    //cx ������ ����������ʱ�����ͼ��    
               //cx++; //����Լ��趨
               if(cx >=WIDTH){                       
                  cx=5;     //����������ͷ��ʼ��                   
                  //drawBack(holder);                   
               }
        	}
        	else if(S_GYROSCOPE == SelectPosition){
        		cy[0] = Integer.parseInt(strArray[0]) + 
            			Integer.parseInt(strArray[1])*256;
        		cy[0] /= 16.4;
        		cy[0] = centerY - cy[0];
        		cy[1] = Integer.parseInt(strArray[2]) + 
            			Integer.parseInt(strArray[3])*256;
        		cy[1] /= 16.4;
        		cy[1] = centerY - cy[1];
        		cy[2] = Integer.parseInt(strArray[4]) + 
            			Integer.parseInt(strArray[5])*256;
        		cy[2] /= 16.4;
        		cy[2] = centerY - cy[2];
            	if(cx == 5){
            		drawBack(holder);  //����֮�����ԭ����ͼ�񣬴��¿�ʼ
            	}
            	for(int i=0;i<3;i++){
    	        	Canvas canvas = holder.lockCanvas(new Rect(cx,cy[i]-2,cx+2,cy[i]+2)); 
    	        	if(i==0)
    	            {	paint.setColor(Color.GREEN);//���ò�����ɫ
    	         	   canvas.drawPoint(cx, cy[i], paint); //���    
    	            }
    	            else if(i==1)
    	            { paint.setColor(Color.RED);
    	            canvas.drawPoint(cx, cy[i], paint); //���           
    	            }   
    	            else if(i==2)
    	            { paint.setColor(Color.BLUE);
    	            canvas.drawPoint(cx, cy[i], paint); //���           
    	            }   
    	     	   holder.unlockCanvasAndPost(canvas);  //��������
            	}
         	   cx++;    //cx ������ ����������ʱ�����ͼ��    
               //cx++; //����Լ��趨
               if(cx >=WIDTH){                       
                  cx=5;     //����������ͷ��ʼ��                   
                  //drawBack(holder);                   
               }
        	}
        	else if(S_MAGNETOMETER == SelectPosition){
        		cy[0] = Integer.parseInt(strArray[0]) + 
            			Integer.parseInt(strArray[1])*256;
        		cy[0] = centerY - cy[0];
        		cy[1] = Integer.parseInt(strArray[2]) + 
            			Integer.parseInt(strArray[3])*256;
        		cy[1] = centerY - cy[1];
        		cy[2] = Integer.parseInt(strArray[4]) + 
            			Integer.parseInt(strArray[5])*256;
        		cy[2] = centerY - cy[2];
            	if(cx == 5){
            		drawBack(holder);  //����֮�����ԭ����ͼ�񣬴��¿�ʼ
            	}
            	for(int i=0;i<3;i++){
    	        	Canvas canvas = holder.lockCanvas(new Rect(cx,cy[i]-2,cx+2,cy[i]+2)); 
    	        	if(i==0)
    	            {	paint.setColor(Color.GREEN);//���ò�����ɫ
    	         	   canvas.drawPoint(cx, cy[i], paint); //���    
    	            }
    	            else if(i==1)
    	            { paint.setColor(Color.RED);
    	            canvas.drawPoint(cx, cy[i], paint); //���           
    	            }   
    	            else if(i==2)
    	            { paint.setColor(Color.BLUE);
    	            canvas.drawPoint(cx, cy[i], paint); //���           
    	            }   
    	     	   holder.unlockCanvasAndPost(canvas);  //��������
            	}
         	   cx++;    //cx ������ ����������ʱ�����ͼ��    
               //cx++; //����Լ��趨
               if(cx >=WIDTH){                       
                  cx=5;     //����������ͷ��ʼ��                   
                  //drawBack(holder);                   
               }
        	}
        	else if(S_PRESSURE == SelectPosition){
        		cy[0] = Integer.parseInt(strArray[0])*100000 + 
            			Integer.parseInt(strArray[1])*10000 + 
            			Integer.parseInt(strArray[2])*1000 +
            			Integer.parseInt(strArray[3])*100 +
            			Integer.parseInt(strArray[4])*10 +
            			Integer.parseInt(strArray[5]);
        		cy[0] = centerY - cy[0];
            	if(cx == 5){
            		drawBack(holder);  //����֮�����ԭ����ͼ�񣬴��¿�ʼ
            	}
            	for(int i=0;i<3;i++){
    	        	Canvas canvas = holder.lockCanvas(new Rect(cx,cy[i]-2,cx+2,cy[i]+2)); 
    	        	if(i==0)
    	            {	paint.setColor(Color.GREEN);//���ò�����ɫ
    	         	   canvas.drawPoint(cx, cy[i], paint); //���    
    	            }
    	            else if(i==1)
    	            { paint.setColor(Color.RED);
    	            canvas.drawPoint(cx, cy[i], paint); //���           
    	            }   
    	            else if(i==2)
    	            { paint.setColor(Color.BLUE);
    	            canvas.drawPoint(cx, cy[i], paint); //���           
    	            }   
    	     	   holder.unlockCanvasAndPost(canvas);  //��������
            	}
         	   cx++;    //cx ������ ����������ʱ�����ͼ��    
               //cx++; //����Լ��趨
               if(cx >=WIDTH){                       
                  cx=5;     //����������ͷ��ʼ��                   
                  //drawBack(holder);                   
               }
        	}
        	//cy[0] = centerY - Integer.parseInt(strArray[0]);
        	/*
        	cy[0] = centerY - (Integer.parseInt(strArray[0])*100 + 
        			Integer.parseInt(strArray[1])*10
        			+Integer.parseInt(strArray[2]));
        	cy[1] = centerY - (Integer.parseInt(strArray[3])*100 + 
        			Integer.parseInt(strArray[4])*10
        			+Integer.parseInt(strArray[5]));
        	cy[2] = centerY -(Integer.parseInt(strArray[6])*100 + 
        			Integer.parseInt(strArray[7])*10
        			+Integer.parseInt(strArray[8]));
        	if(cx == 5){
        		drawBack(holder);  //����֮�����ԭ����ͼ�񣬴��¿�ʼ
        	}
        	for(int i=0;i<3;i++){
	        	Canvas canvas = holder.lockCanvas(new Rect(cx,cy[i]-2,cx+2,cy[i]+2)); 
	        	if(i==0)
	            {	paint.setColor(Color.GREEN);//���ò�����ɫ
	         	   canvas.drawPoint(cx, cy[i], paint); //���    
	            }
	            else if(i==1)
	            { paint.setColor(Color.RED);
	            canvas.drawPoint(cx, cy[i], paint); //���           
	            }   
	            else if(i==2)
	            { paint.setColor(Color.BLUE);
	            canvas.drawPoint(cx, cy[i], paint); //���           
	            }   
	     	   holder.unlockCanvasAndPost(canvas);  //��������
        	}
     	   cx++;    //cx ������ ����������ʱ�����ͼ��    
           //cx++; //����Լ��趨
           if(cx >=WIDTH){                       
              cx=5;     //����������ͷ��ʼ��                   
              //drawBack(holder);                   
           }*/
        }
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