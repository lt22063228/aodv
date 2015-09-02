package adhoc.setup;

import java.io.IOException;
import java.net.BindException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

//import com.sun.org.apache.bcel.internal.generic.GOTO;

//import sun.util.logging.resources.logging;


import adhoc.constants.Constants;
import android.util.Log;

public class AdHocMessageHandler implements Runnable {

	private static final String MSG_TAG = "AdHocMessageHandler --> ";

	private AdHocBroadcastSender sender;
	private AdHocSetup application;
	private Queue<Message> receivedMessages;
	private Thread handlerThread;
	private volatile boolean keepRunning = true;
	private int msgIp;
	private int msgType;
	private Message msg;

	public AdHocMessageHandler(AdHocSetup app)
			throws BindException, SocketException, UnknownHostException,
			IOException {
		this.application = app;
		this.sender = new AdHocBroadcastSender(application);
		receivedMessages = new ConcurrentLinkedQueue<Message>();
	}

	public void startThread() {
		keepRunning = true;
		handlerThread = new Thread(this);
		handlerThread.start();
	}

	public void stopThread() {
		keepRunning = false;
		handlerThread.interrupt();
	}

	private String getAvailableIp() {
		// 查询bitArray，获取值为0的最小下标作为可用IP
		int index = 1;
		while (Constants.BitArray.get(index)) {
			index++;
		}
		return "" + index;
	}

	public void run() {
		while (keepRunning) {
			try {
				synchronized (receivedMessages) {
					while (receivedMessages.isEmpty()) {
//						Log.d(MSG_TAG, "queue has no message , go to wait ...");
						receivedMessages.wait();
					}
				}
				msg = receivedMessages.poll();
//				Log.d(MSG_TAG, "pull message from queue ...");
				msgType = msg.getType();
				msgIp = msg.getIp();

				switch (msgType) {
					case Constants.DHCP_TEST:
						Log.d(MSG_TAG, "got dhcp test ...");
						break;
					case Constants.DHCP_REQUEST:
						if(application.getIpLastField() == 254) {
							continue;
						}
						if (msgIp == application.getIpLastField()) {
							Log.d(MSG_TAG, "broadcast message from self ...");
							continue;
						}
						Log.d(MSG_TAG, "got dhcp request from ip " + msgIp + " ...");
						sender.sendPacket(msgIp, Constants.DHCP_REQUEST_REPLY, getAvailableIp());
						//Add by Eric
						sender.sendPacket(msgIp, Constants.GIVE_PHONE_INFO, Constants.phoneInfo);
						Log.d(MSG_TAG, "send replay & phone info which is : "+Constants.phoneInfo);
						//
						Log.d(MSG_TAG, "DHCP_REPLY has been send to node " + msgIp + " ...");
						Log.d(MSG_TAG, "available ip is : " + getAvailableIp() + "");
						break;
					case Constants.DHCP_REQUEST_REPLY:
						Log.d(MSG_TAG, "got dhcp request reply from ip " + msgIp + " ...");
						if (!Constants.BitArray.get(msgIp)) {
							Constants.receivedIp.set(msg.getIpContent(), true);
							Constants.BitArray.set(msgIp, true);
							synchronized (Constants.dirlinkList) {
								Constants.dirlinkList.add(msgIp);
							}
//							adhoc.updateAddListView(msgIp, Constants.DIRLINK_LISTVIEW);
						}
						break;
					case Constants.DHCP_CONFIRM:
						if(application.getIpLastField() == 254) {
							continue;
						}
						Log.d(MSG_TAG, "got dhcp confirm from ip " + msgIp + " ...");
						if (msgIp == msg.getIpContent()) {
	
							sender.sendPacket(msgIp, Constants.DHCP_CONFIRM_REPLY, new String(Constants.bitArray));
							Log.d(MSG_TAG, "DHCP_CONFIRM_REPLY has been send to node " + msgIp + " ...");
							synchronized (Constants.dirlinkList) {
								if (Constants.dirlinkList.indexOf(new Integer(msgIp)) == -1) {
									Constants.dirlinkList.add(msgIp);
//									adhoc.updateAddListView(msgIp, Constants.DIRLINK_LISTVIEW);
								}
							}

						}
	
						if (!Constants.BitArray.get(msg.getIpContent())) {
							Constants.BitArray.set(msg.getIpContent(), true);
//							adhoc.updateAddListView(msg.getIpContent(), Constants.ALLLINK_LISTVIEW);
							new Thread(new Runnable() {
	
								@Override
								public void run() {
									// TODO Auto-generated method stub
	
									try {
										sender.startNoAckAreaBroadcast(Constants.DHCP_CONFIRM, "" + msg.getIpContent());
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
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									Log.d(MSG_TAG, "DHCP_CONFIRM_REPLY has been send to node " + msgIp + " ...");
								}
	
							}).start();
						}
	
						break;
					case Constants.DHCP_CONFIRM_REPLY:
						Log.d(MSG_TAG, "got dhcp confirm reply from ip " + msgIp + " ...");
						synchronized (Constants.ackList) {
							Constants.ackList.set(msgIp, true);
						}

						Constants.BitArray.join(msg.getBitContent());
						break;
					case Constants.DHCP_RELEASE:
						if(application.getIpLastField() == 254) {
							continue;
						}
						Log.d(MSG_TAG, "got dhcp release from ip " + msgIp + " ...");
						if (msgIp == msg.getIpContent()) {
	
							// sender.sendPacket(msgIp,
							// Constants.DHCP_RELEASE_REPLY, "0");
							// Log.d(MSG_TAG,
							// "DHCP_RELEASE_REPLY has been send to node " + msgIp +
							// " ...");
							synchronized (Constants.dirlinkList) {
								if (Constants.dirlinkList.indexOf(new Integer(msgIp)) != -1) {
									Constants.dirlinkList.remove(new Integer(msgIp));
//									adhoc.updateRemoveListView(msgIp, Constants.DIRLINK_LISTVIEW);
								}
							}

						}
	
						if (Constants.BitArray.get(msg.getIpContent())) {
							Constants.BitArray.set(msg.getIpContent(), false);
//							adhoc.updateRemoveListView(msg.getIpContent(), Constants.ALLLINK_LISTVIEW);
	
							new Thread(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub
	
									try {
										sender.startNoAckAreaBroadcast( Constants.DHCP_RELEASE, "" + msg.getIpContent());
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
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									Log.d(MSG_TAG, "DHCP_CONFIRM_REPLY has been send to node " + msgIp + " ...");
								}
	
							}).start();
	
						}
	
						break;
					// case Constants.DHCP_RELEASE_REPLY :
					// Log.d(MSG_TAG, "got dhcp confirm reply from ip " + msgIp +
					// " ...");
					// Constants.ackList.set(msgIp, true);
					// break;
					case Constants.DIRLINK_CONFIRM:

						// 发送reply
						sender.sendPacket(msgIp, Constants.DIRLINK_CONFIRM_REPLY, "" + application.getIpLastField());
//						Log.d(MSG_TAG, "DIRLINK_CONFIRM_REPLY has been send to node " + msgIp + " ...");
						break;
					case Constants.DIRLINK_CONFIRM_REPLY:
						// 类似confirm ip reply
//						Log.d(MSG_TAG, "got dirlink confirm reply from ip " + msgIp + " ...");
						synchronized (Constants.ackList) {
							Constants.ackList.set(msgIp, true);
						}

						break;
					case Constants.DIRLINK_SEARCH:
						if(application.getIpLastField() == 254) {
							continue;
						}

						synchronized (Constants.dirlinkList) {
							if (msgIp == application.getIpLastField()) {
								continue;
							} 
							else if (Constants.dirlinkList.indexOf(new Integer(msgIp)) != -1) {
								continue;
							} 
							else {
								sender.sendPacket(msgIp, Constants.DIRLINK_SEARCH_REPLY, "" + msgIp);
//								Log.d(MSG_TAG, "DIRLINK_SEARCH_REPLY has been send to node " + msgIp + " ...");
								Constants.dirlinkList.add(msgIp);
//								adhoc.updateAddListView(msgIp, Constants.DIRLINK_LISTVIEW);
							}
						}

						break;
						// 消息内容和自身节点bitarray进行异或，判断是否是两个子网交汇
					case Constants.DIRLINK_SEARCH_REPLY:
						synchronized (Constants.dirlinkList) {
							// 正常处理
							if (Constants.dirlinkList.indexOf(new Integer(msgIp)) != -1) {
								continue;
							} 
							else if (Constants.dirlinkList.indexOf(new Integer(msgIp)) != -1) {
								continue;
							}
							else {
								Constants.dirlinkList.add(msgIp);
//								adhoc.updateAddListView(msgIp, Constants.DIRLINK_LISTVIEW);
							}
						}

						break;
						
						
					case Constants.BANDWIDTH_TEST:
						Log.d(MSG_TAG, "Got Bandwidth Test MSG...");
						int maxWaitTime = 5000;
						int xTime =0;
						while(Constants.bandwidthTestIsUsed && xTime<=maxWaitTime){
							Thread.sleep(1000);
							xTime +=1000;
						}
						if(xTime==maxWaitTime+1000) 
							Log.d(MSG_TAG, "bandwidthTest is busy...");
						//控制测试链接的使用
						else{
							Log.d(MSG_TAG, "Now send Bandwidht test confirm...");
							Constants.bandwidthTestIsUsed = true;
							sender.sendPacket(msgIp, Constants.BANDWIDTH_TEST_CONFIRM, "");
							Constants.bandwidthTestIsUsed = false;
						}
						
						break;
						
					case Constants.BANDWIDTH_TEST_CONFIRM:
						Log.d(MSG_TAG, "Got Bandwidht Test Confirm...");
						if(Constants.bandwidthTestIsUsed){
							Log.d(MSG_TAG, "bandwidthTest now id used...");
						}
						else {
							Constants.bandwidthTestIsUsed = true;
							Log.d(MSG_TAG, "Send Bandwidth Test Start D...");
							sender.sendPacket(msgIp, Constants.BANDWIDTH_TEST_START_D, "");
						}
						break;
					case Constants.BANDWIDTH_TEST_ING_D:
						if(Constants.startTime_D ==0)
							Constants.startTime_D =System.currentTimeMillis();
						
						Constants.bandwidthTestSize_D += msg.getBitContent().length;
						break;
						
					case Constants.BANDWIDTH_TEST_ING_U:
						Constants.bandwidthTestSize_U += msg.getBitContent().length;
						break;
					case Constants.BANDWIDTH_TEST_END_U:
						sender.sendPacket(msgIp, Constants.BANDWIDTH_TEST_END, Constants.bandwidthTestSize_U +"");
						Constants.bandwidthTestSize_U =0 ;
						Constants.bandwidthTestIsUsed =false;
						break;
					
					case Constants.BANDWIDTH_TEST_START_D:
						Log.d(MSG_TAG, "Got Bandwidth Test Start D...");
						int x = 0;
						Vector<Integer> vector= new Vector<Integer>();
						for(int i=0;i<6250;++i){
							vector.add(i);
						}
						Log.d(MSG_TAG, "vector to String :"+ vector.toString().length());
						while(x<1){
							sender.sendPacket(msgIp, Constants.BANDWIDTH_TEST_ING_D, vector.toString()+"1234567890");
							x++;
						}
						Log.d(MSG_TAG, "bandwidth test ing d send end...");
						sender.sendPacket(msgIp, Constants.BANDWIDTH_TEST_START_U, "");
						break;
					case Constants.BANDWIDTH_TEST_START_U:
						Log.d(MSG_TAG, "Got bt Start u...");
						Vector<Integer> vector1= new Vector<Integer>();
						for(int i=0;i<6250;++i){
							vector1.add(i);
						}
						Constants.endTime_D =System.currentTimeMillis();
						int x1 = 0;
						
						Constants.startTime_U =System.currentTimeMillis();
						
						
						Log.d(MSG_TAG, "vector1 to String :"+ vector1.toString().length());
						while(x1<1){
							sender.sendPacket(msgIp, Constants.BANDWIDTH_TEST_ING_U, vector1.toString()+"1234567890");
							x1++;
						}
						sender.sendPacket(msgIp, Constants.BANDWIDTH_TEST_END_U, "");
						Constants.endTime_U =System.currentTimeMillis();
						break;
						
					case Constants.BANDWIDTH_TEST_END:
						double bandwidth_U = msg.getDoubleContent()/(Constants.endTime_U- Constants.startTime_U);
						double bandwidth_D = Constants.bandwidthTestSize_D/(Constants.endTime_D- Constants.startTime_D);
						Constants.mapBandwidth_U.put(msgIp, bandwidth_U);
						Constants.mapBandwidth_D.put(msgIp, bandwidth_D);
						Constants.bandwidthTestIsUsed=false;
						break;
						
					case Constants.GIVE_PHONE_INFO:
						String get_phone_info = msg.getStrContent();
						Constants.phoneInfoList.add(get_phone_info);
						
					
						break;
						
					default:
						break;
				}

			} catch (InterruptedException e) {
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

	public void addMessage(String msg) {
		receivedMessages.add(new Message(msg));
		synchronized (receivedMessages) {
			receivedMessages.notify();
//			Log.d(MSG_TAG, "queue got message , notify queue ... ");
		}
	}

	private class Message {

		private String str[];
		private String msgType;
		private String senderNodeAddress;
		private String msgContent;

		public Message(String message) {
			this.str = message.split("/", 3);
//			Log.d(MSG_TAG, "msg split length is : " + str.length + " ");
			this.msgType = str[0];
			this.senderNodeAddress = str[1];
			this.msgContent = str[2];
		}

		public int getType() {
			return Integer.parseInt(this.msgType);
		}

		public int getIp() {
			return Integer.parseInt(this.senderNodeAddress);
		}

		public int getIpContent() {
			return Integer.parseInt(this.msgContent);
		}

		public byte[] getBitContent() {
			return this.msgContent.getBytes();
		}
		
		public double getDoubleContent(){
			return Double.parseDouble(this.msgContent);
		}
		
		public String getStrContent(){
			return this.msgContent;
		}
	}

}
