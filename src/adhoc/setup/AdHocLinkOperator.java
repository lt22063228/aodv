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
					//查询ackList，若值为false，则表示没有收到下标作为IP的节点对消息的回复
					for (int i = 1; i < 255; i++) {
						if(!Constants.ackList.get(i)) {
//							Log.d(MSG_TAG, "not got DIR_LINK_CONFIRM_REPLY from node : " + i);
							//从直连节点中移除该节点
							Constants.dirlinkList.remove(new Integer(i));
							//更新ListView
//							adhoc.updateRemoveListView(i , Constants.DIRLINK_LISTVIEW);
						}
					}
				}

				
				//此后应该启用一个新的线程，判定是否自身还有直连节点
				//若有：则对删除掉的直连节点进行路由查找，查看此节点是否还在网络中，若不在网络中则帮此节点释放IP
				//若无：则此节点陷入孤立，节点状态转入孤立状态，进行孤立状态处理，具体处理过程待定...
				
				//如何判断是否因为链接的断掉造成网络的分割，如果造成了分割怎么处理，或者不处理？？？
				
				
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
