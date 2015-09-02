package adhoc.udp;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


import adhoc.aodv.exception.DataExceedsMaxSizeException;
import adhoc.constants.Constants;
import android.util.Log;

public class UdpSender {
	
	private final static String MSG_TAG = "AdHoc --> UdpSender";
	
	private DatagramSocket datagramSocket;
	private int receiverPort = Constants.AODV_RECEIV_PORT;
	private String subNet = "192.168.2.";
	
	public UdpSender() throws SocketException, UnknownHostException, BindException{
	    datagramSocket = new DatagramSocket();

	}

	/**
	 * Sends data using the UDP protocol to a specific receiver
	 * @param destinationNodeID indicates the ID of the receiving node. Should be a positive integer.
	 * @param data is the message which is to be sent. 
	 * @throws IOException 
	 * @throws SizeLimitExceededException is thrown if the length of the data to be sent exceeds the limit
	 */
	public boolean sendPacket(int destinationNodeID, byte[] data) throws IOException, DataExceedsMaxSizeException{
		if(data.length <= Constants.MAX_PACKAGE_SIZE){
				InetAddress IPAddress = InetAddress.getByName(subNet+destinationNodeID);
				//do we have a packet to be broadcasted?
				DatagramPacket sendPacket;
				if(destinationNodeID == Constants.BROADCAST_ADDRESS){
					datagramSocket.setBroadcast(true);
					sendPacket = new DatagramPacket(data, data.length, IPAddress, receiverPort+1);
				}else {
					datagramSocket.setBroadcast(false);
					sendPacket = new DatagramPacket(data, data.length, IPAddress, receiverPort);
				}
				
				datagramSocket.send(sendPacket);
				return true;
			} else {
				throw new DataExceedsMaxSizeException();
			}
	}
	
	
	//添加广播发送方法
	public boolean sendPacketSpecificBroadcast(byte[] data) throws IOException, DataExceedsMaxSizeException{
		
		if(data.length <= Constants.MAX_PACKAGE_SIZE){
			InetAddress IPAddress = InetAddress.getByName("192.168.2.255");
			DatagramPacket sendPacket;
			
			datagramSocket.setBroadcast(true);
			sendPacket = new DatagramPacket(data, data.length, IPAddress, receiverPort);
			
	//		Log.d("BROADCAST", "BEFORE Send in UdpSender.");
			
			datagramSocket.send(sendPacket);
			
		//	Log.d("BROADCAST", "AFTER Send in UdpSender.");
			return true;
		} else {
			throw new DataExceedsMaxSizeException();
		}
	}
	//原来的发送方法
	public boolean sendPacketUnicast(int destinationNodeID, byte[] data) throws IOException, DataExceedsMaxSizeException{
		if(data.length <= Constants.MAX_PACKAGE_SIZE){
				InetAddress IPAddress = InetAddress.getByName(subNet+destinationNodeID);
				//do we have a packet to be broadcasted?
				DatagramPacket sendPacket;
				if(destinationNodeID == Constants.BROADCAST_ADDRESS){
					datagramSocket.setBroadcast(true);
					sendPacket = new DatagramPacket(data, data.length, IPAddress, receiverPort+1);
				}else {
					datagramSocket.setBroadcast(false);
					sendPacket = new DatagramPacket(data, data.length, IPAddress, receiverPort);
				}
				
				datagramSocket.send(sendPacket);
				return true;
			} else {
				throw new DataExceedsMaxSizeException();
			}
	}
	
	public void closeSoket(){
		datagramSocket.close();
	}

}
