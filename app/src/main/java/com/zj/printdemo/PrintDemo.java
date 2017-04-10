package com.zj.printdemo;

import android.content.Intent;
import com.zj.btsdk.BluetoothService;
import cn.com.zj.command.sdk.Print_pic;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.util.Log;

public class PrintDemo extends Activity {
	Button btnSearch;
	Button btnSendDraw;
	Button btnSend;
	Button btnClose;
	EditText edtContext;
	EditText edtPrint;
	private static final int REQUEST_ENABLE_BT = 2;
	BluetoothService mService = null;
	BluetoothDevice con_dev = null;
	private static final int REQUEST_CONNECT_DEVICE = 1;  //获取设备消息
    private static final String ENCODING = "GBK";
    private static final int D58MMWIDTH = 384;
    private static final int D80MMWIDTH = 576;
	
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mService = new BluetoothService(this, mHandler);
		//蓝牙不可用退出程序
		if(!mService.isAvailable()){
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
		}		
	}

    @Override
    public void onStart() {
    	super.onStart();
    	//蓝牙未打开，打开蓝牙
		if(!mService.isBTopen())
		{
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
		try {
			btnSendDraw = (Button) this.findViewById(R.id.btn_test);
			btnSendDraw.setOnClickListener(new ClickEvent());
			btnSearch = (Button) this.findViewById(R.id.btnSearch);
			btnSearch.setOnClickListener(new ClickEvent());
			btnSend = (Button) this.findViewById(R.id.btnSend);
			btnSend.setOnClickListener(new ClickEvent());
			btnClose = (Button) this.findViewById(R.id.btnClose);
			btnClose.setOnClickListener(new ClickEvent());
			edtContext = (EditText) findViewById(R.id.txt_content);
			btnClose.setEnabled(false);
			btnSend.setEnabled(false);
			btnSendDraw.setEnabled(false);
		} catch (Exception ex) {
            Log.e("出错信息",ex.getMessage());
		}
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mService != null) 
			mService.stop();
		mService = null; 
	}
	
	class ClickEvent implements View.OnClickListener {
		public void onClick(View v) {
			if (v == btnSearch) {			
				Intent serverIntent = new Intent(PrintDemo.this,DeviceListActivity.class);      //运行另外一个类的活动
				startActivityForResult(serverIntent,REQUEST_CONNECT_DEVICE);
			} else if (v == btnSend) {
                String msg = edtContext.getText().toString();
                if( msg.length() > 0 ){
                    mService.sendMessage(msg+"\n", ENCODING);
                }
			} else if (v == btnClose) {
				mService.stop();
			} else if (v == btnSendDraw) {
                String msg = "";
                String lang = getString(R.string.strLang);
				printImage();
				
            	byte[] cmd = new byte[3];
        	    cmd[0] = 0x1b;
        	    cmd[1] = 0x21;
            	if((lang.compareTo("en")) == 0){	
            		cmd[2] |= 0x10;
            		mService.write(cmd);           //倍宽、倍高模式
            		mService.sendMessage("Congratulations!\n", ENCODING);
            		cmd[2] &= 0xEF;
            		mService.write(cmd);           //取消倍高、倍宽模式
            		msg = "  You have sucessfully created communications between your device and our bluetooth printer.\n\n"
                          +"  the company is a high-tech enterprise which specializes" +
                          " in R&D,manufacturing,marketing of thermal printers and barcode scanners.\n\n";
                         

            		mService.sendMessage(msg,"GBK");
            	}else if((lang.compareTo("ch")) == 0){
            		cmd[2] |= 0x10;
            		mService.write(cmd);           //倍宽、倍高模式
        		    mService.sendMessage("恭喜您！\n", ENCODING);
            		cmd[2] &= 0xEF;
            		mService.write(cmd);           //取消倍高、倍宽模式
            		msg = "  您已经成功的连接上了我们的蓝牙打印机！\n\n"
            		+ "  本公司是一家专业从事研发，生产，销售商用票据打印机和条码扫描设备于一体的高科技企业.\n\n";
            	    
            		mService.sendMessage(msg,"GBK");	
            	}
			}
		}
	}
	
    /**
     * 创建一个Handler实例，用于接收BluetoothService类返回回来的消息
     */
    private final  Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case BluetoothService.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:   //已连接
                	Toast.makeText(getApplicationContext(), "Connect successful",
                            Toast.LENGTH_SHORT).show();
        			btnClose.setEnabled(true);
        			btnSend.setEnabled(true);
        			btnSendDraw.setEnabled(true);
                    break;
                case BluetoothService.STATE_CONNECTING:  //正在连接
                	Log.d("蓝牙调试","正在连接.....");
                    break;
                case BluetoothService.STATE_LISTEN:     //监听连接的到来
                case BluetoothService.STATE_NONE:
                	Log.d("蓝牙调试","等待连接.....");
                    break;
                }
                break;
            case BluetoothService.MESSAGE_CONNECTION_LOST:    //蓝牙已断开连接
                Toast.makeText(getApplicationContext(), "Device connection was lost",
                               Toast.LENGTH_SHORT).show();
    			btnClose.setEnabled(false);
    			btnSend.setEnabled(false);
    			btnSendDraw.setEnabled(false);
                break;
            case BluetoothService.MESSAGE_UNABLE_CONNECT:     //无法连接设备
            	Toast.makeText(getApplicationContext(), "Unable to connect device",
                        Toast.LENGTH_SHORT).show();
            	break;
            }
        }
        
    };
        
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {    
        switch (requestCode) {
        case REQUEST_ENABLE_BT:      //请求打开蓝牙
            if (resultCode == Activity.RESULT_OK) {   //蓝牙已经打开
            	Toast.makeText(this, "Bluetooth open successful", Toast.LENGTH_LONG).show();
            } else {                 //用户不允许打开蓝牙
            	finish();
            }
            break;
        case  REQUEST_CONNECT_DEVICE:     //请求连接某一蓝牙设备
        	if (resultCode == Activity.RESULT_OK) {   //已点击搜索列表中的某个设备项
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);  //获取列表项中设备的mac地址
                con_dev = mService.getDevByMac(address);   
                
                mService.connect(con_dev);
            }
            break;
        }
    } 
    
    //打印图形
	private void printImage() {
    	byte[] sendData = null;
    	Print_pic pg = new Print_pic();
        Bitmap bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.android_logo);
        int height = D58MMWIDTH * bm.getHeight() / bm.getWidth();
        bm = Bitmap.createScaledBitmap(bm, D58MMWIDTH, height, false);
        sendData = PrintPicture.POS_PrintBMP(bm, D58MMWIDTH, 0);
    	mService.write(sendData);   //打印byte流数据
    }
}
