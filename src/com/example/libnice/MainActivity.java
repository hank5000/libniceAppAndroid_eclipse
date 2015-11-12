package com.example.libnice;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Hashtable;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
	MainActivity instance = this;
	ImageView qrView = null;
	SurfaceView qrSfView = null;
	SurfaceView videoSurfaceView = null;
	int stream_id = 0;
	
	boolean bSourceSide = false;
	Handler handle = new Handler();
	String remoteSdp = "";

	Runnable serverTask = new Runnable(){
		@Override
		public void run() {
			try {
				String method = "Server";
				String postParameters = "register="+URLEncoder.encode("FALSE", "UTF-8")
						       +"&username="+URLEncoder.encode("HankWu","UTF-8");
				String remoteSDP = QueryToServer.excutePost(method, postParameters);
				if(remoteSDP.equals("NOBODY")) {
					showToast("No Remote SDP");
				} else {
					showToast("Get remote SDP "+remoteSDP);
					remoteSdp = remoteSDP;
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	
	Runnable clientTask = new Runnable(){
		@Override
		public void run() {
			try {
				String method = "Client";
				String findusername = "HankWu";
				String postParameters = "findusername="+URLEncoder.encode(findusername,"UTF-8") + "&SDP="+URLEncoder.encode(sdp,"UTF-8");
				String remoteSDP = QueryToServer.excutePost(method, postParameters);
				if(remoteSDP.equals("OFFLINE")) {
					showToast(findusername+"is OFFLINE");
				} else {
					showToast("Get remote SDP "+remoteSDP);
					remoteSdp = remoteSDP;
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};


//	public static Bitmap encodeToQrCode(String text, int width, int height){
//	    QRCodeWriter writer = new QRCodeWriter();
//	    BitMatrix matrix = null;
//	    try {
//	        matrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);
//	    } catch (WriterException ex) {
//	        ex.printStackTrace();
//	    }
//	    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//	    for (int x = 0; x < width; x++){
//	        for (int y = 0; y < height; y++){
//	            bmp.setPixel(x, y, matrix.get(x,y) ? Color.BLACK : Color.WHITE);
//	        }
//	    }
//	    return bmp;
//	}
	
	OnClickListener initListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			nice.init();
			int useReliable = 1;
			nice.createAgent(useReliable);
			nice.setStunAddress(STUN_IP, STUN_PORT);
			int controllMode = 0;// 0 => controlling, 1=>controlled
			nice.setControllingMode(controllMode);
			String streamName = "HankWu";
			int numberOfComponent = 2;
			// TODO: return stream id
			stream_id = nice.addStream(streamName,numberOfComponent);
			// register a receive Observer to get byte array from jni side to java side.
			int forComponentIndex = 1;
			nice.registerReceiveCallback(new RecvCallback(), stream_id,forComponentIndex);
//			forComponentIndex = 2;
//			nice.registerReceiveCallback(new RecvCallback(), stream_id,forComponentIndex);
			
			// register a state Observer to catch stream/component state change
			nice.registerStateObserver(new StateObserver());
			// TODO: add stream id, each stream has self SDP. 
			sdp = nice.getLocalSdp(stream_id);
			Thread a =new Thread(new Runnable(){
				@Override
				public void run() {
					if(bSourceSide) {
						// Send SDP to Server
						String method = "Server";
						String postParameters;
						try {
							postParameters = "register="+URLEncoder.encode("TRUE", "UTF-8")
									       +"&username="+URLEncoder.encode("HankWu","UTF-8")
									       +"&SDP="+URLEncoder.encode(sdp,"UTF-8");
							QueryToServer.excutePost(method, postParameters);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
			a.start();
			
			
			// get sdp qrcode bitmap and show on surfaceView
//			instance.runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					Bitmap bmp = encodeToQrCode(sdp,600,600);
//					qrSfView.setBackground(new BitmapDrawable(getResources(),bmp));
//				}
//			});
		}
	};
	
	OnClickListener getListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
//
//					Intent intent = new Intent("com.google.zxing.client.android.SCAN");
//					if(getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size()==0)
//					{
//						LOGD("please install ZXing QRCode");
//					}
//					else
//					{
//							// SCAN_MODE, 可判別所有支援的條碼
//							// QR_CODE_MODE, 只判別 QRCode
//							// PRODUCT_MODE, UPC and EAN 碼
//							// ONE_D_MODE, 1 維條碼
//							intent.putExtra("QR_CODE_MODE", "QR_CODE_MODE");
//							// 呼叫ZXing Scanner，完成動作後回傳 1 給 onActivityResult 的 requestCode 參數
//							startActivityForResult(intent, 1);
//					}
			if(bSourceSide) {
				(new Thread(serverTask)).start();
			} else {
				(new Thread(clientTask)).start();
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
					nice.setRemoteSdp(remoteSdp);

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

    //ByteBuffer naluBuffer = ByteBuffer.allocate(1024 * 1024);
    ByteBuffer naluBuffer = ByteBuffer.allocateDirect(1024*1024);
	
	int DEFAULT_DIVIDED_SIZE = 1024*1024;
	
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
					nice.sendMsg(sendmsg,stream_id,1);
					AddTextToChat("Me:"+sendmsg);
			    }
			    });
			   editDialog.setNeutralButton("play video", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					final String path = editText.getText().toString();

					Thread a = new Thread(new Runnable(){
						@Override
						public void run() {
							int counter = 0;
							if(!bInit) {
								initMediaExtractor(path);
								bInit = true;
							}
							for(;;){
								// TODO Auto-generated method stub
								int naluSize = me.readSampleData(naluBuffer, 0);
								
								
								LOGD("Sent naluSize : "+naluSize);
								int divideSize = DEFAULT_DIVIDED_SIZE;
								int sentSize = 0;
								//nice.sendMsg("NALU", 1);
								
								//for(;;) {
								if(naluSize > 0)
								{
									for(;;) {
										if((naluSize-sentSize) < divideSize) {
											divideSize = naluSize-sentSize;
										}
										
										naluBuffer.position(sentSize);
										naluBuffer.limit(divideSize+sentSize);
										// Reliable mode : if send buffer size bigger than MTU, the destination side will received data partition which is divided by 1284.
										// Normal mode   : if send buffer size bigger than MTU, the destination side will received all data in once receive.
										nice.sendDataDirect(naluBuffer.slice(),divideSize,stream_id,1);
										
										naluBuffer.limit(naluBuffer.capacity());
				
					                    sentSize += divideSize;
										if(sentSize >= naluSize) {
											break;
										}
									}
									me.advance();

									try {
										Thread.sleep(33);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

								} else {
									me.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
								}
							}
						}
					});
					a.start();
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
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		initBtn = (Button) findViewById(R.id.initBtn);
		getBtn = (Button) findViewById(R.id.getBtn);
		setBtn = (Button) findViewById(R.id.setBtn);
		sendBtn = (Button) findViewById(R.id.sendBtn);

		qrSfView = (SurfaceView) findViewById(R.id.QRCodeSurfaceView);
		videoSurfaceView = (SurfaceView) findViewById(R.id.VideoSurfaceView);

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
			nice.init();
			//sdp = nice.jcreateNiceAgentAndGetSdp(STUN_IP, STUN_PORT);

			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
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

	
	public class StateObserver implements libnice.StateObserver {
			@Override
			public void cbComponentStateChanged(final int stream_id, final int component_id,
					final int state) {
				Log.d("cbComponentStateChanged","Stream["+stream_id+"]["+component_id+"]:"+libnice.StateObserver.STATE_TABLE[state]);
				
				instance.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						Toast.makeText(instance, "comp id:"+component_id+" is "+libnice.StateObserver.STATE_TABLE[state], Toast.LENGTH_LONG).show();
					}
				});
			}
			
			@Override
			public void cbCandidateGatheringDone(int stream_id) {
				// TODO Auto-generated method stub
				Log.d("cbCandidateGatheringDone","Candidate Gathering Done Stream "+stream_id);
			}
	}
	MediaPlayer mMediaPlayer;
	public class RecvCallback implements libnice.ReceiveCallback {
			boolean bVideo = false;
			int w = 0;
			int h = 0;
			String sps = null;
			String pps = null;
			String mime= null;
			
		    LocalServerSocket mLss = null;
		    LocalSocket mReceiver = null;
		    LocalSocket mSender   = null;
		    int         mSocketId;
		    final String LOCAL_ADDR = "DataChannelToVideoDecodeThread-";
		    public OutputStream os = null;
		    public WritableByteChannel writableByteChannel;
		    public InputStream is = null;
		    VideoThread vt = null;
			

		    @Override
			public void onMessage(byte[] msg) {
		    	
				if(!bVideo) {
					LOGD("not video");
					String tmp = new String(msg);
					if(tmp.startsWith("Video")) {
						bVideo = true;
						String[] tmps = tmp.split(":");
						mime = tmps[1];
						w = Integer.valueOf(tmps[2]);
						h = Integer.valueOf(tmps[3]);
						sps = tmps[4];
						pps = tmps[5];
//						
                        for (int jj = 0; jj < 10; jj++) {
                            try {
                                mSocketId = new Random().nextInt();
                                mLss = new LocalServerSocket(LOCAL_ADDR + mSocketId);
                                break;
                            } catch (IOException e) {
                                LOGD("fail to create localserversocket :" + e);
                            }
                        }
                        //    DECODE FLOW
                        //
                        //    Intermediary:                             Localsocket       MediaCodec inputBuffer     MediaCodec outputBuffer
                        //        Flow    : Data Channel =======> Sender ========> Receiver ==================> Decoder =================> Display to surface/ Play by Audio Track
                        //       Thread   : |<---Data Channel thread--->|          |<--------- Decode Thread --------->|                 |<--------- Display/play Thread -------->|
                        //
                        mReceiver = new LocalSocket();
                        try {
                            mReceiver.connect(new LocalSocketAddress(LOCAL_ADDR + mSocketId));
                            mReceiver.setReceiveBufferSize(100000);
                            mReceiver.setSoTimeout(2000);
                            mSender = mLss.accept();
                            mSender.setSendBufferSize(100000);
                        } catch (IOException e) {
                            LOGD("fail to create mSender mReceiver :" + e);
                            e.printStackTrace();
                        }
                        try {
                            os = mSender.getOutputStream();
                            writableByteChannel = Channels.newChannel(os);
                            is = mReceiver.getInputStream();
                        } catch (IOException e) {
                            LOGD("fail to get mSender mReceiver :" + e);
                            e.printStackTrace();
                        }
                        
                        vt = new VideoThread(videoSurfaceView.getHolder().getSurface(),mime, w,h,sps,pps,is);
                        vt.start();
					}
					LOGD(tmp);
					AddTextToChat(tmp);
				} else {
					//LOGD("video");

					try {
						writableByteChannel.write(ByteBuffer.wrap(msg));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						LOGD("os write fail"+e);
					}
				}
			}

	}

	
	MediaExtractor me = new MediaExtractor();
	boolean bInit = false;
	void initMediaExtractor(String path) {
		try {
			me.setDataSource(path);
			MediaFormat mf = null;
			String mime = null;
			String videoMsg = "Video";
			int w = 0;
			int h = 0;
			String s_sps = null;
			String s_pps = null;
			
			for(int i=0;i<me.getTrackCount();i++) {
				mf = me.getTrackFormat(i);
				mime = mf.getString(MediaFormat.KEY_MIME);

					
				if(mime.startsWith("video")) {
					me.selectTrack(i);
					mime = mf.getString(MediaFormat.KEY_MIME);

					w = mf.getInteger(MediaFormat.KEY_WIDTH);
					h = mf.getInteger(MediaFormat.KEY_HEIGHT);
					
					ByteBuffer sps_b = mf.getByteBuffer("csd-0");
			        byte[] sps_ba = new byte[sps_b.remaining()];
			        sps_b.get(sps_ba);
			        s_sps = bytesToHex(sps_ba);
					
			        mf.getByteBuffer("csd-1");
			        ByteBuffer pps_b = mf.getByteBuffer("csd-1");
			        byte[] pps_ba = new byte[pps_b.remaining()];
			        pps_b.get(pps_ba);
			        s_pps = bytesToHex(pps_ba);
			        
			        videoMsg = videoMsg + ":" + mime + ":" + w + ":" + h + ":" + s_sps + ":" + s_pps + ":";
			        
			        nice.sendMsg(videoMsg, stream_id,1);
					
					break;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	  final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	  public static String bytesToHex(byte[] bytes) {
		    char[] hexChars = new char[bytes.length * 2];
		    for ( int j = 0; j < bytes.length; j++ ) {
		      int v = bytes[j] & 0xFF;
		      hexChars[j * 2] = hexArray[v >>> 4];
		      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		    }
		    return new String(hexChars);
		  }
	  
	  public void showToast(final String tmp) {
		  runOnUiThread(new Runnable(){
			@Override
			public void run() {
				Toast.makeText(instance, tmp, Toast.LENGTH_LONG).show();
			}
		  });
	  }
	

}
