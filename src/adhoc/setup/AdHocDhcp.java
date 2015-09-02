package adhoc.setup;

import java.io.IOException;
import java.net.BindException;
import java.net.SocketException;
import java.net.UnknownHostException;

import adhoc.constants.Constants;
import android.util.Log;

public class AdHocDhcp {
	
	private final static  String MSG_TAG = "AdHocDhcp --> ";
	private AdHocBroadcastSender sender;
	private AdHocSetup application;
	
	public AdHocDhcp(AdHocSetup app) throws BindException, SocketException, UnknownHostException, IOException {		
		Log.d(MSG_TAG, "calling construct funcition");
		this.application = app;
		this.sender = new AdHocBroadcastSender(application);
		//初始化bitArray
		Constants.BitArray.init();
		//初始化receiveIp和ackList，添加255个成员，分别全部初始化为false和true
		for(int i = 0; i < 255; i++) {
//			Constants.ipPond.add(true);
			Constants.receivedIp.add(false);
			Constants.ackList.add(true);
		}
		//清空直连节点IP列表
		Constants.dirlinkList.clear();
	}
	
	public void init() {
		//AdHocDhcp类初始化函数，和构造函数中的初始化步骤相同
		Constants.BitArray.init();
		for(int i = 0; i < 255; i++) {
//			Constants.ipPond.set(i, true);
			Constants.receivedIp.set(i, false);
			Constants.ackList.set(i,true);
		}
		Constants.dirlinkList.clear();
	}

	public int getReceivedIp() {
		//获取接收到的可用IP，从receivedIp列表中取值为true的最大下标作为可用IP，返回此IP
		//目的在于使当前节点处于两个AdHoc网络中间时，能够加入较大的网络
		int index = Constants.receivedIp.lastIndexOf(true);
//		Log.d(MSG_TAG, "the biggest received ip is : " + index);
		return index;
	}
	
	public int getIp() throws BindException, SocketException, UnknownHostException, IOException, InterruptedException {

		this.init();
		
		Log.d(MSG_TAG, "dhcp request has been sended ...");
		sender.startBroadcast(Constants.DHCP_REQUEST, "0");

		Log.d(MSG_TAG, "receive delay started ...");
		
		Thread.sleep(500);	
		
		Log.d(MSG_TAG, "receive delay over ...");
		
		if(this.getReceivedIp() == -1) {
			//若为-1，则表示没有收到可用IP，则返回可用IP为1
			Log.d(MSG_TAG, "not have ip received ..., initial ip to 192.168.2.1");
//			Constants.ipPond.set(1, false);
			//将bitArray中IP地址作为下标的相应位置1
			Constants.BitArray.set(1, true);
			return 1;
		}
		
		Log.d(MSG_TAG, "got the biggest received ip : " + this.getReceivedIp());
//		Constants.ipPond.set(this.getReceivedIp(), false);
		//将bitArray中IP地址作为下标的相应位置1
		Constants.BitArray.set(this.getReceivedIp(), true);
		return this.getReceivedIp();
	}
	
	public void confirmIp(int ip) throws BindException, SocketException, UnknownHostException, IOException, InterruptedException {
		//将所有直连节点IP作为下标，将ackList中的相应位置设为false，等待接收直连节点的消息回复
		for (int itr : Constants.dirlinkList) {
			Log.d(MSG_TAG, "ready to get DHCP_CONFIRM_REPLY from node : " + itr + " ...");
			Constants.ackList.set(itr, false);
		}
		//开启局域广播
		sender.startAreaBroadcast(Constants.DHCP_CONFIRM, "" + ip, 2);
		
		Log.d(MSG_TAG, "confirm ip delay start ...");
		
		Thread.sleep(100);
		
		Log.d(MSG_TAG, "confirm ip delay over ...");
		
		synchronized (Constants.dirlinkList) {
			//查询ackList，若值为false，则表示没有收到下标作为IP的节点对消息的回复
			for (int i = 1; i < 255; i++) {
				if (!Constants.ackList.get(i)) {
					Log.d(MSG_TAG, "not got confirm reply from node : " + i);
					//从直连节点中移除该节点
					Constants.dirlinkList.remove(new Integer(i));
					//更新ListView
//					adhoc.updateRemoveListView(i, Constants.DIRLINK_LISTVIEW);
				}
			}
		}

	}
	
	public void releaseIp(int ip) throws BindException, SocketException, UnknownHostException, IOException, InterruptedException {
//		for (int itr : Constants.dirlinkList) {
//			Log.d(MSG_TAG, "ready to get DHCP_RELEASE_REPLY from node : " + itr + " ..., remove this node from dirlinkList ...");
//			Constants.ackList.set(itr, false);
//		}
		sender.startNoAckAreaBroadcast(Constants.DHCP_RELEASE, "" + ip);
	}
}
