package adhoc.setup;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import adhoc.constants.Constants;
import android.util.Log;

public class AdHocBroadcastSender {
	
	private static final String MSG_TAG = "AdHocBroadcastSender --> ";
	private String subNet = "192.168.2.";
	private AdHocSetup application;
	
	public AdHocBroadcastSender(AdHocSetup app) {
		this.application = app;
	}
	
	public boolean sendPacket (int destinationIp, int type, String content) throws IOException, SocketException, UnknownHostException, BindException {
		String msgType = "" + type;
		String msgIp = "" + application.getIpLastField();
		String msg = msgType + "/" + msgIp + "/" + content;
		byte data[] = msg.getBytes();
		InetAddress IPAddress  = InetAddress.getByName(subNet + destinationIp);
		DatagramSocket socket = new DatagramSocket();
		DatagramPacket datagramPacket = new DatagramPacket(data, data.length, IPAddress, Constants.DHCP_RECEIVE_PORT);
		socket.send(datagramPacket);
		socket.close();
		return true;
	}
	
	public void startBroadcast(int type, String content) throws BindException, SocketException, UnknownHostException, IOException, InterruptedException {
		//发送广播消息
		String msgType = "" + type;
		String msgIp = "" + application.getIpLastField();
		String msg = msgType + "/" + msgIp + "/" + content;
		byte data[] = msg.getBytes();
		InetAddress IPAddress  = InetAddress.getByName(subNet + "255");
		DatagramSocket socket = new DatagramSocket();
		socket.setBroadcast(true);
		for(int count = 0; count < 5; count++) {
			DatagramPacket packet = new DatagramPacket(data, data.length, IPAddress, 8765);
			socket.send(packet);
			Log.d(MSG_TAG, "round " + count + " broadcast has been send ...");
			Thread.sleep(200);
		}
		socket.close();
		Log.d(MSG_TAG, "all broadcast message has been send ...");
//		for(int count = 0; count < 3; count++) {
//			for(int i = 1; i < 254; i++) {
//				this.sendPacket(i, type, content);
//				Thread.sleep(1);
//			}
//			Log.d(MSG_TAG, "round " + count + " broadcast has been send ...");
//		}
//		Log.d(MSG_TAG, "all broadcast message has been send ...");
	}
	
	public void startAreaBroadcast(int type, String content, int counts) throws BindException, SocketException, UnknownHostException, IOException, InterruptedException {

		synchronized(Constants.dirlinkList) {
			//局域广播，即：向所有直连节点发送三轮UDP消息
			for(int count = 0; count < counts; count ++) {
				for (int itr : Constants.dirlinkList) {
					if (!Constants.ackList.get(itr)) {
//						Log.d(MSG_TAG, "broadcast message : " + type + " to destination : " + itr + "  has been send ...");
//						Log.d(MSG_TAG, "waiting for reply from node : " + itr);
						this.sendPacket(itr, type, content);
					}
				}
//				Log.d(MSG_TAG, "round : " + count + " area broadcast has been send ...");
				Thread.sleep(50);
			}
		}

	}
	
	public void startNoAckAreaBroadcast(int type, String content) throws BindException, SocketException, UnknownHostException, IOException, InterruptedException {

		//局域广播，即：向所有直连节点发送三轮UDP消息
		synchronized (Constants.dirlinkList) {
			for(int count = 0; count < 2; count ++) {
				for (int itr : Constants.dirlinkList) {
					Log.d(MSG_TAG, "Msg send to "+itr);
					this.sendPacket(itr, type, content);
//					Log.d(MSG_TAG, "broadcast message : " + type + " to destination : " + itr + "  has been send ...");
				}
//				Log.d(MSG_TAG, "round : " + count + " area broadcast has been send ...");
				Thread.sleep(50);
			}
		}

	}
}
