package adhoc.aodv.pdu;


import adhoc.aodv.exception.BadPduFormatException;
import adhoc.constants.Constants;

public class HelloPacket implements Packet{
	
	private final static String MSG_TAG = "AdHoc --> HelloPacket";
	
	private byte pduType;  // 数据包的类型,如HELLO等
	private int sourceAddress; //源地址, 目标地址一直是广播地址
	private int sourceSeqNr; // 序列号,用来避免环路,loopfree
	
	public HelloPacket(){
		
	}
	
	public HelloPacket(int sourceAddress, int sourceSeqNr){
		pduType = Constants.HELLO_PDU;
		this.sourceAddress = sourceAddress;
		this.sourceSeqNr = sourceSeqNr;
	}
	
	public int getSourceAddress(){
		return sourceAddress;
	}
	
	@Override
	public int getDestinationAddress() {
		//broadcast address
		return Constants.BROADCAST_ADDRESS;
	}
	
	public int getSourceSeqNr(){
		return sourceSeqNr;
	}

	@Override
	public byte[] toBytes() {
		// 将数据包转换成byte[]数组,包括其中的pdutype, sourceAddress, sourceSeqNr
		byte[] result = new byte[9];
		result[0] = pduType;
		System.arraycopy(Constants.intToByteArray(sourceAddress), 0, result, 1, 4);
		System.arraycopy(Constants.intToByteArray(sourceSeqNr), 0, result, 5, 4);
		return result;
	}
	
//	@Override
//	public String toString(){
//		return pduType+";"+sourceAddress+";"+sourceSeqNr;
//	}
	
	@Override
	public void parseBytes(byte[] rawPdu) throws BadPduFormatException {
//		String[] s = new String(rawPdu).split(";",3);
//		if(s.length != 3){
//			throw new BadPduFormatException(	"HelloPacket: could not split " +
//												"the expected # of arguments from rawPdu. " +
//												"Expecteded 3 args but were given "+s.length	);
//		}
//		try {
			pduType = rawPdu[0];
			if(pduType != Constants.HELLO_PDU){
				throw new BadPduFormatException(	"HelloPacket: pdu type did not match. " +
													"Was expecting: "+Constants.HELLO_PDU+
													" but parsed: "+pduType	);
			}
			byte[] value = new byte[4];
			System.arraycopy(rawPdu, 1, value, 0, 4);
			sourceAddress = Constants.byteArraytoInt(value);
			System.arraycopy(rawPdu, 5, value, 0, 4);
			sourceSeqNr = Constants.byteArraytoInt(value);
//		} catch (NumberFormatException e) {
//			throw new BadPduFormatException("HelloPacket: falied in parsing arguments to the desired types");
//		}
	}

}
