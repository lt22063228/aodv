package adhoc.setup;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


import adhoc.constants.Constants;
import android.util.Log;

public class AdHocBroadcastReceiver implements Runnable {
	//ʵ��Runnable�ӿ�
	private static final String MSG_TAG = "AdHocBroadcastReceiver --> ";
	private DatagramSocket socket;
	//��ʼ��һ��UDP��Ϣ������
	private AdHocMessageHandler handler;
	//����UDP��Ϣ�����߳�
	private Thread udpBroadcastReceiverThread;
	//run()������whileѭ����־
	private boolean keepRunning;
	//�߳��Ƿ�����������״̬��־
	private boolean isBlocked;
	//�洢����������Ϣ
	private String receivedMsg;
	public AdHocBroadcastReceiver(AdHocMessageHandler handle) {
		Log.d(MSG_TAG, "calling UdpBroadcastReceiver construct function");
		this.handler = handle;
		this.keepRunning = true;
		this.isBlocked = false;
		this.receivedMsg = "";
	}
	//�����㲥����
	public void startBroadcastListening(){
		this.keepRunning = true;
		udpBroadcastReceiverThread = new Thread(this);
		udpBroadcastReceiverThread.start();
	}
	//ֹͣ�㲥����
	public void stopBroadcastListening(){
		this.keepRunning = false;
		if(this.isBlocked) {
			socket.close();
			udpBroadcastReceiverThread.interrupt();
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		byte[] buffer = new byte[1024];
		try {
			socket = new DatagramSocket(Constants.DHCP_RECEIVE_PORT);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(keepRunning){	
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			try {
				this.isBlocked = true;				
//				Log.d(MSG_TAG, "broadcast udp receiver listening ...");
				socket.receive(packet);
//				Log.d(MSG_TAG, "package received ...");				
				this.isBlocked = false;
				
				int length = packet.getLength();
				
				if(length > 0) {
					receivedMsg = new String(buffer, 0, packet.getLength());
//					Log.d(MSG_TAG, "got message : " + receivedMsg);
					//������������Ϣ������Ϣ�������
					handler.addMessage(receivedMsg);
//					Log.d(MSG_TAG, "message has add to the message queue ...");
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		socket.close();
	}

}
