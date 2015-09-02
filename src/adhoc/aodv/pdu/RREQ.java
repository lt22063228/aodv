package adhoc.aodv.pdu;


import adhoc.aodv.exception.BadPduFormatException;
import adhoc.constants.Constants;


public class RREQ extends AodvPDU {
	
	private final static String MSG_TAG = "AdHoc --> RREQ";
	
    private int srcSeqNum;
    private int hopCount = 0; // 在传播的过程中更新
    private int broadcastID;
    
    public RREQ(){
    	
    }
    
    /**
     * Constructor for creating a route request PDU
     * @param sourceNodeAddress the originators node address
     * @param destinationNodeAddress the address of the desired node
     * @param sourceSequenceNumber originators sequence number
     * @param destinationSequenceNumber should be set to the last known sequence number of the destination
     * @param broadcastId along with the source address this number uniquely identifies this route request PDU
     */
    public RREQ(int sourceNodeAddress, int destinationNodeAddress, int sourceSequenceNumber, int destinationSequenceNumber, int broadcastId) {
		super(sourceNodeAddress, destinationNodeAddress, destinationSequenceNumber);
    	pduType = Constants.RREQ_PDU;
        srcSeqNum = sourceSequenceNumber;
        this.broadcastID = broadcastId;
    }

	public int getBroadcastId(){
		return broadcastID;
	}

	public int getSourceSequenceNumber(){
		return srcSeqNum;
	}
	
	public void setDestSeqNum(int destinationSequenceNumber){
		destSeqNum = destinationSequenceNumber;
	}

	public int getHopCount(){
		return hopCount;
	}
	
	public void incrementHopCount(){
		hopCount++;
	}

	@Override
	public byte[] toBytes() {
		int length = super.toBytes().length;
		byte[] result = new byte[12 + length];
		System.arraycopy(super.toBytes(), 0, result, 0, length);
		System.arraycopy(Constants.intToByteArray(srcSeqNum), 0, result, length, 4);
		System.arraycopy(Constants.intToByteArray(hopCount), 0, result, length + 4, 4);	
		System.arraycopy(Constants.intToByteArray(broadcastID), 0, result, length + 8, 4);		
		return result;
	}
	
//	@Override
//	public String toString(){
//		return super.toString()+srcSeqNum+";"+hopCount+";"+broadcastID;
//	}
	
	@Override
	public void parseBytes(byte[] rawPdu) throws BadPduFormatException {
//		String[] s = new String(rawPdu).split(";",7);
//		if(s.length != 7){
//			throw new BadPduFormatException(	"RREQ: could not split " +
//												"the expected # of arguments from rawPdu. " +
//												"Expecteded 7 args but were given "+s.length	);
//		}
//		try {
			pduType = rawPdu[0];
			if(pduType != Constants.RREQ_PDU){
				throw new BadPduFormatException(	"RREQ: pdu type did not match. " +
													"Was expecting: "+Constants.RREQ_PDU+
													" but parsed: "+pduType	);
			}
			byte[] value = new byte[4];
			System.arraycopy(rawPdu, 1, value, 0, 4);
			srcAddress = Constants.byteArraytoInt(value);
			System.arraycopy(rawPdu, 5, value, 0, 4);
			destAddress = Constants.byteArraytoInt(value);
			System.arraycopy(rawPdu, 9, value, 0, 4);
			destSeqNum = Constants.byteArraytoInt(value);
			System.arraycopy(rawPdu, 13, value, 0, 4);
			srcSeqNum = Constants.byteArraytoInt(value);
			System.arraycopy(rawPdu, 17, value, 0, 4);
			hopCount = Constants.byteArraytoInt(value);
			System.arraycopy(rawPdu, 21, value, 0, 4);
			broadcastID = Constants.byteArraytoInt(value);
//		} catch (NumberFormatException e) {
//			throw new BadPduFormatException("RREQ: falied in parsing arguments to the desired types");
//		}
	}
}
