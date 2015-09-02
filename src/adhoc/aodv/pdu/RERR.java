package adhoc.aodv.pdu;

import java.util.ArrayList;

import adhoc.aodv.exception.BadPduFormatException;
import adhoc.constants.Constants;

public class RERR extends AodvPDU {
	
	private final static String MSG_TAG = "AdHoc --> RERR";
	
	private int unreachableNodeAddress;
	private int unreachableNodeSequenceNumber;
	private ArrayList<Integer> destAddresses = new ArrayList<Integer>(); // 目前使用这个目标地址作为下一跳的所有地址

	
	
	public RERR(){
		
	}
	
	/**
	 * 
	 * @param unreachableNodeAddress
	 * @param unreachableNodeSequenceNumber
	 * @param destinationAddresses
	 */
    public RERR(int unreachableNodeAddress ,int unreachableNodeSequenceNumber, ArrayList<Integer> destinationAddresses) {
    	this.unreachableNodeAddress = unreachableNodeAddress;
    	this.unreachableNodeSequenceNumber = unreachableNodeSequenceNumber;
    	pduType = Constants.RERR_PDU;
        destAddresses = destinationAddresses;
        destAddress = -1;
    }

	/**
	 * Constructor of a route error message  
	 * @param
	 * @param
	 * @param destinationAddress the node which hopefully will receive this PDU packet
	 */
    public RERR(int unreachableNodeAddress ,int unreachableNodeSequenceNumber, int destinationAddress){
    	this.unreachableNodeAddress = unreachableNodeAddress;
    	this.unreachableNodeSequenceNumber = unreachableNodeSequenceNumber;
    	pduType = Constants.RERR_PDU;
        destAddress = destinationAddress;
    }
    
	public int getUnreachableNodeAddress(){
		return unreachableNodeAddress;
	}
	
	public int getUnreachableNodeSequenceNumber(){
		return unreachableNodeSequenceNumber;
	}
	
	public ArrayList<Integer> getAllDestAddresses(){
		return destAddresses;
	}
	
	@Override
	public byte[] toBytes() {
		byte[] result = new byte[9];
		result[0] = pduType;
		System.arraycopy(Constants.intToByteArray(unreachableNodeAddress), 0, result, 1, 4);
		System.arraycopy(Constants.intToByteArray(unreachableNodeSequenceNumber), 0, result, 5, 4);
		return result;
	}
	
//	@Override
//	public String toString() {
//		return Byte.toString(pduType)+";"+unreachableNodeAddress+";"+unreachableNodeSequenceNumber;
//	}
	
	@Override
	public void parseBytes(byte[] rawPdu) throws BadPduFormatException {
//		String[] s = new String(rawPdu).split(";",3);
//		if(s.length != 3){
//			throw new BadPduFormatException(	"RERR: could not split " +
//												"the expected # of arguments from rawPdu. " +
//												"Expecteded 3 args but were given "+s.length	);
//		}
//		try {
			pduType = rawPdu[0];
			if(pduType != Constants.RERR_PDU){
				throw new BadPduFormatException(	"RERR: pdu type did not match. " +
													"Was expecting: "+Constants.RERR_PDU+
													" but parsed: "+pduType	);
			}
			byte[] value = new byte[4];
			System.arraycopy(rawPdu, 1, value, 0, 4);
			unreachableNodeAddress = Constants.byteArraytoInt(value);
			System.arraycopy(rawPdu, 5, value, 0, 4);
			unreachableNodeSequenceNumber = Constants.byteArraytoInt(value);
//		} catch (NumberFormatException e) {
//			throw new BadPduFormatException("RERR: falied in parsing arguments to the desired types");
//		}	
	}
}
