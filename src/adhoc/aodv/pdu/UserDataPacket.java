package adhoc.aodv.pdu;

import java.io.UnsupportedEncodingException;

import adhoc.aodv.exception.BadPduFormatException;
import adhoc.constants.Constants;
import android.util.Base64;

public class UserDataPacket implements Packet{
	
	private final static String MSG_TAG = "AdHoc --> UserDataPacket";
	
	private byte[] data;
	private int destAddress;
	private byte pduType;
	private int sourceAddress;
	private int packetID;
	private byte dataType;
	private int forwardAddress; //添加转发地址
	
	public UserDataPacket(){
		
	}
	
	public UserDataPacket(int packetIdentifier,int destinationAddress, byte[] data, int sourceAddress, byte type){
		pduType = Constants.USER_DATA_PACKET_PDU;
		packetID = packetIdentifier;
		destAddress = destinationAddress;
		this.data = data;
		this.sourceAddress = sourceAddress;
		this.dataType = type;
		forwardAddress = 255;
	}
	
	public byte[] getData(){
		return data;
	}
	
	public int getSourceNodeAddress(){
		return sourceAddress;
	}
	
	@Override
    public int getDestinationAddress() {
        return destAddress;
    }
	
	public void setForwardAddress(int nodeAddress){
		forwardAddress = nodeAddress;
	}
	
	public int getForwardAddress( ){
		return forwardAddress;
	}
	
	@Override
	public byte[] toBytes() {
		byte[] result = new byte[data.length + 18];
		result[0] = pduType;
		System.arraycopy(Constants.intToByteArray(packetID), 0, result, 1, 4);
		System.arraycopy(Constants.intToByteArray(sourceAddress), 0, result, 5, 4);
		System.arraycopy(Constants.intToByteArray(destAddress), 0, result, 9, 4);
		result[13] = dataType;
		
		System.arraycopy(Constants.intToByteArray(forwardAddress), 0, result, 14, 4);
		
		//System.arraycopy(data, 0, result, 14, data.length);
		System.arraycopy(data, 0, result, 18, data.length);
		return result;
		
	}


	
	@Override
	public void parseBytes(byte[] rawPdu) throws BadPduFormatException {

		pduType = rawPdu[0];
		if(pduType != Constants.USER_DATA_PACKET_PDU){
			throw new BadPduFormatException(	"UserDataPacket: pdu type did not match. " +
													"Was expecting: "+Constants.USER_DATA_PACKET_PDU+
													" but parsed: "+pduType	);
		}

		byte[] value = new byte[4];
		System.arraycopy(rawPdu, 1, value, 0, 4);
		packetID = Constants.byteArraytoInt(value);

		System.arraycopy(rawPdu, 5, value, 0, 4);
		sourceAddress = Constants.byteArraytoInt(value);

		System.arraycopy(rawPdu, 9, value, 0, 4);
		destAddress = Constants.byteArraytoInt(value);

		dataType = rawPdu[13];
		
		System.arraycopy(rawPdu, 14, value, 0, 4);
		forwardAddress = Constants.byteArraytoInt(value);
		
		
		byte[] userdata = new byte[rawPdu.length - 18];
		System.arraycopy(rawPdu, 18, userdata, 0, rawPdu.length - 18);
		this.data = userdata;
	}

	public int getPacketID() {
		return packetID;
	}
	
	public byte getDataType() {
		return dataType;
	}

}