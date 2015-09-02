package adhoc.setup;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import adhoc.constants.Constants;

public class AdHocLinkOperator implements Runnable {
	
//	private static final String MSG_TAG = "AdHocLinkOperator --> ";
	
	private AdHocBroadcastSender sender;
	private AdHocSetup application;
	private Thread operatorThread;
	private Boolean keepRunning;
	private DatagramSocket socket;
	private DatagramPacket packet;
	
	public AdHocLinkOperator(AdHocSetup app) {
		this.application = app;
		this.sender = new AdHocBroadcastSender(application);
		this.keepRunning = true;

	}
	
	public void initAckList() {
		for(int i = 0; i < 255; i++) {
			Constants.ackList.set(i, true);
		}
		for(int itr : Constants.dirlinkList) {
//			Log.d(MSG_TAG, "ready to get DIR_LINK_CONFIRM_REPLY from node : " + itr + " ...");
			Constants.ackList.set(itr, false);
		}
	}
	
	public void startThread(){
		keepRunning = true;
		operatorThread = new Thread(this);
		operatorThread.start();
	}

	public void stopThread() {
		keepRunning = false;
		operatorThread.interrupt();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		String msgType = "" + Constants.DIRLINK_SEARCH;
		String msgIp = "" + application.getIpLastField();
		String msgContent = msgIp;
		String msg = msgType + "/" + msgIp + "/" + msgContent;
		
		try {
			InetAddress address = InetAddress.getByName("192.168.2.255");
			socket = new DatagramSocket();
			socket.setBroadcast(true);
			byte data[] = msg.getBytes();
			packet = new DatagramPacket(data, data.length, address, Constants.DHCP_RECEIVE_PORT);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		while(keepRunning) {
			try {
				Thread.sleep(1500);
				this.initAckList();
				sender.startAreaBroadcast(Constants.DIRLINK_CONFIRM, "" + application.getIpLastField(), 2);
				
//				Log.d(MSG_TAG, "dirlink confirm delay start ...");
				
				Thread.sleep(200);
				
//				Log.d(MSG_TAG, "dirlink confirm delay over ...");
				
				synchronized (Constants.dirlinkList) {
					//��ѯackList����ֵΪfalse�����ʾû���յ��±���ΪIP�Ľڵ����Ϣ�Ļظ�
					for (int i = 1; i < 255; i++) {
						if(!Constants.ackList.get(i)) {
//							Log.d(MSG_TAG, "not got DIR_LINK_CONFIRM_REPLY from node : " + i);
							//��ֱ���ڵ����Ƴ��ýڵ�
							Constants.dirlinkList.remove(new Integer(i));
							//����ListView
//							adhoc.updateRemoveListView(i , Constants.DIRLINK_LISTVIEW);
						}
					}
				}

				
				//�˺�Ӧ������һ���µ��̣߳��ж��Ƿ�������ֱ���ڵ�
				//���У����ɾ������ֱ���ڵ����·�ɲ��ң��鿴�˽ڵ��Ƿ��������У����������������˽ڵ��ͷ�IP
				//���ޣ���˽ڵ�����������ڵ�״̬ת�����״̬�����й���״̬�������崦����̴���...
				
				//����ж��Ƿ���Ϊ���ӵĶϵ��������ķָ�������˷ָ���ô�������߲���������
				
				
				socket.send(packet);
				
				Thread.sleep(500);
				

				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BindException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	

}
