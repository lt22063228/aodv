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
		//��ʼ��bitArray
		Constants.BitArray.init();
		//��ʼ��receiveIp��ackList�����255����Ա���ֱ�ȫ����ʼ��Ϊfalse��true
		for(int i = 0; i < 255; i++) {
//			Constants.ipPond.add(true);
			Constants.receivedIp.add(false);
			Constants.ackList.add(true);
		}
		//���ֱ���ڵ�IP�б�
		Constants.dirlinkList.clear();
	}
	
	public void init() {
		//AdHocDhcp���ʼ���������͹��캯���еĳ�ʼ��������ͬ
		Constants.BitArray.init();
		for(int i = 0; i < 255; i++) {
//			Constants.ipPond.set(i, true);
			Constants.receivedIp.set(i, false);
			Constants.ackList.set(i,true);
		}
		Constants.dirlinkList.clear();
	}

	public int getReceivedIp() {
		//��ȡ���յ��Ŀ���IP����receivedIp�б���ȡֵΪtrue������±���Ϊ����IP�����ش�IP
		//Ŀ������ʹ��ǰ�ڵ㴦������AdHoc�����м�ʱ���ܹ�����ϴ������
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
			//��Ϊ-1�����ʾû���յ�����IP���򷵻ؿ���IPΪ1
			Log.d(MSG_TAG, "not have ip received ..., initial ip to 192.168.2.1");
//			Constants.ipPond.set(1, false);
			//��bitArray��IP��ַ��Ϊ�±����Ӧλ��1
			Constants.BitArray.set(1, true);
			return 1;
		}
		
		Log.d(MSG_TAG, "got the biggest received ip : " + this.getReceivedIp());
//		Constants.ipPond.set(this.getReceivedIp(), false);
		//��bitArray��IP��ַ��Ϊ�±����Ӧλ��1
		Constants.BitArray.set(this.getReceivedIp(), true);
		return this.getReceivedIp();
	}
	
	public void confirmIp(int ip) throws BindException, SocketException, UnknownHostException, IOException, InterruptedException {
		//������ֱ���ڵ�IP��Ϊ�±꣬��ackList�е���Ӧλ����Ϊfalse���ȴ�����ֱ���ڵ����Ϣ�ظ�
		for (int itr : Constants.dirlinkList) {
			Log.d(MSG_TAG, "ready to get DHCP_CONFIRM_REPLY from node : " + itr + " ...");
			Constants.ackList.set(itr, false);
		}
		//��������㲥
		sender.startAreaBroadcast(Constants.DHCP_CONFIRM, "" + ip, 2);
		
		Log.d(MSG_TAG, "confirm ip delay start ...");
		
		Thread.sleep(100);
		
		Log.d(MSG_TAG, "confirm ip delay over ...");
		
		synchronized (Constants.dirlinkList) {
			//��ѯackList����ֵΪfalse�����ʾû���յ��±���ΪIP�Ľڵ����Ϣ�Ļظ�
			for (int i = 1; i < 255; i++) {
				if (!Constants.ackList.get(i)) {
					Log.d(MSG_TAG, "not got confirm reply from node : " + i);
					//��ֱ���ڵ����Ƴ��ýڵ�
					Constants.dirlinkList.remove(new Integer(i));
					//����ListView
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
