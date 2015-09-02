package player.messageManager;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import player.configuration.AppConfiguration;
import player.taskManager.ForwardSendTest;
import player.taskManager.TaskManager;

import adhoc.aodv.Receiver;
import adhoc.aodv.exception.DataExceedsMaxSizeException;
import adhoc.constants.Constants;
import adhoc.udp.UdpSender;

/**
 * Class running as a separate thread, and responsible for receiving data
 * packets over the UDP protocol.
 * 
 * @author Rabie
 * 
 */
public class MessageReceiver implements Runnable {
	private ForwardSendTest forwardSendTest;

	private DatagramSocket datagramSocket;
	private UdpBroadcastReceiver udpBroadcastReceiver;
	private volatile boolean keepRunning = true;
	private Thread udpReceiverthread;
	int receivenum = 0, broadcastnum = 0;

	public MessageReceiver()
			throws SocketException, UnknownHostException, BindException {
	//	this.forwardSendTest = forwardSendTestIn;

		datagramSocket = new DatagramSocket(null);
		datagramSocket.setReuseAddress(true);
		datagramSocket = new DatagramSocket(new InetSocketAddress(
				/* "192.168.2."+nodeAddress , */Constants.AODV_RECEIV_PORT));
		// datagramSocket.setBroadcast(true);
		udpBroadcastReceiver = new UdpBroadcastReceiver(
				Constants.AODV_RECEIV_PORT);
	}

	public void startThread() {
		keepRunning = true;
		udpBroadcastReceiver.startBroadcastReceiverthread();
		udpReceiverthread = new Thread(this);
		udpReceiverthread.start();
	}

	public void stopThread() {
		keepRunning = false;
		udpBroadcastReceiver.stopBroadcastThread();
		udpReceiverthread.interrupt();
	}

	public void run() {
		while (keepRunning) {
			try {
				// 52kb buffer
				byte[] buffer = new byte[51200];
				DatagramPacket receivePacket = new DatagramPacket(buffer,
						buffer.length);

				datagramSocket.receive(receivePacket);

				receivenum++;
				String s = "receivenum" + receivenum;
				TaskManager.sharedInstance().writeLogtofile(s);

				// if(AppConfiguration.myAddress==2){
				// byte[] data=receivePacket.getData();
				// try {
				// udpSender.sendPacketUnicast(3, data);
				// } catch (DataExceedsMaxSizeException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				// }

			} catch (IOException e) {

			}
		}
	}

	private class UdpBroadcastReceiver implements Runnable {
		private DatagramSocket brodcastDatagramSocket;
		private volatile boolean keepBroadcasting = true;
		private Thread udpBroadcastReceiverThread;

		public UdpBroadcastReceiver(int receiverPort) throws SocketException,
				BindException {
			brodcastDatagramSocket = new DatagramSocket(null);
			brodcastDatagramSocket.setReuseAddress(true);
			brodcastDatagramSocket.setBroadcast(true);
			brodcastDatagramSocket = new DatagramSocket(receiverPort + 1);
		}

		public void startBroadcastReceiverthread() {
			keepBroadcasting = true;
			udpBroadcastReceiverThread = new Thread(this);
			udpBroadcastReceiverThread.start();
		}

		private void stopBroadcastThread() {
			keepBroadcasting = false;
			udpBroadcastReceiverThread.interrupt();
		}

		public void run() {
			while (keepBroadcasting) {
				try {
					// 52kb buffer
					byte[] buffer = new byte[52000];
					DatagramPacket brodcastReceivePacket = new DatagramPacket(
							buffer, buffer.length);

					brodcastDatagramSocket.receive(brodcastReceivePacket);
					
//					String[] ip = brodcastReceivePacket.getAddress().toString().split("\\.");
//					int a = Integer.parseInt(ip[ip.length - 1]);
//					
//					
//					if (AppConfiguration.myAddress == 2 && a == 1) {
//						byte[] result = new byte[brodcastReceivePacket.getLength()];
//						System.arraycopy(brodcastReceivePacket.getData(), 0,result, 0, brodcastReceivePacket.getLength());
						broadcastnum++;
						String ss = "broadcastnum" + broadcastnum;
						TaskManager.sharedInstance().writeLogtofile(ss);
//						forwardSendTest.add(result);
//					}
//					if (AppConfiguration.myAddress == 3 && a ==2)  {
//						broadcastnum++;
//						String ss = "broadcastnum" + broadcastnum;
//						TaskManager.sharedInstance().writeLogtofile(ss);
//					
//					}

				} catch (IOException e) {

				}
			}
		}
	}

}

// package player.messageManager;
//
// import java.io.IOException;
// import java.net.DatagramPacket;
// import java.net.DatagramSocket;
// import java.net.SocketException;
//
// import player.configuration.AppConfiguration;
// import player.configuration.NetworkConfiguration;
// import player.model.MessageProtos.Message;
// import player.taskManager.TaskManager;
//
//
// import adhoc.constants.Constants;
//
// import com.google.protobuf.InvalidProtocolBufferException;
//
//
//
// public class MessageReceiver extends Thread{
//
// private DatagramSocket datagramSocket = null;
// private byte[] datagramBuffer = null;
// private int receivenum=0;
//
// public MessageReceiver() {
// datagramBuffer = new byte[64*1024];
// try {
// datagramSocket = new DatagramSocket( Constants.AODV_RECEIV_PORT);
// datagramSocket.setBroadcast(false);
// } catch (SocketException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// System.exit(-1);
// }
//
// String s="ip"+AppConfiguration.myAddress;
// TaskManager.sharedInstance().writeLogtofile(s);
// }
//
// public void run() {
// while (true) {
// DatagramPacket packet = new DatagramPacket(datagramBuffer,
// datagramBuffer.length);
// try {
// datagramSocket.receive(packet);
// } catch (IOException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// continue;
// }
// // byte[] payload = new byte[packet.getLength()];
// // System.arraycopy(packet.getData(), 0, payload, 0, payload.length);
// // Message newMessage = null;
// // try {
// // newMessage = Message.parseFrom(payload);
// // } catch (InvalidProtocolBufferException e) {
// // // TODO Auto-generated catch block
// // e.printStackTrace();
// // continue;
// // }
// // if (null != newMessage) {
// // TaskManager.sharedInstance().processMessage(newMessage);
// // }
//
// receivenum++;
// String s="receivenum"+receivenum;
// TaskManager.sharedInstance().writeLogtofile(s);
// String ss="time"+System.currentTimeMillis();
// TaskManager.sharedInstance().writeLogtofile(ss);
//
// }
// }
// }
