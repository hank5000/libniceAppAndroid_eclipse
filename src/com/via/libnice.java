package com.via;
import android.util.Log;

import com.example.libnice.MainActivity;
import com.gstreamer.GStreamer;

public class libnice {
	static {
		System.loadLibrary("gstreamer_android");
		System.loadLibrary("nice4android");
	}
	
	private native int Init();
	private native String createNiceAgentAndGetSdp(String stun_ip,int stun_port);
	private native int mainLoopStart();
	private native int mainTest();
	private native int setRemoteSdp(String jremoteSdp,long Size);
	private native int sendData(String data);
	static MainActivity act;
	
	public void setAct(MainActivity a) {
		act = a;
	}
	
	private static int a = 0;
	Thread mainLoopThread = new Thread(new Runnable(){
		@Override
		public void run() {
			// Just use it to run gloop
			mainLoopStart();
		}
	});
	
	public int main_test() {
		return mainTest();
	}
	
	public int libInit() {
		int ret = Init();
		if(ret==1)
			mainLoopThread.start();
		return ret;
	}
	
	public String jcreateNiceAgentAndGetSdp(String stun_ip,int stun_port) {
		return createNiceAgentAndGetSdp(stun_ip,stun_port);
	}
	
	public void jsetRemoteSdp(String remoteSdp) {
		setRemoteSdp(remoteSdp,remoteSdp.length());
	}
	
	public void jsendData(String msg) {
		sendData(msg);
	}

	public void jniCallBackMsg(String msg) {
		Log.d("libnice-cb ",msg);
	}
	
	public void jniCallBackInt(int i) {
		Log.d("libnice-cb ",i+"");
	}
	
	public static void jniCallBackMsgStatic(byte[] msg) {
		Log.d("libnice-cb-static", new String(msg));
		act.AddTextToChat("From:"+new String(msg));
	}
	
	public void registerObserver(libnice.Observer obs) {
		this.registerObserverNative(obs);
	}
	
	private native void registerObserverNative(libnice.Observer obs);
	
	public interface Observer{
		void obCallback(byte[] msg);
	}
	
}
