package adhoc.aodv.exception;

public class DataExceedsMaxSizeException extends AodvException {

	/**
	 * 
	 */
	private final static String MSG_TAG = "AdHoc --> DataExceedsMaxSizeException";
	
	private static final long serialVersionUID = 1L;

	public DataExceedsMaxSizeException(){
		
	}
	
	public DataExceedsMaxSizeException(String message){
		super(message);
	}

}
