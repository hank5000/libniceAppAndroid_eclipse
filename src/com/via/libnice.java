package com.via;
import java.nio.ByteBuffer;

import android.util.Log;

import com.example.libnice.MainActivity;
import com.gstreamer.GStreamer;

public class libnice {
	static {
		System.loadLibrary("gstreamer_android");
		System.loadLibrary("nice4android");
	}
	
	private native int initNative();
	private native int createAgentNative(int useReliable);
	private native int setStunAddressNative(String stun_ip,int stun_port);
	private native int setControllingModeNative(int controllingMode);
	private native int addStreamNative(String streamName, int numberOfComponent);
	private native String getLocalSdpNative();
	private native int setRemoteSdpNative(String jremoteSdp,long Size);
	private native int sendMsgNative(String data,int compId);
	private native int sendDataNative(byte[] data,int len ,int compId);
	private native int sendDataDirectNative(ByteBuffer data, int len, int compId);
	private native int sendVideoDataDirectNative(ByteBuffer data,int len, int compId);

	private native int mainLoopStart();
	private native int mainTest();

	private native void registerReceiveObserverNative(libnice.ReceiveObserver obs,int compId);
	private native void registerStateObserverNative(libnice.StateObserver obs);
	
	
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
	
	public int init() {
		int ret = initNative();
		if(ret==1)
			mainLoopThread.start();
		return ret;
	}
	
	public int createAgent(int useReliable) {
		return createAgentNative(useReliable);
	}
	
	public int setStunAddress(String stun_ip, int stun_port) {
		return setStunAddressNative( stun_ip, stun_port);
	}
	
	public int setControllingMode(int controllingMode) {
		return setControllingModeNative(controllingMode);
	}
	
	public int addStream(String streamName, int numberOfComponent) {
		return addStreamNative(streamName, numberOfComponent);
	}
	
	public String getLocalSdp() {
		return getLocalSdpNative();
	}
	
	public void setRemoteSdp(String remoteSdp) {
		setRemoteSdpNative(remoteSdp,remoteSdp.length());
	}
	
	public void sendData(byte[] buf,int len ,int compId) {
		//sendMsgNative(msg,compId);
		//byte[] a = msg.getBytes();
		sendDataNative(buf,len,compId);
	}
	
	public void sendDataDirect(ByteBuffer buf, int len, int compId) {
		sendDataDirectNative(buf,len,compId);
	}
	
	public void sendVideoDataDirect(ByteBuffer buf, int len, int compId) {
		sendVideoDataDirectNative(buf,len,compId);
	}
	
	public void sendMsg(String msg, int compId) {
		sendMsgNative(msg, compId);
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
	
	public void registerReceiveObserver(libnice.ReceiveObserver obs ,int compId) {
		this.registerReceiveObserverNative(obs,compId);
	}
	
	public void registerStateObserver(libnice.StateObserver stateObserver) {
		this.registerStateObserverNative(stateObserver);
	}
		
	public interface ReceiveObserver{
		void obCallback(byte[] msg);
	}
	
	public interface StateObserver {
		String[] STATE_TABLE = {"disconnected", "gathering", "connecting",
                "connected", "ready", "failed"};
		
		void cbCandidateGatheringDone(int stream_id);
		void cbComponentStateChanged(int stream_id,int component_id,int state);
	}
	
	public interface ReceiveCallback {
		void onMessage(byte[] buf,int use_len);
	}
}
