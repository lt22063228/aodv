package player.taskManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import player.cacheManager.CacheInputStream;
import player.cacheManager.CacheManager;
import player.configuration.AppConfiguration;
import player.configuration.CacheConfiguration;
import player.configuration.NetworkConfiguration;
import player.model.MessageTypes;
import player.model.URI;
import player.model.MessageProtos.Message;
import player.sendManager.SendManager;


import android.util.Log;

import com.google.protobuf.ByteString;

public class TaskManager {

	int number_yes=0;//计数，从其他节点收到的
	
	File filedebug_task=null;//6.17
	FileOutputStream fos_task = null;//6.17
	byte []newLine="\r\n".getBytes();//6.17
	
	private ConcurrentHashMap<String, Task> tasks;

	private static TaskManager sharedInstanceManager = null;

	public static TaskManager sharedInstance() {
		if (null == sharedInstanceManager) {
			sharedInstanceManager = new TaskManager();
		}
		return sharedInstanceManager;
	}

	public TaskManager() {
        filedebug_task=new File(CacheConfiguration.cachepath,"filedebug_task.txt");//6.17
		
		try {
			if(!filedebug_task.exists())
			filedebug_task.createNewFile();
			fos_task = new FileOutputStream(filedebug_task);
			
		} catch (Exception e) {
			e.printStackTrace();
		}//6.17
		tasks = new ConcurrentHashMap<String, Task>();
		new TaskManagerScanner().start();
	}
	
	public void writeLogtofile(String s){
		byte[] buffer=s.getBytes();
		try {
			fos_task.write(buffer);
			fos_task.write(newLine);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}//6.24

	public void addTask(URI newURI, CacheInputStream callback) {
		
		String key = "";
		if(callback == null)
			key = newURI.identifier + newURI.offset+"pre";
		
		else 
			key = newURI.identifier + newURI.offset;
		
		
		if (!tasks.containsKey(key)) {
			Task newTask = new Task();
			newTask.uri = newURI;
			newTask.callback = callback;
			tasks.put(key, newTask);
			Message.Builder builder = Message.newBuilder();
			builder.setType(MessageTypes.HANDSHAKE);
			builder.setIdentifier(newURI.identifier);
			builder.setOffset(newURI.offset);
			builder.setIp(AppConfiguration.myAddress);
			Message handshakeMessage = builder.build();
			Log.d("TaskManager", "HANDSHAKE msg sended");
			SendManager.sharedInstance().sendBroadcast(handshakeMessage);
			try {
				//add by Eric 这样能保证host改的可能性降低
				Thread.sleep(0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void processMessage(Message newMessage) {
		String key = newMessage.getIdentifier() + newMessage.getOffset();
		
		URI uri = new URI(newMessage.getIdentifier(), newMessage.getOffset());
		switch (newMessage.getType()) {
		case MessageTypes.HANDSHAKE:
		{
			//server side
			Log.d("TaskManager : ", "HANDSHAKE : "+ newMessage.getIp());
			byte[] localData = CacheManager.sharedInstance().get(uri);
			Log.d("TaskManager", "request offset : "+uri.offset);
			if (null != localData) {
				Log.d("TaskManager : ", "I can help this node : "+ newMessage.getIp());
				//send reply 
				Message.Builder builder = Message.newBuilder();
				builder.setType(MessageTypes.HANDSHAKE_REPLY);
				builder.setIdentifier(newMessage.getIdentifier());
				builder.setOffset(newMessage.getOffset());
				builder.setIp(AppConfiguration.myAddress);
				Message replyMessage = builder.build();
				SendManager.sharedInstance().sendUnicastOrSpecificBroadcast(replyMessage, newMessage.getIp(),true);
			}
			else {
				Log.d("TaskManager : ", "can't help this node : "+ newMessage.getIp());
			}
		}
		break;
		case MessageTypes.HANDSHAKE_REPLY:
		{
			Log.d("TaskManager : ", "HANDSHAKE_REPLY: "+ newMessage.getIp());
			Task currentTask = tasks.get(key);
			//too late
			if (null == currentTask) {
				break;
			}
				
			if (TaskStatus.HANDSHAKE_MESSAGE_SENT == currentTask.status) {
				//sent transmission message
				Message.Builder builder = Message.newBuilder();
				builder.setType(MessageTypes.TRANSMISSION);
				builder.setIdentifier(newMessage.getIdentifier());
				builder.setOffset(newMessage.getOffset());
				builder.setIp(AppConfiguration.myAddress);
				Message transmissionMessage = builder.build();
				SendManager.sharedInstance().sendUnicastOrSpecificBroadcast(transmissionMessage, newMessage.getIp(),true);
				Log.d("TaskManager", "send transmission msg to : "+ newMessage.getIp());
				//improvement
				currentTask.status = TaskStatus.TRANSMISSION_MESSAGE_SENT;
				currentTask.TTL = Task.defaultTTL;
			}
			else if (TaskStatus.TRANSMISSION_MESSAGE_SENT == currentTask.status) {
				int[] newHosts = null;
				if (null == currentTask.hosts) {
					newHosts = new int[1];
					newHosts[0] = newMessage.getIp();
				}
				else {
					int currentLength = currentTask.hosts.length;
					newHosts = new int[currentLength+1];
					for (int i = 0; i < currentLength; i++) {
						newHosts[i] = currentTask.hosts[i];
					}
					newHosts[currentLength] = newMessage.getIp();
				}
				synchronized (currentTask) {
					currentTask.hosts = newHosts;
				}
				
			}
		}
		break;
		case MessageTypes.TRANSMISSION:
		{
			Log.d("TaskManager : ", "TRANSMISSION from : "+ newMessage.getIp());
			byte[] localData = CacheManager.sharedInstance().get(uri);
			if (null != localData) {
				//reply with localData
				Message.Builder builder = Message.newBuilder();
				builder.setType(MessageTypes.TRANSMISSION_REPLY);
				builder.setIdentifier(newMessage.getIdentifier());
				builder.setOffset(newMessage.getOffset());
				builder.setIp(AppConfiguration.myAddress);
				ByteString byteString = ByteString.copyFrom(localData);
				builder.setPayload(byteString);
				Message transmissionReplyMessage = builder.build();
				SendManager.sharedInstance().sendUnicastOrSpecificBroadcast(transmissionReplyMessage, newMessage.getIp(),false);
				Log.d("TaskManager", "send some data to: "+ newMessage.getIp());
				String ip = NetworkConfiguration.SUBNET+newMessage.getIp();
				Long uploadForThisClient = CacheManager.sharedInstance().uploadCount.get(ip);
				long dataLengthUploadedThisTime = localData.length;
				if (null != uploadForThisClient) {
					dataLengthUploadedThisTime += uploadForThisClient.longValue();
				}
				CacheManager.sharedInstance().uploadCount.put(ip, Long.valueOf(dataLengthUploadedThisTime));
			}
		}
		break;
		case MessageTypes.TRANSMISSION_REPLY:
		{
			Log.d("TaskManager : ", "TRANSMISSION_REPLY : "+ newMessage.getIp());
			ByteString payload = newMessage.getPayload();
			//here we check if the checksums match
			//e.g. if (payload.hashCode() == newMessage.getChecksum())
			if (true) {
				byte[] payloadBytes = payload.toByteArray();
				CacheManager.sharedInstance().put(uri, payloadBytes);
				String ip = NetworkConfiguration.SUBNET+newMessage.getIp();
				Long downloadForThisClient = CacheManager.sharedInstance().downloadCount.get(ip);
				long dataLengthReceivedThisTime = payloadBytes.length;
				CacheManager.sharedInstance().totalCountFromWifi += dataLengthReceivedThisTime;
				if (null != downloadForThisClient) {					
					dataLengthReceivedThisTime += downloadForThisClient.longValue();
				}else{
					CacheManager.sharedInstance().lastDownloadCount.put(ip, Long.valueOf(0));
				}
				CacheManager.sharedInstance().downloadCount.put(ip, Long.valueOf(dataLengthReceivedThisTime));
				//add success callback here
				Task currentTask = tasks.get(key);
				if (null != currentTask && null != currentTask.callback) {
					//notify the corresponding stream
					synchronized (currentTask.callback) {
						currentTask.callback.notifyAll();
					}
				}
			}
			number_yes++;
			Log.d("TaskManager", "Get data from other nodes"+number_yes);
			String sss="Get data from other nodes"+number_yes;
			writeLogtofile(sss);
			tasks.remove(key);
		}
		break;
		default:
			break;
		}
	}

	private class TaskManagerScanner extends Thread {

		@Override
		public void run() {
			while (true) {
				Iterator<Entry<String, Task>> iterator = tasks.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, Task> currentEntry = iterator.next();
					Task currentTask = currentEntry.getValue();
					//remove this later
					currentTask.TTL -= 1;
					if (currentTask.TTL==0) {
						if (null==currentTask.hosts || 0==currentTask.hosts.length) {
							//add fail callback here
							if (null != currentTask.callback) {
								synchronized (currentTask.callback) {
									currentTask.callback.notifyAll();
								}
							}
							iterator.remove();
						}
						else {
							//start the next host
							Message.Builder builder = Message.newBuilder();
							builder.setIdentifier(currentTask.uri.identifier);
							builder.setOffset(currentTask.uri.offset);
							builder.setIp(AppConfiguration.myAddress);
							switch (currentTask.status) {//这个分支执行不到
							case HANDSHAKE_MESSAGE_SENT:
							{
								builder.setType(MessageTypes.HANDSHAKE);
								Log.d("TaskManager", "send HANDSHAKE");
								SendManager.sharedInstance().sendBroadcast(builder.build());
								
								System.out.println("We should never be here because it's HANDSHAKE_MESSAGE_SENT!");
							}
							break;
							case TRANSMISSION_MESSAGE_SENT://向hosts[]中的其他节点请求数据
							{
								builder.setType(MessageTypes.TRANSMISSION);
								int dest = currentTask.hosts[0];
								if (currentTask.hosts.length == 1) {
									currentTask.hosts = null;
								}
								else {
									int []newHosts = new int[currentTask.hosts.length-1];
									for (int i = 1; i < newHosts.length; i++) {
										newHosts[i] = currentTask.hosts[i];
									}
									synchronized (currentTask) {
										currentTask.hosts = newHosts;
									}
									
								}
								SendManager.sharedInstance().sendUnicastOrSpecificBroadcast(builder.build(), dest, true);
								
								currentTask.TTL = Task.defaultTTL;
							}
							break;
							default:
								System.out.println("Unknown task status, assuming TRANSMISSION_MESSAGE_SENT.");
								builder.setType(MessageTypes.TRANSMISSION);
								int dest = currentTask.hosts[0];
								if (currentTask.hosts.length == 1) {
									currentTask.hosts = null;
								}
								else {
									int []newHosts = new int[currentTask.hosts.length-1];
									for (int i = 1; i < newHosts.length; i++) {
										newHosts[i] = currentTask.hosts[i];
									}
									synchronized (currentTask) {
										currentTask.hosts = newHosts;
									}
									
								}
								SendManager.sharedInstance().sendUnicastOrSpecificBroadcast(builder.build(), dest,true);
								
								currentTask.TTL = Task.defaultTTL;
						
								break;
							}
						}
					}
					/*
					else if(currentTask.TTL<0){
						synchronized (currentTask.callback) {
							currentTask.callback.notifyAll();
						}
					}*/
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

}
