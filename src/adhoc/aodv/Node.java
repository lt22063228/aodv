package adhoc.aodv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.BindException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


import adhoc.aodv.exception.InvalidNodeAddressException;
import adhoc.aodv.pdu.AodvPDU;
import adhoc.aodv.pdu.UserBroadcastDataPacket;
import adhoc.aodv.pdu.UserDataPacket;
import adhoc.constants.Constants;
import adhoc.etc.Debug;
import android.os.Environment;

public class Node extends Observable implements Runnable {

	private final static String MSG_TAG = "AdHoc --> Node";
	private final static int MAX_BUFFER_SIZE = 1024;

	private int nodeAddress;
	private int nodeSequenceNumber = Constants.FIRST_SEQUENCE_NUMBER;
	private int nodeBroadcastID = Constants.FIRST_BROADCAST_ID;
	private int nodePackageIdentifier = Constants.FIRST_PACKAGE_IDENTIFIER;
	private Sender sender;
	private Receiver receiver;
	private RouteTableManager routeTableManager;
	private Object sequenceNumberLock = 0;
	private Thread notifierThread;
	private Queue<MessageToObserver> messagesForObservers;
	private volatile boolean keepRunning = true;

	private String[] routePath = null;
	private int index = 0;
	private String newPath = null;
	private Date date = null;
	private SimpleDateFormat format = null;
	private String time = null;
	private String fileName = null;
	private File file = null;
	private FileWriter writer = null;
	private String newLine = null;
	/**
	 * Creates an instance of the Node class
	 * @param nodeAddress
	 * @throws InvalidNodeAddressException Is thrown if the given node address is outside of the valid interval of node addresses
	 * @throws SocketException is cast if the node failed to instantiate port connections to the ad-hoc network
	 * @throws UnknownHostException
	 * @throws BindException this exception is thrown if network interface already is connected to a another network 
	 */
	public Node(int nodeAddress) throws InvalidNodeAddressException, SocketException, UnknownHostException, BindException {
		if(nodeAddress > Constants.MAX_VALID_NODE_ADDRESS 
				|| nodeAddress < Constants.MIN_VALID_NODE_ADDRESS){
			//given address is out of the valid range
			throw new InvalidNodeAddressException();
		}
		this.nodeAddress = nodeAddress;
		routeTableManager = new RouteTableManager(nodeAddress, this);
		sender = new Sender(this, nodeAddress, routeTableManager);
		receiver = new Receiver(sender, nodeAddress, this, routeTableManager);
		messagesForObservers = new ConcurrentLinkedQueue<MessageToObserver>();
		routePath = new String[1024];

	}

	/**
	 * Starts executing the AODV routing protocol 
	 * @throws UnknownHostException 
	 * @throws SocketException 
	 * @throws BindException 
	 */
	public void startThread(){
		keepRunning = true;
		routeTableManager.startTimerThread();
		sender.startThread();
		receiver.startThread();
		notifierThread = new Thread(this);
		notifierThread.start();

		routePath = new String[1024];
		index = 0;
		newPath = null;
		nodePackageIdentifier = Constants.FIRST_PACKAGE_IDENTIFIER;
		date = new Date();
		format = new SimpleDateFormat("hh-mm-ss");
		time = format.format(date);
		fileName = Environment.getExternalStorageDirectory().getPath() + "/AdhocForwardPath_" + nodeAddress + "_" + time + ".txt";
		file = new File(fileName);
		newLine = System.getProperty("line.separator");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			writer = new FileWriter(file, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Debug.print("Node: all library threads are running");
	}

	/**
	 * Stops the AODV protocol. 
	 * 
	 * Note: using this method tells the running threads to terminate. 
	 * This means that it does not insure that any remaining userpackets is sent before termination.
	 * Such behavior can be achieved by monitoring the notifications by registering as an observer.
	 */
	public void stopThread(){
		keepRunning = false;
		receiver.stopThread();
		sender.stopThread();
		routeTableManager.stopTimerThread();
		notifierThread.interrupt();

		this.writeRemained();
		try {
			if(writer != null)
				writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Debug.print("Node: all library threads are stopped");
	}

	/**
	 * Method to be used by the application layer to send data to a single destination node or all neighboring nodes (broadcast).
	 * @param packetIdentifier is an ID that is associated for this packet. This is given from the application layer to identify which packet failed or succeed in sending
	 * @param destinationAddress the address of the destination node. Should be set to Constants.BROADCAST_ADDRESS if the data is to be broadcasted. 
	 * @param data an array of bytes containing the desired data to send. Note that the size of the data may not exceed Constants.MAX_PACKAGE_SIZE
	 */
	public void sendDataUnicast(int destinationAddress, byte[] data, byte type) {
		this.sendDataUnicast(this.getNextPackageIdentifier(), destinationAddress, data, type);
	}

	public void sendDataUnicast(int packetIdentifier, int destinationAddress, byte[] data, byte type){
		sender.queueUserMessageFromNode(new UserDataPacket(packetIdentifier,destinationAddress, data, nodeAddress, type));
	}
	
	
	public void sendDataSpecificBroadcast(int destinationAddress, byte[] data, byte type) {
		this.sendDataSpecificBroadcast(this.getNextPackageIdentifier(), destinationAddress, data, type);
	}
	public void sendDataSpecificBroadcast(int packetIdentifier, int destinationAddress, byte[] data, byte type){
		sender.queueUserMessageFromNode(new UserDataPacket(packetIdentifier,destinationAddress, data, nodeAddress, type));
	}
	
	

	public void sendBroadcastData(byte[] data, byte type) {
		sender.queueUserBroadcastMessageToForward(new UserBroadcastDataPacket(nodeBroadcastID, data, nodeAddress, type));
		this.getNextBroadcastID(); 
	}

	public void recordPath(int identifier, int srcAddress, int desAddress, int nextHop) {
		newPath = identifier + "," + srcAddress + "," + desAddress + "," + nodeAddress + "," + nextHop;
		routePath[index] = newPath;
		index++;
		if(index == MAX_BUFFER_SIZE) {
			//Ð´ÈëÎÄ¼þ
			this.writeBuffer(MAX_BUFFER_SIZE);
			index = 0;
		}
	}

	public void writeRemained() {
		this.writeBuffer(index);
	}

	public void writeBuffer(int length) {
		try {
			if(writer != null){
				for(int i = 0; i < length; i++) {
					writer.write(routePath[i] + newLine);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected int getNextPackageIdentifier() {
		if(nodePackageIdentifier == Constants.MAX_PACKAGE_IDENTIFIER){
			nodePackageIdentifier = Constants.FIRST_PACKAGE_IDENTIFIER;
		} else {
			nodePackageIdentifier++;
		}
		return nodePackageIdentifier;			
	}

	/**
	 * Method for getting the current sequence number for this node
	 * @return an integer value of the current sequence number
	 */
	protected int getCurrentSequenceNumber(){
		return nodeSequenceNumber;
	}

	/**
	 * Increments the given number but does NOT set this number as the nodes sequence number
	 * @param number is the number which to increment
	 */
	protected int getNextSequenceNumber(int number){
		if((number >= Constants.MAX_SEQUENCE_NUMBER || number < Constants.FIRST_SEQUENCE_NUMBER)){
			return Constants.FIRST_SEQUENCE_NUMBER;
		} else {
			return number++;
		}
	}


	/**
	 * Increments and set the sequence number before returning the new value. 
	 * @return returns the next sequence number
	 */
	protected int getNextSequenceNumber(){
		synchronized (sequenceNumberLock) {
			if(nodeSequenceNumber == Constants.UNKNOWN_SEQUENCE_NUMBER
					|| nodeSequenceNumber == Constants.MAX_SEQUENCE_NUMBER	){

				nodeSequenceNumber = Constants.FIRST_SEQUENCE_NUMBER;
			}
			else{
				nodeSequenceNumber++;	
			}
			return nodeSequenceNumber;
		}
	}

	/**
	 * Increments the broadcast ID 
	 * @return returns the incremented broadcast ID
	 */
	protected int getNextBroadcastID() {
		synchronized (sequenceNumberLock) {
			if(nodeBroadcastID == Constants.MAX_BROADCAST_ID){
				nodeBroadcastID = Constants.FIRST_BROADCAST_ID;
			} else {
				nodeBroadcastID++;
			}
			return nodeBroadcastID;			
		}
	}

	/**
	 * Only used for debugging
	 * @return returns the current broadcast ID of this node
	 */
	protected int getCurrentBroadcastID(){
		return nodeBroadcastID;
	}

	/**
	 * Notifies the application layer about 
	 * @param senderNodeAddess the source node which sent a message
	 * @param data the actual data which the application message contained
	 */
	protected void notifyAboutDataReceived(int senderNodeAddess, byte[] data, int packetID, byte dataType) {	
		messagesForObservers.add(new PacketToObserver(senderNodeAddess,data,packetID,ObserverConst.DATA_RECEIVED, dataType));
		wakeNotifierThread();
	}

	protected void notifyAboutBroadcastDataReceived(int senderNodeAddess, byte[] data, int packetID, byte dataType) {	
		messagesForObservers.add(new PacketToObserver(senderNodeAddess,data,packetID,ObserverConst.BROADCAST_DATA_RECEIVED, dataType));
		wakeNotifierThread();
	}

	/**
	 * Notifies the observer(s) about the route establishment failure for a destination
	 * @param nodeAddress is the unreachable destination
	 */
	protected void notifyAboutRouteEstablishmentFailure(int faliedToReachAddress) {
		messagesForObservers.add(new ValueToObserver(faliedToReachAddress, ObserverConst.ROUTE_ESTABLISHMENT_FAILURE));
		wakeNotifierThread();
	}

	/**
	 * Notifies the observer(s) that a packet is sent successfully from this node.
	 * NOTE: This does not guarantee that the packet also is received at the destination node
	 * @param packetIdentifier the ID of a packet which the above layer can recognize
	 */
	protected void notifyAboutDataSentSucces(int packetIdentifier){
		messagesForObservers.add(new ValueToObserver(packetIdentifier, ObserverConst.DATA_SENT_SUCCESS));
		wakeNotifierThread();
	}

	/**
	 * Notifies the observer(s) that an invalid destination address where detected for a user packet to be sent
	 * @param packetIdentifier an integer that identifies the user packet with bad destination address 
	 */
	protected void notifyAboutInvalidAddressGiven(int packetIdentifier){
		messagesForObservers.add(new ValueToObserver(packetIdentifier, ObserverConst.INVALID_DESTINATION_ADDRESS));
		wakeNotifierThread();
	}

	protected void notifyAboutSizeLimitExceeded(int packetIdentifier){
		messagesForObservers.add(new ValueToObserver(packetIdentifier, ObserverConst.DATA_SIZE_EXCEEDES_MAX));
		wakeNotifierThread();
	}

	protected void notifyAboutRouteToDestIsInvalid(int destinationAddress){
		messagesForObservers.add(new ValueToObserver(destinationAddress, ObserverConst.ROUTE_INVALID));
		wakeNotifierThread();
	}

	protected void notifyAboutNewNodeReachable(int destinationAddress){
		messagesForObservers.add(new ValueToObserver(destinationAddress, ObserverConst.ROUTE_CREATED));
		wakeNotifierThread();
	}

	private void wakeNotifierThread(){
		synchronized (messagesForObservers) {
			messagesForObservers.notify();
		}
	}
	/**
	 * This interface defines the a structure for an observer to retrieve a message from the observable
	 * @author rabie
	 *
	 */
	public interface MessageToObserver{

		/**
		 * 
		 * @return returns the type of this message as a String
		 */
		public int getMessageType();

		/**
		 * This method is used to retrieve the data that the observable wants to notify about
		 * @return returns the object that is contained
		 */
		public Object getContainedData();


	}

	public class ValueToObserver implements MessageToObserver{
		private Integer value;
		private int type;

		public ValueToObserver(int value, int msgType) {
			this.value = new Integer(value);
			type = msgType;
		}
		@Override
		public Object getContainedData() {
			return value;
		}

		@Override
		public int getMessageType() {
			return type;
		}

	}

	/**
	 * This class presents a received package from another node, to the application layer
	 * @author Rabie
	 *
	 */
	public class PacketToObserver implements MessageToObserver{
		private byte[] data;
		private int senderNodeAddress;
		private int type;
		private int packetID;
		private byte dataType;

		public PacketToObserver(int senderNodeAddress, byte[] data, int id, int msgType, byte dataType) {
			type = msgType;
			this.data = data;
			this.senderNodeAddress = senderNodeAddress;
			this.packetID = id;
			this.dataType = dataType;
		}

		/**
		 * A method to retrieve the senders address of this data
		 * @return returns an integer value representing the unique address of the sending node
		 */
		public int getSenderNodeAddress(){
			return senderNodeAddress;
		}

		public int getPacketID() {
			return packetID;
		}

		/**
		 * A method to retrieve the data sent
		 * @return returns a byte array containing the data which 
		 * where sent by another node with this node as destination
		 */
		@Override
		public Object getContainedData() {
			return data;
		}

		@Override
		public int getMessageType() {
			return type;
		}

		public int getDataType() {
			return dataType;
		}
	}

	protected void queuePDUmessage(AodvPDU pdu) {
		sender.queuePDUmessage(pdu);
	}

	@Override
	public void run() {
		while(keepRunning){
			try{
				synchronized (messagesForObservers) {
					while(messagesForObservers.isEmpty()){
						messagesForObservers.wait();
					}
				}
				setChanged();
				notifyObservers(messagesForObservers.poll());
			}catch (InterruptedException e) {
				// thread stopped
			}
		}
	}
}
