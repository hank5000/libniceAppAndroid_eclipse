package com.example.libnice;


import java.io.File;
import java.util.Hashtable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.via.libnice;
import com.google.zxing.*;
// avoid gstreamer_android cannot find issue.
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class MainActivity extends Activity {
	libnice nice = new libnice();
	Handler handler = new Handler();
	Button initBtn = null;
	Button getBtn  = null;
	Button setBtn = null;
	Button sendBtn = null;
	TextView resultView = null;
	//SurfaceView qrView = null;
	//sSurfaceHolder sh = null;
	MainActivity instance = this;
	ImageView qrView = null;
	
	public static Bitmap encodeToQrCode(String text, int width, int height){
	    QRCodeWriter writer = new QRCodeWriter();
	    BitMatrix matrix = null;
	    try {
	        matrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);
	    } catch (WriterException ex) {
	        ex.printStackTrace();
	    }
	    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
	    for (int x = 0; x < width; x++){
	        for (int y = 0; y < height; y++){
	            bmp.setPixel(x, y, matrix.get(x,y) ? Color.BLACK : Color.WHITE);
	        }
	    }
	    return bmp;
	}
	
	OnClickListener initListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			nice.libInit();
			sdp = nice.jcreateNiceAgentAndGetSdp(STUN_IP, STUN_PORT);
			nice.registerObserver(new obser());
			//TODO: generate an QR code and render to imageView.

			
			instance.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					Bitmap bmp = encodeToQrCode(sdp,420,420);
					qrView.setImageBitmap(bmp);
				}
				
			});
			

		}
	};
	
	
	OnClickListener getListener = new OnClickListener(){

		@Override
		public void onClick(View v) {

					Intent intent = new Intent("com.google.zxing.client.android.SCAN");
					if(getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size()==0)
					{
						LOGD("please install ZXing QRCode");
					}
					else
					{
							// SCAN_MODE, 可判別所有支援的條碼
							// QR_CODE_MODE, 只判別 QRCode
							// PRODUCT_MODE, UPC and EAN 碼
							// ONE_D_MODE, 1 維條碼
							intent.putExtra("SCAN_MODE", "SCAN_MODE");
							// 呼叫ZXing Scanner，完成動作後回傳 1 給 onActivityResult 的 requestCode 參數
							startActivityForResult(intent, 1);
					}
			
		}
	};
	
	OnClickListener setListener = new OnClickListener(){

		@Override
		public void onClick(View v) {

			   AlertDialog.Builder editDialog = new AlertDialog.Builder(MainActivity.this);
			   editDialog.setTitle("--- send remote sdp ---");
			   				   
			   editDialog.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
			    // do something when the button is clicked
			    public void onClick(DialogInterface arg0, int arg1) {
					nice.jsetRemoteSdp(remoteSdp);	
			    }
			    });
			   editDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
			          // do something when the button is clicked
			    public void onClick(DialogInterface arg0, int arg1) {
					
			    }
			    });
			   editDialog.show();
			
		}
	};
	

	
	OnClickListener sendListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			AlertDialog.Builder editDialog = new AlertDialog.Builder(MainActivity.this);
			   editDialog.setTitle("--- send message ---");
			   
			   final EditText editText = new EditText(MainActivity.this);
			   editDialog.setView(editText);
			   
			   editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			    // do something when the button is clicked
			    public void onClick(DialogInterface arg0, int arg1) {
			    	String sendmsg = editText.getText().toString();
					nice.jsendData(sendmsg);
					AddTextToChat("Me:"+sendmsg);
			    }
			    });
			   editDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			          // do something when the button is clicked
			    public void onClick(DialogInterface arg0, int arg1) {
			    
			    }
			    });
			   editDialog.show();
						
		}
	};
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initBtn = (Button) findViewById(R.id.initBtn);
		getBtn = (Button) findViewById(R.id.getBtn);
		setBtn = (Button) findViewById(R.id.setBtn);
		sendBtn = (Button) findViewById(R.id.sendBtn);
		qrView = (ImageView) findViewById(R.id.QRCodeView);
		resultView = (TextView) findViewById(R.id.textView2);
		initBtn.setOnClickListener(initListener);
		getBtn.setOnClickListener(getListener);
		setBtn.setOnClickListener(setListener);
		sendBtn.setOnClickListener(sendListener);
		nice.setAct(instance);

	}

	public void AddTextToChat(final String msg) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				resultView.setText(msg +"\n"+ resultView.getText().toString());
			}
			
		});
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	String STUN_IP 	= "74.125.204.127";
	int    STUN_PORT= 19302;

	private void LOGD(String msg) {
		Log.d("Libnice-java",msg);
	}
	String sdp = "";
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			nice.libInit();
			sdp = nice.jcreateNiceAgentAndGetSdp(STUN_IP, STUN_PORT);

			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	String remoteSdp = "";
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(requestCode == 1) {
			if(resultCode==RESULT_OK) {
				final String contents = intent.getStringExtra("SCAN_RESULT");
				TextView tv = (TextView) findViewById(R.id.textView1);
				tv.setText(contents);
				remoteSdp = contents;
			}
			
			
			
		}
	}

	
	public class obser implements libnice.Observer {

		@Override
		public void obCallback(byte[] msg) {
			// TODO Auto-generated method stub
			Log.d("obser","haha:"+new String(msg));
		}
	}

}
