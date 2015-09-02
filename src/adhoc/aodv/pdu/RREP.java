package adhoc.aodv.pdu;


import java.util.ArrayList;

import adhoc.aodv.exception.BadPduFormatException;
import adhoc.constants.Constants;

public class RREP extends AodvPDU {
	
	private final static String MSG_TAG = "AdHoc --> RREP";
	
    private int hopCount = 0; // 在传播的过程中更新
    private int srcSeqNum;

    
    //Add by Eric
    private ArrayList<String> phoneInfoList;
    
    //
    public RREP(){
    }
    
    //Add by Eric
    public RREP(	int sourceAddress,
			int destinationAddress,
			int sourceSequenceNumber,
			int destinationSequenceNumber,
			int hopCount,
			ArrayList<String> phoneInfoList){

		super(sourceAddress,destinationAddress,destinationSequenceNumber);
		pduType = Constants.RREP_PDU;
		srcSeqNum = sourceSequenceNumber;
		this.hopCount = hopCount;
		this.phoneInfoList = new ArrayList<String>();
		this.phoneInfoList = phoneInfoList;
}

    //
    
    public RREP(	int sourceAddress,
    				int destinationAddress,
    				int sourceSequenceNumber,
    				int destinationSequenceNumber,
    				int hopCount){
    	
    	super(sourceAddress,destinationAddress,destinationSequenceNumber);
    	pduType = Constants.RREP_PDU;
    	srcSeqNum = sourceSequenceNumber;
    	this.hopCount = hopCount;
    }
    
    public RREP(	int sourceAddress,
    				int destinationAddress,
    				int sourceSequenceNumber,
    				int destinationSequenceNumber) {
    	
    	super(sourceAddress,destinationAddress,destinationSequenceNumber);
    	pduType = Constants.RREP_PDU;
    	srcSeqNum = sourceSequenceNumber;
    }
	
	public int getHopCount(){
		return hopCount;
	}
	//&&
	public void addPhoneInfo(String info){
		this.phoneInfoList.add(info);
	}
	
	public void incrementHopCount(){
		hopCount++;
	}
	
	public int getDestinationSequenceNumber(){
		return destSeqNum;
	}

	@Override
	public byte[] toBytes() {
		int length = super.toBytes().length;
		byte[] result = new byte[8 + length];
		System.arraycopy(super.toBytes(), 0, result, 0, length);
		System.arraycopy(Constants.intToByteArray(srcSeqNum), 0, result, length, 4);
		System.arraycopy(Constants.intToByteArray(hopCount), 0, result, length + 4, 4);		
		return result;
	}
	
//	@Override
//	public String toString() {
//		return super.toString()+srcSeqNum+";"+hopCount;
//	}
	
	@Override
	public void parseBytes(byte[] rawPdu) throws BadPduFormatException {
//		String[] s = new String(rawPdu).split(";",6);
//		if(s.length != 6){
//			throw new BadPduFormatException(	"RREP: could not split " +
//												"the expected # of arguments from rawPdu. " +
//												"Expecteded 6 args but were given "+s.length	);
//		}
//		try {
			pduType = rawPdu[0];
			if(pduType != Constants.RREP_PDU){
				throw new BadPduFormatException(	"RREP: pdu type did not match. " +
													"Was expecting: "+Constants.RREP_PDU+
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
//		} catch (NumberFormatException e) {
//			throw new BadPduFormatException("RREP: falied in parsing arguments to the desired types");
//		}
		
	}
}
