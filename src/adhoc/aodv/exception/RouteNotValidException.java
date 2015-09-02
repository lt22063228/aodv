package adhoc.aodv.exception;

public class RouteNotValidException extends AodvException{

	/**
	 * 
	 */
	private final static String MSG_TAG = "AdHoc --> RouteNotValidException";
	
	private static final long serialVersionUID = 1L;

	public RouteNotValidException(){
		
	}
	
	public RouteNotValidException(String message) {
		super(message);
	}	
}