package adhoc.aodv.exception;

public class BadPduFormatException extends AodvException {
	
	/**
	 * 
	 */
	private final static String MSG_TAG = "AdHoc --> BadPduFormatException";
	
	private static final long serialVersionUID = 1L;

	public BadPduFormatException(){
		
	}
	
	public BadPduFormatException(String message){
		super(message);
	}

}
