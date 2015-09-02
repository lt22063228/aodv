package adhoc.aodv.pdu;

import adhoc.constants.Constants;



public abstract class AodvPDU implements Packet{
	
	private final static String MSG_TAG = "AdHoc --> AODVPdu";
	
	protected byte pduType;
    protected int srcAddress, destAddress; // 这两个地址是指UDP中用到的地址
    protected int destSeqNum;
    
    
    public AodvPDU(){
    	
    }
    
    public AodvPDU(int sourceAddress, int destinationAddess, int destinationSequenceNumber){
    	srcAddress = sourceAddress;
    	destAddress = destinationAddess;
    	destSeqNum = destinationSequenceNumber;
    }
    
    public int getSourceAddress() {
        return srcAddress;
    }

    @Override
    public int getDestinationAddress() {
        return destAddress;
    }

    public int getDestinationSequenceNumber() {
        return destSeqNum;
    }
    
    public byte getType(){
    	return pduType;
    }
    
    @Override
    public byte[] toBytes(){
		byte[] result = new byte[13];
		result[0] = pduType;
		System.arraycopy(Constants.intToByteArray(srcAddress), 0, result, 1, 4);
		System.arraycopy(Constants.intToByteArray(destAddress), 0, result, 5, 4);
		System.arraycopy(Constants.intToByteArray(destSeqNum), 0, result, 9, 4);		
		return result;
    }
}
