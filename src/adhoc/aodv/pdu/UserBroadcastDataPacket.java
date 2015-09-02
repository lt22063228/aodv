package adhoc.aodv.pdu;

import adhoc.aodv.exception.BadPduFormatException;
import adhoc.constants.Constants;

public class UserBroadcastDataPacket implements Packet {
	
	private final static String MSG_TAG = "AdHoc --> UserBroadcastDataPacket";
	
	private byte[] data;
	private byte pduType;
	private int sourceAddress;
	private int packetID;
	private byte dataType;
	
	public UserBroadcastDataPacket(){
		
	}
	
	public UserBroadcastDataPacket(int packetIdentifier, byte[] data, int sourceAddress, byte type){
		pduType = Constants.USER_BROADCAST_DATA_PACKET_PDU;
		packetID = packetIdentifier;
		this.data = data;
		this.sourceAddress = sourceAddress;
		this.dataType = type;
	}
	
	public byte[] getData(){
		return data;
	}
	
	public int getSourceNodeAddress(){
		return sourceAddress;
	}

	@Override
	public byte[] toBytes() {
		// TODO Auto-generated method stub
		byte[] result = new byte[data.length + 14];
		result[0] = pduType;
		System.arraycopy(Constants.intToByteArray(packetID), 0, result, 1, 4);
		System.arraycopy(Constants.intToByteArray(sourceAddress), 0, result, 5, 4);
		result[9] = dataType;
		System.arraycopy(data, 0, result, 10, data.length);
		return result;
	}

	@Override
	public void parseBytes(byte[] rawPdu) throws BadPduFormatException {
		// TODO Auto-generated method stub
		pduType = rawPdu[0];
		if(pduType != Constants.USER_BROADCAST_DATA_PACKET_PDU){
			throw new BadPduFormatException(	"UserDataPacket: pdu type did not match. " +
													"Was expecting: "+Constants.USER_DATA_PACKET_PDU+
													" but parsed: "+pduType	);
		}

		byte[] value = new byte[4];
		System.arraycopy(rawPdu, 1, value, 0, 4);
		packetID = Constants.byteArraytoInt(value);
//		System.out.println(packetID);
		System.arraycopy(rawPdu, 5, value, 0, 4);
		sourceAddress = Constants.byteArraytoInt(value);
//		System.out.println(sourceAddress);
		dataType = rawPdu[9];
		byte[] userdata = new byte[rawPdu.length - 14];
		System.arraycopy(rawPdu, 10, userdata, 0, rawPdu.length - 14);
		this.data = userdata;
	}

	@Override
	public int getDestinationAddress() {
		// TODO Auto-generated method stub
		return 0;
	}
	

	public int getPacketID() {
		return packetID;
	}
	
	public byte getDataType() {
		return dataType;
	}

}
