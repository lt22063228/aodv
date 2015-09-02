package player.taskManager;

import java.io.IOException;
import java.net.BindException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import adhoc.aodv.exception.DataExceedsMaxSizeException;
import adhoc.udp.UdpSender;

public class ForwardSendTest extends Thread {
	private long time1, time2, time3;
	private Queue<byte[]> userMessagesToForward;
	private UdpSender udpSender;

	public ForwardSendTest() {
		time1 = System.currentTimeMillis();
		time2 = System.currentTimeMillis();
		time3 = 500;
		userMessagesToForward = new ConcurrentLinkedQueue<byte[]>();
		try {
			udpSender = new UdpSender();
		} catch (BindException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void add(byte[] data) {
		userMessagesToForward.add(data);
	}

	@Override
	public void run() {
		while (true) {
			if (!userMessagesToForward.isEmpty()) {
				byte[] message = userMessagesToForward.poll();
				try {
					udpSender.sendPacketUnicast(255, message);
				} catch (DataExceedsMaxSizeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				time1 = time2;
				time2 = System.currentTimeMillis();
			}
			long detal = time3 - (time2 - time1);
			if (detal > 0) {
				try {
					Thread.sleep(detal);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
