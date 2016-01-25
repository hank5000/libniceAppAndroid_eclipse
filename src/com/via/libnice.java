package com.via;
import java.nio.ByteBuffer;

import android.util.Log;
import com.gstreamer.GStreamer;

public class libnice {
	static {
		System.loadLibrary("gstreamer_android");
		System.loadLibrary("nice4android");
	}
	
	final static int MAX_STREAM = 20;
	final static int MAX_COMPONENT = 20;
	
	String[] mStreamName = new String[MAX_STREAM];
	String[][] mComponentName = new String[MAX_STREAM][MAX_COMPONENT];
	long agentCtxHandle = 0;

	private native int initNative();
	private native long /*agent handle*/ createAgentNative(int useReliable);
	private native int setStunAddressNative(long agentHandle,String stun_ip,int stun_port);
	private native int setControllingModeNative(long agentHandle,int controllingMode);
	private native int /*stream id*/ addStreamNative(long agentHandle,String streamName, int numberOfComponent); // return stream id which is signed by libnice
	private native String /*sdp which is encoded by base 64*/ getLocalSdpNative(long agentHandle,int stream_id);
	private native int setRemoteSdpNative(long agentHandle,String jremoteSdp,long Size);
	private native int sendMsgNative(long agentHandle,String data,int streamId,int compId);
	private native int sendDataNative(long agentHandle,byte[] data,int len ,int streamId,int compId);
	private native int sendDataDirectNative(long agentHandle,ByteBuffer data, int len ,int streamId,int compId);
	private native int setDirectBufferIndexNative(long agentHandle,ByteBuffer data, int index);
	private native int sendDataDirectByIndexNative(long agentHandle,ByteBuffer data, int len ,int index,int streamId,int compId);
	private native int restartStreamNative(long agentHandle,int streamId);
	
	public int restartStream(int streamId) {
		return restartStreamNative(agentCtxHandle,streamId);
	}

	public int sendDataDirectByIndex(ByteBuffer data,int len,int index,int streamId,int CompId) {
		return sendDataDirectByIndexNative(agentCtxHandle,data,len,index,streamId,CompId);
	}
	
	public int setDirectBufferIndex(ByteBuffer data,int index) {
		return setDirectBufferIndexNative(agentCtxHandle,data,index);
	}
	
	
	private native int mainLoopStart();
	// register callback function for stream[streamId],component[compId]
	private native void registerReceiveCallbackNative(long agentHandle,libnice.ReceiveCallback recv_cb_obj,int streamId,int compId);
	private native void registerStateObserverNative(long agentHandle,libnice.StateObserver obs);
	

	private static int a = 0;
	Thread mainLoopThread = new Thread(new Runnable(){
		@Override
		public void run() {
			// Just use it to run gloop
			mainLoopStart();
		}
	});

	public int init() {
		int ret = initNative();
		if(ret==1)
			mainLoopThread.start();
		return ret;
	}

	public int createAgent(int useReliable) {
		agentCtxHandle = createAgentNative(useReliable);
		return 1;
	}

	public int setStunAddress(String stun_ip, int stun_port) {
		return setStunAddressNative(agentCtxHandle,stun_ip, stun_port);
	}

	public int setControllingMode(int controllingMode) {
		return setControllingModeNative(agentCtxHandle,controllingMode);
	}

	public int addStream(String streamName, int numberOfComponent) {
		return addStreamNative(agentCtxHandle,streamName, numberOfComponent);
	}

	public String getLocalSdp(int streamId) {
		return getLocalSdpNative(agentCtxHandle,streamId);
	}

	public void setRemoteSdp(String remoteSdp) {
		setRemoteSdpNative(agentCtxHandle,remoteSdp,remoteSdp.length());
	}

	public void sendData(byte[] buf,int len,int streamId ,int compId) {
		sendDataNative(agentCtxHandle,buf,len,streamId,compId);
	}
	
	public void sendDataDirect(ByteBuffer buf, int len,int streamId, int compId) {
		sendDataDirectNative(agentCtxHandle,buf,len,streamId,compId);
	}
	
	
	public void sendMsg(String msg, int streamId,int compId) {
		sendMsgNative(agentCtxHandle,msg,streamId,compId);
	}

	public void registerReceiveCallback(libnice.ReceiveCallback obs,int streamId ,int compId) {
		this.registerReceiveCallbackNative(agentCtxHandle,obs,streamId,compId);
	}
	
	public void registerStateObserver(libnice.StateObserver stateObserver) {
		this.registerStateObserverNative(agentCtxHandle,stateObserver);
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
		void onMessage(byte[] buf);
	}
}
