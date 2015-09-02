package adhoc.aodv.exception;

public abstract class AodvException extends Exception{

	/**
	 * 
	 */
	private final static String MSG_TAG = "AdHoc --> AODVException";
	
	private static final long serialVersionUID = 1L;

	public AodvException(){}
	
	public AodvException(String message) {
		super(message);
	}

}
