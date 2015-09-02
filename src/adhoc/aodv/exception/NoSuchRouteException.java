package adhoc.aodv.exception;

public class NoSuchRouteException extends AodvException{

	/**
	 * 
	 */
	private final static String MSG_TAG = "AdHoc --> NoSuchRouteException";
	
	private static final long serialVersionUID = 1L;

	public NoSuchRouteException(){
		
	}
	
	public NoSuchRouteException(String message) {
		super(message);
	}
	

}