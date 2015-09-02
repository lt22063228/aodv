package player.taskManager;


import java.io.IOException;
import java.net.BindException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import adhoc.aodv.exception.DataExceedsMaxSizeException;
import adhoc.udp.UdpSender;
import android.util.Log;

import com.google.protobuf.ByteString;

import player.cacheManager.CacheManager;
import player.configuration.AppConfiguration;
import player.model.MessageTypes;
import player.model.URI;
import player.model.MessageProtos.Message;
import player.sendManager.SendManager;

//7.2
public class Diubaolv extends Thread{
	private UdpSender udpSender;
	//private NetworkCodeing networkCodeing;
	int data[];
	long time1,time2;
	@Override
	public void run() {
		
		
		try {
			udpSender=new UdpSender();
			//networkCodeing=new NetworkCodeing();
			//data=new int[3];
		} catch (BindException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (SocketException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		if(AppConfiguration.myAddress==1){			
			URI uri = new URI("test", 0);
			byte[] debug_localData = CacheManager.sharedInstance().get(uri);
//			data[0]=1;
//			data[1]=5;
//			data[2]=7;
//			
//			int value1=data[0]+data[1]+data[2];
//			int value2=2*data[0]+data[1]+data[2];
//			int value3=data[0]+2*data[1]+data[2];
//			int value4=data[0]+data[1]+2*data[2];
//				
//			byte[] byte1=new byte[2];byte1[0]=1;byte1[1]=(byte)value1;
//			byte[] byte2=new byte[2];byte2[0]=2;byte2[1]=(byte)value2;
//			byte[] byte3=new byte[2];byte3[0]=3;byte3[1]=(byte)value3;
//			byte[] byte4=new byte[2];byte4[0]=4;byte4[1]=(byte)value4;
			
			
			
		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
//		for(int i=0;i<100;i++){
//		try {
//			udpSender.sendPacketUnicast(255, byte1);
//			udpSender.sendPacketUnicast(255, byte2);
//			udpSender.sendPacketUnicast(255, byte3);
//			udpSender.sendPacketUnicast(255, byte4);
//		} catch (DataExceedsMaxSizeException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		}
//		String ss="1234DONE";
//	    TaskManager.sharedInstance().writeLogtofile(ss);
		for(int i=0;i<100;i++){

			try {
				udpSender.sendPacketUnicast(255, debug_localData);
			} catch (DataExceedsMaxSizeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		String s="DATA block DONE";
	    TaskManager.sharedInstance().writeLogtofile(s);
		}


		
	}
}
