package adhoc.aodv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import player.cacheManager.CacheManager;
import player.configuration.CacheConfiguration;
import player.model.MessageProtos;
import player.model.MessageTypes;
import player.model.URI;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

//import sun.util.logging.resources.logging;

import adhoc.aodv.exception.AodvException;
import adhoc.aodv.exception.BadPduFormatException;
import adhoc.aodv.exception.NoSuchRouteException;
import adhoc.aodv.exception.RouteNotValidException;
import adhoc.aodv.pdu.HelloPacket;
import adhoc.aodv.pdu.RERR;
import adhoc.aodv.pdu.RREP;
import adhoc.aodv.pdu.RREQ;
import adhoc.aodv.pdu.UserBroadcastDataPacket;
import adhoc.aodv.pdu.UserDataPacket;
import adhoc.aodv.routes.ForwardRouteEntry;
import adhoc.constants.Constants;
import adhoc.etc.Debug;
import adhoc.udp.UdpReceiver;
import android.util.Log;

public class Receiver implements Runnable {
	
	private final static String MSG_TAG = "AdHoc --> Receiver";
	
	private Sender sender;
	private Queue<Message> receivedMessages;
	private RouteTableManager routeTableManager;
	private UdpReceiver udpReceiver;
	private int nodeAddress;
	private Thread receiverThread;
	private HashMap<Integer, Integer> broadcastIDMap;

    /**
     */
    private Node parent;
	private volatile boolean keepRunning = true;

	public Receiver(Sender sender, int nodeAddress, Node parent, RouteTableManager routeTableManager) throws SocketException, UnknownHostException, BindException {
		this.parent = parent;
		this.nodeAddress = nodeAddress;
		this.sender = sender;
		receivedMessages = new ConcurrentLinkedQueue<Message>();
		this.routeTableManager = routeTableManager;
		udpReceiver = new UdpReceiver(this, nodeAddress);
		broadcastIDMap = new HashMap<Integer, Integer>();
	}

	public void startThread(){
		keepRunning = true;
		udpReceiver.startThread();
		receiverThread = new Thread(this);
		receiverThread.start();
	}
	
	/**
	 * Stops the receiver thread.
	 */
	public void stopThread() {
		keepRunning = false;
		udpReceiver.stopThread();
		receiverThread.interrupt();
	}
	
	public void run() {
			while (keepRunning) {
				try {
					synchronized (receivedMessages) {
						while (receivedMessages.isEmpty()) {
							receivedMessages.wait();
						}
					}
	
					Message msg = receivedMessages.poll();
					if(msg.senderNodeAddress != nodeAddress){
						try {
							switch (msg.getType()) {
							case Constants.HELLO_PDU:
								HelloPacket hello = new HelloPacket();
								hello.parseBytes(msg.data);
								//Log.d(MSG_TAG, "hello hah ");
								helloMessageReceived(hello);
								break;
							case Constants.RREQ_PDU:
								RREQ rreq = new RREQ();
								rreq.parseBytes(msg.data);
								routeRequestReceived(rreq, msg.senderNodeAddress);
								break;
							case Constants.RREP_PDU:
								RREP rrep = new RREP();
								rrep.parseBytes(msg.data);
								routeReplyReceived(rrep, msg.senderNodeAddress);
								break;
							case Constants.RERR_PDU:
								RERR rerr = new RERR();
								rerr.parseBytes(msg.data);
								routeErrorRecived(rerr);
								break;
							case Constants.USER_DATA_PACKET_PDU:
								Log.d("BROADCAST", "Receive in Receiver!");
								UserDataPacket userDataPacket = new UserDataPacket();
								userDataPacket.parseBytes(msg.data);
								userDataPacketReceived(userDataPacket);
								break;
							case Constants.USER_BROADCAST_DATA_PACKET_PDU:
								UserBroadcastDataPacket userBroadcastDataPacket = new UserBroadcastDataPacket();
								userBroadcastDataPacket.parseBytes(msg.data);
								userBroadcastPacketReceived(userBroadcastDataPacket);
								break;
							default:
								// The received message is not in the domain of protocol messages
								break;
							}
						} catch (BadPduFormatException e) {
							Debug.print(e.getMessage());
						}
					} else {
					}
				} catch (InterruptedException e) {
					// Thread Stopped
				}
			}
	}

	/**
	 * Method used by the lower network layer to queue messages for later processing
	 * 
	 * @param senderNodeAddress Is the address of the node that sent a message
	 * @param msg is an array of bytes which contains the sent data
	 */
	public void addMessage(int senderNodeAddress, byte[] msg) {
		receivedMessages.add(new Message(senderNodeAddress, msg));
		synchronized (receivedMessages) {
			receivedMessages.notify();
		}
	}

	/**
	 * Handles a HelloHeader, when such a message is received from a neighbor
	 * 
	 * @param hello is the HelloHeader message received
	 */
	private void helloMessageReceived(HelloPacket hello) {
		try {
			routeTableManager.setValid(hello.getSourceAddress(), hello.getSourceSeqNr());
		} catch (NoSuchRouteException e) {
			routeTableManager.createForwardRouteEntry(	hello.getSourceAddress(),
														hello.getSourceAddress(),
														hello.getSourceSeqNr(),
														1,	true);
		}
		Debug.print("Receiver: received hello pdu from: "+hello.getSourceAddress());
	}

	/**
	 * Handles the incoming RREP messages
	 * 
	 * @param rrep is the message received
	 * @param senderNodeAddress the address of the sender
	 */
	private void routeReplyReceived(RREP rrep, int senderNodeAddress) {
		//rrepRoutePrecursorAddress is an local int used to hold the next-hop address from the forward route 
		int rrepRoutePrecursorAddress = -1;
		//Create route to previous node with unknown seqNum (neighbour)
		if(routeTableManager.createForwardRouteEntry(	senderNodeAddress,
													senderNodeAddress,
													Constants.UNKNOWN_SEQUENCE_NUMBER, 1, true)){
			Debug.print("Receiver: RREP where received and route to: "+senderNodeAddress+" where created with destSeq: "+Constants.UNKNOWN_SEQUENCE_NUMBER);
		}
		rrep.incrementHopCount();

		if (rrep.getSourceAddress() != nodeAddress) {
			//forward the RREP, since this node is not the one which requested a route
			
			//Add by Eric
			rrep.addPhoneInfo(Constants.phoneInfo);
			//
			sender.queuePDUmessage(rrep);
		
			// handle the first part of the route (reverse route) - from this node to the one which originated a RREQ
			try {
				//add the sender node to precursors list of the reverse route
				ForwardRouteEntry reverseRoute = routeTableManager.getForwardRouteEntry(rrep.getSourceAddress());
				reverseRoute.addPrecursorAddress(senderNodeAddress);
				rrepRoutePrecursorAddress = reverseRoute.getNextHop();
			} catch (AodvException e) {
				//no reverse route is currently known so the RREP is not sure to reach the originator of the RREQ
			}
		}
		// handle the second part of the route - from this node to the destination address in the RREP
		try {
			ForwardRouteEntry oldRoute = routeTableManager.getForwardRouteEntry(rrep.getDestinationAddress());
			if(rrepRoutePrecursorAddress != -1){
				oldRoute.addPrecursorAddress(rrepRoutePrecursorAddress);
			}
			//see if the RREP contains updates (better seqNum or hopCountNum) to the old route
				routeTableManager.updateForwardRouteEntry(oldRoute,
						new ForwardRouteEntry(	rrep.getDestinationAddress(),
												senderNodeAddress,
												rrep.getHopCount(),
												rrep.getDestinationSequenceNumber(),
												oldRoute.getPrecursors()));
		} catch (NoSuchRouteException e) {
			ArrayList<Integer> precursorNode = new ArrayList<Integer>();
			if(rrepRoutePrecursorAddress != -1){
				precursorNode.add(rrepRoutePrecursorAddress);
			}
			routeTableManager.createForwardRouteEntry(	rrep.getDestinationAddress(), 
														senderNodeAddress, 
														rrep.getDestinationSequenceNumber(),
														rrep.getHopCount(),
														precursorNode, true);
		} catch (RouteNotValidException e) {
			//FIXME den er gal paa den
			Debug.print("Receiver: FATAL ERROR");
			try {
				//update the previously known route with the better route contained in the RREP
				routeTableManager.setValid(rrep.getDestinationAddress(), rrep.getDestinationSequenceNumber());
				if(rrepRoutePrecursorAddress != -1){
					routeTableManager.getForwardRouteEntry(rrep.getDestinationAddress()).addPrecursorAddress(rrepRoutePrecursorAddress);
				}
			}catch (AodvException e1) {
				 
			}
		}
	}

	/**
	 * Handles a RREQ message when received
	 * 
	 * @param rreq the RREQ message that were received
	 * @param senderNodeAddress the node (a neighbor) which sent this message
	 */
	private void routeRequestReceived(RREQ rreq, int senderNodeAddress) {
		if (routeTableManager.routeRequestExists(rreq.getSourceAddress(), rreq.getBroadcastId())) {
			return;
		}
		//Create route to previous node with unknown seqNum (neighbour)
		if(routeTableManager.createForwardRouteEntry(	senderNodeAddress,
													senderNodeAddress,
													Constants.UNKNOWN_SEQUENCE_NUMBER, 1, true)){
			Debug.print("Receiver: RREQ where received from: "+senderNodeAddress+" and route where created with destSeq: "+Constants.UNKNOWN_SEQUENCE_NUMBER);
		}
		
		// Increments the hopCount and Adds the RREQ to the table
		rreq.incrementHopCount();
		routeTableManager.createRouteRequestEntry(rreq, true);

		//a reverse route may already exists, so we need to compare route info value to know what to update
		try {
			ForwardRouteEntry oldRoute = routeTableManager.getForwardRouteEntry(rreq.getSourceAddress());
			
			if(isIncomingRouteInfoBetter(	rreq.getSourceSequenceNumber(),
											oldRoute.getDestinationSequenceNumber(),
											rreq.getHopCount(),
											oldRoute.getHopCount())){
				//remove the old entry and then replace with new information
				routeTableManager.updateForwardRouteEntry(oldRoute,
						new ForwardRouteEntry(	rreq.getSourceAddress(),
												senderNodeAddress,
												rreq.getHopCount(),
												rreq.getSourceSequenceNumber(),
												oldRoute.getPrecursors()));
			}
		} catch (NoSuchRouteException e) {
			// Creates a reverse route for the RREP that may be received later on
			routeTableManager.createForwardRouteEntry(	rreq.getSourceAddress(),
														senderNodeAddress,
														rreq.getSourceSequenceNumber(),
														rreq.getHopCount(), true);
		} catch (RouteNotValidException e) {
			try {
				routeTableManager.setValid(rreq.getSourceAddress(), rreq.getSourceSequenceNumber());
			} catch (NoSuchRouteException e1) {
				routeTableManager.createForwardRouteEntry(	rreq.getSourceAddress(),
															senderNodeAddress,
															rreq.getSourceSequenceNumber(),
															rreq.getHopCount(), true);
			}
		}

		//check if this node is the destination,
		RREP rrep = null;
		try {
			ArrayList<String> phoneInfoList = new ArrayList<String>();
			
			if (rreq.getDestinationAddress() == nodeAddress) {
				if(parent.getNextSequenceNumber(parent.getCurrentSequenceNumber()) == rreq.getDestinationSequenceNumber()){
					parent.getNextSequenceNumber();
				}
				// the RREQ has reached it's destination, so this node has to reply with a RREP
	/*			rrep = new RREP(	rreq.getSourceAddress(),
									nodeAddress,
									rreq.getSourceSequenceNumber(),
									parent.getCurrentSequenceNumber()	);
									*/
				rrep = new RREP(
								rreq.getSourceAddress(),
								nodeAddress,
								rreq.getSourceSequenceNumber(),
								parent.getCurrentSequenceNumber(),
								0,
								phoneInfoList);
				
			} else {
				//this node is not the destination of the RREQ so we need to check if we have the requested route
				ForwardRouteEntry entry = routeTableManager.getForwardRouteEntry(rreq.getDestinationAddress());

				// If a valid route exists with a seqNum thats is grater or equal to the RREQ, then send a RREP
				if (isIncomingSeqNrBetter(entry.getDestinationSequenceNumber(), rreq.getDestinationSequenceNumber())) {
					rrep = new RREP(	rreq.getSourceAddress(),
										entry.getDestinationAddress(),
										rreq.getSourceSequenceNumber(),
										entry.getDestinationSequenceNumber(),
										entry.getHopCount()	);
					// Gratuitous RREP for the destination Node
					RREP gRrep = new RREP(	entry.getDestinationAddress(),
											rreq.getSourceAddress(),
											entry.getDestinationSequenceNumber(),
											rreq.getSourceSequenceNumber(),
											rreq.getHopCount()	);
					sender.queuePDUmessage(gRrep);
				}
			}
		} catch (NoSuchRouteException e) {
			//this node is an intermediate node, but do not know a route to the desired destination
		} catch (RouteNotValidException e) {
			//this node know a route but it is not active any longer.
			try {
				int maxSeqNum = getMaximumSeqNum(routeTableManager.getLastKnownDestSeqNum(rreq.getDestinationAddress()),
													rreq.getDestinationSequenceNumber()	);
				rreq.setDestSeqNum(maxSeqNum);
			} catch (NoSuchRouteException e1) {
				//table route were deleted by the timer
			}
		} finally {
			//if a RREP is created, then send it, otherwise broadcast the RREQ
			if (rrep == null) {
				sender.queuePDUmessage(rreq);
			} else {
				sender.queuePDUmessage(rrep);
			}
		}
	}

	/**
	 * Handles a RERR message when received
	 * 
	 * @param rerrMsg is the received error message
	 */
	private void routeErrorRecived(RERR rerrMsg) {
		Debug.print("Receiver: RRER received, unreachableNode: "+rerrMsg.getUnreachableNodeAddress());
		try {
			ForwardRouteEntry entry = routeTableManager.getForwardRouteEntry(
															rerrMsg.getUnreachableNodeAddress());

			//only send a RERR if the message contain a seqNum that is greater or equal to the entry known in the table
			if (isIncomingSeqNrBetter(rerrMsg.getUnreachableNodeSequenceNumber(),
										entry.getDestinationSequenceNumber()))
			{
				RERR rerr = new RERR(	rerrMsg.getUnreachableNodeAddress(),
										rerrMsg.getUnreachableNodeSequenceNumber(), 
										entry.getPrecursors()	);
				sender.queuePDUmessage(rerr);
				routeTableManager.setInvalid(rerrMsg.getUnreachableNodeAddress(), rerrMsg.getUnreachableNodeSequenceNumber());
			}
		} catch (AodvException e) {
			//no route is known so we do not have to react on the error message
		}
	}

	/**
	 * Handles a userDataPacket when received
	 * @param userData is the received packet
	 * @param senderNodeAddress the originator of the message
	 */
	private void userDataPacketReceived(UserDataPacket userData) {
		if (userData.getDestinationAddress() == nodeAddress 
				|| userData.getDestinationAddress() == Constants.BROADCAST_ADDRESS	) {
//			System.out.println("packet id is : " + userData.getPacketID());
			Log.d("BROADCAST", "Get my data !!!");
		/*	Log.d("BROADCAST", "Object Data Received from : "+ userData.getSourceNodeAddress());
			Log.d("BROADCAST", "Object Data des node  : "+ userData.getDestinationAddress());
			Log.d("BROADCAST", "Object Data forward id : "+ userData.getForwardAddress());
			Log.d("BROADCAST", "Object Data data type : "+ userData.getDataType());
			Log.d("BROADCAST", "Object Data packet id : "+ userData.getPacketID());*/
			Log.d("BROADCAST", "Object Data data length : "+ userData.getData().length);
			parent.notifyAboutDataReceived(userData.getSourceNodeAddress(), userData.getData(), userData.getPacketID(), userData.getDataType());
		} else if(userData.getForwardAddress() == nodeAddress){
			Log.d("BROADCAST", "Forward Data Receive:"+ userData.getDestinationAddress()+"/"+ nodeAddress);
			sender.queueUserMessageToForward(userData);
		}else {

		

			//若检查出是缓存数据，直接写到磁盘中去
			byte[] receivedData = userData.getData();
			player.model.MessageProtos.Message newMessage = null;
			try {
				newMessage = MessageProtos.Message.parseFrom(receivedData);
			} catch (InvalidProtocolBufferException e) {
				
				e.printStackTrace();
			}
			if (null != newMessage) {
				if(newMessage.getType() == MessageTypes.TRANSMISSION_REPLY){
					
					Log.d("BROADCAST", "Overhear CacheData!!!");
					
					URI uri = new URI(newMessage.getIdentifier(), newMessage.getOffset());
					ByteString payload = newMessage.getPayload();
					
					byte[] payloadBytes = payload.toByteArray();
					
					
					String dirname = CacheConfiguration.cachepath + "/" +uri.identifier;
					File dir = new File(dirname);
					if(!dir.exists()){
						dir.mkdirs();
					}
					String filename = dir + "/" + uri.offset;
					File newfile = new File(filename);
					if(!newfile.exists()){
						Log.d("BROADCAST", "No this cache.");
						Log.d("BROADCAT--OVERHEAR", "cache name: "+filename);
						try {
							long dataLengthReceivedThisTime = payloadBytes.length;
							CacheManager.sharedInstance().totalCountFromWifi += dataLengthReceivedThisTime;
							newfile.createNewFile();
							FileOutputStream fos = new FileOutputStream(newfile);
							fos.write(payloadBytes);
							fos.close();
						
						} catch (IOException e) {
							Log.d("BROADCAST", "Write cache exception.");
							e.printStackTrace(); 
						}	
					}
					else {
						
					}
	
				}
					
				
			}
			
			
	
		
			
		}
	}
	
	private void userBroadcastPacketReceived(UserBroadcastDataPacket userData) {
		if (userData.getSourceNodeAddress() == nodeAddress) {
			return;
		}
		else if (broadcastIDMap.get(userData.getSourceNodeAddress()) != null && userData.getPacketID() == broadcastIDMap.get(userData.getSourceNodeAddress())) {
			return;
//			System.out.println("packet id is : " + userData.getPacketID());
		} 
		else {
			broadcastIDMap.put(userData.getSourceNodeAddress(), userData.getPacketID());
			sender.queueUserBroadcastMessageToForward(userData);
			parent.notifyAboutBroadcastDataReceived(userData.getSourceNodeAddress(), userData.getData(), userData.getPacketID(), userData.getDataType());
		}
	}	
	
	/**
	 * Computes the maximum of the two sequence numbers, such that the possibility of rollover is taken to account
	 * @param firstSeqNum the first of the given sequence numbers which to compare
	 * @param secondSeqNum the second of the given sequence numbers which to compare
	 * @return returns the maximum sequence number
	 */
	public static int getMaximumSeqNum(int firstSeqNum, int secondSeqNum){
		if(isIncomingSeqNrBetter(firstSeqNum, secondSeqNum)){
			return firstSeqNum;
		} else {
			return secondSeqNum;
		}
	}
	
	/**
	 * Used to compare sequence numbers
	 * @param incomingSeqNum the sequence number contained in a received AODV PDU message
	 * @param currentSeqNum the sequence number contained in a known forward route
	 * @return returns true if incomingSeqNr is greater or equal to currentSeqNr
	 */
	private static boolean isIncomingSeqNrBetter(int incomingSeqNum, int currentSeqNum) {
		return isIncomingRouteInfoBetter(incomingSeqNum, currentSeqNum, 0, 1);
	}
	
	/**
	 * Used to compare sequence numbers and hop count
	 * @param incommingSeqNum the sequence number contained in a received AODV PDU message
	 * @param currentSeqNum the sequence number contained in a known forward route
	 * @return returns true if incomingSeqNum > currentSeqNum OR incomingSeqNum == currentSeqNum AND incomingHopCount < currentHopCount  
	 */
	protected static boolean isIncomingRouteInfoBetter(int incomingSeqNum, int currentSeqNum, int incomingHopCount, int currentHopCount) {
		if (Math.abs(incomingSeqNum - currentSeqNum) > Constants.SEQUENCE_NUMBER_INTERVAL) {

			if ((incomingSeqNum % Constants.SEQUENCE_NUMBER_INTERVAL) >= (currentSeqNum % Constants.SEQUENCE_NUMBER_INTERVAL)) {
				if ((incomingSeqNum % Constants.SEQUENCE_NUMBER_INTERVAL) == (currentSeqNum % Constants.SEQUENCE_NUMBER_INTERVAL)
						&& incomingHopCount > currentHopCount) {
					return false;
				}
				return true;
			} else {
				//the node have an older route so it should not be used
				return false;
			}
		} else {
			if (incomingSeqNum >= currentSeqNum) {
				if (incomingSeqNum == currentSeqNum && incomingHopCount > currentHopCount) {
					return false;
				}
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * @author Rabie A class to contain the received data from a lower network layer (UDP). Objects
	 *         of this type is stored in a receiving queue for later processing
	 * 
	 */
	private class Message {
		private int senderNodeAddress;
		private byte[] data;

		public Message(int senderNodeAddress, byte[] data) {
			this.senderNodeAddress = senderNodeAddress;
			this.data = data;
		}

		public byte getType() throws NumberFormatException {
			return data[0];
		}
	}
}
