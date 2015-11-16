package com.example.libnice;


import com.via.libnice;
public class CommunicationPart implements libnice.ReceiveCallback {
	int COMMUNICATION_COMPONENT_ID = -1;
	int STREAM_ID = -1;
	final static int STREAM_ID_CLIENT_PART = 1;
	// TODO: maybe server has a lot stream id, it may be set by...?
	int STREAM_ID_SERVER_PART = 1; 
	MainActivity mAct = null;
	libnice mNice = null;
	String loggingMessage = "";

	public CommunicationPart(MainActivity ma,libnice nice,int streamId,int compId) {
		mAct = ma;
		mNice = nice;
		STREAM_ID = streamId;
		COMMUNICATION_COMPONENT_ID = compId;
	}

	@Override
	public void onMessage(byte[] buf) {
		String tmp = new String(buf);
		loggingMessage += tmp + "\n";
		if(tmp.startsWith("VIDEOSTART")) {
			
		}
	}

	public void sendMessage(String msg) {
		mNice.sendMsg(msg, STREAM_ID_CLIENT_PART, COMMUNICATION_COMPONENT_ID);
	}
}
