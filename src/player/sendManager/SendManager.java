package player.sendManager;

import adhoc.constants.Constants;
import android.R.bool;
import android.util.Log;

import player.configuration.AppConfiguration;
import player.model.MessageProtos.Message;

public class SendManager {
	
	private static SendManager sharedInstance = null;
	
	public static SendManager sharedInstance() {
		if (null == sharedInstance) {
			sharedInstance = new SendManager();
		}
		return sharedInstance;
	}

	public void sendBroadcast(Message message) {
		for (int i = 1; i < 254; i++) {
			if (AppConfiguration.myAddress == i) {
				continue;
			}
			else {
				if (true == Constants.BitArray.get(i)) {
					AppConfiguration.sharedNode.sendDataUnicast(i, message.toByteArray(), (byte)0);
					Log.d("SendManager", "broadcast send to : "+ i);
				}
			}
		}

//		for (int host : hosts) {
//			try {
//				InetAddress address = InetAddress.getByName(NetworkConfiguration.SUBNET+host);
//				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
//				packet.setAddress(address);
//				packet.setPort(NetworkConfiguration.DHCP_RECEIVE_PORT);
//				byte[] byteArray = message.toByteArray();
//				packet.setData(byteArray);
//				datagramSocket.send(packet);
//			} catch (IOException e) {
//				e.printStackTrace();
//				continue;
//			}
//		}
	}
	
	public void sendUnicastOrSpecificBroadcast(Message message, int dest, Boolean isUnicast) {
		if (isUnicast) {
			AppConfiguration.sharedNode.sendDataUnicast(dest, message.toByteArray(), (byte)0);
		}
		else {
			AppConfiguration.sharedNode.sendDataSpecificBroadcast(dest, message.toByteArray(), (byte)1);
		}
	}
}
