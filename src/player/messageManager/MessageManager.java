package player.messageManager;

import java.util.concurrent.ConcurrentLinkedQueue;

import player.model.MessageProtos.Message;
import player.taskManager.TaskManager;



public class MessageManager {
	private ConcurrentLinkedQueue<Message> messages = null;
	
	private MessageQueueScanner scanner = null;
	
	private static MessageManager sharedInstance = null;
	
	public MessageManager() {
		this.messages = new ConcurrentLinkedQueue<Message>();
		scanner = new MessageQueueScanner();
		scanner.start();
	}
	
	public static MessageManager sharedInstance() {
		if (null == sharedInstance) {
			sharedInstance = new MessageManager();
		}
		return sharedInstance;
	}
	
	public void addMessage(Message newMessage) {
		messages.add(newMessage);
		scanner.notifyAll();
		//inform the scanner thread to do its work
	}
	
	private class MessageQueueScanner extends Thread {
		@Override
		public void run() {
			while (true) {
				if (messages.isEmpty()) {
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else {
					Message newMessage = messages.poll();
					if (null != newMessage) {
						TaskManager.sharedInstance().processMessage(newMessage);
					}
				}
			}
		}
	}
}
